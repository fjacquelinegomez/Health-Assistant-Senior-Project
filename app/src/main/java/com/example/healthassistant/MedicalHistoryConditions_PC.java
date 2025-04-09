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

public class MedicalHistoryConditions_PC extends AppCompatActivity {
    // Firebase
    FirebaseFirestore db;
    String userId;

    // Data lists
    ArrayList<String> otherConditions = new ArrayList<>();
    ArrayList<String> customConditions = new ArrayList<>();

    // UI components
    CheckBox hypertension, diabetes, asthma, osteoarthritis, copd;
    CheckBox flu, cold, migraine, pneumonia, gastroenteritis;
    CheckBox depression, anxiety, bipolar, ocd, ptsd;
    private EditText editTextConditionOther;
    private Button addItemButtonCondition;
    private LinearLayout conditionContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history_conditions_pc);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Find views
        hypertension = findViewById(R.id.checkboxHypertension);
        diabetes = findViewById(R.id.checkboxDiabetes);
        asthma = findViewById(R.id.checkboxAsthma);
        osteoarthritis = findViewById(R.id.checkboxOsteoarthritis);
        copd = findViewById(R.id.checkboxCOPD);
        flu = findViewById(R.id.checkboxFlu);
        cold = findViewById(R.id.checkboxCold);
        migraine = findViewById(R.id.checkboxMigraine);
        pneumonia = findViewById(R.id.checkboxPneumonia);
        gastroenteritis = findViewById(R.id.checkboxGastroenteritis);
        depression = findViewById(R.id.checkboxDepression);
        anxiety = findViewById(R.id.checkboxAnxiety);
        bipolar = findViewById(R.id.checkboxBipolar);
        ocd = findViewById(R.id.checkboxOCD);
        ptsd = findViewById(R.id.checkboxPTSD);

        editTextConditionOther = findViewById(R.id.editTextConditionsOther);
        addItemButtonCondition = findViewById(R.id.addItemButtonConditions);
        conditionContainer = findViewById(R.id.conditionsContainer);

        addItemButtonCondition.setOnClickListener(v -> {
            String itemText = editTextConditionOther.getText().toString().trim();
            if (!itemText.isEmpty() && !customConditions.contains(itemText)) {
                customConditions.add(itemText);
                addItemToContainer(itemText, conditionContainer);
                editTextConditionOther.setText("");
            }
        });

        // Load from Firestore
        loadMedicalConditionsFromFirestore();
    }

    private void loadMedicalConditionsFromFirestore() {
        db.collection("users").document(userId)
                .collection("medicalHistory").document("conditions")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> medicalConditionsFromDb = (List<String>) documentSnapshot.get("medicalConditions");
                        if (medicalConditionsFromDb != null) {
                            for (String condition : medicalConditionsFromDb) {
                                switch (condition) {
                                    case "Hypertension": hypertension.setChecked(true); break;
                                    case "Diabetes": diabetes.setChecked(true); break;
                                    case "Asthma": asthma.setChecked(true); break;
                                    case "Osteoarthritis": osteoarthritis.setChecked(true); break;
                                    case "COPD": copd.setChecked(true); break;
                                    case "Depression": depression.setChecked(true); break;
                                    case "Anxiety": anxiety.setChecked(true); break;
                                    case "Bipolar": bipolar.setChecked(true); break;
                                    case "OCD": ocd.setChecked(true); break;
                                    case "PTSD": ptsd.setChecked(true); break;
                                    case "Flu": flu.setChecked(true); break;
                                    case "Cold": cold.setChecked(true); break;
                                    case "Migraine": migraine.setChecked(true); break;
                                    case "Pneumonia": pneumonia.setChecked(true); break;
                                    case "Gastroenteritis": gastroenteritis.setChecked(true); break;
                                    default:
                                        otherConditions.add(condition);
                                        addItemToContainer(condition, conditionContainer);
                                        break;
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading medical conditions", e));
    }

    public void onDoneButtonPressed(View view) {
        ArrayList<String> selectedConditions = new ArrayList<>();

        // Add standard checkboxes
        if (hypertension.isChecked()) selectedConditions.add("Hypertension");
        if (diabetes.isChecked()) selectedConditions.add("Diabetes");
        if (asthma.isChecked()) selectedConditions.add("Asthma");
        if (osteoarthritis.isChecked()) selectedConditions.add("Osteoarthritis");
        if (copd.isChecked()) selectedConditions.add("COPD");
        if (depression.isChecked()) selectedConditions.add("Depression");
        if (anxiety.isChecked()) selectedConditions.add("Anxiety");
        if (bipolar.isChecked()) selectedConditions.add("Bipolar");
        if (ocd.isChecked()) selectedConditions.add("OCD");
        if (ptsd.isChecked()) selectedConditions.add("PTSD");
        if (flu.isChecked()) selectedConditions.add("Flu");
        if (cold.isChecked()) selectedConditions.add("Cold");
        if (migraine.isChecked()) selectedConditions.add("Migraine");
        if (pneumonia.isChecked()) selectedConditions.add("Pneumonia");
        if (gastroenteritis.isChecked()) selectedConditions.add("Gastroenteritis");

        // Add previously loaded custom conditions
        selectedConditions.addAll(otherConditions);

        // Add newly entered custom conditions
        selectedConditions.addAll(customConditions);

        // Save to Firestore
        Map<String, Object> conditionData = new HashMap<>();
        conditionData.put("medicalConditions", selectedConditions);

        db.collection("users").document(userId)
                .collection("medicalHistory").document("conditions")
                .set(conditionData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Medical conditions saved");
                    //delete below if errors
                    Intent intent = new Intent(this, MedicalHistory_PC.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);  // Reopen the existing activity
                    startActivity(intent);  // This brings the existing ProfileCustomizationActivity to the front
                    finish();  // Close the current MedicalHistoryActivity
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving medical conditions", e));
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
            customConditions.remove(text);
        });

        itemLayout.addView(textView);
        itemLayout.addView(deleteButton);

        container.addView(itemLayout);
    }
}