package com.example.healthassistant;

import android.content.Intent;
import android.graphics.Color;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class BloodPressure_HealthGoals extends AppCompatActivity {

    private DatabaseReference databaseRef;
    private String userId;

    private EditText inputBPDate, inputBPSys, inputBPDiast;

    private TableLayout BPLogTable;
    private Button bpEditButton;
    private boolean bpisEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blood_pressure_health_goals);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initialize Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("bp_logs");

        // Initialize UI Elements
        inputBPDate = findViewById(R.id.date_bp);
        inputBPSys = findViewById(R.id.sys_bp);
        inputBPDiast= findViewById(R.id.diast_bp);

        BPLogTable = findViewById(R.id.BloodPLogTable);
        bpEditButton = findViewById(R.id.editBPLabResults);


        Button bpCreateLog = findViewById(R.id.saveBPLabResults);
        bpCreateLog.setOnClickListener(view -> createBPLog());


        /*bottom bar navigation functionality*/

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);

        bpEditButton.setOnClickListener(v -> {
            bpisEditing = !bpisEditing;
            bpEditButton.setText(bpisEditing ? "Save Table" : "Edit Table");
            loadBPLogs(); // load cholesterol data when the page is opened
        });

        loadBPLogs();

        nav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(BloodPressure_HealthGoals.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(BloodPressure_HealthGoals.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(BloodPressure_HealthGoals.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(BloodPressure_HealthGoals.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(BloodPressure_HealthGoals.this, MedicationManager.class));
                    break;
            }
            return true;
        });
    }


    //creating the log and saving it to the Firebase Realtime database
    private void createBPLog(){
        String date = inputBPDate.getText().toString().trim();
        String bp_sys = inputBPSys.getText().toString().trim();
        String bp_diast = inputBPDiast.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(bp_sys) || TextUtils.isEmpty(bp_diast)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a unique key for each log
        String logId = databaseRef.push().getKey();


        // Create log entry
        Map<String, String> logEntry = new HashMap<>();
        logEntry.put("date", EncryptionUtils.encrypt(date));
        logEntry.put("bp_sys", EncryptionUtils.encrypt(bp_sys));
        logEntry.put("bp_diast", EncryptionUtils.encrypt(bp_diast));

        // Save to Firebase
        databaseRef.child(logId).setValue(logEntry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Log created successfully", Toast.LENGTH_SHORT).show();
                    loadBPLogs(); // Refresh the table
                    clearInputFields();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to create log", Toast.LENGTH_SHORT).show());
    }

    // Clear Input Fields
    private void clearInputFields() {
        inputBPDate.setText("");
        inputBPSys.setText("");
        inputBPDiast.setText("");
    }



    //load data from firebase when user logs in
    private void loadBPLogs() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                BPLogTable.removeAllViews(); // Clear table before loading new data

                // Add table headers
                TableRow headerRow = new TableRow(BloodPressure_HealthGoals.this);
                headerRow.addView(createStyledHeader("Date"));
                headerRow.addView(createStyledHeader("Systolic Reading"));
                headerRow.addView(createStyledHeader("Diastolic Reading"));


                BPLogTable.addView(headerRow);


                for (DataSnapshot bp : snapshot.getChildren()) {
                    String logId = bp.getKey();
                    String dateEncryp = bp.child("date").getValue(String.class);
                    String bpSysEncryp = bp.child("bp_sys").getValue(String.class);
                    String bpDiastEncryp = bp.child("bp_diast").getValue(String.class);

                    //leveraging EncryptionUtils java file to encode Personally Identifiable Information (PII)
                    String date = EncryptionUtils.decrypt(dateEncryp);
                    String sys = EncryptionUtils.decrypt(bpSysEncryp);
                    String diast = EncryptionUtils.decrypt(bpDiastEncryp);


                    TableRow row = new TableRow(BloodPressure_HealthGoals.this);
                    row.addView(createTextView(date));
                    try {
                        int sysVal = Integer.parseInt(sys);
                        int diastVal = Integer.parseInt(diast);


                        row.addView(createTextView(sys, getBPColor(sysVal, diastVal)));
                        row.addView(createTextView(diast, getBPColor(diastVal, diastVal)));
                    } catch (NumberFormatException e) {
                        // Fallback in case of invalid entries
                        row.addView(createTextView(sys, Color.LTGRAY));
                        row.addView(createTextView(diast, Color.LTGRAY));
                    }



                    // Add Delete Button
                    if (bpisEditing) {
                        Button deleteButton = new Button(BloodPressure_HealthGoals.this);
                        deleteButton.setText("Delete");
                        deleteButton.setTextColor(getResources().getColor(android.R.color.white));
                        deleteButton.setTextSize(14);
                        deleteButton.setTypeface(ResourcesCompat.getFont(BloodPressure_HealthGoals.this, R.font.poppins_medium));
                        deleteButton.setPadding(16, 8, 16, 8);
                        deleteButton.setOnClickListener(view -> deleteLog(logId));
                        row.addView(deleteButton);
                    }

                    BPLogTable.addView(row);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BloodPressure_HealthGoals.this, "Failed to load logs", Toast.LENGTH_SHORT).show();
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
        textView.setBackgroundResource(R.drawable.cell_shape_header_hg); // Your drawable
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins_medium)); // Your font
        return textView;
    }

    private TextView createTextView(String text) {
        return createTextView(text, Color.TRANSPARENT);  // Default color
    }


    // Helper Method: Create a TextView for Table Rows
    private TextView createTextView(String text, int backgroundColor) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins_medium)); // Apply font
        textView.setBackgroundColor(backgroundColor); // this will change the cell color based on the user's input
        return textView;
    }


    // Logic below will change the cell colors based on the user's input :)
    private int getBPColor(int sys, int dia) {
        if (sys > 180 || dia > 120) {
            return getColor(R.color.hyper_red); // Hypertensive Crisis
        } else if (sys >= 140 || dia >= 90) {
            return getColor(R.color.st2_orange); // Stage 2 Hypertension
        } else if (sys >= 130 || dia >= 80) {
            return getColor(R.color.st1_orange); // Stage 1 Hypertension
        } else if (sys >= 120 && dia < 80) {
            return getColor(R.color.elevated_yellow); // Elevated
        } else {
            return getColor(R.color.normal_green); // Normal
        }
    }


    // Delete Log Function
    private void deleteLog(String logId) {
        databaseRef.child(logId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Log deleted", Toast.LENGTH_SHORT).show();
                    loadBPLogs(); // Refresh the table
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete log", Toast.LENGTH_SHORT).show());
    }
}
