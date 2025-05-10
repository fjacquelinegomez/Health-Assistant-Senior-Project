package com.example.healthassistant;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Weight_HealthGoal extends AppCompatActivity {

    private EditText inputGoalMin, inputGoalMax, inputWeight;
    private Button setGoalButton, editGoalButton, saveWeightButton;
    private TextView goalDisplay, goalFeedbackText, goalTitle;
    private LinearLayout goalBackground;
    private LinearLayout goalInputSection;
    private LineChart weightProgressChart;

    private int goalMin = 0;
    private int goalMax = 0;
    private long goalSetTime = 0;

    private SharedPreferences preferences;
    private static final String PREFS_NAME = "WeightPrefs";
    private static final String PREF_GOAL_MIN = "weightGoalMin";
    private static final String PREF_GOAL_MAX = "weightGoalMax";
    private static final String PREF_GOAL_TIME = "weightGoalTime";

    private DatabaseReference dbRef;
    private final List<Entry> weightEntries = new ArrayList<>();
    private final List<String> dateLabels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_health_goal);

        inputGoalMin = findViewById(R.id.inputGoalMin);
        inputGoalMax = findViewById(R.id.inputGoalMax);
        inputWeight = findViewById(R.id.inputWeight);
        setGoalButton = findViewById(R.id.setGoalButton);
        editGoalButton = findViewById(R.id.editGoalButton);
        saveWeightButton = findViewById(R.id.saveWeightButton);
        goalDisplay = findViewById(R.id.goalDisplay);
        goalTitle = findViewById(R.id.goalTitleDisplay);
        goalBackground = findViewById(R.id.goalBackground);
        goalInputSection = findViewById(R.id.goalInputSection);
        goalFeedbackText = findViewById(R.id.goalFeedbackText);
        weightProgressChart = findViewById(R.id.weightProgressChart);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadGoalFromPrefs();

        dbRef = FirebaseDatabase.getInstance().getReference("weight_logs");

        setGoalButton.setOnClickListener(v -> setWeightGoal());
        saveWeightButton.setOnClickListener(v -> saveWeight());

        editGoalButton.setOnClickListener(v -> {
            inputGoalMin.setEnabled(true);
            inputGoalMax.setEnabled(true);
            setGoalButton.setEnabled(true);

            inputGoalMin.setText(String.valueOf(goalMin));
            inputGoalMax.setText(String.valueOf(goalMax));

            goalInputSection.setVisibility(View.VISIBLE);
            goalDisplay.setVisibility(View.GONE);
            goalTitle.setVisibility(View.GONE);
            goalBackground.setVisibility(View.GONE);
            editGoalButton.setVisibility(View.GONE);
        });

        loadWeightsFromFirebase();
    }

    // Validates and saves the user’s weight goal range and updates the goal display.
    private void setWeightGoal() {
        String minStr = inputGoalMin.getText().toString();
        String maxStr = inputGoalMax.getText().toString();

        if (minStr.isEmpty() || maxStr.isEmpty()) {
            Toast.makeText(this, "Enter both min and max goal values", Toast.LENGTH_SHORT).show();
            return;
        }

        goalMin = Integer.parseInt(minStr);
        goalMax = Integer.parseInt(maxStr);
        goalSetTime = System.currentTimeMillis();

        saveGoalToPrefs();

        goalDisplay.setText(goalMin + "–" + goalMax + " lb");
        goalDisplay.setVisibility(View.VISIBLE);
        goalTitle.setVisibility(View.VISIBLE);
        goalBackground.setVisibility(View.VISIBLE);
        goalInputSection.setVisibility(View.GONE);
        inputGoalMin.setEnabled(false);
        inputGoalMax.setEnabled(false);
        setGoalButton.setEnabled(false);
        editGoalButton.setVisibility(View.VISIBLE);
    }

    // Loads the saved goal range and timestamp from SharedPreferences and updates UI visibility accordingly.
    private void loadGoalFromPrefs() {
        goalMin = preferences.getInt(PREF_GOAL_MIN, 0);
        goalMax = preferences.getInt(PREF_GOAL_MAX, 0);
        goalSetTime = preferences.getLong(PREF_GOAL_TIME, 0);

        if (goalSetTime != 0) {
            goalDisplay.setText(goalMin + "–" + goalMax + " lb");
            goalDisplay.setVisibility(View.VISIBLE);
            goalTitle.setVisibility(View.VISIBLE);
            goalBackground.setVisibility(View.VISIBLE);
            goalInputSection.setVisibility(View.GONE);
            inputGoalMin.setEnabled(false);
            inputGoalMax.setEnabled(false);
            setGoalButton.setEnabled(false);
            editGoalButton.setVisibility(View.VISIBLE);
        } else {
            goalInputSection.setVisibility(View.VISIBLE);
            goalDisplay.setVisibility(View.GONE);
            goalTitle.setVisibility(View.GONE);
            goalBackground.setVisibility(View.GONE);
            inputGoalMin.setText("");
            inputGoalMax.setText("");
            editGoalButton.setVisibility(View.GONE);
        }
    }

    // Saves the current goal values and timestamp to SharedPreferences.
    private void saveGoalToPrefs() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_GOAL_MIN, goalMin);
        editor.putInt(PREF_GOAL_MAX, goalMax);
        editor.putLong(PREF_GOAL_TIME, goalSetTime);
        editor.apply();
    }

    // Saves a new weight entry with today’s date to Firebase, updates feedback immediately, and refreshes the chart and logs.
    private void saveWeight() {
        String weightStr = inputWeight.getText().toString();
        if (weightStr.isEmpty()) {
            Toast.makeText(this, "Please enter your weight", Toast.LENGTH_SHORT).show();
            return;
        }

        float currentWeight = Float.parseFloat(weightStr);

        // Generate today's date string
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        String date = sdf.format(new Date());

        String id = dbRef.push().getKey();
        if (id == null) return;

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("weight", currentWeight);
        logEntry.put("date", date);

        dbRef.child(id).setValue(logEntry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Weight saved", Toast.LENGTH_SHORT).show();

                    // Immediately reflect this weight in feedback
                    updateGoalFeedback(currentWeight);

                    // Then reload full chart and data
                    loadWeightsFromFirebase();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save weight", Toast.LENGTH_SHORT).show();
                });
    }

    // Updates the feedback message based on how the given weight compares to the user’s goal range.
    private void updateGoalFeedback(float currentWeight) {
        if (goalMin == 0 || goalMax == 0) return;

        if (currentWeight >= goalMin && currentWeight <= goalMax) {
            goalFeedbackText.setText("You're within your goal range 🎯");
        } else if (currentWeight > goalMax) {
            goalFeedbackText.setText(String.format("You're %.1f lb above your goal ⬆️", currentWeight - goalMax));
        } else {
            goalFeedbackText.setText(String.format("You're %.1f lb below your goal ⬇️", goalMin - currentWeight));
        }
    }

    // Loads all weight logs from Firebase, updates the chart, and sets the goal feedback to today's log or most recent entry.
    private void loadWeightsFromFirebase() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                weightEntries.clear();
                dateLabels.clear();

                int index = 0;
                float latestWeight = -1f;
                Date latestDate = null;

                float todayWeight = -1f;
                String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Float weight = snap.child("weight").getValue(Float.class);
                    String dateStr = snap.child("date").getValue(String.class);
                    if (weight == null || dateStr == null) continue;

                    try {
                        Date logDate = sdf.parse(dateStr);
                        if (logDate != null && (latestDate == null || logDate.after(latestDate))) {
                            latestDate = logDate;
                            latestWeight = weight;
                        }

                        // Track today’s log (if exists)
                        if (dateStr.equals(todayStr)) {
                            todayWeight = weight;
                        }

                        weightEntries.add(new Entry(index, weight));

                        // Format date for display
                        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
                        dateLabels.add(displayFormat.format(logDate != null ? logDate : new Date()));

                        index++;
                    } catch (Exception e) {
                        dateLabels.add(dateStr);
                    }
                }

                // Priority: use today’s weight if exists, otherwise latest past weight
                if (todayWeight > 0f) {
                    updateGoalFeedback(todayWeight);
                } else if (latestWeight > 0f) {
                    updateGoalFeedback(latestWeight);
                }

                updateProgressChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Weight_HealthGoal.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Updates the line chart with all logged weights and formats the X-axis with readable, spaced date labels.
    private void updateProgressChart() {
        LineDataSet dataSet = new LineDataSet(weightEntries, "Weight Progress (lb)");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        weightProgressChart.setData(lineData);
        weightProgressChart.getDescription().setEnabled(false);

        XAxis xAxis = weightProgressChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setAvoidFirstLastClipping(true); // avoid cutoff on edges
        xAxis.setLabelRotationAngle(-30f);     // gentler tilt
        xAxis.setLabelCount(dateLabels.size(), false); // allow automatic label spacing
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < dateLabels.size()) ? dateLabels.get(index) : "";
            }
        });

        weightProgressChart.setExtraBottomOffset(16f); // Prevent overlap
        weightProgressChart.invalidate();
    }
}
