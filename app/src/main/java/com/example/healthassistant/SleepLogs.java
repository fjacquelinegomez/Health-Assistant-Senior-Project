package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthassistant.databinding.ActivitySleepLogsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SleepLogs extends AppCompatActivity {

    private ActivitySleepLogsBinding binding;
    private DatabaseReference databaseRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySleepLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("sleepLogs");

        // Set up button click listener
        binding.btnCreateLog.setOnClickListener(view -> createLog());

        // Bottom navigation functionality
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(SleepLogs.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(SleepLogs.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(SleepLogs.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(SleepLogs.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(SleepLogs.this, MedicationManager.class));
                    break;
            }
            return true;
        });

        loadSleepLogs(); // Load data when the activity starts

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });
    }

    // Create Log Function
    private void createLog() {
        String date = binding.inputDateSleep.getText().toString().trim();
        String hoursSlept = binding.inputHoursSlept.getText().toString().trim();
        String rating = binding.inputRating.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(hoursSlept) || TextUtils.isEmpty(rating)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a unique key for each log
        String logId = databaseRef.push().getKey();

        // Create log entry
        Map<String, String> logEntry = new HashMap<>();
        logEntry.put("date", date);
        logEntry.put("hoursSlept", hoursSlept);
        logEntry.put("rating", rating);

        // Save to Firebase
        databaseRef.child(logId).setValue(logEntry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Log created successfully", Toast.LENGTH_SHORT).show();
                    loadSleepLogs(); // Refresh the table
                    clearInputFields();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to create log", Toast.LENGTH_SHORT).show());
    }

    // Clear Input Fields
    private void clearInputFields() {
        binding.inputDateSleep.setText("");
        binding.inputHoursSlept.setText("");
        binding.inputRating.setText("");
    }

    // Load Logs from Firebase
    private void loadSleepLogs() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.sleepLogTable.removeAllViews(); // Clear table before loading new data

                // Add table headers
                TableRow headerRow = new TableRow(SleepLogs.this);
                headerRow.addView(createTextView("Date"));
                headerRow.addView(createTextView("Hours Slept"));
                headerRow.addView(createTextView("Rating"));
                binding.sleepLogTable.addView(headerRow);

                for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                    String logId = logSnapshot.getKey();
                    String date = logSnapshot.child("date").getValue(String.class);
                    String hoursSlept = logSnapshot.child("hoursSlept").getValue(String.class);
                    String rating = logSnapshot.child("rating").getValue(String.class);

                    TableRow row = new TableRow(SleepLogs.this);
                    row.addView(createTextView(date));
                    row.addView(createTextView(hoursSlept));
                    row.addView(createTextView(rating));

                    // Add Delete Button
                    Button deleteButton = new Button(SleepLogs.this);
                    deleteButton.setText("Delete");
                    deleteButton.setOnClickListener(view -> deleteLog(logId));
                    row.addView(deleteButton);

                    binding.sleepLogTable.addView(row);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SleepLogs.this, "Failed to load logs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper Method: Create a TextView for Table Rows
    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    // Delete Log Function
    private void deleteLog(String logId) {
        databaseRef.child(logId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Log deleted", Toast.LENGTH_SHORT).show();
                    loadSleepLogs(); // Refresh the table
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete log", Toast.LENGTH_SHORT).show());
    }
}
