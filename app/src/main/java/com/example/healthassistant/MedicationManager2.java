package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthassistant.databinding.ActivityFoodManagerBinding;
import com.example.healthassistant.databinding.ActivityMedicationManager2Binding;
import com.example.healthassistant.databinding.ActivityMedicationManagerBinding;

import java.util.ArrayList;
import java.util.List;

public class MedicationManager2 extends AppCompatActivity {
    ActivityMedicationManager2Binding binding;
    private RecyclerView medicationsRecyclerView;
    private MedicationAdapter medicationAdapter;
    private List<Medication> medicationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_manager2);

        /**bottom bar navigation functionality**/
        binding = ActivityMedicationManager2Binding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(MedicationManager2.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(MedicationManager2.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(MedicationManager2.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(MedicationManager2.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(MedicationManager2.this, MedicationManager.class));
                    break;

            }
            return true;
        });

        //add medication button
        Button buttonAddMed = (Button) findViewById(R.id.addMedicationButton);
        buttonAddMed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MedicationManager2.this, AddMedication.class));
            }
        });

        // Initialize RecyclerView
        medicationsRecyclerView = binding.medicationsRecyclerView; // Ensure this ID matches your XML layout

        // Initialize the medication list and adapter
        medicationList = new ArrayList<>(); // Create the list of medications
        medicationAdapter = new MedicationAdapter(medicationList); // Create the adapter
        medicationsRecyclerView.setAdapter(medicationAdapter); // Set the adapter to the RecyclerView

        // (Optional) Set a layout manager if needed
        medicationsRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set a layout manager

        // Existing bottom navigation code...
        Intent intent = getIntent();
        String name = intent.getStringExtra("NAME");
        String expireDate = intent.getStringExtra("EXPIRE_DATE");
        int totalPills = intent.getIntExtra("TOTAL_PILLS", 0);

        if (expireDate != null) {
            medicationList.add(new Medication(name, expireDate, totalPills));
            medicationAdapter.notifyDataSetChanged();  // Update RecyclerView
        }
    }
}