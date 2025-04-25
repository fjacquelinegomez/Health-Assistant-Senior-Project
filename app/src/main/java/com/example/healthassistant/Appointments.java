package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthassistant.databinding.ActivityAppointmentsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Appointments extends AppCompatActivity {
    ActivityAppointmentsBinding binding;

    private EditText dateInput, typeInput, providerInput;
    private Button addAppointmentButton;
    private DatabaseReference appointmentsRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments").child(currentUser.getUid());
        }


        // Find views from layout
        dateInput = findViewById(R.id.dateReminderLinearLayout).findViewById(R.id.date); // if you set an id for EditText
        typeInput = findViewById(R.id.typeAppointmentInputLinearLayout).findViewById(R.id.type); // make sure your EditText has id "type"
        providerInput = findViewById(R.id.providerAppointmentInputLinearLayout).findViewById(R.id.name); // and id "name"
        addAppointmentButton = findViewById(R.id.addAppointmentButton);

        // Button click listener
        addAppointmentButton.setOnClickListener(v -> addAppointment());

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

    private void addAppointment() {
        String date = dateInput.getText().toString().trim();
        String type = typeInput.getText().toString().trim();
        String provider = providerInput.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(type) || TextUtils.isEmpty(provider)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String appointmentId = appointmentsRef.push().getKey();  // Unique ID
        Appointment appointment = new Appointment(date, type, provider);

        if (appointmentId != null) {
            appointmentsRef.child(appointmentId).setValue(appointment)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Add row to table
                            addAppointmentToTable(appointment);
                            Toast.makeText(this, "Appointment added", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to save appointment", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        dateInput.setText("");
        typeInput.setText("");
        providerInput.setText("");
    }

    private void addAppointmentToTable(Appointment appointment) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        TextView dateCol = new TextView(this);
        dateCol.setText(appointment.getDate());
        dateCol.setPadding(10, 10, 10, 10);

        TextView typeCol = new TextView(this);
        typeCol.setText(appointment.getType());
        typeCol.setPadding(10, 10, 10, 10);

        TextView providerCol = new TextView(this);
        providerCol.setText(appointment.getProvider());
        providerCol.setPadding(10, 10, 10, 10);

        row.addView(dateCol);
        row.addView(typeCol);
        row.addView(providerCol);

        //appointmentTable.addView(row);
    }
    public class Appointment {
        private String date;
        private String type;
        private String provider;

        public Appointment() {
            // Required for Firebase
        }

        public Appointment(String date, String type, String provider) {
            this.date = date;
            this.type = type;
            this.provider = provider;
        }

        public String getDate() {
            return date;
        }

        public String getType() {
            return type;
        }

        public String getProvider() {
            return provider;
        }
    }
}
