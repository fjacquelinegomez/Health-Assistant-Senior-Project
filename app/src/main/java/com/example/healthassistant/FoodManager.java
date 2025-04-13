//get api key

package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healthassistant.databinding.ActivityFoodManagerBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import android.os.Handler;
import android.os.Looper;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;

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

        List<String>[] allergiesHolder = new List[]{new ArrayList<>()};
        List<String>[] conditionsHolder = new List[]{new ArrayList<>()};
        final boolean[] allergiesFetched = {false};
        final boolean[] conditionsFetched = {false};

// Fetch allergies
        db.collection("users").document(userId)
                .collection("medicalHistory").document("allergies")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> userAllergies = (List<String>) documentSnapshot.get("foodAllergies");
                        if (userAllergies != null) allergiesHolder[0] = userAllergies;
                    }
                    allergiesFetched[0] = true;
                    checkAndGenerate(allergiesHolder[0], conditionsHolder[0], allergiesFetched[0], conditionsFetched[0]);
                });

// Fetch conditions
        db.collection("users").document(userId)
                .collection("medicalHistory").document("conditions")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> userConditions = (List<String>) documentSnapshot.get("medicalConditions");
                        if (userConditions != null) conditionsHolder[0] = userConditions;
                    }
                    conditionsFetched[0] = true;
                    checkAndGenerate(allergiesHolder[0], conditionsHolder[0], allergiesFetched[0], conditionsFetched[0]);
                });




    }

    private void checkAndGenerate(List<String> allergies, List<String> conditions, boolean allergiesReady, boolean conditionsReady) {
        if (allergiesReady && conditionsReady) {
            generateFoodRecommendations(allergies, conditions);
        }
    }

    private void generateFoodRecommendations(List<String> allergies, List<String> conditions) {
        String intolerances = TextUtils.join(",", allergies);  // Convert allergies list to a comma-separated string

        // Determine the diet restrictions based on the medical conditions
        String diet = determineDietFromConditions(conditions);

        RetrofitClient.getInstance().getSpoonacularService()
                .getRecipes(
                        "12f9cc92173944a8aec01fec5f79c485",  // Replace with your actual API key
                        10,                                  // Number of recipes to fetch
                        intolerances,                        // User's allergies
                        diet                                 // User's diet based on conditions
                ).enqueue(new Callback<RecipeResponse>() {
                    @Override
                    public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Get the list of recipes from the response
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
    }

    private void displayFoodRecommendations(List<Recipe> recipes) {
        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recipesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up the Adapter
        RecipeAdapter adapter = new RecipeAdapter(recipes);
        recyclerView.setAdapter(adapter);
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
                // Add more conditions here as needed
            }
        }

        return String.join(",", tags);  // Join all the tags into a comma-separated string
    }
}


