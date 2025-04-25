package com.example.healthassistant;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healthassistant.databinding.ActivityHomescreenBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Homescreen extends AppCompatActivity {
    //used for bottom bar
    ActivityHomescreenBinding binding;

    // Initializing variables for notifications
    private FirebaseFirestore database;
    private NotificationHelper notificationHelper;
    private static int notificationCounter = 0;

    private TextView greeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //bottom bar
        binding = ActivityHomescreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialize the greeting text at the top so it displays the user's first name
        greeting = findViewById(R.id.nameText);

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
                    startActivity(new Intent(Homescreen.this, HealthGoals.class));
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
        ImageButton buttonSleep = findViewById(R.id.sleepLogsButton);
        buttonSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ButtonClick", "Sleep Logs button clicked");
                startActivity(new Intent(Homescreen.this, SleepLogs2.class));
            }


        });

        //symptoms tracker button
        ImageButton buttonSymp = findViewById(R.id.sympLogsButton);
        buttonSymp.setOnClickListener(v -> {
            Log.d("ButtonClick", "Symptoms Logs button clicked");
            startActivity(new Intent(Homescreen.this, SymptomLogs.class));
        });



        Button test = findViewById(R.id.pintest);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Homescreen.this, PinTest.class));
        }

        });

        // Firebase logic to retrieve and display the user's first name
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String fullName = snapshot.child("fullName").getValue(String.class);
                        if (fullName != null && !fullName.isEmpty()) {
                            String firstName = fullName.split(" ")[0];  // Extract first name
                            greeting.setText(firstName);
                        }
                    } else {
                        greeting.setText("friend!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Homescreen.this, "Failed to load name", Toast.LENGTH_SHORT).show();
                }
            });
        }

        //settings button
        ImageButton buttonSettings = findViewById(R.id.userProfileButton);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Homescreen.this, Settings.class));
            }
        });

        // Initializing Firestore and NotificationHelper
        database = FirebaseFirestore.getInstance();
        notificationHelper = new NotificationHelper(this);

        // Sends notifications to users if their medication is expiring or needs to be refilled (will only send if user clicks on homescreen tho)
        checkMedicationsExpiration();
        checkMedicationsRefill();
    }

    // Goes through the user's current medications and check if they're close to expiring (7 days)
    // Sends a notification if it is close to expiring
    private void checkMedicationsExpiration() {
        // Gets the current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();

        // Creates reference to the userMedications collection
        CollectionReference userMedicationRef = database.collection("userMedications");

        // Pulls the user's current medications from Firestore
        //database.collection("userMedications")
        userMedicationRef.whereEqualTo("userID", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Loops through all of the user's current medications
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String expirationDate = document.getString("expirationDate");
                            DocumentReference medRef = document.getDocumentReference("medicationRef");

                            // Create a Medication object
                            Medication medication = new Medication();
                            medication.setExpirationDate(expirationDate);

                            // Checks if current medication is expiring soon
                            if (medication.isExpiringSoon(expirationDate)) {
                                // Grabs the name of the medication
                                medRef.get().addOnSuccessListener(medSnapshot -> {
                                    if (medSnapshot.exists()) {
                                        String medicationName = medSnapshot.getString("Name");
                                        String message = "Your medication " + medicationName + " is expiring soon!";

                                        // Checks if user turned on notification permissions
                                        if (notificationHelper.checkNotificationPermission(Homescreen.this, 101)) {
                                            // Sends the expiration notification
                                            int notificationId = notificationCounter++; // Gives each expired medication a unique notif Id so notifs don't replace old ones
                                            notificationHelper.showNotification("Your Medication Expiring Soon", message, notificationId);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    // Goes through the user's current medications and check if they're close to needing a refill (10 pills left)
    private void checkMedicationsRefill() {
        // Gets the current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();

        // Creates reference to the userMedications collection
        CollectionReference userMedicationRef = database.collection("userMedications");

        // Pulls the user's current medications from Firestore
        //database.collection("userMedications")
        userMedicationRef.whereEqualTo("userID", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Loops through all of the user's current medications
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Grabs values from the database
                            int totalPills = document.getLong("totalPills").intValue();
                            int pillsTaken = document.getLong("pillsTaken").intValue();
                            DocumentReference medRef = document.getDocumentReference("medicationRef");

                            // Create a Medication object
                            Medication medication = new Medication();
                            medication.setTotalPills(totalPills);
                            medication.setPillsTaken(pillsTaken);

                            // Checks if current medication needs to be refilled soon
                            if (medication.isRefillNeeded(totalPills, pillsTaken)) {
                                // Grabs the name of the medication
                                medRef.get().addOnSuccessListener(medSnapshot -> {
                                    if (medSnapshot.exists()) {
                                        String medicationName = medSnapshot.getString("Name");
                                        String message = "Your medication " + medicationName + " needs to be refilled soon!";

                                        // Checks if user turned on notification permissions
                                        if (notificationHelper.checkNotificationPermission(Homescreen.this, 101)) {
                                            // Sends the refill notification
                                            int notificationId = notificationCounter++; // Gives each expired medication a unique notif Id so notifs don't replace old ones
                                            notificationHelper.showNotification("Your Medication Running Low", message, notificationId);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }
}