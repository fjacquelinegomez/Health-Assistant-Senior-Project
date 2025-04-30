package com.example.healthassistant;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextThemeWrapper;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    private EditText timeInput;
    private String storedTime = "";
    private EditText totalPillsInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        // initializing buttons and other UI components
        Button saveMedicationButton = findViewById(R.id.saveMedicationButton);
        Button addMedicationButton = findViewById(R.id.addMedicationButton);
        Button deleteMedicationButton = findViewById(R.id.deleteMedicationButton);
        TextView pillsTakenText = findViewById(R.id.pillsTakenText);
        EditText pillsTakenInput = findViewById(R.id.pillsTakenInput);

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
            Calendar currentDay = Calendar.getInstance();
            int year = currentDay.get(Calendar.YEAR);
            int month = currentDay.get(Calendar.MONTH); // Zero based (0 = January...)
            int day = currentDay.get(Calendar.DAY_OF_MONTH);

            // Changes the date dialog picker to match the colors of the app
            ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(AddMedication.this, R.style.CustomDatePickerDialog);
            // Shows the date picker
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    //AddMedication.this,
                    contextThemeWrapper,
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

        // Sets up TimePicker input component
        timeInput = findViewById(R.id.timeInput);
        timeInput.setOnClickListener(view -> { // shows a timepicker when the button is clicked
            // Puts the clock to the current time
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            // Changes the time dialog picker to match the colors of the app
            ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(AddMedication.this, R.style.CustomTimePickerDialog);
            // Shows the time picker
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    contextThemeWrapper,
                    // listens to users time selection
                    (view1, selectedHour, selectedMinute) -> {
                        Log.d("medicationTime", "Inputted time: " + selectedHour + ":" + selectedMinute);
                        Calendar selectedTime = Calendar.getInstance();
                        selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                        selectedTime.set(Calendar.MINUTE, selectedMinute);
                        // Formats the time to hh:mm a when displayed
                        String displayTime = formatTime(selectedHour, selectedMinute, "12");
                        timeInput.setText(displayTime);
                        // Stores the time in 24 hour format (for easier computation)
                        String inputtedTime = formatTime(selectedHour, selectedMinute, "24");
                        storedTime = inputtedTime;
                    }, hour, minute, false);
            // Displays the time picker
            timePickerDialog.show();
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
        totalPillsInput = findViewById(R.id.countInput);
        EditText notesInput = findViewById(R.id.notesInput);

        // Edit medication logic (reusing the add medication code for this)
        String medicationId = getIntent().getStringExtra("medicationId");
        // This checks if the userMed Id being passed in exists or not
        // If it does exist the user is editing a med, if not then they're adding a med
        if (medicationId != null) {
            // Switches the buttons out, save and delete are only able to be accessed when editing
            saveMedicationButton.setVisibility(View.VISIBLE);
            deleteMedicationButton.setVisibility(View.VISIBLE);
            addMedicationButton.setVisibility(View.GONE);

            // Users can edit how many pills they've taken
            pillsTakenText.setVisibility(View.VISIBLE);
            pillsTakenInput.setVisibility(View.VISIBLE);

            DocumentReference userMedRef = database.collection("userMedications").document(medicationId);
            userMedRef.get().addOnSuccessListener(document -> {
                // Populating the fields to what the user saved to before
                try {
                    // Convert time from 24 hour to 12 hour
                    String formattedTime = DateFormat.format("hh:mm a", new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(document.getString("medicationTime"))).toString();
                    timeInput.setText(formattedTime);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                expireInput.setText(document.getString("expirationDate"));
                notesInput.setText(document.getString("additionalNotes"));
                // Have to convert the fields we stored as int to a string
                dosageInput.setText(String.valueOf(document.getLong("dosageAmount")));
                pillsTakenInput.setText(String.valueOf(document.getLong("pillsTaken")));
                totalPillsInput.setText(String.valueOf(document.getLong("totalPills")));
                // Setting the spinners to the correct positions
                setSpinnerSelection(medicationFormSpinner, document.getString("medicationForm"));
                setSpinnerSelection(frequencyCountSpinner, String.valueOf(document.getLong("frequencyCount")));
                setSpinnerSelection(frequencyUnitSpinner, document.getString("frequencyUnit"));

                // Grabs the medication name from the medication document referenced in userMed doc
                DocumentReference medRef = document.getDocumentReference("medicationRef");
                // Fetches the medication document being referenced
                if (medRef != null) {
                    medRef.get().addOnSuccessListener(medDocument ->{
                        if (medDocument.exists()) {
                            String medicationNameEdit = medDocument.getString("Name");
                            TextView medicationNameEditTextView = findViewById(R.id.medicationNameText);
                            medicationNameEditTextView.setText(medicationNameEdit);
                        }
                    });
                }
            });
        }
        // Save button logic (when users are editing their medication entry)
        saveMedicationButton.setOnClickListener(v -> {
            // Grabs user's input
            String medicationForm  = selectedMedicationForm;
            String dosageAmountText = dosageInput.getText().toString();
            String frequencyCountText = selectedFrequencyCount;
            String frequencyUnit = selectedFrequencyUnit;
            String medicationTime = storedTime;
            String expirationDate = expireInput.getText().toString();
            String pillsTakenNewText = pillsTakenInput.getText().toString();
            String totalPillsText = totalPillsInput.getText().toString();
            String additionalNotes = notesInput.getText().toString();

            // Checks if required fills are empty
            boolean valid = checkInput(medicationTime, expirationDate, totalPillsText);
            if (valid == false) {
                return;
            }

            // Converts to int to make it legible in the database
            int dosageAmount = Integer.parseInt(dosageAmountText);
            int frequencyCount = Integer.parseInt(frequencyCountText);
            int pillsTaken = Integer.parseInt(pillsTakenNewText);
            int totalPills = Integer.parseInt(totalPillsText);

            // Creates a hashmap of the users new edited values
            // FIXME: forgot that i could've done update instead of put...
            Map<String, Object> userMedData = new HashMap<>();
            userMedData.put("medicationForm", medicationForm);
            userMedData.put("dosageAmount", dosageAmount);
            userMedData.put("frequencyCount", frequencyCount);
            userMedData.put("frequencyUnit", frequencyUnit);
            userMedData.put("medicationTime", medicationTime);
            userMedData.put("expirationDate", expirationDate);
            userMedData.put("pillsTaken", pillsTaken);
            userMedData.put("totalPills", totalPills);
            userMedData.put("additionalNotes", additionalNotes);

            // Saves new changes to the userMed document
            database.collection("userMedications").document(medicationId).update(userMedData);

            // Reroutes user to the current medications page (showing new changes)
            Intent intent = new Intent(this, MedicationManager2.class);
            this.startActivity(intent);
        });

        // Delete button logic, will delete the medication from the user's medication manager (and database)
        deleteMedicationButton.setOnClickListener(v -> {
            database.collection("userMedications").document(medicationId).delete();
            Intent intent = new Intent(AddMedication.this, MedicationManager2.class);
            startActivity(intent);
        });

        // The logic for when the user clicks the "Add Medication" Button after filling in fields
        // Should save their medication to their database and medication manager
        addMedicationButton.setOnClickListener(v -> {
            // Grabs user's input
            String medicationForm  = selectedMedicationForm;
            String dosageAmountText = dosageInput.getText().toString();
            String frequencyCountText = selectedFrequencyCount;
            String frequencyUnit = selectedFrequencyUnit;
            String medicationTime = storedTime;
            String expirationDate = expireInput.getText().toString();
            String totalPillsText = totalPillsInput.getText().toString();
            String additionalNotes = notesInput.getText().toString();

            // Checks if required fills are empty
            boolean valid = checkInput(medicationTime, expirationDate, totalPillsText);
            if (valid == false) {
                return;
            }

            // Converts to int to make it legible in the database
            int dosageAmount = Integer.parseInt(dosageAmountText);
            int frequencyCount = Integer.parseInt(frequencyCountText);
            int totalPills = Integer.parseInt(totalPillsText);

            // Grabs the user's unique ID
            String userId = user.getCurrentUser().getUid();

            // Save medication to medication manager and links it to the user adding it
            // Saves medication itself to the medications database
            saveMedicationToFirestore(medicationName, rxcui);

            // checks added medication for drug interaction and adds medication to user's medication database
            // If there is an interaction then a warning pops up and user can choose how to proceed
            // FIXME: for now I hardcoded Aspirin to show that the dialogue works
            //checkInteractions(rxcui, medicationName, "Aspirin", userId, medicationForm, frequencyCount, frequencyUnit, medicationTime, dosageAmount, expirationDate, totalPills, additionalNotes);

            // Save medication (along with other inputted details) to the user's medication database
            saveUserMedication(rxcui, medicationName, userId, medicationForm, frequencyCount, frequencyUnit, medicationTime, dosageAmount, expirationDate, totalPills, additionalNotes);
        });
    }

    // Saves added medication to firestore
    private void saveMedicationToFirestore(String medicationName, String rxcui) {
        // Creates a document reference in the medications database using the rxcui as the entry identifier
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
    private void saveUserMedication(String rxcui, String medicationName, String userId, String medicationForm, int frequencyCount, String frequencyUnit,
                                    String medicationTime, int dosageAmount, String expirationDate, int totalPills, String additionalNotes) {
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
        userMedData.put("medicationTime", medicationTime);
        userMedData.put("expirationDate", expirationDate);
        userMedData.put("pillsTaken", 0);
        userMedData.put("totalPills", totalPills);
        userMedData.put("additionalNotes", additionalNotes);

        // Creates a log of whether or not user has taken a medication
        // FIXME: will only log the current day
        String today = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
        Map<String, Object> takenTodayMap = new HashMap<>();
        takenTodayMap.put(today, false);
        userMedData.put("takenToday", takenTodayMap);


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

    // Helper function to format time to either 12 or 24 hour format
    public String formatTime(int hour, int minute, String format) {
        // Creates time instance of user's selection
        Calendar selectedTime = Calendar.getInstance();
        selectedTime.set(Calendar.HOUR_OF_DAY, hour);
        selectedTime.set(Calendar.MINUTE, minute);

        String formattedTime = "";
        // Formats time based off indicated format
        if (format == "12") {
            formattedTime = DateFormat.format("hh:mm a", selectedTime).toString();
        } else if (format == "24") {
            formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        }

        // Returns formatted time
        return formattedTime;
    }

    // Checks if required fields are filled in
    private boolean checkInput(String medicationTime, String expirationDate, String totalpills) {
        // flag that updates if any one of the fields are empty
        boolean valid = true;

        // Checks if the following fields are empty and sets a lil required icon to indicate that it needs to be filled out
        if (medicationTime.isEmpty()) {
            timeInput.setError("Required");
            valid = false;
        }

        if (expirationDate.isEmpty()) {
            expireInput.setError("Required");
            valid = false;
        }

        if (totalpills.isEmpty()) {
            totalPillsInput.setError("Required");
            valid = false;
        }

        return valid;
    }

    // Checks for medication interactions
    private void checkInteractions(String rxcui, String medicationName, String genericName, String userId, String medicationForm, int frequencyCount, String frequencyUnit,
                                      String medicationTime, int dosageAmount, String expirationDate, int totalPills, String additionalNotes) { // FIXME: should prob have used medication object oops

        // Gets the current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();

        // Creates reference to the userMedications collection
        CollectionReference userMedicationRef = database.collection("userMedications");

        // Pulls the user's current medications from Firestore
        userMedicationRef.whereEqualTo("userID", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Will hold all the stored medications it interacts with
                        List<String> interactingMedications = new ArrayList<>();

                        // Variables that ensures async task is done before proceeding
                        int totalDocs = task.getResult().size();
                        AtomicInteger asyncTaskCounter = new AtomicInteger(0);

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Pulls more information from the actual medication (medication name and interactions)
                            DocumentReference medRef = document.getDocumentReference("medicationRef");
                            medRef.get().addOnCompleteListener(medSnapshot -> {
                                if (medSnapshot.isSuccessful()) {
                                    DocumentSnapshot medDoc = medSnapshot.getResult();
                                    String medName = medDoc.getString("Name");
                                    List<String> interactions = (List<String>) medDoc.get("interactions");
                                    Log.d("drugInteraction", interactions.get(0));

                                    // Adds to the list of medications it interacts with if it matches the interactions list
                                    // FIXME: not working ;-;
                                    if (interactions != null && interactions.contains(genericName)) {
                                        interactingMedications.add(medName);
                                        Log.d("drugInteraction", "added interaction");
                                    }
                                }
                            });
                        }

                        Log.d("drugInteraction", "running");
                        // Ensures async task is done before proceeding
                        if (asyncTaskCounter.incrementAndGet() == totalDocs) {
                            Log.d("drugInteraction", "inside");
                            if (!interactingMedications.isEmpty()) {
                                // shows interaction warning dialog
                                Log.d("drugInteraction", "interaction");
                                showInteractionWarning(interactingMedications, rxcui, medicationName, genericName, userId, medicationForm, frequencyCount, frequencyUnit, medicationTime, dosageAmount, expirationDate, totalPills, additionalNotes);
                            } else {
                                // saves the user's medication like normal
                                Log.d("drugInteraction", "no interaction");
                                saveUserMedication(rxcui, medicationName, userId, medicationForm, frequencyCount, frequencyUnit, medicationTime, dosageAmount, expirationDate, totalPills, additionalNotes);
                            }
                        }
                    }
                });
    }

    private void showInteractionWarning(List<String> interactingMedications, String rxcui, String medicationName, String genericName, String userId, String medicationForm, int frequencyCount, String frequencyUnit,
                                        String medicationTime, int dosageAmount, String expirationDate, int totalPills, String additionalNotes) {
        // Builds the warning message
        StringBuilder message = new StringBuilder(medicationName + " interacts with the following medication(s) in your Medication Manager: ");
        //for (String medName : interactingMedications) {
           // message.append(medName).append(", ");
        //}
        message.append("Please consult a healthcare professional or use caution. Do you still wish to proceed?");

        // shows the actual alert and lets the user interact with it
        new AlertDialog.Builder(this)
                .setTitle("Medication Interaction Warning")
                .setMessage(message.toString())
                .setPositiveButton("Add", (dialog, which) -> {
                    // if user decides to add anyways it saves the medication
                    saveUserMedication(rxcui, medicationName, userId, medicationForm, frequencyCount, frequencyUnit, medicationTime, dosageAmount, expirationDate, totalPills, additionalNotes);
                    dialog.dismiss();
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    // if user decides to back out it takes them back to the page
                    dialog.dismiss();
                })
                .show();
    }
}
