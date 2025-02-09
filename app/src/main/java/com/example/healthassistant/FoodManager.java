package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthassistant.databinding.ActivityFoodManagerBinding;

public class FoodManager extends AppCompatActivity {

    ActivityFoodManagerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_food_manager);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
         **/



        binding = ActivityFoodManagerBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(FoodManager.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(FoodManager.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(FoodManager.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(FoodManager.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(FoodManager.this, MedicationManager.class));
                    break;

            }
            return true;
        });

    }
}