package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthassistant.databinding.ActivityFoodManagerBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import food.RecipeResponse;
import food.RecipeResponse.Recipe;
import food.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Class for generating food recommendations based on user allergies, conditions, medications, and preferences.
// Also handles time-based filtering for breakfast and lunch recommendations.
public class FoodManager extends AppCompatActivity {
    ActivityFoodManagerBinding binding;
    private final List<String> avoidIngredients = Collections.synchronizedList(new ArrayList<>());
    private final List<String>[] allergiesHolder = new List[]{new ArrayList<>()};
    private final List<String>[] conditionsHolder = new List[]{new ArrayList<>()};
    private List<String> medicationNames = new ArrayList<>();
    private List<Recipe> eatenTodayRecipes = new ArrayList<>();
    private EatenFoodAdapter eatenAdapter;
    private boolean allergiesLoaded = false;
    private boolean conditionsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        db.collection("users").document(userId)
                .collection("medicalHistory").document("allergies")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> userAllergies = (List<String>) documentSnapshot.get("foodAllergies");
                        allergiesHolder[0] = userAllergies != null ? userAllergies : new ArrayList<>();
                    } else {
                        allergiesHolder[0] = new ArrayList<>();
                    }
                    allergiesLoaded = true;
                    checkIfReadyToGenerate();
                });

        db.collection("users").document(userId)
                .collection("medicalHistory").document("conditions")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> userConditions = (List<String>) documentSnapshot.get("medicalConditions");
                        conditionsHolder[0] = userConditions != null ? userConditions : new ArrayList<>();
                    } else {
                        conditionsHolder[0] = new ArrayList<>();
                    }
                    conditionsLoaded = true;
                    checkIfReadyToGenerate();
                });

        RecyclerView eatenRecyclerView = findViewById(R.id.eatenRecyclerView);
        eatenAdapter = new EatenFoodAdapter(eatenTodayRecipes);
        eatenRecyclerView.setAdapter(eatenAdapter);
        eatenRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadEatenTodayRecipes();
    }

    // Waits for allergies and conditions to load before proceeding.
    private void checkIfReadyToGenerate() {
        if (allergiesLoaded && conditionsLoaded) {
            fetchMedicationsAndStart();
        }
    }

    // Loads user's medications from Firestore, then preferences.
    private void fetchMedicationsAndStart() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("userMedications")
                .whereEqualTo("userID", userId)
                .get()
                .addOnSuccessListener(query -> {
                    List<String> allMeds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        List<String> meds = extractMedicationNames(doc);
                        allMeds.addAll(meds);
                    }
                    medicationNames = allMeds;
                    fetchFoodPreferencesAndStart();
                })
                .addOnFailureListener(e -> {
                    medicationNames = new ArrayList<>();
                    fetchFoodPreferencesAndStart();
                });
    }

    // Loads user's food preferences and initiates recommendation flow.
    private void fetchFoodPreferencesAndStart() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference prefRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("Food_Preferences_PC");

        prefRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> foodPrefs = new ArrayList<>();
                for (DataSnapshot entry : snapshot.getChildren()) {
                    Boolean checked = entry.getValue(Boolean.class);
                    if (checked != null && checked) {
                        foodPrefs.add(entry.getKey());
                    }
                }
                startRecommendationFlow(medicationNames, allergiesHolder[0], conditionsHolder[0], foodPrefs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                startRecommendationFlow(medicationNames, allergiesHolder[0], conditionsHolder[0], new ArrayList<>());
            }
        });
    }

    // Combines all user health data to fetch food recommendations.
    private void startRecommendationFlow(List<String> medications, List<String> allergies, List<String> conditions, List<String> foodPreferences) {
        this.medicationNames = medications != null ? medications : new ArrayList<>();

        Runnable proceed = () -> {
            List<String> combinedExclusions = new ArrayList<>(avoidIngredients);
            if (allergies != null) combinedExclusions.addAll(allergies);
            if (foodPreferences != null) combinedExclusions.addAll(foodPreferences);

            String diet = determineDietFromInputs(conditions != null ? conditions : new ArrayList<>(), foodPreferences != null ? foodPreferences : new ArrayList<>());
            String include = getIncludedIngredients(foodPreferences != null ? foodPreferences : new ArrayList<>());

            List<String> cleanedExclusions = new ArrayList<>();
            for (String item : combinedExclusions) {
                String normalized = item.trim().toLowerCase();
                if (!include.toLowerCase().contains(normalized) &&
                        !normalized.contains("lunch") &&
                        !normalized.equals("vegetarian") &&
                        !normalized.equals("vegan") &&
                        !normalized.equals("omnivore") &&
                        !normalized.equals("ketogenic")) {
                    cleanedExclusions.add(item);
                }
            }

            List<String> preferenceList = Arrays.asList(include.split(","));
            String mealType = isBeforeNoon() ? "breakfast" : "main course";
            fetchRecipesProportionally(preferenceList, cleanedExclusions, diet, mealType);
        };

        if (this.medicationNames.isEmpty()) {
            proceed.run();
        } else {
            fetchFoodInteractionsFromMeds(proceed);
        }
    }

    // Makes separate Spoonacular API calls for each selected food preference, dividing the total desired recipe count evenly among them,
    // and then merges the results into a single list for display.
    private void fetchRecipesProportionally(List<String> preferences, List<String> exclusions, String diet, String mealType) {
        int totalRecipes = 12;
        int perQuery = Math.max(1, totalRecipes / preferences.size());
        List<Recipe> combinedResults = new ArrayList<>();
        AtomicInteger completed = new AtomicInteger(0);

        for (String preference : preferences) {
            RetrofitClient.getInstance().getSpoonacularService()
                    .getRecipesWithExclusions("12f9cc92173944a8aec01fec5f79c485", perQuery, String.join(",", exclusions), diet, preference.trim(), mealType, "random")
                    .enqueue(new Callback<RecipeResponse>() {
                        @Override
                        public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Recipe> filtered = new ArrayList<>();
                                for (Recipe r : response.body().getResults()) {
                                    if (!eatenTodayTitles.contains(r.getTitle().toLowerCase())) {
                                        filtered.add(r);
                                    }
                                }
                                combinedResults.addAll(filtered);
                            }
                            if (completed.incrementAndGet() == preferences.size()) {
                                displayFoodRecommendations(combinedResults);
                            }
                        }

                        @Override
                        public void onFailure(Call<RecipeResponse> call, Throwable t) {
                            Log.e("FoodManager", "API call failed for " + preference + ": " + t.getMessage());
                            if (completed.incrementAndGet() == preferences.size()) {
                                displayFoodRecommendations(combinedResults);
                            }
                        }
                    });
        }
    }


    // Fetches food exclusions based on medication interactions.
    private void fetchFoodInteractionsFromMeds(Runnable onComplete) {
        if (medicationNames == null || medicationNames.isEmpty()) {
            Log.i("FoodManager", "No medications provided");
            onComplete.run();
            return;
        }

        AtomicInteger medsProcessed = new AtomicInteger(0);
        int totalMeds = medicationNames.size();
        List<String> displayWarnings = Collections.synchronizedList(new ArrayList<>());

        for (String med : medicationNames) {
            String cleanedMed = extractGenericName(med);
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
                        updateFoodInteractionWarnings(displayWarnings);
                        onComplete.run();
                    });
                }
            }).start();
        }
    }

    // Updates the UI to display warnings about food interactions.
    private void updateFoodInteractionWarnings(List<String> warnings) {
        TextView textView = findViewById(R.id.moderationWarning);
        if (warnings == null || warnings.isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            StringBuilder displayText = new StringBuilder();
            Set<String> avoidSet = new LinkedHashSet<>();
            Set<String> moderationSet = new LinkedHashSet<>();

            for (String warning : warnings) {
                if (warning.startsWith("Avoid: ")) {
                    String[] items = warning.replace("Avoid: ", "").split(",");
                    for (String item : items) avoidSet.add(item.trim());
                } else if (warning.startsWith("Moderation: ")) {
                    String[] items = warning.replace("Moderation: ", "").split(",");
                    for (String item : items) moderationSet.add(item.trim());
                }
            }

            if (!avoidSet.isEmpty()) displayText.append("\uD83D\uDEA9 Avoid: ").append(String.join(", ", avoidSet)).append("\n");
            if (!moderationSet.isEmpty()) displayText.append("\uD83C\uDF3F Moderation: ").append(String.join(", ", moderationSet));

            textView.setText(displayText.toString().trim());
            textView.setVisibility(View.VISIBLE);
        }
    }

    // Contacts OpenFDA to identify food-medication conflicts.
    private List<String> getFoodInteractionsFromOpenFDA(String medName) {
        List<String> interactions = new ArrayList<>();
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
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) return interactions;

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
            reader.close();

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
                                            moderationList.add("Vitamin K (e.g., spinach, kale)");
                                            break;
                                        case "fiber":
                                            moderationList.add("Fiber (e.g., beans, whole grains)");
                                            break;
                                        default:
                                            avoidList.add(entry.getKey());
                                            break;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (!avoidList.isEmpty()) interactions.add("Avoid: " + String.join(", ", avoidList));
            if (!moderationList.isEmpty()) interactions.add("Moderation: " + String.join(", ", moderationList));

        } catch (Exception e) {
            Log.e("OpenFDA", "Exception: " + e.getMessage());
        }
        return interactions;
    }

    // Determines the best diet tag to use based on conditions + preferences.
    private String determineDietFromInputs(List<String> conditions, List<String> foodPreferences) {
        Set<String> tags = new HashSet<>();

        for (String condition : conditions) {
            switch (condition) {
                case "Hypertension": tags.add("low-sodium"); break;
                case "Diabetes": tags.add("low-sugar"); tags.add("low-carb"); break;
                case "High Cholesterol": tags.add("low-fat"); break;
                case "Osteoarthritis": tags.add("anti-inflammatory"); break;
                case "Gastroenteritis": tags.add("bland"); break;
                case "Asthma": tags.add("low-dairy"); break;
                case "COPD": tags.add("high-protein"); break;
                case "Depression": tags.add("omega-3"); break;
                case "Anxiety": tags.add("magnesium-rich"); break;
                case "Bipolar": tags.add("low-sugar"); break;
            }
        }

        for (String pref : foodPreferences) {
            switch (pref) {
                case "Vegetarian": tags.add("vegetarian"); break;
                case "Vegan": tags.add("vegan"); break;
                case "Ketogenic": tags.add("ketogenic"); break;
                case "Light fat": tags.add("low-fat"); break;
            }
        }

        return String.join(",", tags);
    }

    // Chooses ingredients to include in recommendations based on time (breakfast/lunch) and preferences.
    private String getIncludedIngredients(List<String> foodPreferences) {
        Set<String> includes = new HashSet<>();
        boolean isBreakfastTime = isBeforeNoon();

        if (isBreakfastTime) {
            if (foodPreferences.contains("Pancakes")) includes.add("pancakes");
            if (foodPreferences.contains("Eggs")) includes.add("eggs");
            if (foodPreferences.contains("Ham")) includes.add("ham");
            if (foodPreferences.contains("Tomatoe")) includes.add("tomato");
            if (foodPreferences.contains("Sandwiches")) includes.add("sandwich");
            if (foodPreferences.contains("Fruits")) includes.add("fruits");
            if (foodPreferences.contains("Cheese")) includes.add("cheese");
            if (foodPreferences.contains("Olives")) includes.add("olives");
            if (foodPreferences.contains("Croissants")) includes.add("croissant");
        } else {
            if (foodPreferences.contains("Salad")) includes.add("salad");
            if (foodPreferences.contains("Meat")) includes.add("meat");
            if (foodPreferences.contains("Pork")) includes.add("pork");
            if (foodPreferences.contains("Cucumber")) includes.add("cucumber");
            if (foodPreferences.contains("Veggies")) includes.add("vegetables");
            if (foodPreferences.contains("Sandwich Lunch")) includes.add("sandwich");
            if (foodPreferences.contains("Snacks Lunch")) includes.add("snack");
        }
        if (foodPreferences.contains("Water")) includes.add("water");
        if (foodPreferences.contains("Spark Water")) includes.add("sparkling water");
        if (foodPreferences.contains("Milk")) includes.add("milk");
        if (foodPreferences.contains("Soft Drink")) includes.add("soda");
        if (foodPreferences.contains("Alcohol")) includes.add("wine");
        if (foodPreferences.contains("Juice")) includes.add("juice");

        Log.d("RecipeRequest", "Time = " + (isBreakfastTime ? "Morning" : "Afternoon"));
        Log.d("RecipeRequest", "Include = " + String.join(",", includes));

        return String.join(",", includes);
    }

    // Makes the API call to Spoonacular with diet and inclusion/exclusion filters.
    private void fetchRecipesWithExclusions(List<String> exclusions, String diet, String query) {
        try {
            String exclude = String.join(",", exclusions);
            String mealType = isBeforeNoon() ? "breakfast" : "main course";

            Log.d("RecipeRequest", "Diet = " + diet);
            Log.d("RecipeRequest", "Query = " + query);
            Log.d("RecipeRequest", "Exclude = " + exclude);
            Log.d("RecipeRequest", "Type = " + mealType);

            RetrofitClient.getInstance().getSpoonacularService()
                    .getRecipesWithExclusions("12f9cc92173944a8aec01fec5f79c485", 10, exclude, diet, query, mealType, "random")
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
            Log.e("FoodManager", "Error building request: " + e.getMessage());
        }
    }

    // Populates the UI with recommended recipes.
    private void displayFoodRecommendations(List<Recipe> recipes) {
        RecyclerView recyclerView = findViewById(R.id.recipesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecipeAdapter adapter = new RecipeAdapter(recipes);
        recyclerView.setAdapter(adapter);

        adapter.setOnAddClickListener(recipe -> {
            eatenTodayRecipes.add(recipe);
            eatenAdapter.notifyDataSetChanged();
            adapter.removeRecipe(recipe);

            saveRecipeToEatenToday(recipe); // new
        });
    }

    // Utility to extract medications from Firestore documents.
    private List<String> extractMedicationNames(QueryDocumentSnapshot doc) {
        List<String> meds = new ArrayList<>();
        String medName = doc.getString("medicationName");
        if (medName != null && !medName.isEmpty()) {
            meds.add(medName);
        }
        return meds;
    }

    // Cleans medication names for use in FDA queries.
    private String extractGenericName(String fullName) {
        fullName = fullName.toLowerCase();
        if (fullName.contains("[")) {
            fullName = fullName.substring(0, fullName.indexOf("[")).trim();
        }

        String cleanedName = fullName.replaceAll("\\d+\\s*(mg|mcg|g|ml|oz|l|units|iu|tsp|tbsp|mEq)?", "")
                .replaceAll("oral|tablet|capsule|liquid|syrup|suspension|solution|spray|patch|strip|dose|injection|infusion|topical|intravenous|subcutaneous|intramuscular|intranasal|transdermal|inhalation", "")
                .replaceAll("extended release|delayed release|controlled release|slow release|sustained release|immediate release|long-acting|short-acting|burst|effervescent", "")
                .replaceAll("[\\(\\)\\{\\}\\[\\]]", "")
                .trim();

        String[] words = cleanedName.split("\\s+");
        if (words.length > 0) {
            cleanedName = words[0];
        }
        return cleanedName;
    }

    // Checks the current time to determine if it's morning.
    private boolean isBeforeNoon() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour < 12;
//        return true;
    }

    private void saveRecipeToEatenToday(Recipe recipe) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .collection("eatenToday").document(today)
                .collection("items").add(recipe)
                .addOnSuccessListener(docRef -> Log.d("FoodManager", "Recipe saved to eatenToday"))
                .addOnFailureListener(e -> Log.e("FoodManager", "Failed to save recipe", e));
    }

    private final Set<String> eatenTodayTitles = new HashSet<>();
    private void loadEatenTodayRecipes() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("eatenToday").document(today)
                .collection("items")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    eatenTodayRecipes.clear();
                    eatenTodayTitles.clear(); // clear the exclusion list too

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Recipe recipe = doc.toObject(Recipe.class);
                        eatenTodayRecipes.add(recipe);
                        eatenTodayTitles.add(recipe.getTitle().toLowerCase()); // or use recipe.getId() for better accuracy
                    }

                    eatenAdapter.notifyDataSetChanged();
                });
    }
}
