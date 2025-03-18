package com.example.healthassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthassistant.databinding.ActivityHomescreenBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class Homescreen extends AppCompatActivity {

    //used for bottom bar
    ActivityHomescreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //bottom bar
        binding = ActivityHomescreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(Homescreen.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(Homescreen.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(Homescreen.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(Homescreen.this, HealthGoals_PC.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(Homescreen.this, MedicationManager.class));
                    break;
            }
            return false;
        });

        //apointments button
        ImageButton buttonAppoint = (ImageButton) findViewById(R.id.appointmentReminderButton);
        buttonAppoint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Homescreen.this, Appointments.class));
            }
        });

        //sleep tracker button
        ImageButton buttonSleep = (ImageButton) findViewById(R.id.sleepLogsButton);
        buttonSleep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Homescreen.this, SleepLogs.class));
            }
        });

        //symptoms tracker button
        ImageButton buttonSymp = (ImageButton) findViewById(R.id.symptomLogsButton);
        buttonSymp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Homescreen.this, SymptomLogs.class));
            }
        });
        Button test = findViewById(R.id.pintest);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Homescreen.this, PinTest.class));
        }
        });

        //settings button
        ImageButton buttonSettings = (ImageButton) findViewById(R.id.userProfileButton);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Homescreen.this, Settings.class));
            }
        });

    }
}