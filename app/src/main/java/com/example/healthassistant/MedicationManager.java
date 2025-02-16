package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthassistant.databinding.ActivityFoodManagerBinding;
import com.example.healthassistant.databinding.ActivityMedicationManagerBinding;

public class MedicationManager extends AppCompatActivity {

    ActivityMedicationManagerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_manager);
        /**
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button back = findViewById(R.id.medicationManagerBackButton);
        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.medicationManagerBackButton) {
                    startActivity(new Intent(MedicationManager.this, Homescreen.class));
                }
            }
        };
        back.setOnClickListener(buttonClickListener);
         **/


        binding = ActivityMedicationManagerBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(MedicationManager.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(MedicationManager.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(MedicationManager.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(MedicationManager.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(MedicationManager.this, MedicationManager.class));
                    break;

            }
            return true;
        });
        Button button = (Button) findViewById(R.id.currentMedicationButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MedicationManager.this, MedicationManager2.class));
            }
        });

    }
}