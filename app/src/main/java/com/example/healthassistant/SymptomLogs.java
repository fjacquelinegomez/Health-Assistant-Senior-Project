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
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthassistant.databinding.ActivityAppointmentsBinding;
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

public class SymptomLogs extends AppCompatActivity {

    ActivitySymptomLogsBinding binding;
    private DatabaseReference databaseRef;
    private String userId;
    private EditText inputDate, inputSymp, inputRating;
    private TableLayout sympLogTable;
    private Button SympEditButton;
    private boolean isEditing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_symptom_logs);



        // Initialize Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("symptomLogs");

        // Initialize UI Elements
        inputDate = findViewById(R.id.inputSympDate);
        inputSymp = findViewById(R.id.inputSymp);
        inputRating = findViewById(R.id.inputSympRating);
        sympLogTable = findViewById(R.id.sympLogTable);
        SympEditButton = findViewById(R.id.btnSympEditTable);

        Button btnCreateLog = findViewById(R.id.btnSympCreateLog);
        btnCreateLog.setOnClickListener(view -> createLog());

        SympEditButton.setOnClickListener(v -> {
            isEditing = !isEditing;
            SympEditButton.setText(isEditing ? "Save Table" : "Edit Table");
            loadSympLogs(); // Refresh table with or without delete buttons
        });


        loadSympLogs(); // Load data when the activity starts


        /*bottom bar navigation functionality*/
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);

        nav.setOnItemSelectedListener(item -> {
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }



    // Create Log Function
    private void createLog() {
        String date = inputDate.getText().toString().trim();
        String symptom = inputSymp.getText().toString().trim();
        String rating = inputRating.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(symptom) || TextUtils.isEmpty(rating)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a unique key for each log
        String logId = databaseRef.push().getKey();

        // Create log entry
        Map<String, String> logEntry = new HashMap<>();
        logEntry.put("date", date);
        logEntry.put("symptom", symptom);
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
                headerRow.addView(createStyledHeader("Date"));
                headerRow.addView(createStyledHeader("Symptoms"));
                headerRow.addView(createStyledHeader("Rating"));
                //if (isEditing) {
                //headerRow.addView(createStyledHeader("Delete"));
                //}
                sympLogTable.addView(headerRow);

                for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                    String logId = logSnapshot.getKey();
                    String date = logSnapshot.child("date").getValue(String.class);
                    String symptom = logSnapshot.child("symptom").getValue(String.class);
                    String rating = logSnapshot.child("rating").getValue(String.class);

                    TableRow row = new TableRow(SymptomLogs.this);
                    row.addView(createTextView(date));
                    row.addView(createTextView(symptom));
                    row.addView(createTextView(rating));

                    // Add Delete Button
                    if (isEditing) {
                        Button deleteButton = new Button(SymptomLogs.this);
                        deleteButton.setText("Delete");
                        deleteButton.setTextColor(getResources().getColor(android.R.color.white));
                        deleteButton.setTextSize(14);
                        deleteButton.setTypeface(ResourcesCompat.getFont(SymptomLogs.this, R.font.poppins_medium));
                        deleteButton.setPadding(16, 8, 16, 8);
                        deleteButton.setOnClickListener(view -> deleteLog(logId));
                        row.addView(deleteButton);
                    }

                    sympLogTable.addView(row);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SymptomLogs.this, "Failed to load logs", Toast.LENGTH_SHORT).show();
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
        textView.setBackgroundResource(R.drawable.cell_shape_header_symptoms); // Your drawable
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
                    loadSympLogs(); // Refresh the table
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete log", Toast.LENGTH_SHORT).show());
    }
}
