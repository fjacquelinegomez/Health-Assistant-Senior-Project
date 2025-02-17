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

import com.example.healthassistant.databinding.ActivityAppointmentsBinding;
import com.example.healthassistant.databinding.ActivityMedicationManagerBinding;

public class Appointments extends AppCompatActivity {
    ActivityAppointmentsBinding binding;

    /**this was already created**/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointments);

        /**section below is new**/

        /**bottom bar navigation functionality**/
        binding = ActivityAppointmentsBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(Appointments.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(Appointments.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(Appointments.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(Appointments.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(Appointments.this, MedicationManager.class));
                    break;
            }
            return true;
        });
    }
}