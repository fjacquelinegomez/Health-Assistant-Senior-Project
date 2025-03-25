package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthassistant.databinding.ActivityAppointmentsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SymptomLogs extends AppCompatActivity {

    ActivityAppointmentsBinding binding;
    private DatabaseReference databaseRef;
    private String userId;
    private EditText inputDate, inputSymp, inputRating;
    private TableLayout sympLogTable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_symptom_logs);


        // Initialize Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("sleepLogs");

        // Initialize UI Elements
        inputDate = findViewById(R.id.inputSympDate);
        inputSymp = findViewById(R.id.inputSymp);
        inputRating = findViewById(R.id.inputRating);
        sympLogTable = findViewById(R.id.sympLogTable);

        Button btnCreateLog = findViewById(R.id.btnSympCreateLog);
        btnCreateLog.setOnClickListener(view -> createLog());


        loadSympLogs(); // Load data when the activity starts


        /**bottom bar navigation functionality**/
        binding = ActivityAppointmentsBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(SymptomLogs.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(SymptomLogs.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(SymptomLogs.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(SymptomLogs.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(SymptomLogs.this, MedicationManager.class));
                    break;
            }
            return true;
        });


        String[] symptoms = {"Headache", "Fever", "Cough", "Fatigue", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, symptoms);
        AutoCompleteTextView inputSymp = findViewById(R.id.inputSymp);
        inputSymp.setAdapter(adapter);



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }



    // Create Log Function
    private void createLog() {
        String date = inputDate.getText().toString().trim();
        String hoursSlept = inputSymp.getText().toString().trim();
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
                    loadSympLogs(); // Refresh the table
                    clearInputFields();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to create log", Toast.LENGTH_SHORT).show());
    }

    // Clear Input Fields
    private void clearInputFields() {
        inputDate.setText("");
        inputSymp.setText("");
        inputRating.setText("");
    }

    // Load Logs from Firebase
    private void loadSympLogs() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sympLogTable.removeAllViews(); // Clear table before loading new data

                // Add table headers
                TableRow headerRow = new TableRow(SymptomLogs.this);
                headerRow.addView(createTextView("Date"));
                headerRow.addView(createTextView("Hours Slept"));
                headerRow.addView(createTextView("Rating"));
                sympLogTable.addView(headerRow);

                for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                    String logId = logSnapshot.getKey();
                    String date = logSnapshot.child("date").getValue(String.class);
                    String hoursSlept = logSnapshot.child("hoursSlept").getValue(String.class);
                    String rating = logSnapshot.child("rating").getValue(String.class);

                    TableRow row = new TableRow(SymptomLogs.this);
                    row.addView(createTextView(date));
                    row.addView(createTextView(hoursSlept));
                    row.addView(createTextView(rating));

                    // Add Delete Button
                    Button deleteButton = new Button(SymptomLogs.this);
                    deleteButton.setText("Delete");
                    deleteButton.setOnClickListener(view -> deleteLog(logId));
                    row.addView(deleteButton);

                    sympLogTable.addView(row);
                }
            }




            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SymptomLogs.this, "Failed to load logs", Toast.LENGTH_SHORT).show();
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
                    loadSympLogs(); // Refresh the table
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete log", Toast.LENGTH_SHORT).show());
    }
}