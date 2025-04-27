package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthassistant.databinding.ActivityFoodManagerBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class HealthGoals extends AppCompatActivity {


    private ImageButton stress_hg_btn, cholesterol_hg_btn, bp_hg_btn;

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



        // initialize a stress button
        stress_hg_btn = findViewById(R.id.stressLogButton);

        // Stress Button Logic
        stress_hg_btn.setOnClickListener(v -> {
            Intent intent = new Intent(HealthGoals.this, Stress_HealthGoals_PC.class);
            startActivity(intent);
        });


        // initialize cholesterol button
        cholesterol_hg_btn = findViewById(R.id.CholesterolLogButton);

        // Cholesterol Button Logic
        cholesterol_hg_btn.setOnClickListener(v -> {
            Intent intent = new Intent(HealthGoals.this, Cholesterol_HealthGoals.class);
            startActivity(intent);
        });


        bp_hg_btn = findViewById(R.id.BloodPLogButton);

        // Blood Pressure Button Logic
        bp_hg_btn.setOnClickListener(v -> {
            Intent intent = new Intent(HealthGoals.this, BloodPressure_HealthGoals.class);
            startActivity(intent);
        });


    }

}