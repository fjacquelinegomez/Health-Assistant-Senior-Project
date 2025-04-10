//get api key

package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healthassistant.databinding.ActivityFoodManagerBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import food.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// These two should match your package structure
import food.RecipeResponse.Recipe;
import food.RecipeResponse;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class FoodManager extends AppCompatActivity {

    ActivityFoodManagerBinding binding;

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

        db.collection("users").document(userId)
                .collection("medicalHistory").document("allergies")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> userAllergies = (List<String>) documentSnapshot.get("foodAllergies");
                        if (userAllergies != null && !userAllergies.isEmpty()) {
                            // Pass the allergies to generate food recommendations
                            generateFoodRecommendations(userAllergies);
                        }
                    }
                });
    }

    private void generateFoodRecommendations(List<String> allergies) {
        String intolerances = TextUtils.join(",", allergies);  // Convert allergies list to a comma-separated string

        // Access RetrofitClient directly
        RetrofitClient.getInstance().getSpoonacularService()
                .getRecipes(
                        "12f9cc92173944a8aec01fec5f79c485",  // Replace with your actual API key
                        10,                          // Number of recipes to fetch
                        intolerances,                // User's allergies
                        null                          // Optionally, you can pass the user's diet here as well
                ).enqueue(new Callback<RecipeResponse>() {
                    @Override
                    public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Get the list of recipes from the response
                            List<Recipe> recipes = response.body().getResults();

                            // Call your method to display these recipes
                            displayFoodRecommendations(recipes);
                        } else {
                            Log.e("FoodManager", "Failed to get recipes: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeResponse> call, Throwable t) {
                        Log.e("FoodManager", "API call failed: " + t.getMessage());
                    }
                });
    }

//    private void displayFoodRecommendations(List<Recipe> recipes) {
//        // Display the recipes (e.g., populate a RecyclerView)
//        // For now, just log the recipes for demonstration purposes
//        for (Recipe recipe : recipes) {
//            Log.d("FoodManager", "Recipe: " + recipe.getTitle() + ", Image: " + recipe.getImage());
//        }
//    }
        private void displayFoodRecommendations(List<Recipe> recipes) {
        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recipesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up the Adapter
        RecipeAdapter adapter = new RecipeAdapter(recipes);
        recyclerView.setAdapter(adapter);
    }
}

//    private void displayFoodRecommendations(List<Recipe> recipes) {
//        // Setup RecyclerView
//        RecyclerView recyclerView = findViewById(R.id.recipesRecyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // Set up the Adapter
//        RecipeAdapter adapter = new RecipeAdapter(recipes);
//        recyclerView.setAdapter(adapter);
//    }



//public class FoodManager extends AppCompatActivity {
//
//    ActivityFoodManagerBinding binding;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_food_manager);
//
//        binding = ActivityFoodManagerBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.home:
//                    startActivity(new Intent(FoodManager.this, Homescreen.class));
//                    break;
//                case R.id.searchMedication:
//                    startActivity(new Intent(FoodManager.this, Search.class));
//                    break;
//                case R.id.foodManager:
//                    startActivity(new Intent(FoodManager.this, FoodManager.class));
//                    break;
//                case R.id.healthGoals:
//                    startActivity(new Intent(FoodManager.this, HealthGoals_PC.class));
//                    break;
//                case R.id.medicationManager:
//                    startActivity(new Intent(FoodManager.this, MedicationManager.class));
//                    break;
//            }
//            return true;
//        });
//
//        db.collection("users").document(userId)
//                .collection("medicalHistory").document("allergies")
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        List<String> userAllergies = (List<String>) documentSnapshot.get("foodAllergies");
//                        if (userAllergies != null && !userAllergies.isEmpty()) {
//                            // Pass the allergies to generate food recommendations
//                            generateFoodRecommendations(userAllergies);
//                        }
//                    }
//                });
//    }
//
//    private void generateFoodRecommendations(List<String> allergies) {
//        String intolerances = TextUtils.join(",", allergies);  // Convert allergies list to a comma-separated string
//
//        RetrofitClient.getInstance().getSpoonacularService()
//                .getRecipes(
//                        "12f9cc92173944a8aec01fec5f79c485",  // Replace with your actual API key
//                        10,                          // Number of recipes to fetch
//                        intolerances,                // User's allergies
//                        null                          // Optionally, you can pass the user's diet here as well
//                ).enqueue(new Callback<RecipeResponse>() {
//                    @Override
//                    public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
//                        if (response.isSuccessful() && response.body() != null) {
//                            // Get the list of recipes from the response
//                            List<Recipe> recipes = response.body().getResults();
//
//                            // Call your method to display these recipes
//                            displayFoodRecommendations(recipes);
//                        } else {
//                            Log.e("FoodManager", "Failed to get recipes: " + response.message());
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<RecipeResponse> call, Throwable t) {
//                        Log.e("FoodManager", "API call failed: " + t.getMessage());
//                    }
//                });
//    }
//
//    private void displayFoodRecommendations(List<Recipe> recipes) {
//        // Setup RecyclerView
//        RecyclerView recyclerView = findViewById(R.id.recipesRecyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // Set up the Adapter
//        RecipeAdapter adapter = new RecipeAdapter(recipes);
//        recyclerView.setAdapter(adapter);
//    }
//}
