package com.example.healthassistant;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddMedication extends AppCompatActivity {
    // Initializes firebase variables
    private FirebaseFirestore database;
    private FirebaseAuth user;

    // Initializes spinner selection variables
    private String selectedMedicationForm;
    private String selectedFrequencyCount;
    private String selectedFrequencyUnit;

    // Initializes date/time picker selection variables
    private EditText expireInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        // Mostly just UI component initialization (it's messy sorry ;-;)
        // Cancel button logic
        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> finish());

        // Spinner setup for different inputs and also handles the user selections
        // Medication form
        Spinner medicationFormSpinner = findViewById(R.id.formInput);
        String[] medicationForms = {"Tablet", "Capsule", "Liquid", "Injection", "Inhaler"};
        setupSpinner(medicationFormSpinner, medicationForms, "medicationForm");

        // Frequency count
        Spinner frequencyCountSpinner = findViewById(R.id.frequencyCountInput);
        String[] frequencyCounts = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        setupSpinner(frequencyCountSpinner, frequencyCounts, "frequencyCount");

        // Frequency unit
        Spinner frequencyUnitSpinner = findViewById(R.id.frequencyUnitInput);
        String[] frequencyUnits = {"every X hours", "per day", "per week", "as needed"};
        setupSpinner(frequencyUnitSpinner, frequencyUnits, "frequencyUnit");

        // Sets up DatePicker input component
        // https://developer.android.com/develop/ui/views/components/pickers
        expireInput = findViewById(R.id.expireInput);
        expireInput.setOnClickListener(v -> {
            // Puts the calendar to the current date
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH); // Zero based (0 = January...)
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Shows the date picker
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddMedication.this,
                    // listens to users date selection
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Formats the date to MM/dd/yyyy
                        String formattedDate = formatDate(selectedYear, selectedMonth, selectedDay);
                        expireInput.setText(formattedDate);
                    },
                    year, month, day // Sets the current day as the default expiration date
            );
            // Displays the date picker
            datePickerDialog.show();
        });

        // Initializes Firebase instances
        database = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();

        // Sets the medication name text of the selected medication
        String medicationName = getIntent().getStringExtra("medicationName");
        TextView medicationNameTextView = findViewById(R.id.medicationNameText);
        medicationNameTextView.setText(medicationName);

        // Receives the RxCUI from the last activity
        String rxcui = getIntent().getStringExtra("rxcui");

        // Initializes other UI components
        EditText dosageInput = findViewById(R.id.dosageInput);
        EditText totalPillsInput = findViewById(R.id.countInput);
        EditText notesInput = findViewById(R.id.notesInput);

        // Edit medication logic (reusing the add medication code for this)
        String medicationId = getIntent().getStringExtra("medicationId");
        // This checks if the userMed Id being passed in exists or not
        // If it does exist the user is editing a med, if not then they're adding a med
        if (medicationId != null) {
            Log.e("editMedication", "Medication Id: " + medicationId);
            DocumentReference medRef = database.collection("userMedications").document(medicationId);
            medRef.get().addOnSuccessListener(document -> {
                // Populating the fields to what the user saved to before
                expireInput.setText(document.getString("expirationDate"));
                notesInput.setText(document.getString("additionalNotes"));
                // Have to convert the fields we stored as int to a string
                dosageInput.setText(String.valueOf(document.getLong("dosageAmount")));
                totalPillsInput.setText(String.valueOf(document.getLong("totalPills")));
                // Setting the spinners to the correct positions
                setSpinnerSelection(medicationFormSpinner, document.getString("medicationForm"));
                setSpinnerSelection(frequencyCountSpinner, String.valueOf(document.getLong("frequencyCount")));
                setSpinnerSelection(frequencyUnitSpinner, document.getString("frequencyUnit"));
            });
        }

        Button saveMedicationButton = findViewById(R.id.saveMedicationButton);
        // Save button logic (when editing)
        saveMedicationButton.setOnClickListener(v -> {
            // Grabs user's input
            String medicationForm  = selectedMedicationForm;
            String dosageAmountText = dosageInput.getText().toString();
            String frequencyCountText = selectedFrequencyCount;
            String frequencyUnit = selectedFrequencyUnit;
            String expirationDate = expireInput.getText().toString();
            String totalPillsText = totalPillsInput.getText().toString();
            String additionalNotes = notesInput.getText().toString();

            // FIXME: Handle empty fields

            // Converts to int to make it legible in the database
            int dosageAmount = Integer.parseInt(dosageAmountText);
            int frequencyCount = Integer.parseInt(frequencyCountText);
            int totalPills = Integer.parseInt(totalPillsText);

            // Creates a hashmap of the users new edited values
            Map<String, Object> userMedData = new HashMap<>();
            userMedData.put("medicationForm", medicationForm);
            userMedData.put("dosageAmount", dosageAmount);
            userMedData.put("frequencyCount", frequencyCount);
            userMedData.put("frequencyUnit", frequencyUnit);
            userMedData.put("expirationDate", expirationDate);
            userMedData.put("totalPills", totalPills);
            userMedData.put("additionalNotes", additionalNotes);

            // Saves new changes to the userMed document
            database.collection("userMedications").document(medicationId).update(userMedData);

            // Reroutes user to the current medications page (showing new changes)
            Intent intent = new Intent(this, MedicationManager2.class);
            this.startActivity(intent);
        });

        Button addMedicationButton = findViewById(R.id.addMedicationButton);
        // The logic for when the user clicks the "Add Medication" Button after filling in fields
        // Should save their medication to their database and medication manager
        addMedicationButton.setOnClickListener(v -> {
            // Grabs user's input
            String medicationForm  = selectedMedicationForm;
            String dosageAmountText = dosageInput.getText().toString();
            String frequencyCountText = selectedFrequencyCount;
            String frequencyUnit = selectedFrequencyUnit;
            String expirationDate = expireInput.getText().toString();
            String totalPillsText = totalPillsInput.getText().toString();
            String additionalNotes = notesInput.getText().toString();

            // FIXME: Handle empty fields

            // Converts to int to make it legible in the database
            int dosageAmount = Integer.parseInt(dosageAmountText);
            int frequencyCount = Integer.parseInt(frequencyCountText);
            int totalPills = Integer.parseInt(totalPillsText);

            // Grabs the user's unique ID
            String userId = user.getCurrentUser().getUid();

            // Save medication to medication manager and links it to the user adding it
            // Saves medication itself to the medications database
            saveMedicationToFirestore(medicationName, rxcui);

            // Save medication (along with other inputted details) to the user's medication database
            saveUserMedication(rxcui, medicationName, userId, medicationForm, frequencyCount, frequencyUnit, dosageAmount, expirationDate, totalPills, additionalNotes);
        });
    }

    // Saves added medication to firestore
    private void saveMedicationToFirestore(String medicationName, String rxcui) {
        Log.d("Firestore", "Hello");
        // Creates a document referebce in the medications database using the rxcui as the entry identifier
        DocumentReference medRef = database.collection("medications").document(rxcui);

        // Checks if the medication already exists in the medications database
        medRef.get().addOnCompleteListener(task -> {
           if (!task.getResult().exists()) {
               // If medication doesn't exist in the medication database yet, adds to medication database then userMedications database
               // Creates new medication document
               Map<String, Object> medicationData = new HashMap<>();
               medicationData.put("RxCUI", rxcui);
               medicationData.put("Name", medicationName);

               // Saves to medications database
               medRef.set(medicationData).addOnSuccessListener(aVoid -> {
                   Log.d("Firestore", "Medication added: " + medicationName);
               }).addOnFailureListener(e -> Log.e("Firestore", "Error adding medication", e));
           }
        });
    }

    // References added medication to the user
    private void saveUserMedication(String rxcui, String medicationName, String userId, String medicationForm, int frequencyCount,
                                    String frequencyUnit, int dosageAmount, String expirationDate, int totalPills, String additionalNotes) {
        // Creates a document reference in the userMedications database using the userId-rxcui as the entry identifier
        DocumentReference userMedRef = database.collection("userMedications").document(userId + "_" + rxcui);

        // Creates a document reference to the medication the user is trying to save (keeps it modular)
        DocumentReference medRef = database.collection("medications").document(rxcui);

        // Creates userMedication hashmap with their inputted values
        Map<String, Object> userMedData = new HashMap<>();
        userMedData.put("userID", userId);
        userMedData.put("medicationRef", medRef);
        userMedData.put("medicationForm", medicationForm);
        userMedData.put("dosageAmount", dosageAmount);
        userMedData.put("frequencyCount", frequencyCount);
        userMedData.put("frequencyUnit", frequencyUnit);
        userMedData.put("expirationDate", expirationDate);
        userMedData.put("totalPills", totalPills);
        userMedData.put("additionalNotes", additionalNotes);

        // Saves to userMedications database
        userMedRef.set(userMedData).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "User Medication added:" + medicationName + "to" + userId);
            // Shows the new medication added in medication manager
            Intent intent = new Intent(AddMedication.this, MedicationManager2.class);
            startActivity(intent);
        }).addOnFailureListener(e -> Log.e("Firestore", "Error saving user-medication", e));

    }

    // Helper function that sets up the spinner/dropdown options (cleans up redundant code)
    // https://www.geeksforgeeks.org/spinner-in-android-with-example/
    private void setupSpinner(Spinner spinner, String[] choices, String spinnerType) {
        // Sets up spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, choices);
        spinner.setAdapter(adapter);

        // Handles selection for each spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerType == "medicationForm") {
                    selectedMedicationForm = parent.getItemAtPosition(position).toString();
                } else if (spinnerType == "frequencyCount") {
                    selectedFrequencyCount = parent.getItemAtPosition(position).toString();
                } else if (spinnerType == "frequencyUnit") {
                    selectedFrequencyUnit = parent.getItemAtPosition(position).toString();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Does nothing if nothing is selected
            }
        });
    }

    // Helper function to set the spinner selection from what the user previously selected (this is for edit med)
    private void setSpinnerSelection(Spinner spinner, String selection) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        int position = adapter.getPosition(selection);
        spinner.setSelection(position);
    }

    // Helper function to format date to MM/dd/yyyy
    private String formatDate(int year, int month, int day) {
        // Creates calendar instance of user's selection
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, day);

        // Defines date format to MM/dd/yyyy
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        return dateFormat.format(selectedDate.getTime()); // uses .getTime() to return it as a Date object rather than a calendar object
    }
}
