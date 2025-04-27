package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healthassistant.databinding.ActivityProfileCustomizationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileCustomization extends AppCompatActivity {

    private ImageButton btnMH, btnHG, btnDR, btnFP;
    private Button btnContinue;
    private boolean isMedicalHistoryCompleted = false;
    //new
    private boolean isHealthGoalsCompleted;
    private boolean isDietaryRestrictionsCompleted;
    private boolean isFoodPreferencesCompleted;
    private ActivityResultLauncher<Intent> activityLauncher;
    private TextView greeting;
    private TextView description;
    private boolean fromSettings;
    ActivityProfileCustomizationBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_customization);

        //initialize all the buttons

        btnMH = findViewById(R.id.medicalHistoryButton);
        btnHG = findViewById(R.id.healthGoalsButton);
        btnDR = findViewById(R.id.dietRestrictionButton);
        btnFP = findViewById(R.id.foodPreferenceButton);
        btnContinue = findViewById(R.id.btnContinue);
        greeting = findViewById(R.id.headerText);
        description = findViewById(R.id.welcomeTextView);


        fromSettings = getIntent().getBooleanExtra("FROM_SETTINGS", false); // Default is false
        Log.d("ProfileCustomization", "fromSettings value: " + fromSettings); // Debugging
        // Set the button text based on 'fromSettings' value
        if (fromSettings) {
            btnContinue.setText("Done"); // Change button text to "Done"
            btnContinue.setOnClickListener(v -> {
                // Navigate to Homescreen when "Done" is clicked, bypassing medical history check
                startActivity(new Intent(ProfileCustomization.this, Homescreen.class));
                finish(); // Optional: Close the ProfileCustomization activity after navigation
            });
        } else {
            btnContinue.setText("Continue"); // Keep button text as "Continue"
            btnContinue.setOnClickListener(v -> {
                if (isMedicalHistoryCompleted) {
                    // Proceed to Homescreen if Medical History is completed
                    startActivity(new Intent(ProfileCustomization.this, Homescreen.class));
                } else {
                    // Show message if Medical History is not completed
                    Toast.makeText(ProfileCustomization.this, "Please complete your Medical History first!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Register result callback
        activityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        isMedicalHistoryCompleted = true;  // Mark as completed
                        setButtonsEnabled(true);  // Unlock everything
                    }
                }
        );

        // Medical History Button Logic
        btnMH.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileCustomization.this, MedicalHistory_PC.class);
            activityLauncher.launch(intent);
        });

//        // Continue Button Logic
//        btnContinue.setOnClickListener(v -> {
//            if (isMedicalHistoryCompleted) {
//                startActivity(new Intent(ProfileCustomization.this, Homescreen.class));
//            } else {
//                Toast.makeText(this, "Please complete your Medical History first!", Toast.LENGTH_SHORT).show();
//            }
//        });


        //logic when a user presses health goals
        btnHG.setOnClickListener(v ->{
            Intent intent = new Intent(ProfileCustomization.this, HealthGoals_PC.class);
            activityLauncher.launch(intent);
        });

        //logic when a user presses food preferences
        btnFP.setOnClickListener(v ->{
            Intent intent = new Intent(ProfileCustomization.this, FoodPreferences_PC.class);
            activityLauncher.launch(intent);
        });

        //logic when a user presses dietary restrictions
        btnDR.setOnClickListener(v ->{
            Intent intent = new Intent(ProfileCustomization.this, DietaryRestrictions_PC.class);
            //intent.putExtra("DIETARY_RESTRICTIONS_COMPLETED", isDietaryRestrictionsCompleted);
            activityLauncher.launch(intent);
        });


        // Firebase logic to retrieve and display the user's first name
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            //databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            databaseRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String fullName = snapshot.child("fullName").getValue(String.class);
                        if (fullName != null && !fullName.isEmpty()) {
                            String decryptedFullName = EncryptionUtils.decrypt(fullName); // NEW, encrypts the name from firebase
                            String firstName = decryptedFullName.split(" ")[0];  // Extract first name

                            if (!fromSettings) {
                                greeting.setText(String.format("Hello %s!", firstName));
                            } else {
                                greeting.setText(getString(R.string.header_pc_settings)); // "Profile Customization"
                                description.setText(getString(R.string.description_pc_settings)); // "Profile Customization"
                            }
                        }
                        else {
                            // If no name exists in Firebase, set the default greeting
                            if (!fromSettings) {
                                greeting.setText("Hello!");
                            } else {
                                greeting.setText(getString(R.string.header_pc_settings));
                            }
                        }

                        // Fetch completion status
                        isMedicalHistoryCompleted = snapshot.child("medicalHistoryCompleted").getValue(Boolean.class) != null &&
                                snapshot.child("medicalHistoryCompleted").getValue(Boolean.class);
                        isHealthGoalsCompleted = snapshot.child("healthGoalsCompleted").getValue(Boolean.class) != null &&
                                snapshot.child("healthGoalsCompleted").getValue(Boolean.class);
                        isDietaryRestrictionsCompleted = snapshot.child("dietaryRestrictionsCompleted").getValue(Boolean.class) != null &&
                                snapshot.child("dietaryRestrictionsCompleted").getValue(Boolean.class);
                        isFoodPreferencesCompleted = snapshot.child("foodPreferencesCompleted").getValue(Boolean.class) != null &&
                                snapshot.child("foodPreferencesCompleted").getValue(Boolean.class);

                        // Update button states based on completion status
                        updateButtonStates();
                    } else {
                        greeting.setText("Hello!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileCustomization.this, "Failed to load name", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Helper method to enable or disable buttons
    private void setButtonsEnabled(boolean isEnabled) {
        btnHG.setEnabled(isEnabled);
        btnDR.setEnabled(isEnabled);
        btnFP.setEnabled(isEnabled);
        btnContinue.setEnabled(isEnabled);

        // Optional: Make the disabled buttons look visually "dimmed"
        float alpha = isEnabled ? 1.0f : 0.5f;
        btnHG.setAlpha(alpha);
        btnDR.setAlpha(alpha);
        btnFP.setAlpha(alpha);
        btnContinue.setAlpha(alpha);
    }
    private void updateButtonStates() {
        if (fromSettings) {
            // Unlock all buttons when accessed from settings
            btnMH.setEnabled(true);
            btnHG.setEnabled(true);
            btnDR.setEnabled(true);
            btnFP.setEnabled(true);
            btnContinue.setEnabled(true);

            btnMH.setAlpha(1.0f);
            btnHG.setAlpha(1.0f);
            btnDR.setAlpha(1.0f);
            btnFP.setAlpha(1.0f);
            btnContinue.setAlpha(1.0f);
        } else {
            // Normal behavior during registration
            btnMH.setEnabled(true); // Medical history is always enabled
            btnHG.setEnabled(isMedicalHistoryCompleted);
            btnDR.setEnabled(isMedicalHistoryCompleted);
            btnFP.setEnabled(isMedicalHistoryCompleted);
            btnContinue.setEnabled(isMedicalHistoryCompleted);

            btnMH.setAlpha(1.0f);
            btnHG.setAlpha(isMedicalHistoryCompleted ? 1.0f : 0.5f);
            btnDR.setAlpha(isMedicalHistoryCompleted ? 1.0f : 0.5f);
            btnFP.setAlpha(isMedicalHistoryCompleted ? 1.0f : 0.5f);
            btnContinue.setAlpha(isMedicalHistoryCompleted ? 1.0f : 0.5f);
        }
    }
}