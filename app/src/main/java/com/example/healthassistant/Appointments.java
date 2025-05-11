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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.healthassistant.databinding.ActivityAppointmentsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Appointments extends AppCompatActivity {
    ActivityAppointmentsBinding binding;
    private DatabaseReference databaseRef;
    private String userId;

    private EditText dateInput, typeInput, providerInput;
    private TableLayout apptLogTable;

    private Button apptEdit;
    private boolean isEditing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        // Initialize Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("appointments");


        // Find views from layout
        dateInput = findViewById(R.id.inputApptDate); // if you set an id for EditText
        typeInput = findViewById(R.id.inputApptType); // make sure your EditText has id "type"
        providerInput = findViewById(R.id.inputApptDr); // and id "name"
        apptLogTable = findViewById(R.id.appointmentsTableLayout);
        apptEdit = findViewById(R.id.editAppointmentsTableButton);


        Button btnCreateLog = findViewById(R.id.addAppointmentButton);
        btnCreateLog.setOnClickListener(view -> createLog());

        apptEdit.setOnClickListener(v -> {
            isEditing = !isEditing;
            apptEdit.setText(isEditing ? "Save Table" : "Edit Table");
            loadApptLogs(); // Refresh table with or without delete buttons
        });


        loadApptLogs(); // Load data when the activity starts


        // Bottom Navigation
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(Appointments.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(Appointments.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(Appointments.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(Appointments.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(Appointments.this, MedicationManager.class));
                    break;
            }
            return true;
        });
    }

    // Create Log Function
    private void createLog() {
        String date = dateInput.getText().toString().trim();
        String type = typeInput.getText().toString().trim();
        String provider = providerInput.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(type) || TextUtils.isEmpty(provider)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a unique key for each log
        String logId = databaseRef.push().getKey();

        // Create log entry
        Map<String, String> logEntry = new HashMap<>();
        logEntry.put("date", date);
        logEntry.put("type", type);
        logEntry.put("provider", provider);

        // Save to Firebase
        databaseRef.child(logId).setValue(logEntry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Log created successfully", Toast.LENGTH_SHORT).show();
                    loadApptLogs(); // Refresh the table
                    clearInputFields();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to create log", Toast.LENGTH_SHORT).show());
    }

    // Clear Input Fields
    private void clearInputFields() {
        dateInput.setText("");
        typeInput.setText("");
        providerInput.setText("");
    }

    // Load Logs from Firebase
    private void loadApptLogs() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                apptLogTable.removeAllViews(); // Clear table before loading new data

                // Add table headers
                TableRow headerRow = new TableRow(Appointments.this);
                headerRow.addView(createStyledHeader("Date"));
                headerRow.addView(createStyledHeader("Type"));
                headerRow.addView(createStyledHeader("Provider"));
                //if (isEditing) {
                //headerRow.addView(createStyledHeader("Delete"));
                //}
                apptLogTable.addView(headerRow);

                for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                    String logId = logSnapshot.getKey();
                    String date = logSnapshot.child("date").getValue(String.class);
                    String type = logSnapshot.child("type").getValue(String.class);
                    String provider = logSnapshot.child("provider").getValue(String.class);

                    TableRow row = new TableRow(Appointments.this);
                    row.addView(createTextView(date));
                    row.addView(createTextView(type));
                    row.addView(createTextView(provider));

                    // Add Delete Button
                    if (isEditing) {
                        Button deleteButton = new Button(Appointments.this);
                        deleteButton.setText("Delete");
                        deleteButton.setTextColor(getResources().getColor(android.R.color.white));
                        deleteButton.setTextSize(14);
                        deleteButton.setTypeface(ResourcesCompat.getFont(Appointments.this, R.font.poppins_medium));
                        deleteButton.setPadding(16, 8, 16, 8);
                        deleteButton.setOnClickListener(view -> deleteLog(logId));
                        row.addView(deleteButton);
                    }

                    apptLogTable.addView(row);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Appointments.this, "Failed to load logs", Toast.LENGTH_SHORT).show();
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
        textView.setBackgroundResource(R.drawable.cell_shape_header); // Your drawable
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
                    loadApptLogs(); // Refresh the table
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete log", Toast.LENGTH_SHORT).show());
    }
}