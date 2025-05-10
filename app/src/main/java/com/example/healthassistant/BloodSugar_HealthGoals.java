package com.example.healthassistant;

import android.content.SharedPreferences;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class BloodSugar_HealthGoals extends AppCompatActivity {

    private EditText inputBloodSugar, inputGoalMin, inputGoalMax;
    private Spinner contextSpinner;
    private Button saveButton, setGoalButton;
    private LineChart dailyLineChart;
    private BarChart weeklyBarChart;
    private TextView goalProgressText, goalDisplay;

    private TextView goalFeedbackText, goalTitle;
    private LinearLayout goalBackground;
    private LinearLayout goalInputSection;

    private final ArrayList<Entry> dailyEntries = new ArrayList<>();
    private final Map<Integer, Integer> inRangeCountMap = new HashMap<>();
    private final Map<Integer, Integer> totalCountMap = new HashMap<>();

    private int goalMin = 80;
    private int goalMax = 130;
    private long goalSetTime = 0;

    private static final String TAG = "BloodSugarFirebase";
    private final String[] contextLabels = {
            "Fasting", "Before Breakfast", "After Breakfast",
            "Before Lunch", "After Lunch", "Before Dinner",
            "After Dinner", "Bedtime"
    };

    private SharedPreferences preferences;
    private static final String PREFS_NAME = "BloodSugarPrefs";
    private static final String PREF_GOAL_MIN = "goalMin";
    private static final String PREF_GOAL_MAX = "goalMax";
    private static final String PREF_GOAL_TIME = "goalSetTime";

    // Firebase DB reference
    private DatabaseReference dbRef;
    private Button editGoalButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_sugar_health_goals);

        inputBloodSugar = findViewById(R.id.inputBloodSugar);
        inputGoalMin = findViewById(R.id.inputGoalMin);
        inputGoalMax = findViewById(R.id.inputGoalMax);
        contextSpinner = findViewById(R.id.contextSpinner);
        saveButton = findViewById(R.id.saveButton);
        setGoalButton = findViewById(R.id.setGoalButton);
        dailyLineChart = findViewById(R.id.dailyLineChart);
        weeklyBarChart = findViewById(R.id.weeklyBarChart);
        goalProgressText = findViewById(R.id.goalProgressText);
        goalDisplay = findViewById(R.id.goalDisplay);
        goalTitle = findViewById(R.id.goalTitleDisplay);
        goalBackground = findViewById(R.id.goalBackground);
        goalInputSection = findViewById(R.id.goalInputSection);
        editGoalButton = findViewById(R.id.editGoalButton);


        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadGoalFromPrefs();

        // Initialize Firebase reference
        dbRef = FirebaseDatabase.getInstance().getReference("blood_sugar_logs");

        setupContextSpinner();
        setupDailyLineChart();
        setupWeeklyBarChart();

        setGoalButton.setOnClickListener(v -> setWeeklyGoal());
        saveButton.setOnClickListener(v -> logBloodSugar());

        // Load data from Firebase when activity starts
        loadLogsFromFirebase();

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
    }

    // Reload data when returning to this screen
    @Override
    protected void onResume() {
        super.onResume();
        loadLogsFromFirebase();
    }

    // Method to load past entries
    private void loadLogsFromFirebase() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dailyEntries.clear();
                inRangeCountMap.clear();
                totalCountMap.clear();

                String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                Calendar weekStartCal = Calendar.getInstance();
                weekStartCal.set(Calendar.HOUR_OF_DAY, 0);
                weekStartCal.set(Calendar.MINUTE, 0);
                weekStartCal.set(Calendar.SECOND, 0);
                weekStartCal.set(Calendar.MILLISECOND, 0);
                int currentDayOfWeek = weekStartCal.get(Calendar.DAY_OF_WEEK);
                weekStartCal.add(Calendar.DAY_OF_MONTH, -(currentDayOfWeek - Calendar.SUNDAY));
                Date startOfWeek = weekStartCal.getTime();

                Calendar weekEndCal = (Calendar) weekStartCal.clone();
                weekEndCal.add(Calendar.DAY_OF_MONTH, 7);
                Date endOfWeek = weekEndCal.getTime();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Float value = snap.child("value").getValue(Float.class);
                    String context = snap.child("context").getValue(String.class);
                    String dateStr = snap.child("date").getValue(String.class);

                    if (value == null || context == null || dateStr == null) continue;

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date logDate = sdf.parse(dateStr);
                        if (logDate == null) continue;

                        // Line Chart (today only)
                        if (dateStr.equals(todayStr)) {
                            int contextIndex = Arrays.asList(contextLabels).indexOf(context);
                            if (contextIndex != -1) {
                                dailyEntries.add(new Entry(contextIndex, value));
                            }
                        }

                        // Bar Chart (this week only: Sunday–Saturday)
                        if (!logDate.before(startOfWeek) && logDate.before(endOfWeek)) {
                            Calendar logCal = Calendar.getInstance();
                            logCal.setTime(logDate);
                            int dayOfWeek = logCal.get(Calendar.DAY_OF_WEEK); // 1 = Sunday, 7 = Saturday
                            int chartIndex = (dayOfWeek + 5) % 7;

                            totalCountMap.put(chartIndex, totalCountMap.getOrDefault(chartIndex, 0) + 1);
                            if (value >= goalMin && value <= goalMax) {
                                inRangeCountMap.put(chartIndex, inRangeCountMap.getOrDefault(chartIndex, 0) + 1);
                            }

                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Date parse failed for: " + dateStr, e);
                    }
                }

                updateDailyChart();
                updateWeeklyChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase read failed", error.toException());
            }
        });
    }

    private void setupContextSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contextLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        contextSpinner.setAdapter(adapter);
    }

    private void setWeeklyGoal() {
        if (!canUpdateGoal()) {
            Toast.makeText(this, "Goal can only be updated once a week.", Toast.LENGTH_SHORT).show();
            return;
        }

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
        goalDisplay.setText(goalMin + "–" + goalMax + " mg/dL");
        goalDisplay.setVisibility(View.VISIBLE);
        goalTitle.setVisibility(View.VISIBLE);
        goalBackground.setVisibility(View.VISIBLE);
        goalInputSection.setVisibility(View.GONE);
        inputGoalMin.setEnabled(false);
        inputGoalMax.setEnabled(false);
        setGoalButton.setEnabled(false);

        // Show edit button
        editGoalButton.setVisibility(View.VISIBLE);
    }

    private void loadGoalFromPrefs() {
        goalMin = preferences.getInt(PREF_GOAL_MIN, 80);
        goalMax = preferences.getInt(PREF_GOAL_MAX, 130);
        goalSetTime = preferences.getLong(PREF_GOAL_TIME, 0);

        if (goalSetTime != 0) {
            // A goal is saved, show the display and edit button
            goalDisplay.setText(goalMin + "–" + goalMax + " mg/dL");
            goalDisplay.setVisibility(View.VISIBLE);
            goalTitle.setVisibility(View.VISIBLE);
            goalBackground.setVisibility(View.VISIBLE);
            goalInputSection.setVisibility(View.GONE);
            inputGoalMin.setEnabled(false);
            inputGoalMax.setEnabled(false);
            setGoalButton.setEnabled(false);
            editGoalButton.setVisibility(View.VISIBLE); // Show edit button
        } else {
            // No goal saved yet
            goalInputSection.setVisibility(View.VISIBLE);
            goalDisplay.setVisibility(View.GONE);
            goalTitle.setVisibility(View.GONE);
            goalBackground.setVisibility(View.GONE);
            inputGoalMin.setText("");
            inputGoalMax.setText("");
            editGoalButton.setVisibility(View.GONE); // Hide edit button
        }
    }


    private void saveGoalToPrefs() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_GOAL_MIN, goalMin);
        editor.putInt(PREF_GOAL_MAX, goalMax);
        editor.putLong(PREF_GOAL_TIME, goalSetTime);
        editor.apply();
    }

    private boolean canUpdateGoal() {
        return true;
    }

    // Saves to firebase
    private void logBloodSugar() {
        String valueStr = inputBloodSugar.getText().toString();
        if (valueStr.isEmpty()) {
            Toast.makeText(this, "Please enter a blood sugar value", Toast.LENGTH_SHORT).show();
            return;
        }

        float value = Float.parseFloat(valueStr);
        String context = contextSpinner.getSelectedItem().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        String date = sdf.format(new Date());


        String id = dbRef.push().getKey();
        if (id == null) return;

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("value", value);
        logEntry.put("context", context);
        logEntry.put("date", date);

        dbRef.child(id).setValue(logEntry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Saved to Firebase", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Entry saved");

                    int contextIndex = contextSpinner.getSelectedItemPosition();
                    dailyEntries.add(new Entry(contextIndex, value));
                    updateDailyChart();

                    Calendar cal = Calendar.getInstance();
                    int rawDay = cal.get(Calendar.DAY_OF_WEEK); // 1 = Sunday, 7 = Saturday
                    int chartIndex = (rawDay + 5) % 7;

                    int total = totalCountMap.getOrDefault(chartIndex, 0) + 1;
                    totalCountMap.put(chartIndex, total);

                    if (value >= goalMin && value <= goalMax) {
                        int inRange = inRangeCountMap.getOrDefault(chartIndex, 0) + 1;
                        inRangeCountMap.put(chartIndex, inRange);
                    }


                    updateWeeklyChart();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Firebase save failed", e);
                });
    }

    private void updateDailyChart() {
        // Generate new X-values in order and build matching labels
        List<Entry> sortedEntries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        for (int i = 0; i < dailyEntries.size(); i++) {
            Entry entry = dailyEntries.get(i);
            sortedEntries.add(new Entry(i, entry.getY())); // force chronological X
            xLabels.add(contextSpinner.getItemAtPosition((int) entry.getX()).toString()); // use original context label
        }

        LineDataSet dataSet = new LineDataSet(sortedEntries, "Blood Sugar (mg/dL)");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        dailyLineChart.setData(lineData);
        dailyLineChart.getDescription().setEnabled(false);

        XAxis xAxis = dailyLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f); // tilt labels for space
        xAxis.setLabelCount(xLabels.size(), true); // ensure even label spacing
        xAxis.setDrawLabels(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < xLabels.size()) ? xLabels.get(index) : "";
            }
        });

        dailyLineChart.invalidate();
    }

    private void updateWeeklyChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        float totalInRange = 0;
        float totalOverall = 0;

        int todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

        for (int i = 0; i < 7; i++) {
            int actualDay = (todayIndex + i) % 7;
            int inRange = inRangeCountMap.getOrDefault(actualDay, 0);
            int total = totalCountMap.getOrDefault(actualDay, 0);

            float percent = (total == 0) ? 0 : (inRange * 100f / total);
            entries.add(new BarEntry(i, percent));

            totalInRange += inRange;
            totalOverall += total;
        }

        BarDataSet dataSet = new BarDataSet(entries, "% In Range");
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        weeklyBarChart.setData(barData);

        weeklyBarChart.getAxisLeft().setAxisMinimum(0f);
        weeklyBarChart.getAxisLeft().setAxisMaximum(100f);
        weeklyBarChart.getAxisLeft().setGranularity(10f);
        weeklyBarChart.getAxisLeft().setLabelCount(6, true);
        weeklyBarChart.getAxisRight().setEnabled(false);

        XAxis xAxis = weeklyBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new LabelFormatter(getWeekdayLabelsSundayFirst()));

        weeklyBarChart.getDescription().setEnabled(false);
        weeklyBarChart.invalidate();

        float percent = (totalOverall == 0) ? 0 : (totalInRange * 100f / totalOverall);
        goalProgressText.setText("You’ve stayed in your goal range " + Math.round(percent) + "% of the time so far this week.");
    }

    private void setupDailyLineChart() {
        dailyEntries.clear();
        updateDailyChart();
    }

    private void setupWeeklyBarChart() {
        updateWeeklyChart();
    }

    private String[] getWeekdayLabelsSundayFirst() {
        return new String[] {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    }

    public static class LabelFormatter extends ValueFormatter {
        private final String[] labels;

        public LabelFormatter(String[] labels) {
            this.labels = labels;
        }

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            if (index >= 0 && index < labels.length) {
                return labels[index];
            } else {
                return "";
            }
        }
    }
}
