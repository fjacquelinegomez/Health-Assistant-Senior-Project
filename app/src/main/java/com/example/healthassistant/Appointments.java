package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Appointments extends AppCompatActivity {
    ActivityAppointmentsBinding binding;

    private DatabaseReference database;
    private String uid;
    private EditText inputdateAppt, inputtypeAppt, inputdoctorAppt;
    private TableLayout ApptLogTable;
    private Button btnEditAppt;
    private boolean ApptisEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointments);

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);

        nav.setOnItemSelectedListener(item -> {
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

        //initialize variables for saving to the realtime database
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference("users").child(uid).child("appointments");

        // Initialize UI elements
        inputdateAppt = findViewById(R.id.inputApptDate);
        inputtypeAppt = findViewById(R.id.inputApptType);
        inputdoctorAppt = findViewById(R.id.inputApptDr);
        ApptLogTable = findViewById(R.id.appointmentsTableLayout);
        btnEditAppt = findViewById(R.id.editAppointmentsTableButton);

        Button btnCreateLog = findViewById(R.id.addAppointmentButton);
        btnCreateLog.setOnClickListener(view -> createLog());

        btnEditAppt.setOnClickListener(v -> {
            ApptisEditing = !ApptisEditing;
            btnEditAppt.setText(ApptisEditing ? "Save Table" : "Edit Table");
            loadAppointments(); // Load data when the activity starts
        });


        loadAppointments();
        
    }

    //create log function
    private void createLog() {
        String apptdate = inputdateAppt.getText().toString().trim();
        String appttype = inputtypeAppt.getText().toString().trim();
        String doctor = inputdoctorAppt.getText().toString().trim();

        if (TextUtils.isEmpty(apptdate) || TextUtils.isEmpty(appttype) || TextUtils.isEmpty(doctor)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a unique key for each log
        String logId = database.push().getKey();

        // Create log entry
        Map<String, String> logEntry = new HashMap<>();
        logEntry.put("date", EncryptionUtils.encrypt(apptdate));
        logEntry.put("type", EncryptionUtils.encrypt(appttype));
        logEntry.put("doctor", EncryptionUtils.encrypt(doctor));

        // Save to Firebase
        database.child(logId).setValue(logEntry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Log created successfully", Toast.LENGTH_SHORT).show();
                    loadAppointments(); // Refresh the table
                    clearInputFields();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to create log", Toast.LENGTH_SHORT).show());
    }

    // Clear Input Fields
    private void clearInputFields() {
        inputdateAppt.setText("");
        inputtypeAppt.setText("");
        inputdoctorAppt.setText("");
    }

    //load data from firebase when user logs in
    private void loadAppointments() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ApptLogTable.removeAllViews(); // Clear table before loading new data

                // Add table headers
                TableRow headerRow = new TableRow(Appointments.this);
                headerRow.addView(createStyledHeader("Date"));
                headerRow.addView(createStyledHeader("Type"));
                headerRow.addView(createStyledHeader("Doctor"));
                //if (ApptisEditing) {
                //headerRow.addView(createStyledHeader("Delete"));
                //}
                ApptLogTable.addView(headerRow);


                for (DataSnapshot appointment : snapshot.getChildren()) {
                    String logId = appointment.getKey();
                    String dateEnc = appointment.child("date").getValue(String.class);
                    String typeEnc = appointment.child("type").getValue(String.class);
                    String providerEnc = appointment.child("doctor").getValue(String.class);

                    //leveraging EncryptionUtils java file to encode Personally Identifiable Information (PII)
                    String date = EncryptionUtils.decrypt(dateEnc);
                    String type = EncryptionUtils.decrypt(typeEnc);
                    String doctor = EncryptionUtils.decrypt(providerEnc);

                    TableRow row = new TableRow(Appointments.this);
                    row.addView(createTextView(date));
                    row.addView(createTextView(type));
                    row.addView(createTextView(doctor));


                    // Add Delete Button
                    if (ApptisEditing) {
                        Button deleteButton = new Button(Appointments.this);
                        deleteButton.setText("Delete");
                        deleteButton.setTextColor(getResources().getColor(android.R.color.white));
                        deleteButton.setTextSize(14);
                        deleteButton.setTypeface(ResourcesCompat.getFont(Appointments.this, R.font.poppins_medium));
                        deleteButton.setPadding(16, 8, 16, 8);
                        deleteButton.setOnClickListener(view -> deleteLog(logId));
                        row.addView(deleteButton);
                    }

                    ApptLogTable.addView(row);


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
        database.child(logId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Log deleted", Toast.LENGTH_SHORT).show();
                    loadAppointments(); // Refresh the table
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete log", Toast.LENGTH_SHORT).show());
    }


}