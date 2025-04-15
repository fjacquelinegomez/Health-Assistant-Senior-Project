package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthassistant.databinding.ActivityFoodManagerBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HealthGoals extends AppCompatActivity {


    private ImageButton btnMH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_health_goals);



        /*bottom bar navigation functionality*/

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);

        nav.setOnItemSelectedListener(item -> {
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



        // initialize a button for testing
        btnMH = findViewById(R.id.stressLogButton);

        // Medical History Button Logic
        btnMH.setOnClickListener(v -> {
            Intent intent = new Intent(HealthGoals.this, Stress_HealthGoals_PC.class);
            startActivity(intent);
        });

    }
}