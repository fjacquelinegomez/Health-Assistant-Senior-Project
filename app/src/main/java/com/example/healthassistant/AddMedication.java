package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddMedication extends AppCompatActivity {
    private EditText expireInput;
    private EditText totalPillsInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        EditText expireInput = findViewById(R.id.expireInput);
        EditText totalPillsInput = findViewById(R.id.countInput);
        Button addMedicationButton = findViewById(R.id.addMedicationButton);

        addMedicationButton.setOnClickListener(v -> {
            String expirationDate = expireInput.getText().toString();
            int totalPills = Integer.parseInt(totalPillsInput.getText().toString());

            Intent intent = new Intent(AddMedication.this, MedicationManager2.class);
            intent.putExtra("EXPIRE_DATE", expirationDate);
            intent.putExtra("TOTAL_PILLS", totalPills);
            startActivity(intent);
        });
    }
    }
