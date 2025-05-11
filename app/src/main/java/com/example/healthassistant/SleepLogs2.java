package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthassistant.databinding.ActivityAppointmentsBinding;
import com.example.healthassistant.databinding.ActivitySleepLogs2Binding;
import com.example.healthassistant.databinding.ActivitySymptomLogsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SleepLogs2 extends AppCompatActivity {

    private EditText inputDate, inputHoursSlept, inputRating;
    private TableLayout sleepLogTable;
    private DatabaseReference databaseRef;
    private String userId;
    private Button SleepEditButton;
    private boolean SleepisEditing;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sleep_logs2);

        // Initialize Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("sleepLogs");

        // Initialize UI Elements
        inputDate = findViewById(R.id.inputDate);
        inputHoursSlept = findViewById(R.id.inputHoursSlept);
        inputRating = findViewById(R.id.inputRating);
        sleepLogTable = findViewById(R.id.sleepLogTable);
        SleepEditButton = findViewById(R.id.btnEditTable);

        Button btnCreateLog = findViewById(R.id.btnCreateLog);
        btnCreateLog.setOnClickListener(view -> createLog());

        SleepEditButton.setOnClickListener(v -> {
            SleepisEditing = !SleepisEditing;
            SleepEditButton.setText(SleepisEditing ? "Save Table" : "Edit Table");
            loadSleepLogs(); // Load data when the activity starts
        });


        Button wipButton = findViewById(R.id.btnAnalyzeSleep);
        wipButton.setOnClickListener(v ->
                Toast.makeText(SleepLogs2.this, "Feature coming soon!", Toast.LENGTH_SHORT).show()
        );

        loadSleepLogs(); // Load data when the activity starts


        /*bottom bar navigation functionality*/

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);

        nav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(SleepLogs2.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(SleepLogs2.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(SleepLogs2.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(SleepLogs2.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(SleepLogs2.this, MedicationManager.class));
                    break;
            }
            return true;
        });

    }

    // Create Log Function
    private void createLog() {
        String date = inputDate.getText().toString().trim();
        String hoursSlept = inputHoursSlept.getText().toString().trim();
        String rating = inputRating.getText().toString().trim();

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
        inputDate.setText("");
        inputHoursSlept.setText("");
        inputRating.setText("");
    }

    // Load Logs from Firebase
    private void loadSleepLogs() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sleepLogTable.removeAllViews(); // Clear table before loading new data

                // Add table headers
                TableRow headerRow = new TableRow(SleepLogs2.this);
                headerRow.addView(createStyledHeader("Date"));
                headerRow.addView(createStyledHeader("Hours Slept"));
                headerRow.addView(createStyledHeader("Rating"));
                //if (SleepisEditing) {
                //headerRow.addView(createStyledHeader("Delete"));
                //}
                sleepLogTable.addView(headerRow);

                for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                    String logId = logSnapshot.getKey();
                    String date = logSnapshot.child("date").getValue(String.class);
                    String hoursSlept = logSnapshot.child("hoursSlept").getValue(String.class);
                    String rating = logSnapshot.child("rating").getValue(String.class);

                    TableRow row = new TableRow(SleepLogs2.this);
                    row.addView(createTextView(date));
                    row.addView(createTextView(hoursSlept));
                    row.addView(createTextView(rating));

                    // Add Delete Button
                    if (SleepisEditing) {
                        Button deleteButton = new Button(SleepLogs2.this);
                        deleteButton.setText("Delete");
                        deleteButton.setTextColor(getResources().getColor(android.R.color.white));
                        deleteButton.setTextSize(14);
                        deleteButton.setTypeface(ResourcesCompat.getFont(SleepLogs2.this, R.font.poppins_medium));
                        deleteButton.setPadding(16, 8, 16, 8);
                        deleteButton.setOnClickListener(view -> deleteLog(logId));
                        row.addView(deleteButton);
                    }

                    sleepLogTable.addView(row);
                }
            }




            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SleepLogs2.this, "Failed to load logs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // make sure the headers appears the exact same as the xml file
    private TextView createStyledHeader(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextSize(17);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setBackgroundResource(R.drawable.cell_shape_header_sleep); // Your drawable
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins_medium)); // Your font
        return textView;
    }

    // Helper Method: Create a TextView for Table Rows
    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins_medium)); // Apply font
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
