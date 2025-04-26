package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthassistant.databinding.ActivityFoodManagerBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import food.RecipeResponse;
import food.RecipeResponse.Recipe;
import food.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodManager extends AppCompatActivity {
    ActivityFoodManagerBinding binding;
    private final List<String> avoidIngredients = Collections.synchronizedList(new ArrayList<>());
    private final List<String>[] allergiesHolder = new List[]{new ArrayList<>()};
    private final List<String>[] conditionsHolder = new List[]{new ArrayList<>()};
    private List<String> medicationNames;
    private List<Recipe> eatenTodayRecipes = new ArrayList<>();
    private EatenFoodAdapter eatenAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_manager);

        binding = ActivityFoodManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(FoodManager.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(FoodManager.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(FoodManager.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(FoodManager.this, HealthGoals_PC.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(FoodManager.this, MedicationManager.class));
                    break;
            }
            return true;
        });

        // Fetch allergies from Firestore
        db.collection("users").document(userId)
                .collection("medicalHistory").document("allergies")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> userAllergies = (List<String>) documentSnapshot.get("foodAllergies");
                        if (userAllergies != null) {allergiesHolder[0] = userAllergies;}
                        else {
                            allergiesHolder[0] = new ArrayList<>(); // Assign empty list if null
                            Log.e("FoodManager", "No allergies found.");
                        }
                    }
                    checkAndGenerateWhenReady(allergiesHolder[0], conditionsHolder[0]);
                });

        // Fetch conditions from Firestore
        db.collection("users").document(userId)
                .collection("medicalHistory").document("conditions")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> userConditions = (List<String>) documentSnapshot.get("medicalConditions");
                        if (userConditions != null) {conditionsHolder[0] = userConditions;}
                        else {
                            conditionsHolder[0] = new ArrayList<>(); // Assign empty list if null
                            Log.e("FoodManager", "No conditions found.");
                        }
                    }
                    checkAndGenerateWhenReady(allergiesHolder[0], conditionsHolder[0]);
                });

        // Fetch medication  from Firestore
        db.collection("userMedications")
                .whereEqualTo("userID", userId)
                .get()
                .addOnCompleteListener(task -> {
                    Log.d("FoodManager", "Querying userMedications for userID: " + userId);
                    if (task.isSuccessful()) {
                        Log.d("FoodManager", "Query successful. Found " + task.getResult().size() + " documents.");
                        List<String> allMeds = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Log.d("FoodManager", "Doc: " + doc.getData());
                            List<String> meds = extractMedicationNames(doc);
                            Log.d("FoodManager", "Extracted meds: " + meds);
                            allMeds.addAll(meds);
                        }

                        // Call startRecommendationFlow ONCE with all medications
                        startRecommendationFlow(allMeds, allergiesHolder[0], conditionsHolder[0]);

                    } else {
                        Log.e("FoodManager", "Query failed", task.getException());
                    }
                });

        // New, eaten foods
        RecyclerView eatenRecyclerView = findViewById(R.id.eatenRecyclerView);
        eatenTodayRecipes = new ArrayList<>();
        eatenAdapter = new EatenFoodAdapter(eatenTodayRecipes);
        eatenRecyclerView.setAdapter(eatenAdapter);
        eatenRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private void checkAndGenerateWhenReady(List<String> allergies, List<String> conditions) {
        // Start generating only when both allergies and conditions are loaded
        if (!allergies.isEmpty() && !conditions.isEmpty()) {
            checkAndGenerate(allergies, conditions);
        }
    }

    private void checkAndGenerate(List<String> allergies, List<String> conditions) {
        // Generate food recommendations using allergies and conditions
        Log.d("FoodManager", "Generating food recommendations for allergies: " + allergies.toString() + " and conditions: " + conditions.toString());
        startRecommendationFlow(medicationNames, allergies, conditions);
    }

    private void startRecommendationFlow(List<String> medications, List<String> allergies, List<String> conditions) {
        this.medicationNames = medications;
        fetchFoodInteractionsFromMeds(() -> {
            // Once interactions are fetched, merge allergies with medication interactions
            List<String> combinedExclusions = new ArrayList<>(avoidIngredients);
            combinedExclusions.addAll(allergies);

            // Determine diet restrictions from conditions
            String diet = determineDietFromConditions(conditions);

            // Now fetch recipes from Spoonacular with the combined exclusions and diet
            fetchRecipesWithExclusions(combinedExclusions, diet);
        });
    }

    private void fetchFoodInteractionsFromMeds(Runnable onComplete) {
        if (medicationNames == null || medicationNames.isEmpty()) {
            Log.e("FoodManager", "No medications available.");
            return;
        }

        AtomicInteger medsProcessed = new AtomicInteger(0);
        int totalMeds = medicationNames.size();
        List<String> displayWarnings = Collections.synchronizedList(new ArrayList<>());

        for (String med : medicationNames) {
            String cleanedMed = extractGenericName(med);
            Log.d("FoodManager", "Cleaned medication name: " + cleanedMed);

            new Thread(() -> {
                List<String> interactions = getFoodInteractionsFromOpenFDA(cleanedMed);

                if (interactions != null && !interactions.isEmpty()) {
                    synchronized (displayWarnings) {
                        displayWarnings.addAll(interactions);
                    }

                    synchronized (avoidIngredients) {
                        for (String warning : interactions) {
                            if (warning.startsWith("Avoid: ")) {
                                String avoidItems = warning.replace("Avoid: ", "").trim();
                                String[] items = avoidItems.split(",");
                                for (String item : items) {
                                    avoidIngredients.add(item.trim());
                                }
                            }
                        }
                    }
                }

                if (medsProcessed.incrementAndGet() == totalMeds) {
                    runOnUiThread(() -> {
                        Log.d("FoodManager", "Final display warnings: " + displayWarnings.toString());
                        Log.d("FoodManager", "Final avoid ingredients: " + avoidIngredients.toString());
                        updateFoodInteractionWarnings(displayWarnings); // Update the moderationWarning TextView correctly now
                        onComplete.run();
                    });
                }
            }).start();
        }
    }

    private void updateFoodInteractionWarnings(List<String> warnings) {
        TextView textView = findViewById(R.id.moderationWarning);

        if (warnings == null || warnings.isEmpty()) {
            textView.setVisibility(View.GONE); // Stay hidden
        } else {
            StringBuilder displayText = new StringBuilder();

            Set<String> avoidSet = new LinkedHashSet<>();
            Set<String> moderationSet = new LinkedHashSet<>();

            for (String warning : warnings) {
                if (warning.startsWith("Avoid: ")) {
                    String[] items = warning.replace("Avoid: ", "").split(",");
                    for (String item : items) {
                        avoidSet.add(item.trim());
                    }
                } else if (warning.startsWith("Moderation: ")) {
                    String[] items = warning.replace("Moderation: ", "").split(",");
                    for (String item : items) {
                        moderationSet.add(item.trim());
                    }
                }
            }
            if (!avoidSet.isEmpty()) {
                displayText.append("🛑 Avoid: ").append(String.join(", ", avoidSet)).append("\n");
            }

            if (!moderationSet.isEmpty()) {
                displayText.append("🌿 Moderation: ").append(String.join(", ", moderationSet));
            }
            textView.setText(displayText.toString().trim());
            textView.setVisibility(View.VISIBLE); // Show warning once there’s content
        }
    }

    private List<String> getFoodInteractionsFromOpenFDA(String medName) {
        List<String> interactions = new ArrayList<>();

        // Map interaction categories to their related terms
        Map<String, List<String>> interactionToIngredients = new HashMap<>();
        interactionToIngredients.put("grapefruit", Arrays.asList("grapefruit", "grapefruit juice"));
        interactionToIngredients.put("alcohol", Arrays.asList("alcohol", "beer", "wine", "liquor"));
        interactionToIngredients.put("caffeine", Arrays.asList("coffee", "black tea", "cola", "caffeine"));
        interactionToIngredients.put("dairy", Arrays.asList("milk", "cheese", "yogurt", "dairy"));
        interactionToIngredients.put("vitamin K", Arrays.asList("spinach", "kale", "collard greens", "lettuce", "vitamin K"));
        interactionToIngredients.put("fiber", Arrays.asList("broccoli", "apples", "beans", "whole grains", "fiber"));

        List<String> avoidList = new ArrayList<>();
        List<String> moderationList = new ArrayList<>();

        try {
            String query = URLEncoder.encode(medName, "UTF-8");
            String apiUrl = "https://api.fda.gov/drug/label.json?search=openfda.generic_name:" + query + "&limit=1";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                Log.e("OpenFDA", "Failed request for: " + medName + ", Response code: " + responseCode);
                return interactions;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
            reader.close();
            conn.disconnect();

            JSONObject json = new JSONObject(result.toString());
            JSONArray results = json.optJSONArray("results");

            if (results != null && results.length() > 0) {
                JSONObject medInfo = results.getJSONObject(0);

                if (medInfo.has("drug_interactions")) {
                    JSONArray interactionsArray = medInfo.getJSONArray("drug_interactions");
                    for (int i = 0; i < interactionsArray.length(); i++) {
                        String interactionText = interactionsArray.getString(i).toLowerCase();

                        for (Map.Entry<String, List<String>> entry : interactionToIngredients.entrySet()) {
                            for (String term : entry.getValue()) {
                                if (interactionText.contains(term.toLowerCase())) {
                                    switch (entry.getKey()) {
                                        case "vitamin K":
                                            if (!moderationList.contains("Vitamin K (e.g., spinach, kale)"))
                                                moderationList.add("Vitamin K (e.g., spinach, kale)");
                                            break;
                                        case "fiber":
                                            if (!moderationList.contains("Fiber (e.g., beans, whole grains)"))
                                                moderationList.add("Fiber (e.g., beans, whole grains)");
                                            break;
                                        default:
                                            if (!avoidList.contains(entry.getKey()))
                                                avoidList.add(entry.getKey());
                                            break;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                Log.d("OpenFDA", "No results found for " + medName);
            }

            // Build the interaction messages
            if (!avoidList.isEmpty()) {
                interactions.add("Avoid: " + String.join(", ", avoidList));
            }
            if (!moderationList.isEmpty()) {
                interactions.add("Moderation: " + String.join(", ", moderationList));
            }

        } catch (Exception e) {
            Log.e("OpenFDA", "Exception for " + medName + ": " + e.getMessage());
        }
        return interactions;
    }

    private void fetchRecipesWithExclusions(List<String> exclusions, String diet) {
        try {
            String exclude = String.join(",", exclusions);
            String encodedExclude = URLEncoder.encode(exclude, "UTF-8");
            String encodedDiet = URLEncoder.encode(diet, "UTF-8");

            String url = "https://api.spoonacular.com/recipes/complexSearch?excludeIngredients=" +
                    encodedExclude +
                    "&diet=" + encodedDiet +
                    "&apiKey=12f9cc92173944a8aec01fec5f79c485";

            // Retrofit call with diet parameter
            RetrofitClient.getInstance().getSpoonacularService()
                    .getRecipesWithExclusions("12f9cc92173944a8aec01fec5f79c485", 10, exclude, diet, "")
                    .enqueue(new Callback<RecipeResponse>() {
                        @Override
                        public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                displayFoodRecommendations(response.body().getResults());
                            } else {
                                Log.e("FoodManager", "Failed to get recipes: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<RecipeResponse> call, Throwable t) {
                            Log.e("FoodManager", "API call failed: " + t.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e("FoodManager", "Error encoding the exclude ingredients: " + e.getMessage());
        }
    }

    private void displayFoodRecommendations(List<Recipe> recipes) {
        RecyclerView recyclerView = findViewById(R.id.recipesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecipeAdapter adapter = new RecipeAdapter(recipes);
        recyclerView.setAdapter(adapter);

        // Adding recs to eaten today and removing from list
        adapter.setOnAddClickListener(recipe -> {
            eatenTodayRecipes.add(recipe);
            eatenAdapter.notifyDataSetChanged();
            adapter.removeRecipe(recipe); // <- now call this new method!
        });

    }

    private List<String> extractMedicationNames(QueryDocumentSnapshot doc) {
        List<String> meds = new ArrayList<>();
        String medName = doc.getString("medicationName");

        if (medName != null && !medName.isEmpty()) {
            Log.d("FoodManager", "Extracted medication: " + medName);
            meds.add(medName);
        } else {
            Log.w("FoodManager", "No medicationName found in document: " + doc.getId());
        }

        return meds;
    }


    // Cleans medication names
    private String extractGenericName(String fullName) {

        // Normalize to lowercase
        fullName = fullName.toLowerCase();
        Log.d("ExtractGenericName", "Original: " + fullName);

        // Strip brand info like "[Zocor]"
        if (fullName.contains("[")) {
            fullName = fullName.substring(0, fullName.indexOf("[")).trim();
        }

        // Remove units, dosage forms, and release terms
        String cleanedName = fullName.replaceAll("\\d+\\s*(mg|mcg|g|ml|oz|l|units|iu|tsp|tbsp|mEq)?", "")  // Remove dosage units
                .replaceAll("oral|tablet|capsule|liquid|syrup|suspension|solution|spray|patch|strip|dose|injection|infusion|topical|intravenous|subcutaneous|intramuscular|intranasal|transdermal|inhalation", "")  // Remove forms of the medication
                .replaceAll("extended release|delayed release|controlled release|slow release|sustained release|immediate release|long-acting|short-acting|burst|effervescent", "")  // Remove release types
                .replaceAll("[\\(\\)\\{\\}\\[\\]]", "")  // Remove parentheses and brackets
                .trim();  // Trim leading and trailing spaces


        // Extract just the first word (the main generic name)
        String[] words = cleanedName.split("\\s+"); // Split by spaces
        if (words.length > 0) {
            cleanedName = words[0]; // Use the first word as the cleaned name
        }

        Log.d("ExtractGenericName", "Cleaned: " + cleanedName);

        return cleanedName;
    }

    // This method determines the diet based on the user's conditions
    private String determineDietFromConditions(List<String> conditions) {
        Set<String> tags = new HashSet<>();

        for (String condition : conditions) {
            switch (condition) {
                case "Hypertension":
                    tags.add("low-sodium");
                    break;
                case "Diabetes":
                    tags.add("low-sugar");
                    tags.add("low-carb");
                    break;
                case "High Cholesterol":
                    tags.add("low-fat");
                    break;
                case "Osteoarthritis":
                    tags.add("anti-inflammatory");
                    break;
                case "Gastroenteritis":
                    tags.add("bland");
                    break;
                case "Asthma":
                    tags.add("low-dairy");
                    break;
                case "COPD":
                    tags.add("high-protein");
                    break;
                case "Depression":
                    tags.add("omega-3");
                    break;
                case "Anxiety":
                    tags.add("magnesium-rich");
                    break;
                case "Bipolar":
                    tags.add("low-sugar");
                    break;
                // Add more mappings if needed
            }
        }
        return String.join(",", tags);  // Join all the tags into a comma-separated string
    }
}





