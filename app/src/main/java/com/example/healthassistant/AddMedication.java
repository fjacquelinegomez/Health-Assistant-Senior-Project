package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddMedication extends AppCompatActivity {
    private EditText expireInput;
    private EditText totalPillsInput;
    private FirebaseFirestore database;
    private FirebaseAuth user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        // Initializes Firebase instances
        database = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();

        // Sets the medication name text of the selected medication
        String medicationName = getIntent().getStringExtra("medicationName");
        TextView medicationNameTextView = findViewById(R.id.medicationNameText);
        medicationNameTextView.setText(medicationName);

        // Receives the RxCUI from the last activity
        String rxcui = getIntent().getStringExtra("rxcui");

        // Initializes UI components
        EditText expireInput = findViewById(R.id.expireInput);
        EditText totalPillsInput = findViewById(R.id.countInput);
        Button addMedicationButton = findViewById(R.id.addMedicationButton);

        // The logic for when the user clicks the "Add Medication" Button after filling in fields
        // Should save their medication to their database and medication manager
        addMedicationButton.setOnClickListener(v -> {
            // Grabs user's input
            String expirationDate = expireInput.getText().toString();
            String totalPillsText = totalPillsInput.getText().toString();

            // FIXME: Handle empty fields

            // Converts to int to make it legible in the database
            int totalPills = Integer.parseInt(totalPillsText);

            // Grabs the user's unique ID
            String userId = user.getCurrentUser().getUid();

            // Save medication to medication manager and links it to the user adding it
            saveMedicationToFirestore(medicationName, expirationDate, totalPills, userId, rxcui);
        });
    }

    // Saves added medication to firestore
    private void saveMedicationToFirestore(String medicationName, String expirationDate, int totalPills, String userId, String rxcui) {
        Log.d("Firestore", "Hello");
        // Creates a document referebce in the medications database using the rxcui as the entry identifier
        DocumentReference medRef = database.collection("medications").document(rxcui);

        // Checks if the medication already exists in the medications database
        medRef.get().addOnCompleteListener(task -> {
           if (task.isSuccessful() && task.getResult().exists()) {
               // If medication already exists in the medication database, it skips to adding the user + medication to the userMedications database
               saveUserMedication(medicationName, expirationDate, totalPills,userId, rxcui);
            } else {
               // If medication doesn't exist in the medication database yet, adds to medication database then userMedications database
               // Creates new medication document
               Map<String, Object> medicationData = new HashMap<>();
               medicationData.put("RxCUI", rxcui);
               medicationData.put("Name", medicationName);

               // Saves to medications database
               medRef.set(medicationData).addOnSuccessListener(aVoid -> {
                   Log.d("Firestore", "Medication added: " + medicationName);
                   saveUserMedication(medicationName, expirationDate, totalPills, userId, rxcui);
               }).addOnFailureListener(e -> Log.e("Firestore", "Error adding medication", e));
           }
        });
    }

    // References added medication to the user
    private void saveUserMedication(String medicationName, String expirationDate, int totalPills, String userId, String rxcui) {
        // Creates a document reference in the userMedications database using the userId-rxcui as the entry identifier
        DocumentReference userMedRef = database.collection("userMedications").document(userId + "_" + rxcui);

        // Creates new userMedication document with their inputted values
        Map<String, Object> userMedData = new HashMap<>();
        userMedData.put("userID", userId);
        userMedData.put("medicationName", medicationName);
        userMedData.put("expirationDate", expirationDate);
        userMedData.put("totalPills", totalPills);

        // Saves to userMedications database
        userMedRef.set(userMedData).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "User Medication added:" + medicationName + "to" + userId);
            Intent intent = new Intent(AddMedication.this, MedicationManager2.class);
            // Shows the new medication added in medication manager (FIXME: this doesn't persist and goes away as soon as you restart)
            intent.putExtra("medicationName", medicationName);
            intent.putExtra("EXPIRE_DATE", expirationDate);
            intent.putExtra("TOTAL_PILLS", totalPills);
            startActivity(intent);
        }).addOnFailureListener(e -> Log.e("Firestore", "Error saving user-medication", e));

    }
}
