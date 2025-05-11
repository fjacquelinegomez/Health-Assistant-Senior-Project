package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SymptomAnalysis extends AppCompatActivity {

    private Spinner symptomSpinner, ratingSpinner, medicineSpinner;
    private TextView analysisResult;
    private Button analyzeButton;
    private ImageButton backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_symptom_analysis);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        symptomSpinner = findViewById(R.id.symptom_spinner);
        medicineSpinner = findViewById(R.id.medicine_spinner);
        ratingSpinner = findViewById(R.id.rating_spinner);
        analyzeButton = findViewById(R.id.analyze_button);
        analysisResult = findViewById(R.id.analysisResult);
        backButton = findViewById(R.id.backButton);

        // Set up back button
        backButton.setOnClickListener(v -> finish());
        setupSpinners();

        analyzeButton.setOnClickListener(v -> analyzeSymptom());




        /*bottom bar navigation functionality*/
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);

        nav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(SymptomAnalysis.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(SymptomAnalysis.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(SymptomAnalysis.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(SymptomAnalysis.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(SymptomAnalysis.this, MedicationManager.class));
                    break;
            }
            return true;
        });

    }

    private void setupSpinners() {
        // Symptoms
        String[] symptoms = {"Fever", "Cough", "Headache", "Fatigue", "Nausea"};
        ArrayAdapter<String> symptomAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, symptoms);
        symptomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        symptomSpinner.setAdapter(symptomAdapter);

        // Ratings
        String[] ratings = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        ArrayAdapter<String> ratingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ratings);
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ratingSpinner.setAdapter(ratingAdapter);

        // Medications
        String[] medicines = {
                "💊 Ibuprofen", "🩹 Acetaminophen", "💤 Melatonin", "💚 Antihistamine",
                "🤧 Decongestant", "🧠 Sertraline", "🫀 Metoprolol", "💉 Insulin",
                "🌿 Herbal Supplement", "💤 ZzzQuil", "🧪 Omeprazole", "🫁 Albuterol",
                "🩺 Aspirin", "🧬 Amoxicillin", "🔥 Naproxen", "🛌 Trazodone"
        };

        ArrayAdapter<String> medicineAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, medicines);
        medicineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        medicineSpinner.setAdapter(medicineAdapter);
    }



    private void analyzeSymptom() {
        String symptom = symptomSpinner.getSelectedItem().toString();
        int rating = Integer.parseInt(ratingSpinner.getSelectedItem().toString());
        String medicine = medicineSpinner.getSelectedItem().toString();

        String result;
        if (rating >= 8) {
            result = "⚠️ You rated your " + symptom + " as severe. If it's persistent, consult a healthcare provider.";
        } else if (rating >= 5) {
            result = "🔍 Your " + symptom + " is moderate. Monitor it and consider using " + medicine + ".";
        } else {
            result = "✅ Mild " + symptom + ". Stay hydrated and rest. " + medicine + " may help.";
        }

        analysisResult.setText(result);
    }
}




