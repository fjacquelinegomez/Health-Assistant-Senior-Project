package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthassistant.databinding.ActivityMedicationManager2Binding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MedicationManager2 extends AppCompatActivity {
    private List<Medication> medicationList = new ArrayList<>();
    private MedicationAdapter medicationAdapter;
    ActivityMedicationManager2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_manager2);

        /** Bottom bar navigation functionality **/
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

        // Set up RecyclerView and adapter
        RecyclerView medicationsRecyclerView = findViewById(R.id.medicationsRecyclerView);
        medicationAdapter = new MedicationAdapter(medicationList, this); // Pass context to adapter
        medicationsRecyclerView.setAdapter(medicationAdapter);
        medicationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get a reference to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference medicationRef = db.collection("userMedications");

        // Filter by userID
        medicationRef.whereEqualTo("userID", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                medicationList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String name = document.getString("medicationName");
                    String expireDate = document.getString("expirationDate");
                    //String dosageForm = document.getString("dosageForm");
                    int totalPills = document.getLong("totalPills").intValue();
                    //int dosesTaken = document.getLong("dosesTaken").intValue();
                    String key = document.getId();

                    //Medication medication = new Medication(name, expireDate, totalPills, dosesTaken, dosageForm, key);
                    Medication medication = new Medication(name, expireDate, totalPills, key);
                    medicationList.add(medication);
                }
                medicationAdapter.notifyDataSetChanged();
            } else {
                Log.e("FirestoreError", "Error loading medications", task.getException());
            }
        });
        /**
        // Retrieve data from Firestore and populate the RecyclerView
        medicationRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        medicationList.clear(); // Clear the list before adding new data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Medication medication = document.toObject(Medication.class);
                            medication.setKey(document.getId());  // Set the Firestore document ID as the key
                            medicationList.add(medication);
                        }
                        Log.d("MedicationManager", "Loaded medications: " + medicationList.size()); // Log the size
                        medicationAdapter.notifyDataSetChanged(); // Notify the adapter to update the UI
                    } else {
                        Toast.makeText(MedicationManager2.this, "Failed to load medications", Toast.LENGTH_SHORT).show();
                        Log.e("FirestoreError", "Error loading medications", task.getException());

                    }
                });
         **/

        // Add Medication Button
        Button button = findViewById(R.id.addMedicationButton);
        button.setOnClickListener(v -> startActivity(new Intent(MedicationManager2.this, AddMedication.class)));
    }

    // This method is called when data is fetched from Firestore, to set the list.
    private void updateMedicationList(List<Medication> medications) {
        medicationList.clear();
        medicationList.addAll(medications);
        medicationAdapter.notifyDataSetChanged(); // Notify the adapter about data changes
    }

    // Method to delete medication from Firestore and update the list
    public void deleteMedicationFromFirestore(Medication medication, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference medicationRef = db.collection("userMedications").document(medication.getKey());

        medicationRef.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Successfully deleted from Firestore
                medicationList.remove(position); // Remove the medication from the local list
                medicationAdapter.notifyItemRemoved(position); // Notify the adapter about the removed item
                medicationAdapter.notifyItemRangeChanged(position, medicationList.size()); // Adjust remaining items
            } else {
                Toast.makeText(MedicationManager2.this, "Failed to delete medication", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

/**
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

        //bottom bar navigation functionality
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
        String name = intent.getStringExtra("medicationName"); // References medication name passed in by search
        String expireDate = intent.getStringExtra("EXPIRE_DATE");
        int totalPills = intent.getIntExtra("TOTAL_PILLS", 0);

        if (expireDate != null) {
            medicationList.add(new Medication(name, expireDate, totalPills));
            medicationAdapter.notifyDataSetChanged();  // Update RecyclerView
        }
    }
}
**/