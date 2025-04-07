package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthassistant.databinding.ActivityAppointmentsBinding;
import com.example.healthassistant.databinding.ActivityMedicationManagerBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Appointments extends AppCompatActivity {
    ActivityAppointmentsBinding binding;

    private DatabaseReference database;
    private String uid;

    /**this was already created**/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointments);

        /**section below is new**/

        /**bottom bar navigation functionality**/
        binding = ActivityAppointmentsBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

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


        // initialize variables for saving to the realtime database
        FirebaseAuth auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference("appointments").child(uid);

        loadAppointments();

        findViewById(R.id.addAppointmentButton).setOnClickListener(view -> addAppointment());
        findViewById(R.id.editAppointmentsTableButton).setOnClickListener(view -> enableEditMode());
    }


    // Load data from Firebase when user logs in
    private void loadAppointments() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TableLayout table = findViewById(R.id.appointmentsTableLayout);
                if(table.getChildCount() > 1) {
                    table.removeViews(1, table.getChildCount() - 1); // Clear old data before loading


                }

                for (DataSnapshot appointment : snapshot.getChildren()) {
                    String date = appointment.child("date").getValue(String.class);
                    String type = appointment.child("type").getValue(String.class);
                    String provider = appointment.child("provider").getValue(String.class);

                    addRowToTable(date, type, provider, appointment.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching data", error.toException());
            }
        });
    }




    // Add new appointment to Firebase and display in the table
    private void addAppointment() {
        String date = ((EditText) findViewById(R.id.inputApptDate)).getText().toString();
        String type = ((EditText) findViewById(R.id.inputApptType)).getText().toString();
        String provider = ((EditText) findViewById(R.id.inputApptDr)).getText().toString();

        if (!date.isEmpty() && !type.isEmpty() && !provider.isEmpty()) {
            DatabaseReference newEntry = database.push();
            newEntry.setValue(new Appointment(date, type, provider));
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }

    // Dynamically add rows to the table
    private void addRowToTable(String date, String type, String provider, String key) {
        TableLayout table = findViewById(R.id.appointmentsTableLayout);
        TableRow row = new TableRow(this);

        row.addView(createCell(date));
        row.addView(createCell(type));
        row.addView(createCell(provider));

        // Add delete button in edit mode
        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setVisibility(View.GONE);
        deleteButton.setOnClickListener(view -> {
            database.child(key).removeValue();
            table.removeView(row);
        });

        row.addView(deleteButton);
        table.addView(row);
    }

    // Helper method to create table cells
    private TextView createCell(String content) {
        TextView textView = new TextView(this);
        textView.setText(content);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setTextSize(17f);
        textView.setPadding(8, 8, 8, 8);
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins_medium));
        return textView;
    }

    // Enable edit mode (reveals delete buttons and shows "Save" button)
    private void enableEditMode() {
        TableLayout table = findViewById(R.id.appointmentsTableLayout);

        for (int i = 1; i < table.getChildCount(); i++) { // Start at 1 to skip the header row
            TableRow row = (TableRow) table.getChildAt(i);
            Button deleteButton = (Button) row.getChildAt(3);
            deleteButton.setVisibility(View.VISIBLE);
        }

        Button saveButton = findViewById(R.id.saveApptButton);
        saveButton.setVisibility(View.VISIBLE);


        saveButton.setOnClickListener(view -> {
            for (int i = 1; i < table.getChildCount(); i++) {
                TableRow row = (TableRow) table.getChildAt(i);
                Button deleteButton = (Button) row.getChildAt(3);
                deleteButton.setVisibility(View.GONE);
            }

            saveButton.setVisibility(View.GONE); // Hides Save button after saving
            Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
        });
    }

    // Appointment class for Firebase data storage
    public static class Appointment {
        public String date;
        public String type;
        public String provider;

        public Appointment() {} // Required empty constructor for Firebase

        public Appointment(String date, String type, String provider) {
            this.date = date;
            this.type = type;
            this.provider = provider;
        }
    }
}
