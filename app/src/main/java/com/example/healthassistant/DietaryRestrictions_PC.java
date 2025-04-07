//other checkboxes are unfinished, must save inputted items to firebase
package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DietaryRestrictions_PC extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    private Button btnDone;
    private CheckBox checkboxPeanut, checkboxTreenuts, checkboxDairy, checkboxEgg, checkboxShellfish, checkboxFish, checkboxSoy, checkboxGluten, checkboxWheat, checkboxSesame;
    private CheckBox checkboxVegetarian, checkboxVegan, checkboxPescatarian, checkboxSodium, checkboxSugar, checkboxFat, checkboxFODMAP, checkboxRenal, checkboxKetogenic;
    private CheckBox checkboxFoodAllerOther, checkboxDietsOther;
    private EditText editTextFoodAllerOther, editTextDietsOther;
    private LinearLayout foodAllerContainer, dietsContainer;
    private boolean isLoading = false; // Flag for loading state

    private List<String> foodAllergies = new ArrayList<>();
    private List<String> diets = new ArrayList<>();
    private List<String> otherFoodAllergies = new ArrayList<>();
    private List<String> otherDiets = new ArrayList<>();

    private LinearLayout otherInputLayoutFood, otherInputLayoutDiets;
    private EditText otherEditTextFood, otherEditTextDiets;
    private Button addItemButtonFood, addItemButtonDiets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dietary_restrictions_pc);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get current user ID
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        btnDone = findViewById(R.id.btn_done);
        checkboxPeanut = findViewById(R.id.checkboxPeanut);
        checkboxTreenuts = findViewById(R.id.checkboxTreenuts);
        checkboxDairy = findViewById(R.id.checkboxDairy);
        checkboxEgg = findViewById(R.id.checkboxEgg);
        checkboxShellfish = findViewById(R.id.checkboxShellfish);
        checkboxFish = findViewById(R.id.checkboxFish);
        checkboxSoy = findViewById(R.id.checkboxSoy);
        checkboxGluten = findViewById(R.id.checkboxGluten);
        checkboxWheat = findViewById(R.id.checkboxWheat);
        checkboxSesame = findViewById(R.id.checkboxSesame);
        checkboxVegetarian = findViewById(R.id.checkboxVegetarian);
        checkboxVegan = findViewById(R.id.checkboxVegan);
        checkboxPescatarian = findViewById(R.id.checkboxPescatarian);
        checkboxSodium = findViewById(R.id.checkboxSodium);
        checkboxSugar = findViewById(R.id.checkboxSugar);
        checkboxFat = findViewById(R.id.checkboxFat);
        checkboxFODMAP = findViewById(R.id.checkboxFODMAP);
        checkboxRenal = findViewById(R.id.checkboxRenal);
        checkboxKetogenic = findViewById(R.id.checkboxKetogenic);
        checkboxFoodAllerOther = findViewById(R.id.checkboxFoodAllerOther);
        checkboxDietsOther = findViewById(R.id.checkboxDietsOther);
        editTextFoodAllerOther = findViewById(R.id.editTextFoodAllerOther);
        editTextDietsOther = findViewById(R.id.editTextDietsOther);
        foodAllerContainer = findViewById(R.id.foodAllerContainer);
        dietsContainer = findViewById(R.id.dietsContainer);

        loadDietaryRestrictions();

        btnDone.setOnClickListener(v -> {
            saveDietaryRestrictions();

            Intent intent = new Intent(DietaryRestrictions_PC.this, ProfileCustomization.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });
        // Initialize other views
        otherInputLayoutFood = findViewById(R.id.foodAllerOtherInputLayout);
        otherInputLayoutDiets = findViewById(R.id.dietsOtherInputLayout);
        otherEditTextFood = findViewById(R.id.editTextFoodAllerOther);
        otherEditTextDiets = findViewById(R.id.editTextDietsOther);
        addItemButtonFood = findViewById(R.id.addItemButtonFoodAller);
        addItemButtonDiets = findViewById(R.id.addItemButtonDiets);

// Show/hide input layout for "Other" food allergies
        checkboxFoodAllerOther.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.GONE;
            otherInputLayoutFood.setVisibility(visibility);
        });

// Show/hide input layout for "Other" diets
        checkboxDietsOther.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.GONE;
            otherInputLayoutDiets.setVisibility(visibility);
        });

// Add custom food allergy
        addItemButtonFood.setOnClickListener(v -> {
            String itemText = otherEditTextFood.getText().toString().trim();
            if (!itemText.isEmpty() && !otherFoodAllergies.contains(itemText)) {
                otherFoodAllergies.add(itemText);
                foodAllergies.add(itemText);
                addItemToContainer(itemText, foodAllerContainer);
                otherEditTextFood.setText("");
            }
        });

// Add custom diet
        addItemButtonDiets.setOnClickListener(v -> {
            String itemText = otherEditTextDiets.getText().toString().trim();
            if (!itemText.isEmpty() && !otherDiets.contains(itemText)) {
                otherDiets.add(itemText);
                diets.add(itemText);
                addItemToContainer(itemText, dietsContainer);
                otherEditTextDiets.setText("");
            }
        });
    }

    // Method to add item to container (with delete button functionality)
    private void addItemToContainer(String text, LinearLayout container) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        textView.setPadding(8, 8, 8, 8);
        textView.setTextSize(16);

        Button deleteButton = new Button(this);
        deleteButton.setText("X");
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        deleteButton.setOnClickListener(v -> {
            container.removeView(itemLayout);

            // Remove from correct list
            if (container == foodAllerContainer) {
                otherFoodAllergies.remove(text);
                foodAllergies.remove(text);
            } else if (container == dietsContainer) {
                otherDiets.remove(text);
                diets.remove(text);
            }
        });

        itemLayout.addView(textView);
        itemLayout.addView(deleteButton);

        container.addView(itemLayout);
    }

    public void saveDietaryRestrictions() {
        if (isLoading) return;
        isLoading = true;

        Map<String, Object> dietaryRestrictions = new HashMap<>();
        dietaryRestrictions.put("foodAllergies", getSelectedFoodAllergies());
        dietaryRestrictions.put("diets", getSelectedDiets());

        db.collection("users").document(userId)
                .collection("dietary_restrictions").document("allergies")
                .set(dietaryRestrictions)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(DietaryRestrictions_PC.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DietaryRestrictions_PC.this, "Error saving data.", Toast.LENGTH_SHORT).show();
                    }
                    isLoading = false;
                });
    }

    private List<String> getSelectedFoodAllergies() {
        List<String> foodAllergies = new ArrayList<>();
        if (checkboxPeanut.isChecked()) foodAllergies.add("Peanut");
        if (checkboxTreenuts.isChecked()) foodAllergies.add("Tree Nuts");
        if (checkboxDairy.isChecked()) foodAllergies.add("Dairy");
        if (checkboxEgg.isChecked()) foodAllergies.add("Egg");
        if (checkboxShellfish.isChecked()) foodAllergies.add("Shellfish");
        if (checkboxFish.isChecked()) foodAllergies.add("Fish");
        if (checkboxSoy.isChecked()) foodAllergies.add("Soy");
        if (checkboxGluten.isChecked()) foodAllergies.add("Gluten");
        if (checkboxWheat.isChecked()) foodAllergies.add("Wheat");
        if (checkboxSesame.isChecked()) foodAllergies.add("Sesame");

        if (checkboxFoodAllerOther.isChecked() && !editTextFoodAllerOther.getText().toString().isEmpty()) {
            foodAllergies.add(editTextFoodAllerOther.getText().toString());
        }
        return foodAllergies;
    }

    private List<String> getSelectedDiets() {
        List<String> diets = new ArrayList<>();
        if (checkboxVegetarian.isChecked()) diets.add("Vegetarian");
        if (checkboxVegan.isChecked()) diets.add("Vegan");
        if (checkboxPescatarian.isChecked()) diets.add("Pescatarian");
        if (checkboxSodium.isChecked()) diets.add("Low Sodium");
        if (checkboxSugar.isChecked()) diets.add("Low Sugar");
        if (checkboxFat.isChecked()) diets.add("Low Fat");
        if (checkboxFODMAP.isChecked()) diets.add("Low FODMAP");
        if (checkboxRenal.isChecked()) diets.add("Renal Diet");
        if (checkboxKetogenic.isChecked()) diets.add("Ketogenic Diet");

        if (checkboxDietsOther.isChecked() && !editTextDietsOther.getText().toString().isEmpty()) {
            diets.add(editTextDietsOther.getText().toString());
        }
        return diets;
    }

    private void loadDietaryRestrictions() {
        db.collection("users").document(userId)
                .collection("dietary_restrictions").document("allergies")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> foodAllergies = (List<String>) documentSnapshot.get("foodAllergies");
                        List<String> diets = (List<String>) documentSnapshot.get("diets");

                        if (foodAllergies != null) {
                            setCheckboxState(checkboxPeanut, foodAllergies);
                            setCheckboxState(checkboxTreenuts, foodAllergies);
                            setCheckboxState(checkboxDairy, foodAllergies);
                            setCheckboxState(checkboxEgg, foodAllergies);
                            setCheckboxState(checkboxShellfish, foodAllergies);
                            setCheckboxState(checkboxFish, foodAllergies);
                            setCheckboxState(checkboxSoy, foodAllergies);
                            setCheckboxState(checkboxGluten, foodAllergies);
                            setCheckboxState(checkboxWheat, foodAllergies);
                            setCheckboxState(checkboxSesame, foodAllergies);
                        }

                        if (diets != null) {
                            setCheckboxState(checkboxVegetarian, diets);
                            setCheckboxState(checkboxVegan, diets);
                            setCheckboxState(checkboxPescatarian, diets);
                            setCheckboxState(checkboxSodium, diets);
                            setCheckboxState(checkboxSugar, diets);
                            setCheckboxState(checkboxFat, diets);
                            setCheckboxState(checkboxFODMAP, diets);
                            setCheckboxState(checkboxRenal, diets);
                            setCheckboxState(checkboxKetogenic, diets);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DietaryRestrictions_PC.this, "Error loading data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void setCheckboxState(CheckBox checkBox, List<String> list) {
        checkBox.setChecked(list.contains(checkBox.getText().toString()));
    }
}