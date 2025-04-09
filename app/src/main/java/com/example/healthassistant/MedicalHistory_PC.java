package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class MedicalHistory_PC extends AppCompatActivity {
    ImageButton allergiesBtn;
    ImageButton conditionsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history_pc);

        allergiesBtn = findViewById(R.id.allergiesButton);
        allergiesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MedicalHistory_PC.this, MedicalHistoryAllergies_PC.class);
            startActivity(intent);
        });

        conditionsBtn = findViewById(R.id.conditionsButton);
        conditionsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MedicalHistory_PC.this, MedicalHistoryConditions_PC.class);
            startActivity(intent);
        });
    }

    public void onSubmitMedicalHistory(View view) { // Call this when user submits
        // Indicate that the user successfully completed this step
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish(); // Close this activity and return to ProfileSetupActivity
    }
}