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

public class Cholesterol_HealthGoals extends AppCompatActivity {

    private DatabaseReference databaseRef;
    private String userId;

    private EditText inputLabDate, inputTotalChol, inputHDLChol, inputLDLChol;
    private TableLayout CholLogTable;
    private Button cholEditButton;
    private boolean cholisEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cholesterol_health_goals);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initialize Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("cholesterol_logs");

        // Initialize UI Elements
        inputLabDate = findViewById(R.id.date_chol_labs);
        inputTotalChol = findViewById(R.id.chol_total_results);
        inputHDLChol= findViewById(R.id.chol_hdl_results);
        inputLDLChol = findViewById(R.id.chol_ldl_results);

        CholLogTable = findViewById(R.id.cholLogTable);
        cholEditButton = findViewById(R.id.editCholLabResults);


        Button cholCreateLog = findViewById(R.id.saveCholLabResults);
        cholCreateLog.setOnClickListener(view -> createCholLog());

        cholEditButton.setOnClickListener(v -> {
            cholisEditing = !cholisEditing;
            cholEditButton.setText(cholisEditing ? "Save Table" : "Edit Table");
            loadCholLogs(); // load cholesterol data when the page is opened
        });

        loadCholLogs();

        /*bottom bar navigation functionality*/

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);

        nav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(Cholesterol_HealthGoals.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(Cholesterol_HealthGoals.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(Cholesterol_HealthGoals.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(Cholesterol_HealthGoals.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(Cholesterol_HealthGoals.this, MedicationManager.class));
                    break;
            }
            return true;
        });
    }


    //creating the log and saving it to the realtime database
    private void createCholLog(){
        String date = inputLabDate.getText().toString().trim();
        String total_chol = inputTotalChol.getText().toString().trim();
        String hdl_chol = inputHDLChol.getText().toString().trim();
        String ldl_chol = inputLDLChol.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(total_chol) || TextUtils.isEmpty(hdl_chol) || TextUtils.isEmpty(ldl_chol)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a unique key for each log
        String logId = databaseRef.push().getKey();


        // Create log entry
        Map<String, String> logEntry = new HashMap<>();
        logEntry.put("date", EncryptionUtils.encrypt(date));
        logEntry.put("total_chol", EncryptionUtils.encrypt(total_chol));
        logEntry.put("hdl_chol", EncryptionUtils.encrypt(hdl_chol));
        logEntry.put("ldl_chol", EncryptionUtils.encrypt(ldl_chol));

        // Save to Firebase
        databaseRef.child(logId).setValue(logEntry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Log created successfully", Toast.LENGTH_SHORT).show();
                    loadCholLogs(); // Refresh the table
                    clearInputFields();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to create log", Toast.LENGTH_SHORT).show());
    }


    // Clear Input Fields
    private void clearInputFields() {
        inputLabDate.setText("");
        inputTotalChol.setText("");
        inputHDLChol.setText("");
        inputLDLChol.setText("");
    }

    //load data from firebase when user logs in
    private void loadCholLogs() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                CholLogTable.removeAllViews(); // Clear table before loading new data

                // Add table headers
                TableRow headerRow = new TableRow(Cholesterol_HealthGoals.this);
                headerRow.addView(createStyledHeader("Date"));
                headerRow.addView(createStyledHeader("Total Cholesterol"));
                headerRow.addView(createStyledHeader("HDL-C"));
                headerRow.addView(createStyledHeader("LDL-C"));

                CholLogTable.addView(headerRow);


                for (DataSnapshot chol : snapshot.getChildren()) {
                    String logId = chol.getKey();
                    String dateEncryp = chol.child("date").getValue(String.class);
                    String totalCEncryp = chol.child("total_chol").getValue(String.class);
                    String hdlEncryp = chol.child("hdl_chol").getValue(String.class);
                    String ldlEncryp = chol.child("ldl_chol").getValue(String.class);

                    //leveraging EncryptionUtils java file to encode Personally Identifiable Information (PII)
                    String date = EncryptionUtils.decrypt(dateEncryp);
                    String total = EncryptionUtils.decrypt(totalCEncryp);
                    String hdl = EncryptionUtils.decrypt(hdlEncryp);
                    String ldl = EncryptionUtils.decrypt(ldlEncryp);

                    TableRow row = new TableRow(Cholesterol_HealthGoals.this);
                    row.addView(createTextView(date));
                    try {
                        int totalVal = Integer.parseInt(total);
                        int hdlVal = Integer.parseInt(hdl);
                        int ldlVal = Integer.parseInt(ldl);

                        row.addView(createTextView(total, getTotalCholesterolColor(totalVal)));
                        row.addView(createTextView(hdl, getHDLColor(hdlVal)));
                        row.addView(createTextView(ldl, getLDLColor(ldlVal)));
                    } catch (NumberFormatException e) {
                        // Fallback in case of invalid entries
                        row.addView(createTextView(total, Color.LTGRAY));
                        row.addView(createTextView(hdl, Color.LTGRAY));
                        row.addView(createTextView(ldl, Color.LTGRAY));
                    }



                    // Add Delete Button
                    if (cholisEditing) {
                        Button deleteButton = new Button(Cholesterol_HealthGoals.this);
                        deleteButton.setText("Delete");
                        deleteButton.setTextColor(getResources().getColor(android.R.color.white));
                        deleteButton.setTextSize(14);
                        deleteButton.setTypeface(ResourcesCompat.getFont(Cholesterol_HealthGoals.this, R.font.poppins_medium));
                        deleteButton.setPadding(16, 8, 16, 8);
                        deleteButton.setOnClickListener(view -> deleteLog(logId));
                        row.addView(deleteButton);
                    }

                    CholLogTable.addView(row);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Cholesterol_HealthGoals.this, "Failed to load logs", Toast.LENGTH_SHORT).show();
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
    private int getTotalCholesterolColor(int value) {
        if (value < 200) return getColor(android.R.color.holo_green_light);
        else if (value <= 239) return getColor(android.R.color.holo_orange_light);
        else return getColor(android.R.color.holo_red_light);
    }

    private int getHDLColor(int value) {
        if (value < 40) return getColor(android.R.color.holo_red_light);
        else if (value <= 59) return getColor(android.R.color.holo_orange_light);
        else return getColor(android.R.color.holo_green_light);
    }

    private int getLDLColor(int value) {
        if (value < 100) return getColor(android.R.color.holo_green_light);
        else if (value <= 159) return getColor(android.R.color.holo_orange_light);
        else return getColor(android.R.color.holo_red_light);
    }


    // Delete Log Function
    private void deleteLog(String logId) {
        databaseRef.child(logId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Log deleted", Toast.LENGTH_SHORT).show();
                    loadCholLogs(); // Refresh the table
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete log", Toast.LENGTH_SHORT).show());
    }

}

