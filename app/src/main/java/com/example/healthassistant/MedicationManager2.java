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
