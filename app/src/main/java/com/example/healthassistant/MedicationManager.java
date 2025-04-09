package com.example.healthassistant;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import java.util.Objects;

import java.util.Calendar;

public class MedicationManager extends AppCompatActivity {

    Button setTimeButton;
    TextView selectedTimeText;
    ActivityMedicationManagerBinding binding;
    private static final int NOTIFICATION_REQUEST_CODE = 101;
    private static final String CHANNEL_ID = "medicationChannel";

    /**
     * this was already created
     **/
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


        setTimeButton = findViewById(R.id.setTimeButton);
        selectedTimeText = findViewById(R.id.selectedTimeText);

        //createNotificationChannel();

        //requestNotificationPermission();

        setTimeButton.setOnClickListener(view -> { // shows a timepicker when the button is clicked
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view1, selectedHour, selectedMinute) -> {
                    //display the time selected
                        selectedTimeText.setText(String.format("Reminder set for %02d:%02d", selectedHour, selectedMinute));
                        // set the alarm
                        setAlarm(selectedHour, selectedMinute);
                    }, hour, minute, true);

            timePickerDialog.show();
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

                        // List of all user's medications
                        List<Medication> medicationList = new ArrayList<>();
                        // Will keep track of whether or not the asynchronous task is completely done
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        final int totalDocuments = documents.size();
                        final int[] completedFetches = {0};

                        for (DocumentSnapshot document : documents) {
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
                                completedFetches[0]++;

                                // Filtered list of medications (expiration and refill)
                                List<Medication> expiringMedications = new ArrayList<>();
                                List<Medication> refillMedications = new ArrayList<>();

                                // If all fetches are done
                                if (completedFetches[0] == totalDocuments) {
                                    // Filters through the main medication list for the medications that are expiring soon or need a refill soon
                                    // and adds the medication to their respective lists
                                    for (Medication medication : medicationList) {
                                        if (medication.isExpiringSoon(medication.getExpirationDate())) {
                                            expiringMedications.add(medication);
                                        }
                                        if (medication.isRefillNeeded(medication.getTotalPills(), medication.getPillsTaken())) {
                                            refillMedications.add(medication);
                                        }
                                    }
                                }

                                // sets up the expired + refill medication UI with the new filtered lists
                                setUpMedicationView(expiringMedications, "expire");
                                setUpMedicationView(refillMedications, "refill");

                            }).addOnFailureListener(e -> {
                                Log.e("Medication Manager", "Error fetching medication", e);
                            });
                        }
                    }
                });
    }

    // Sets up the expiration + refill UI and enables arrow functionality
    private void setUpMedicationView(List<Medication> medications, String view) {
        // Initializing the arrows
        ImageView leftArrow;
        ImageView rightArrow;
        
        if (Objects.equals(view, "expire")) {
            // Initializing the left and right expiration view arrows
            leftArrow = findViewById(R.id.expiringLeftButton);
            rightArrow = findViewById(R.id.expiringRightButton);
            
        } else if (Objects.equals(view, "refill")) {
            // Initializing the left and right refill arrows
            leftArrow = findViewById(R.id.refillLeftButton);
            rightArrow = findViewById(R.id.refillRightButton);
        } else {
            rightArrow = null;
            leftArrow = null;
        }

        // if there's no medication that's expiring or needs a refill then it will stop
        if (medications.isEmpty()) return;

        // sets the first expiring/refill medication as the first page
        final int[] currentIndex = {0};
        updateMedicationView(medications, currentIndex[0], view);
        // sets the visibility of arrows
        leftArrow.setVisibility(View.INVISIBLE);
        if (medications.size() > 1) {
            rightArrow.setVisibility(View.VISIBLE);
        } else {
            rightArrow.setVisibility(View.INVISIBLE);
        }

        // if user clicks left then they go to the previous medication
        leftArrow.setOnClickListener(v -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                updateMedicationView(medications, currentIndex[0], view);

                // sets the visibility of arrows
                rightArrow.setVisibility(View.VISIBLE);
                if (currentIndex[0] == 0) {
                    leftArrow.setVisibility(View.INVISIBLE);
                }
            }
        });

        // if user clicks right then they go to the next medication
        rightArrow.setOnClickListener(v -> {
            if (currentIndex[0] < medications.size() - 1) {
                currentIndex[0]++;
                updateMedicationView(medications, currentIndex[0], view);

                // sets the visibility of arrows
                leftArrow.setVisibility(View.VISIBLE);
                if (currentIndex[0] == medications.size() - 1) {
                    rightArrow.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    // updates the current expiration/refill medication card being shown
    private void updateMedicationView(List<Medication> medications, int index, String view) {
        if (Objects.equals(view, "expire")) {
            // Initializing values being changed (name and expiration date)
            TextView medicationName = findViewById(R.id.expiringMedicationName);
            TextView expirationDate = findViewById(R.id.expiringExpirationDate);

            // Sets values to the new medication values
            Medication medication = medications.get(index);
            medicationName.setText(medication.getName());
            expirationDate.setText("Expires: " + medication.getExpirationDate());
        } else if (Objects.equals(view, "refill")) {
            // Initializing values being changed (name and refill dosage)
            TextView medicationName = findViewById(R.id.refillMedicationName);
            TextView refillDosage = findViewById(R.id.refillDosage);

            // Sets values to the new medication values
            Medication medication = medications.get(index);
            medicationName.setText(medication.getName());
            refillDosage.setText(medication.getPillsTaken() + "/" + medication.getTotalPills() + " " + medication.getMedicationForm() + "s taken");
        }
    }

    // Sets an alarm that triggers at the selected time
    private void setAlarm(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
    }
}