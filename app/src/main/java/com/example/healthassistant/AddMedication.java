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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import medication.OpenFDAApiResponse;
import medication.OpenFDAApiService;
import medication.RetrofitService;
import medication.RxNormApiNameResponse;
import medication.RxNormApiResponse;
import medication.RxNormApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
            Log.d("MedicationDebug", "Add medication button works");

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
            Log.d("MedicationDebug", "User ID in add medication: " + userId);

            // Save medication to medication manager and links it to the user adding it
            // Saves medication itself to the medications database
            Medication medication = new Medication(rxcui, medicationName);
            saveMedicationToFirestore(medication);
            Log.d("MedicationDebug", "Saved medication to firestore");

            // checks added medication for drug interaction and adds medication to user's medication database
            // If there is an interaction then a warning pops up and user can choose how to proceed
            Medication userMedication = new Medication(rxcui, medicationName, userId, medicationForm, frequencyCount, frequencyUnit, medicationTime, dosageAmount, expirationDate, totalPills, additionalNotes);
            DocumentReference medicationReference = database.collection("medications").document(rxcui);
            checkInteractions(userMedication, medicationReference);
            Log.d("MedicationDebug", "Saved user medication to firestore");

            // Save medication (along with other inputted details) to the user's medication database
            //saveUserMedication(userMedication);
        });
    }

    // Saves added medication to firestore
    private void saveMedicationToFirestore(Medication medication) {
        // Creates a document reference in the medications database using the rxcui as the entry identifier
        DocumentReference medRef = database.collection("medications").document(medication.getRxcui());

        // Checks if the medication already exists in the medications database
        medRef.get().addOnCompleteListener(task -> {
           if (task.isSuccessful() && !task.getResult().exists()) {
               // If medication doesn't exist in the medication database yet, adds to medication database then userMedications database

               fetchAllNames(medication.getRxcui(), altNames -> {
                   // Creates new medication document
                   Map<String, Object> medicationData = new HashMap<>();
                   medicationData.put("RxCUI", medication.getRxcui());
                   medicationData.put("Name", medication.getName());
                   medicationData.put("AlternateNames", altNames != null ? altNames : "Unknown");

               // Saves to medications database
               medRef.set(medicationData)
                       .addOnSuccessListener(aVoid -> {
                           Log.d("Firestore", "Medication added: " + medication.getName());
                       })
                       .addOnFailureListener(e -> {
                           Log.e("Firestore", "Error adding medication", e);
                       });
               });
           }
        });
    }

    // References added medication to the user
    private void saveUserMedication(Medication userMedication) {
        // Creates a document reference in the userMedications database using the userId-rxcui as the entry identifier
        DocumentReference userMedRef = database.collection("userMedications").document(userMedication.getUserId() + "_" + userMedication.getRxcui());
        Log.d("MedicationDebug", "User ID in save user medication function: " + userMedication.getUserId());

        // Creates a document reference to the medication the user is trying to save (keeps it modular)
        DocumentReference medRef = database.collection("medications").document(userMedication.getRxcui());

        // Creates userMedication hashmap with their inputted values
        Map<String, Object> userMedData = new HashMap<>();
        userMedData.put("userID", userMedication.getUserId());
        userMedData.put("medicationRef", medRef);
        userMedData.put("medicationForm", userMedication.getMedicationForm());
        userMedData.put("dosageAmount", userMedication.getDosageAmount());
        userMedData.put("frequencyCount", userMedication.getFrequencyCount());
        userMedData.put("frequencyUnit", userMedication.getFrequencyUnit());
        userMedData.put("medicationTime", userMedication.getMedicationTime());
        userMedData.put("expirationDate", userMedication.getExpirationDate());
        userMedData.put("pillsTaken", 0);
        userMedData.put("totalPills", userMedication.getTotalPills());
        userMedData.put("additionalNotes", userMedication.getAdditionalNotes());

        // Creates a log of whether or not user has taken a medication
        String today = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
        Map<String, Object> takenTodayMap = new HashMap<>();
        takenTodayMap.put(today, false);
        userMedData.put("takenToday", takenTodayMap);


        // Saves to userMedications database
        userMedRef.set(userMedData).addOnSuccessListener(aVoid -> {
            // Shows the new medication added in medication manager
            Intent intent = new Intent(AddMedication.this, MedicationManager2.class);
            Log.d("MedicationDebug", "user medication save successful");
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

    // Defines what to do with the list of names after fetching asynchronously
    public interface AllNameCallback {
        void onAllNamesFetched(List names);
    }

    // Fetches all the possible names of the medication (for drug interaction purposes)
    private void fetchAllNames(String rxcui, final AllNameCallback callback) {
        // Creates Retrofit instance of RxNorm api + opens a request to the endpoint
        RxNormApiService apiService = RetrofitService.getRxNormClient().create(RxNormApiService.class);
        Call<RxNormApiNameResponse> call = apiService.fetchAllNames(rxcui);

        // Performs request asynchronously
        call.enqueue(new Callback<RxNormApiNameResponse>() {
            @Override
            public void onResponse(Call<RxNormApiNameResponse> call, Response<RxNormApiNameResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Gets the properties section of the response
                    RxNormApiNameResponse.Properties properties = response.body().getProperties();

                    // List that will hold all the possible drug names RxNorm provides + adds to the list if it exists
                    List<String> names = new ArrayList<>();
                    if (properties.getName() != null) {
                        names.add(properties.getName());
                    }
                    if (properties.getSynonym() != null && !properties.getSynonym().equals(properties.getName())) {
                        names.add(properties.getSynonym());
                    }

                    // Returns list of medication names
                    callback.onAllNamesFetched(names);
                } else {
                    // If no name is found then it's marked as Unknown
                    callback.onAllNamesFetched(Collections.singletonList("Unknown"));
                }
        }
            @Override
            // If the request fails
            public void onFailure(Call<RxNormApiNameResponse> call, Throwable t) { callback.onAllNamesFetched(Collections.singletonList("Unknown")); }
        });
    }

    // Checks for medication interactions
    private void checkInteractions(Medication userMedication, DocumentReference medicationReference) {
        Log.d("MedicationDebug", "In Check interaction");
        // Gets the current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();
        Log.d("MedicationDebug", "User Id in check interactions: " + currentUserId);

        // Grabs the name + alternate names of the medication being added and adds to a list of names to check
        medicationReference.get().addOnSuccessListener(userMedicationDoc -> {
            // Grabs name + alternate names
            String addedMedicationName = userMedicationDoc.getString("Name");
            List<String> addedAlternateNames = (List<String>) userMedicationDoc.get("AlternateNames");

            // Adds names + altername names if it exists
            List<String> namesToCheck = new ArrayList<>();
            if (addedMedicationName != null) {
                namesToCheck.add(addedMedicationName);
            }
            if (addedAlternateNames != null) {
                namesToCheck.addAll(addedAlternateNames);
            }

            List<String> activeIngredients = new ArrayList<>();
            for (String name : namesToCheck) {
                activeIngredients.addAll(extractActiveIngredients(name));
            }

        // Creates reference to the userMedications collection
        CollectionReference userMedicationRef = database.collection("userMedications");

        // Pulls the user's current medications from Firestore
        userMedicationRef.whereEqualTo("userID", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Will hold all the stored medications it interacts with (no duplicates)
                        Set<String> interactingMedications = new HashSet<>();

                        // Variables that ensures async task is done before proceeding
                        int totalDocs = task.getResult().size();
                        Log.d("MedicationDebug", "Total Docs: " + totalDocs);
                        AtomicInteger asyncTaskCounter = new AtomicInteger(0);

                        // Checks if the user has saved a medication before
                        // If they haven't then it skips the interaction check
                        if (totalDocs == 0) {
                            Log.d("MedicationDebug", "No user medications saved yet, skip interaction check");
                            saveUserMedication(userMedication);
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Pulls more information from the actual medication (medication name and interactions)
                            DocumentReference medRef = document.getDocumentReference("medicationRef");
                            medRef.get().addOnCompleteListener(medSnapshot -> {
                                if (medSnapshot.isSuccessful()) {
                                    DocumentSnapshot medDoc = medSnapshot.getResult();
                                    // Grabs the information of the medication being checked against
                                    String medName = medDoc.getString("Name");
                                    //List<String> interactions = (List<String>) medDoc.get("interactions");

                                    // Fetches warnings about medicaiton from openFDA which should have interactions mentioned theorhetically
                                    fetchWarnings(medName, 1, new OnWarningsFetchedListener() {
                                        @Override
                                        public void onWarningsFetched(List<String> warnings) {
                                            if (warnings != null && !warnings.isEmpty()) {
                                                // Goes through each warning
                                                for (String warning : warnings) {
                                                    Log.d("MedicationDebug", "Warning: " + warning);
                                                    // Goes through the ingredients / name of the medication being added
                                                    for (String ingredient : activeIngredients) {
                                                        //Log.d("InteractionWarning", "Ingredient: " + ingredient);
                                                        // Checks if ingredient/name is found in the warning
                                                        if (warning.toLowerCase().contains(ingredient.toLowerCase())) {
                                                            Log.d("MedicationDebug", "interaction found: " + medName);
                                                            interactingMedications.add(medName);
                                                        }
                                                    }
                                                }
                                            }
                                            // Ensures async task is done before proceeding
                                            if (asyncTaskCounter.incrementAndGet() == totalDocs) {
                                                if (!interactingMedications.isEmpty()) {
                                                    // shows interaction warning dialog
                                                    Log.d("MedicationDebug", "Interaction found");
                                                    showInteractionWarning(interactingMedications, userMedication);
                                                } else {
                                                    // saves the user's medication like normal
                                                    Log.d("MedicationDebug", "Interaction not found");
                                                    saveUserMedication(userMedication);
                                                }
                                            }
                                        }
                                    });
                                    /*
                                    // Goes through the medication's interactions
                                    if (interactions != null) {
                                        for (String interaction : interactions) {
                                            for (String nameToCheck : namesToCheck) {
                                                // Adds the medication to the list of interacting medications if interaction found
                                                if (nameToCheck.toLowerCase().contains(interaction.toLowerCase())) {
                                                    interactingMedications.add(medName);
                                                }
                                            }
                                        }
                                    }
                                     */
                                }
                            });
                        }
                    }
                });
        });
    }

    // Displays the interaction warning so users can choose how to proceed
    private void showInteractionWarning(Set<String> interactingMedications, Medication userMedication) {
        // Builds the warning message
        StringBuilder message = new StringBuilder(userMedication.getName() + " interacts with the following medication(s) in your Medication Manager: ");
        // Will tell the user which medication the medication they're adding interacts with
        int i = 0;
        int interactingMedicationsSize = interactingMedications.size();
        for (String med: interactingMedications) {
            message.append(med);
            if (i == interactingMedicationsSize - 1) {
                message.append(". ");
            } else {
                message.append(", ");
            }
            i++;
        }
        message.append("Please consult a healthcare professional or use caution. Do you still wish to proceed?");

        // shows the actual alert and lets the user interact with it
        new AlertDialog.Builder(this)
                .setTitle("Medication Interaction Warning")
                .setMessage(message.toString())
                .setPositiveButton("Add", (dialog, which) -> {
                    // if user decides to add anyways it saves the medication
                    saveUserMedication(userMedication);
                    dialog.dismiss();
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    // if user decides to back out it takes them back to the page
                    dialog.dismiss();
                })
                .show();
    }

    // Defines what to do with the warnings after fetching asynchronously
    public interface OnWarningsFetchedListener {
        void onWarningsFetched(List<String> warnings);
    }

    // Fetches warning about medication from OpenFDA Api
    public void fetchWarnings(String medicationName, int limit, final OnWarningsFetchedListener listener) {
        // Creates instance of OpenFDA api + opens a request to the endpoint
        OpenFDAApiService openFDAApiService = RetrofitService.getOpenFDAClient().create(OpenFDAApiService.class);
        Call<OpenFDAApiResponse> call = openFDAApiService.getDrugWarnings(medicationName, limit);

        // Requests warning from openFDA asynchronously
        call.enqueue(new retrofit2.Callback<OpenFDAApiResponse>() {
            @Override
            public void onResponse(Call<OpenFDAApiResponse> call, retrofit2.Response<OpenFDAApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Will return the list of warnings that openFDA provides
                    List<String> warnings = new ArrayList<>();
                    for (OpenFDAApiResponse.DrugLabel drugLabel : response.body().getResults()) {
                        warnings.addAll(drugLabel.getWarnings());
                    }
                    listener.onWarningsFetched(warnings);
                } else {
                    listener.onWarningsFetched(null); // In case there's no warning for that medication
                }
            }

            @Override
            // In case the Api request fails
            public void onFailure(Call<OpenFDAApiResponse> call, Throwable t) {
                listener.onWarningsFetched(null);
            }
        });
    }

    // Extracts active ingredients from the medication names
    private List<String> extractActiveIngredients(String medicationName) {
        List<String> ingredients = new ArrayList<>();

        // List of terms to ignore during extraction
        List<String> ignoreTerms = Arrays.asList("MG", "Tablet", "Oral", "Caplet", "Caplets", "Capsule", "mg", "tablet", "oral", "Triple", "and", "Strength", "Reliever", "Extra", "Action", "PM");

        if (medicationName != null) {
            // Parses information if it runs into a space, slash, or comma
            String[] parts = medicationName.split("[/ ,]");
            for (String part : parts) {
                // Filters out ignore words I defined + no numbers
                if (part.matches("[a-zA-Z]+") && !ignoreTerms.contains(part)) {
                    ingredients.add(part.trim());
                }
            }
        }
        return ingredients;
    }


}
