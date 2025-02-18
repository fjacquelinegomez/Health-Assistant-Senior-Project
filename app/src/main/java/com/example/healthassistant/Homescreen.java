package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Homescreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homescreen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        ImageButton search = findViewById(R.id.searchButton);
        ImageButton foodManager = findViewById(R.id.foodManagerButton);
        ImageButton healthGoals = findViewById(R.id.healthGoalsButton);
        ImageButton medicationManager = findViewById(R.id.medicationManagerButton);
        ImageButton symptomLogs = findViewById(R.id.symptomLogsButton);
        ImageButton appointments = findViewById(R.id.appointmentReminderButton);
        ImageButton sleepLogs = findViewById(R.id.sleepLogsButton);
        ImageButton settings = findViewById(R.id.userProfileButton);
        Button test = findViewById(R.id.pintest);

        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.searchButton) {
                    startActivity(new Intent(Homescreen.this, Search.class));
                } else if (v.getId() == R.id.foodManagerButton) {
                    startActivity(new Intent(Homescreen.this, FoodManager.class));
                } else if (v.getId() == R.id.healthGoalsButton) {
                    startActivity(new Intent(Homescreen.this, HealthGoals.class));
                } else if (v.getId() == R.id.medicationManagerButton) {
                    startActivity(new Intent(Homescreen.this, MedicationManager.class));
                } else if (v.getId() == R.id.symptomLogsButton) {
                    startActivity(new Intent(Homescreen.this, SymptomLogs.class));
                } else if (v.getId() == R.id.appointmentReminderButton) {
                    startActivity(new Intent(Homescreen.this, Appointments.class));
                } else if (v.getId() == R.id.sleepLogsButton) {
                    startActivity(new Intent(Homescreen.this, SleepLogs.class));
                } else if (v.getId() == R.id.userProfileButton) {
                    startActivity(new Intent(Homescreen.this, Settings.class));
                } else if (v.getId() == R.id.pintest) {
                    startActivity(new Intent(Homescreen.this, PinTest.class));
                }
            }
        };

        search.setOnClickListener(buttonClickListener);
        foodManager.setOnClickListener(buttonClickListener);
        healthGoals.setOnClickListener(buttonClickListener);
        medicationManager.setOnClickListener(buttonClickListener);
        symptomLogs.setOnClickListener(buttonClickListener);
        appointments.setOnClickListener(buttonClickListener);
        sleepLogs.setOnClickListener(buttonClickListener);
        settings.setOnClickListener(buttonClickListener);
        test.setOnClickListener(buttonClickListener);
    }
}