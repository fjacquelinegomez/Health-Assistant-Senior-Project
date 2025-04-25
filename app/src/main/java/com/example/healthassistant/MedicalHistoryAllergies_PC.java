package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicalHistoryAllergies_PC extends AppCompatActivity {
    // Firebase
    FirebaseFirestore db;
    String userId;
    ArrayList<String> foodAllergies = new ArrayList<>(), medicationAllergies = new ArrayList<>();
    ArrayList<String> otherFoodAllergies = new ArrayList<>(),  otherMedicationAllergies = new ArrayList<>(); //new
    // Food allergies - specified checkboxes
    CheckBox peanut, treenuts, dairy, egg, shellfish, fish, soy, gluten, wheat, sesame;
    //Food allergies - other
    CheckBox otherCheckBoxFood;
    LinearLayout otherInputLayoutFood, foodContainer;
    EditText otherEditTextFood;
    Button addItemButtonFood;
    // Medication allergies - specified checkboxes
    CheckBox penicillin, cephalosporins, sulfa, macrolides, fluoroquinolones;
    CheckBox aspirin, nsaids, acetaminophen, opioids;
    CheckBox ace, beta;
    CheckBox carbamazepine, phenytoin, lamotrigine;
    CheckBox insulin, heparin, contrast, anesthetics;
    // Medication allergies - other
    CheckBox otherCheckBoxMeds;
    LinearLayout otherInputLayoutMeds, allergensContainer;
    EditText otherEditTextMeds;
    Button addItemButtonMeds;
    private boolean fromSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history_allergies_pc);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize food allergy checkboxes
        peanut = findViewById(R.id.checkboxPeanut);
        treenuts = findViewById(R.id.checkboxTreenuts);
        dairy = findViewById(R.id.checkboxDairy);
        egg = findViewById(R.id.checkboxEgg);
        shellfish = findViewById(R.id.checkboxShellfish);
        fish = findViewById(R.id.checkboxFish);
        soy = findViewById(R.id.checkboxSoy);
        gluten = findViewById(R.id.checkboxGlutin);
        wheat = findViewById(R.id.checkboxWheat);
        sesame = findViewById(R.id.checkboxSesame);

        // Initialize other food allergy input fields
        otherCheckBoxFood = findViewById(R.id.checkboxFoodAllerOther);
        otherInputLayoutFood = findViewById(R.id.foodAllerOtherInputLayout);
        otherEditTextFood = findViewById(R.id.editTextFoodAllerOther);
        addItemButtonFood = findViewById(R.id.addItemButtonFoodAller);
        foodContainer = findViewById(R.id.foodAllerContainer);

        // Initialize medication allergy checkboxes
        penicillin = findViewById(R.id.checkboxPenicillin);;
        cephalosporins = findViewById(R.id.checkboxCephalosporins);
        sulfa = findViewById(R.id.checkboxSulfa);
        macrolides = findViewById(R.id.checkboxMacrolides);
        fluoroquinolones = findViewById(R.id.checkboxFluoroquinolones);
        aspirin = findViewById(R.id.checkboxAspirin);
        nsaids = findViewById(R.id.checkboxNSAIDs);
        acetaminophen = findViewById(R.id.checkboxAcetaminophen);
        opioids = findViewById(R.id.checkboxOpioids);
        ace = findViewById(R.id.checkboxACE);
        beta = findViewById(R.id.checkboxBeta);
        carbamazepine = findViewById(R.id.checkboxCarb);
        phenytoin = findViewById(R.id.checkboxPhen);
        lamotrigine = findViewById(R.id.checkboxLamo);
        insulin = findViewById(R.id.checkboxInsulin);
        heparin = findViewById(R.id.checkboxHeparin);
        contrast = findViewById(R.id.checkboxContrast);
        anesthetics = findViewById(R.id.checkboxAnesthetics);

        // Initialize other medication allergy input fields
        otherCheckBoxMeds = findViewById(R.id.checkboxAllergensOther);
        otherInputLayoutMeds = findViewById(R.id.allergensOtherInputLayout);
        otherEditTextMeds = findViewById(R.id.editTextAllergensOther);
        addItemButtonMeds = findViewById(R.id.addItemButton);
        allergensContainer = findViewById(R.id.allergensContainer);

        // Food allergies: Listener to show/hide input layout when "Other" checkbox is checked/unchecked
        otherCheckBoxFood.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.GONE;
            otherInputLayoutFood.setVisibility(visibility);
        });

        // For food allergies
        addItemButtonFood.setOnClickListener(v -> {
            String itemText = otherEditTextFood.getText().toString().trim();
            if (!itemText.isEmpty() && !otherFoodAllergies.contains(itemText)) {
                foodAllergies.add(itemText);
                addItemToContainer(itemText, foodContainer);
                otherEditTextFood.setText("");
            }
        });

        // Medication allergies: Listener to show/hide input layout when "Other" checkbox is checked/unchecked
        otherCheckBoxMeds.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.GONE;
            otherInputLayoutMeds.setVisibility(visibility);
        });

        // For medication allergies
        addItemButtonMeds.setOnClickListener(v -> {
            String itemText = otherEditTextMeds.getText().toString().trim();
            if (!itemText.isEmpty() && !otherMedicationAllergies.contains(itemText)) {
                medicationAllergies.add(itemText);
                addItemToContainer(itemText, allergensContainer);
                otherEditTextMeds.setText("");
            }
        });

        // Load data from Firestore
        loadAllergiesFromFirestore();
        fromSettings = getIntent().getBooleanExtra("FROM_SETTINGS", true); // Default is false
        Log.d("ProfileCustomization", "fromSettings value: " + fromSettings); // Debugging
    }

    private void loadAllergiesFromFirestore() {
        db.collection("users").document(userId)
                .collection("medicalHistory").document("allergies")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Load the combined food allergies list
                        List<String> foodAllergiesFromDb = (List<String>) documentSnapshot.get("foodAllergies");
                        if (foodAllergiesFromDb != null) {
                            for (String allergy : foodAllergiesFromDb) {
                                // Set checkbox for each predefined food allergy
                                switch (allergy) {
                                    case "Peanut": peanut.setChecked(true); break;
                                    case "Tree Nuts": treenuts.setChecked(true); break;
                                    case "Dairy": dairy.setChecked(true); break;
                                    case "Egg": egg.setChecked(true); break;
                                    case "Shellfish": shellfish.setChecked(true); break;
                                    case "Fish": fish.setChecked(true); break;
                                    case "Soy": soy.setChecked(true); break;
                                    case "Gluten": gluten.setChecked(true); break;
                                    case "Wheat": wheat.setChecked(true); break;
                                    case "Sesame": sesame.setChecked(true); break;
                                    // Add other cases as needed
                                    default:
                                        // If it's not a predefined allergy, consider it an "Other" allergy
                                        otherFoodAllergies.add(allergy); // Add custom "Other" allergy to the list NEW
                                        addItemToContainer(allergy, foodContainer); // Display custom "Other" allergy
                                        break;
                                }
                            }
                        }

                        // Load medication allergies
                        List<String> medicationAllergiesFromDb = (List<String>) documentSnapshot.get("medicationAllergies");
                        if (medicationAllergiesFromDb != null) {
                            for (String allergy : medicationAllergiesFromDb) {
                                // Set checkbox for each predefined medication allergy
                                switch (allergy) {
                                    case "Penicillin": penicillin.setChecked(true); break;
                                    case "Cephalosporins": cephalosporins.setChecked(true); break;
                                    case "Sulfa drugs": sulfa.setChecked(true); break;
                                    case "Macrolides": macrolides.setChecked(true); break;
                                    case "Fluoroquinolones": fluoroquinolones.setChecked(true); break;
                                    case "Aspirin": aspirin.setChecked(true); break;
                                    case "NSAIDs": nsaids.setChecked(true); break;
                                    case "Acetaminophen": acetaminophen.setChecked(true); break;
                                    case "Opioids": opioids.setChecked(true); break;
                                    case "ACE inhibitors": ace.setChecked(true); break;
                                    case "Beta blockers": beta.setChecked(true); break;
                                    case "Carbamazepine": carbamazepine.setChecked(true); break;
                                    case "Phenytoin": phenytoin.setChecked(true); break;
                                    case "Lamotrigine": lamotrigine.setChecked(true); break;
                                    case "Insulin": insulin.setChecked(true); break;
                                    case "Heparin": heparin.setChecked(true); break;
                                    case "Contrast": contrast.setChecked(true); break;
                                    case "Anesthetics": anesthetics.setChecked(true); break;
                                    // Add other medication allergies as needed
                                    default:
                                        // If it's not a predefined medication allergy, consider it an "Other" allergy
                                        otherMedicationAllergies.add(allergy); // Add custom "Other" allergy to the list NEW
                                        addItemToContainer(allergy, allergensContainer); // Display custom "Other" medication allergy
                                        break;
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading allergies data", e));
    }
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
            if (container == foodContainer) {
                otherFoodAllergies.remove(text);
                foodAllergies.remove(text);
            } else if (container == allergensContainer) {
                otherMedicationAllergies.remove(text);
                medicationAllergies.remove(text);
            }
        });

        itemLayout.addView(textView);
        itemLayout.addView(deleteButton);

        container.addView(itemLayout);
    }
    public void onDoneButtonPressed(View view) {
        // Create the list for food allergies (both standard and "Other" foods)
        ArrayList<String> selectedFoodAllergies = new ArrayList<>(foodAllergies);
        ArrayList<String> selectedMedicationAllergies = new ArrayList<>(medicationAllergies);

        // Add selected checkboxes to the food allergies list
        if (peanut.isChecked()) selectedFoodAllergies.add("Peanut");
        if (treenuts.isChecked()) selectedFoodAllergies.add("Tree Nuts");
        if (dairy.isChecked()) selectedFoodAllergies.add("Dairy");
        if (egg.isChecked()) selectedFoodAllergies.add("Egg");
        if (shellfish.isChecked()) selectedFoodAllergies.add("Shellfish");
        if (fish.isChecked()) selectedFoodAllergies.add("Fish");
        if (soy.isChecked()) selectedFoodAllergies.add("Soy");
        if (gluten.isChecked()) selectedFoodAllergies.add("Gluten");
        if (wheat.isChecked()) selectedFoodAllergies.add("Wheat");
        if (sesame.isChecked()) selectedFoodAllergies.add("Sesame");

        // Add any previously entered "Other" food allergies
        selectedFoodAllergies.addAll(otherFoodAllergies);  // Add the entire list of custom "Other" allergies

        // Add any custom "Other" food allergies if entered by the user
        if (otherCheckBoxFood.isChecked() && !otherEditTextFood.getText().toString().trim().isEmpty()) {
            String otherFood = otherEditTextFood.getText().toString().trim();
            // Make sure the new "Other" allergy doesn't get duplicated
            if (!selectedFoodAllergies.contains(otherFood)) {
                selectedFoodAllergies.add(otherFood);  // Add the custom "Other" food allergy directly to the list
            }
        }
        // Add selected checkboxes to the medication allergies list
        if (penicillin.isChecked()) selectedMedicationAllergies.add("Penicillin");
        if (cephalosporins.isChecked()) selectedMedicationAllergies.add("Cephalosporins");
        if (sulfa.isChecked()) selectedMedicationAllergies.add("Sulfa drugs");
        if (macrolides.isChecked()) selectedMedicationAllergies.add("Macrolides");
        if (fluoroquinolones.isChecked()) selectedMedicationAllergies.add("Fluoroquinolones");
        if (aspirin.isChecked()) selectedMedicationAllergies.add("Aspirin");
        if (nsaids.isChecked()) selectedMedicationAllergies.add("NSAIDs");
        if (acetaminophen.isChecked()) selectedMedicationAllergies.add("Acetaminophen");
        if (opioids.isChecked()) selectedMedicationAllergies.add("Opioids");
        if (ace.isChecked()) selectedMedicationAllergies.add("ACE inhibitors");
        if (beta.isChecked()) selectedMedicationAllergies.add("Beta blockers");
        if (carbamazepine.isChecked()) selectedMedicationAllergies.add("Carbamazepine");
        if (phenytoin.isChecked()) selectedMedicationAllergies.add("Phenytoin");
        if (lamotrigine.isChecked()) selectedMedicationAllergies.add("Lamotrigine");
        if (insulin.isChecked()) selectedMedicationAllergies.add("Insulin");
        if (heparin.isChecked()) selectedMedicationAllergies.add("Heparin");
        if (contrast.isChecked()) selectedMedicationAllergies.add("Contrast");
        if (anesthetics.isChecked()) selectedMedicationAllergies.add("Anesthetics");

        // Add any previously entered "Other" medication allergies
        selectedMedicationAllergies.addAll(otherMedicationAllergies);  // Add the entire list of custom "Other" allergies

        // Add any custom "Other" medication allergies if entered by the user
        if (otherCheckBoxMeds.isChecked() && !otherEditTextMeds.getText().toString().trim().isEmpty()) {
            String otherMedication = otherEditTextFood.getText().toString().trim();
            // Make sure the new "Other" allergy doesn't get duplicated
            if (!selectedMedicationAllergies.contains(otherMedication)) {
                selectedMedicationAllergies.add(otherMedication);  // Add the custom "Other" food allergy directly to the list
            }
        }
        // Save the combined list (both regular foods and "Other" foods) in Firestore
        Map<String, Object> allergyData = new HashMap<>();
        allergyData.put("foodAllergies", selectedFoodAllergies);  // Save the combined list
        allergyData.put("medicationAllergies", selectedMedicationAllergies);  // Save the combined list

        db.collection("users").document(userId)
                .collection("medicalHistory").document("allergies")
                .set(allergyData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Allergies saved");
                    //delete below if errors
                    Intent intent = new Intent(this, MedicalHistory_PC.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);  // Reopen the existing activity
                    startActivity(intent);  // This brings the existing ProfileCustomizationActivity to the front
                    finish();  // Close the current MedicalHistoryActivity
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving allergies", e));
    }
}
