package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthassistant.databinding.ActivityFoodManagerBinding;
import com.example.healthassistant.databinding.ActivityMedicationManager2Binding;
import com.example.healthassistant.databinding.ActivityMedicationManagerBinding;

public class MedicationManager2 extends AppCompatActivity {
    ActivityMedicationManager2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_manager2);

        /**bottom bar navigation functionality**/
        binding = ActivityMedicationManager2Binding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(MedicationManager2.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(MedicationManager2.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(MedicationManager2.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(MedicationManager2.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(MedicationManager2.this, MedicationManager.class));
                    break;

            }
            return true;
        });

    }
}