package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class Stress_HealthGoals_PC extends AppCompatActivity {

    private ToggleButton btnYes, btnNo;
    private EditText editRelaxingActivity;
    private TextView tipText;
    private Button saveButton;
    private GridLayout weekTracker;

    private ToggleButton moodHappy, moodNeutral, moodStressed;

    private DatabaseReference stressLogsRef;

    private final String[] tips = {
            "Try a 5-minute meditation 🌿",
            "Take a short walk 🚶‍♀️",
            "Listen to calming music 🎧",
            "Take deep breaths 🌬️",
            "Stretch or do some yoga 🧘‍♂️",
            "Write down your thoughts ✍️",
            "Drink herbal tea 🍵"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stress_health_goals_pc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*bottom bar navigation functionality*/

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);

        nav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(Stress_HealthGoals_PC.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(Stress_HealthGoals_PC.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(Stress_HealthGoals_PC.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(Stress_HealthGoals_PC.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(Stress_HealthGoals_PC.this, MedicationManager.class));
                    break;
            }
            return true;
        });


        // connect to Firebase
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        stressLogsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("stressLogs");

        // initialize all the pages
        // Bind views
        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);
        editRelaxingActivity = findViewById(R.id.editRelaxingActivity);
        tipText = findViewById(R.id.tipText);
        saveButton = findViewById(R.id.saveStressLogButton);
        weekTracker = findViewById(R.id.weekTracker);


        moodHappy = findViewById(R.id.moodHappy);
        moodNeutral = findViewById(R.id.moodNeutral);
        moodStressed = findViewById(R.id.moodStressed);

        // Toggle logic
        btnYes.setOnClickListener(v -> {
            btnNo.setChecked(false);
            editRelaxingActivity.setVisibility(View.VISIBLE);
            tipText.setVisibility(View.GONE);
        });

        btnNo.setOnClickListener(v -> {
            btnYes.setChecked(false);
            editRelaxingActivity.setVisibility(View.GONE);
            tipText.setVisibility(View.VISIBLE);
            tipText.setText(getRandomTip());
        });


        View.OnClickListener moodClickListener = v -> {
            // Reset all to unselected
            moodHappy.setChecked(false);
            moodNeutral.setChecked(false);
            moodStressed.setChecked(false);

            // Highlight the selected one
            ((ToggleButton) v ).setChecked(true);
        };

        moodHappy.setOnClickListener(moodClickListener);
        moodNeutral.setOnClickListener(moodClickListener);
        moodStressed.setOnClickListener(moodClickListener);

        // Save logic
        saveButton.setOnClickListener(v -> saveEntry());
    }

    private void saveEntry() {
        String today = getTodayDate();
        String weekday = getWeekday();
        boolean didRelax = btnYes.isChecked();
        String activity = editRelaxingActivity.getText().toString().trim();
        String mood = getSelectedMood();

        if (!didRelax && !btnNo.isChecked()) {
            Toast.makeText(this, "Please select Yes or No", Toast.LENGTH_SHORT).show();
            return;
        }

        if (didRelax && activity.isEmpty()) {
            Toast.makeText(this, "Please describe your relaxing activity!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mood == null) {
            Toast.makeText(this, "Please select your mood", Toast.LENGTH_SHORT).show();
            return;
        }


        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("date", today);
        logEntry.put("weekday", weekday);
        logEntry.put("didRelax", didRelax);
        logEntry.put("mood", mood);
        if (didRelax) {
            logEntry.put("activity", activity);
        } else {
            logEntry.put("tipShown", tipText.getText().toString());
        }

        stressLogsRef.child(today).setValue(logEntry).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Log saved! 🌼", Toast.LENGTH_SHORT).show();
            highlightWeekday(weekday);
            clearInputs();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to save log.", Toast.LENGTH_SHORT).show();
        });
    }

    private String getSelectedMood() {
        if (moodHappy.isChecked()) return "Happy";
        if (moodNeutral.isChecked()) return "Neutral";
        if (moodStressed.isChecked()) return "Sad";
        return null;
    }

    private void clearInputs() {
        btnYes.setChecked(false);
        btnNo.setChecked(false);
        editRelaxingActivity.setVisibility(View.GONE);
        editRelaxingActivity.setText("");
        tipText.setVisibility(View.GONE);
        moodHappy.setChecked(false);
        moodNeutral.setChecked(false);
        moodStressed.setChecked(false);
    }

    private void highlightWeekday(String weekday) {
        int dayId = getResources().getIdentifier("check" + weekday, "id", getPackageName());
        TextView checkView = findViewById(dayId);
        if (checkView != null) {
            checkView.setText("✅");
        }
    }

    private String getRandomTip() {
        int index = (int) (Math.random() * tips.length);
        return tips[index];
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    private String getWeekday() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime()); // Example: "Mon"
    }


}