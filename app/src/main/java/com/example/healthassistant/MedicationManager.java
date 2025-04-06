package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthassistant.databinding.ActivityFoodManagerBinding;
import com.example.healthassistant.databinding.ActivityMedicationManagerBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MedicationManager extends AppCompatActivity {

    ActivityMedicationManagerBinding binding;

    /**this was already created**/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_manager);

        /**section below is new**/

        /**bottom bar navigation functionality**/
        binding = ActivityMedicationManagerBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(MedicationManager.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(MedicationManager.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(MedicationManager.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(MedicationManager.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(MedicationManager.this, MedicationManager.class));
                    break;

            }
            return true;
        });

        /**current medication button functionality**/
        Button button = (Button) findViewById(R.id.currentMedicationButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MedicationManager.this, MedicationManager2.class));
            }
        });

        // Expiration and Refill UI components
        // Fetches user's medication list
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference userMedicationRef = database.collection("userMedications");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();

        // Goes through the user's medications
        userMedicationRef.whereEqualTo("userID", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Medication> medicationList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            // Pulls necessary values about the medication for the UI
                            String expirationDate = document.getString("expirationDate");
                            int totalPills = document.getLong("totalPills").intValue();
                            int pillsTaken = document.getLong("pillsTaken").intValue();
                            String medicationForm = document.getString("medicationForm");
                            DocumentReference medRef = document.getDocumentReference("medicationRef");

                            // Pulls the medication name from the medication reference
                            medRef.get().addOnSuccessListener(medSnapshot -> {
                                if (medSnapshot.exists()) {
                                    String medicationName = medSnapshot.getString("Name");
                                    // creates a new medication object with all the desired values then adds it to the main medication list
                                    Medication medication = new Medication();
                                    medication.setName(medicationName);
                                    medication.setExpirationDate(expirationDate);
                                    medication.setTotalPills(totalPills);
                                    medication.setPillsTaken(pillsTaken);
                                    medication.setMedicationForm(medicationForm);
                                    medicationList.add(medication);
                                }

                                // Filters through the main medication list for the expired ones and adds it to another list
                                List<Medication> expiringMedications = new ArrayList<>();
                                for (Medication medication : medicationList) {
                                    if (medication.isExpiringSoon(medication.getExpirationDate())) {
                                        expiringMedications.add(medication);
                                    }
                                }
                                setUpExpiringMedicationView(expiringMedications); // sets up the expired medication UI with this new list

                                // Filters through the main medication list for the ones needing be refilled and adds it to another list
                                List<Medication> refillMedications = new ArrayList<>();
                                for (Medication medication : medicationList) {
                                    if (medication.isRefillNeeded(medication.getTotalPills(), medication.getPillsTaken())) {
                                        refillMedications.add(medication);
                                    }
                                }
                                setUpRefillMedicationView(refillMedications); // sets up the expired medication UI with this new list

                            }).addOnFailureListener(e -> {
                                Log.e("Medication Manager", "Error fetching medication", e);
                            });
                        }
                    }
                });
    }

    // Sets up the expiration UI and enables arrow functionality
    private void setUpExpiringMedicationView(List<Medication> expiringMedications) {
        // Initializing the left and right arrows
        ImageView leftArrow = findViewById(R.id.expiringLeftButton);
        ImageView rightArrow = findViewById(R.id.expiringRightButton);

        // sets the first expiring medication as the first page
        final int[] currentIndex = {0};
        updateExpirationCard(expiringMedications, currentIndex[0]);

        // if user clicks left then they go to the previous medication
        leftArrow.setOnClickListener(v -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                updateExpirationCard(expiringMedications, currentIndex[0]);
            }
        });

        // if user clicks right then they go to the next medication
        rightArrow.setOnClickListener(v -> {
            if (currentIndex[0] < expiringMedications.size() - 1) {
                currentIndex[0]++;
                updateExpirationCard(expiringMedications, currentIndex[0]);
            }
        });
    }

    // updates the current expiration UI being shown
    private void updateExpirationCard(List<Medication> expiringMedications, int index) {
        // Initializing values being changed (name and expiration date)
        TextView medicationName = findViewById(R.id.expiringMedicationName);
        TextView expirationDate = findViewById(R.id.expiringExpirationDate);

        // Sets values to the new medication values
        Medication medication = expiringMedications.get(index);
        medicationName.setText(medication.getName());
        expirationDate.setText("Expires: " + medication.getExpirationDate());
    }

    // Sets up the refill UI and enables arrow functionality
    private void setUpRefillMedicationView(List<Medication> refillMedications) {
        // Initializing the left and right arrows
        ImageView leftArrow = findViewById(R.id.refillLeftButton);
        ImageView rightArrow = findViewById(R.id.refillRightButton);

        // sets the first refill medication as the first page
        final int[] currentIndex = {0};
        updateRefillCard(refillMedications, currentIndex[0]);

        // if user clicks left then they go to the previous medication
        leftArrow.setOnClickListener(v -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                updateRefillCard(refillMedications, currentIndex[0]);
            }
        });

        // if user clicks right then they go to the next medication
        rightArrow.setOnClickListener(v -> {
            if (currentIndex[0] < refillMedications.size() - 1) {
                currentIndex[0]++;
                updateRefillCard(refillMedications, currentIndex[0]);
            }
        });
    }

    // updates the current refill UI being shown
    private void updateRefillCard(List<Medication> refillMedications, int index) {
        // Initializing values being changed (name and refill dosage)
        TextView medicationName = findViewById(R.id.refillMedicationName);
        TextView expirationDate = findViewById(R.id.refillDosage);

        // Sets values to the new medication values
        Medication medication = refillMedications.get(index);
        medicationName.setText(medication.getName());
        expirationDate.setText(medication.getPillsTaken() + "/" + medication.getTotalPills() + " " + medication.getMedicationForm() + "s taken");
    }
}