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

public class HealthGoals extends AppCompatActivity {

    ActivityFoodManagerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_health_goals);



            /**bottom bar navigation functionality**/
            binding = ActivityFoodManagerBinding.inflate(getLayoutInflater());

            setContentView(binding.getRoot());

            binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                switch (item.getItemId()) {
                    case R.id.home:
                        startActivity(new Intent(HealthGoals.this, Homescreen.class));
                        break;
                    case R.id.searchMedication:
                        startActivity(new Intent(HealthGoals.this, Search.class));
                        break;
                    case R.id.foodManager:
                        startActivity(new Intent(HealthGoals.this, FoodManager.class));
                        break;
                    case R.id.healthGoals:
                        startActivity(new Intent(HealthGoals.this, HealthGoals.class));
                        break;
                    case R.id.medicationManager:
                        startActivity(new Intent(HealthGoals.this, MedicationManager.class));
                        break;

                }
                return true;

        });

    }
}