package com.example.healthassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.TextView;

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
    private ActivityResultLauncher<Intent> activityLauncher;
    private TextView greeting;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_customization);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //initialize all the buttons

        btnMH = findViewById(R.id.medicalHistoryButton);
        btnHG = findViewById(R.id.healthGoalsButton);
        btnDR = findViewById(R.id.dietRestrictionButton);
        btnFP = findViewById(R.id.foodPreferenceButton);
        btnContinue = findViewById(R.id.btnContinue);
        greeting = findViewById(R.id.medicalHistoryText);

        // disable all buttons except for MedicalHistory
        setButtonsEnabled(false);

        // Initially disable all buttons except Medical History
        setButtonsEnabled(false);

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

        // Continue Button Logic
        btnContinue.setOnClickListener(v -> {
            if (isMedicalHistoryCompleted) {
                startActivity(new Intent(ProfileCustomization.this, Homescreen.class));
            } else {
                Toast.makeText(this, "Please complete your Medical History first!", Toast.LENGTH_SHORT).show();
            }
        });


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
            activityLauncher.launch(intent);
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
                            greeting.setText(String.format("Hello %s!", firstName));
                        }
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

        //When selected from settings
        SharedPreferences sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean fromSettings = sharedPref.getBoolean("FROM_SETTINGS", false);

        // Initialize the views
        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        //TextView headerTextView = findViewById(R.id.headerText);

        // Check if the views are not null
        if (welcomeTextView != null) {
                //&& headerTextView != null) {
            if (fromSettings) {
                welcomeTextView.setText("Select anything you would like to edit.");
                //headerTextView.setText("Profile Customization");
            }
        } else {
            Log.e("ProfileCustomization", "Views not found: welcomeTextView or headerTextView is null");
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
}