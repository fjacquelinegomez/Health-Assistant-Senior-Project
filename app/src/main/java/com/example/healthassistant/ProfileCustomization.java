package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileCustomization extends AppCompatActivity {

    private ImageButton btnMH, btnHG, btnDR, btnFP;
    private int current_step = 0;

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

        btnMH = findViewById(R.id.medicalHistoryButton);
        btnHG = findViewById(R.id.healthGoalsButton);
        btnDR = findViewById(R.id.dietRestrictionButton);
        btnFP = findViewById(R.id.foodPreferenceButton);

        //disable all buttons until medical history is completed
        lockButton(btnHG);
        lockButton(btnDR);
        lockButton(btnFP);

        // sequence of when to go to next page
        btnMH.setOnClickListener(view -> openForm(0, MedicalHistory_PC.class));
        btnHG.setOnClickListener(view -> openForm(1, HealthGoals_PC.class));
        btnDR.setOnClickListener(view -> openForm(2, DietaryRestrictions_PC.class));
        btnFP.setOnClickListener(view -> openForm(3, FoodPreferences_PC.class));

    }

    private void openForm(int step, Class<?> activityClass) {
        if (current_step == step) {  // Ensure they can only open the next unlocked form
            Intent intent = new Intent(ProfileCustomization.this, activityClass);
            startActivityForResult(intent, step);
        } else {
            Toast.makeText(this, "Please complete the previous step first!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == current_step) {
            current_step++;  // Move to the next step
            unlockNextStep();
        }
    }

    private void unlockNextStep() {
        switch (current_step) {
            case 1:
                unlockButton(btnHG);
                break;
            case 2:
                unlockButton(btnDR);
                break;
            case 3:
                unlockButton(btnFP);
                break;
            case 4:
                // Once all steps are complete, navigate to the home screen
                startActivity(new Intent(ProfileCustomization.this, Homescreen.class));
                finish(); // Close this setup activity
                break;
        }
    }

    private void lockButton(ImageButton button) {
        button.setEnabled(false);
        button.setAlpha(0.5f);
    }

    private void unlockButton(ImageButton button) {
        button.setEnabled(true);
        button.setAlpha(1.0f);
    }


}