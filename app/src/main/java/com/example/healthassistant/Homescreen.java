package com.example.healthassistant;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.healthassistant.databinding.ActivityHomescreenBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import food.RecipeResponse;
import food.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Homescreen extends AppCompatActivity {
    //used for bottom bar
    ActivityHomescreenBinding binding;

    // Initializing variables for notifications
    private FirebaseFirestore database;
    private NotificationHelper notificationHelper;
    private static int notificationCounter = 0;

    private TextView greeting;

    private TextView recipeTitle;
    private ImageView recipeImage;


    private List<String> avoidIngredients = new ArrayList<>();
    private List<String> allergies = new ArrayList<>();
    private List<String> conditions = new ArrayList<>();
    private List<String> preferences = new ArrayList<>();
    private List<String> medications = new ArrayList<>();

    private boolean allergiesLoaded = false;
    private boolean conditionsLoaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //bottom bar
        binding = ActivityHomescreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialize the greeting text at the top so it displays the user's first name
        greeting = findViewById(R.id.nameText);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(Homescreen.this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(Homescreen.this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(Homescreen.this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(Homescreen.this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(Homescreen.this, MedicationManager.class));
                    break;
            }
            return false;
        });

        // Bind views inside your CardView
        recipeTitle = findViewById(R.id.recipeTitle);
        recipeImage = findViewById(R.id.recipeImage);

        // Start loading user data
        loadUserHealthInfo();

        //apointments button
        ImageButton buttonAppoint = (ImageButton) findViewById(R.id.appointmentReminderButton);
        buttonAppoint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Homescreen.this, Appointments.class));
            }
        });

        //sleep tracker button
        ImageButton buttonSleep = findViewById(R.id.sleepLogsButton);
        buttonSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ButtonClick", "Sleep Logs button clicked");
                startActivity(new Intent(Homescreen.this, SleepLogs2.class));
            }


        });

        //symptoms tracker button
        ImageButton buttonSymp = findViewById(R.id.sympLogsButton);
        buttonSymp.setOnClickListener(v -> {
            Log.d("ButtonClick", "Symptoms Logs button clicked");
            startActivity(new Intent(Homescreen.this, SymptomLogs.class));
        });



        Button test = findViewById(R.id.pintest);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Homescreen.this, PinTest.class));
            }

        });

        // Firebase logic to retrieve and display the user's first name
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String fullName = snapshot.child("fullName").getValue(String.class);
                        String firstName = "friend"; // default fallback

                        if (fullName != null && !fullName.isEmpty()) {
                            try {
                                String decryptedFullName = EncryptionUtils.decrypt(fullName);
                                if (decryptedFullName != null && !decryptedFullName.isEmpty()) {
                                    String[] parts = decryptedFullName.split(" ");
                                    if (parts.length > 0) {
                                        firstName = parts[0];
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("Homescreen", "Failed to decrypt or parse full name", e);
                            }
                        }
                        greeting.setText(firstName + "!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Homescreen.this, "Failed to load name", Toast.LENGTH_SHORT).show();
                }
            });
        }


        //settings button
        ImageButton buttonSettings = findViewById(R.id.userProfileButton);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Homescreen.this, Settings.class));
            }
        });

        // Initializing Firestore and NotificationHelper
        database = FirebaseFirestore.getInstance();
        notificationHelper = new NotificationHelper(this);

        // Sends notifications to users if their medication is expiring or needs to be refilled (will only send if user clicks on homescreen tho)
        checkMedicationsExpiration();
        checkMedicationsRefill();
        checkMedicationTime();

        // Sets up medication UI component FIXME: turn this into a separate function
        // Updates the medication taken for the day log (makes sure it's on the current day)
        checkTakenToday();
        // Fetches user's medication list
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
                            String id = document.getId();
                            String expirationDate = document.getString("expirationDate");
                            int totalPills = document.getLong("totalPills").intValue();
                            int pillsTaken = document.getLong("pillsTaken").intValue();
                            String medicationForm = document.getString("medicationForm");
                            Map<String, Boolean> takenToday = (Map<String, Boolean>) document.get("takenToday");
                            DocumentReference medRef = document.getDocumentReference("medicationRef");

                            // Grabs and formats the medication time
                            String medicationTime = document.getString("medicationTime");
                            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault()); // input is 24 hour
                            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // output is 12 hour
                            String formattedTime = "";
                            try {
                                Date time = inputFormat.parse(medicationTime); // Parse the string into a Date
                                formattedTime = outputFormat.format(time); // Formats medication time to 12 hour
                            } catch (ParseException e) {
                                e.printStackTrace(); // Handle parse exception / error
                            }

                            // Pulls the medication name from the medication reference
                            String finalFormattedTime = formattedTime;
                            medRef.get().addOnSuccessListener(medSnapshot -> {
                                if (medSnapshot.exists()) {
                                    String medicationName = medSnapshot.getString("Name");
                                    // creates a new medication object with all the desired values then adds it to the main medication list
                                    Medication medication = new Medication();
                                    medication.setUserId(id);
                                    medication.setName(medicationName);
                                    medication.setExpirationDate(expirationDate);
                                    medication.setTotalPills(totalPills);
                                    medication.setPillsTaken(pillsTaken);
                                    medication.setMedicationForm(medicationForm);
                                    medication.setMedicationTime(finalFormattedTime);
                                    medication.setTakenToday(takenToday);
                                    medicationList.add(medication);
                                }
                                completedFetches[0]++;

                                // sets up the time + expired + refill medication UI with the new filtered lists
                                setUpMedicationView(medicationList, "time");

                            }).addOnFailureListener(e -> {
                                Log.e("Medication Manager", "Error fetching medication", e);
                            });
                        }
                    }
                });
    }

    // Goes through the user's current medications and check if they're close to expiring (7 days)
    // Sends a notification if it is close to expiring
    private void checkMedicationsExpiration() {
        // Gets the current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();

        // Creates reference to the userMedications collection
        CollectionReference userMedicationRef = database.collection("userMedications");

        // Pulls the user's current medications from Firestore
        //database.collection("userMedications")
        userMedicationRef.whereEqualTo("userID", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Loops through all of the user's current medications
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String expirationDate = document.getString("expirationDate");
                            DocumentReference medRef = document.getDocumentReference("medicationRef");

                            // Create a Medication object
                            Medication medication = new Medication();
                            medication.setExpirationDate(expirationDate);

                            // Checks if current medication is expiring soon
                            if (medication.isExpiringSoon(expirationDate)) {
                                // Grabs the name of the medication
                                medRef.get().addOnSuccessListener(medSnapshot -> {
                                    if (medSnapshot.exists()) {
                                        String medicationName = medSnapshot.getString("Name");
                                        String message = "Your medication " + medicationName + " is expiring soon!";

                                        // Checks if user turned on notification permissions
                                        if (notificationHelper.checkNotificationPermission(Homescreen.this, 101)) {
                                            // Sends the expiration notification
                                            int notificationId = notificationCounter++; // Gives each expired medication a unique notif Id so notifs don't replace old ones
                                            notificationHelper.showNotification("Medication Expiration Reminder", message, notificationId);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    // Goes through the user's current medications and check if they're close to needing a refill (10 pills left)
    private void checkMedicationsRefill() {
        // Gets the current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();

        // Creates reference to the userMedications collection
        CollectionReference userMedicationRef = database.collection("userMedications");

        // Pulls the user's current medications from Firestore
        //database.collection("userMedications")
        userMedicationRef.whereEqualTo("userID", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Loops through all of the user's current medications
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Grabs values from the database
                            int totalPills = document.getLong("totalPills").intValue();
                            int pillsTaken = document.getLong("pillsTaken").intValue();
                            DocumentReference medRef = document.getDocumentReference("medicationRef");

                            // Create a Medication object
                            Medication medication = new Medication();
                            medication.setTotalPills(totalPills);
                            medication.setPillsTaken(pillsTaken);

                            // Checks if current medication needs to be refilled soon
                            if (medication.isRefillNeeded(totalPills, pillsTaken)) {
                                // Grabs the name of the medication
                                medRef.get().addOnSuccessListener(medSnapshot -> {
                                    if (medSnapshot.exists()) {
                                        String medicationName = medSnapshot.getString("Name");
                                        String message = "Your medication " + medicationName + " needs to be refilled soon!";

                                        // Checks if user turned on notification permissions
                                        if (notificationHelper.checkNotificationPermission(Homescreen.this, 101)) {
                                            // Sends the refill notification
                                            int notificationId = notificationCounter++; // Gives each expired medication a unique notif Id so notifs don't replace old ones
                                            notificationHelper.showNotification("Medication Refill Reminder", message, notificationId);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    // Goes through the user's current medications and check if they're close to their medication-time (15 min before and right on time)
    private void checkMedicationTime() {
        // Gets the current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();

        // Creates reference to the userMedications collection
        CollectionReference userMedicationRef = database.collection("userMedications");

        // Pulls the user's current medications from Firestore
        //database.collection("userMedications")
        userMedicationRef.whereEqualTo("userID", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Loops through all of the user's current medications
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Grabs values from the database
                            String medicationTime = document.getString("medicationTime");
                            DocumentReference medRef = document.getDocumentReference("medicationRef");

                            // Create a Medication object
                            Medication medication = new Medication();
                            medication.setMedicationTime(medicationTime);
                            Log.d("medicationNotif", "hi");

                            // Checks if current medication needs to be taken soon (within 15 minutes)
                            if (medication.isTimeToTake(medicationTime)) {
                                Log.d("medicationNotif", "hi");
                                // Grabs the name of the medication
                                medRef.get().addOnSuccessListener(medSnapshot -> {
                                    if (medSnapshot.exists()) {
                                        String medicationName = medSnapshot.getString("Name");
                                        String message = "Your medication " + medicationName + " needs to be taken soon!";

                                        // Checks if user turned on notification permissions
                                        if (notificationHelper.checkNotificationPermission(Homescreen.this, 101)) {
                                            // Sends the medication-time notification
                                            int notificationId = notificationCounter++; // Gives each medication a unique notif Id so notifs don't replace old ones
                                            notificationHelper.showNotification("Medication Reminder", message, notificationId);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    // checks if the user's taken today map is linked to the current day (today)
    private void checkTakenToday() {
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
                        // Loops through all of the user's current medications
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String today = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date()); // grabs today's daate
                            Map<String, Boolean> takenToday = (Map<String, Boolean>) document.get("takenToday"); // grabs the date stored

                            // if takenToday doesn't exist yet
                            if (takenToday == null) {
                                takenToday = new HashMap<>();
                            }

                            // If today's entry isn't there yet
                            if (!takenToday.containsKey(today)) {
                                takenToday.put(today, false); // assumes not taken
                                document.getReference().update("takenToday", takenToday); // updates takenToday with current date
                            }
                        }
                    }
                });
    }
    // Sets up the expiration + refill UI and enables arrow functionality
    private void setUpMedicationView(List<Medication> medications, String view) {
        // Initializing the arrows
        ImageView leftArrow;
        ImageView rightArrow;

        if (Objects.equals(view, "time")) {
            // Initializing the left and right medication time arrows
            leftArrow = findViewById((R.id.timeLeftButton));
            rightArrow = findViewById((R.id.timeRightButton));
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
        if (Objects.equals(view, "time")) {
            // Initializing values being changed (name and medication time)
            TextView medicationName = findViewById(R.id.timeMedicationName);
            TextView medicationTime = findViewById(R.id.timeMedicationTime);

            // Sets values to the new medication values
            Medication medication = medications.get(index);
            medicationName.setText(medication.getName());
            medicationTime.setText(medication.getMedicationTime());

            // Updates if medication has been taken in firestore + UI
            // Increments/decrements pill tracking also
            Button takenButton = findViewById(R.id.takenButton);
            // Grabs the current taken status of the medication
            String today = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
            Map<String, Boolean> takenToday = medication.getTakenToday();
            // Taken button logic
            takenButton.setOnClickListener(v -> {
                // Switches taken status to the opposite and updates medication model
                // If currently true (taken), switches to false (not taken)
                boolean hasTaken = takenToday.getOrDefault(today, false);
                boolean newHasTaken = !hasTaken;
                takenToday.put(today, newHasTaken);
                medication.setTakenToday(takenToday);

                // Updates changes in firestore
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                DocumentReference userMedicationRef = database.collection("userMedications").document(medication.getUserId());
                userMedicationRef.update("takenToday", takenToday);

                if (!hasTaken) {
                    // If taken then button changes to green and increments medication
                    takenButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Homescreen.this, R.color.normal_green)));
                    int newPillsTaken = medication.getPillsTaken() + 1;
                    medication.setPillsTaken(newPillsTaken);
                    userMedicationRef.update("pillsTaken", newPillsTaken);
                } else {
                    // If not taken then button reverts back to white and decrements medication
                    takenButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Homescreen.this, R.color.white)));
                    int newPillsTaken = medication.getPillsTaken() - 1;
                    medication.setPillsTaken(newPillsTaken);
                    userMedicationRef.update("pillsTaken", newPillsTaken);
                }
            });

            // Determines the taken status of medication when it first loads
            boolean initialHasTaken = takenToday.getOrDefault(today, false);
            if (initialHasTaken) {
                takenButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Homescreen.this, R.color.normal_green)));
            } else {
                takenButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Homescreen.this, R.color.white)));
            }
        }
    }

    private void loadUserHealthInfo() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .collection("medicalHistory").document("allergies")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        allergies = (List<String>) doc.get("foodAllergies");
                    }
                    allergiesLoaded = true;
                    checkReady();
                });

        db.collection("users").document(userId)
                .collection("medicalHistory").document("conditions")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        conditions = (List<String>) doc.get("medicalConditions");
                    }
                    conditionsLoaded = true;
                    checkReady();
                });
    }

    private void checkReady() {
        if (allergiesLoaded && conditionsLoaded) {
            loadPreferences();
        }
    }

    private void loadPreferences() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference prefRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("Food_Preferences_PC");

        prefRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot entry : snapshot.getChildren()) {
                    Boolean checked = entry.getValue(Boolean.class);
                    if (checked != null && checked) {
                        preferences.add(entry.getKey());
                    }
                }
                fetchOneRecommendation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fetchOneRecommendation(); // still try
            }
        });
    }

    private void fetchOneRecommendation() {
        String diet = "balanced"; // or customize based on user data
        String include = preferences.size() > 0 ? preferences.get(0) : "";
        String exclude = String.join(",", allergies); // or include conditions/meds too

        RetrofitClient.getInstance().getSpoonacularService()
                .getRecipesWithExclusions("12f9cc92173944a8aec01fec5f79c485", 1, exclude, diet, include, "main course", "random")
                .enqueue(new Callback<RecipeResponse>() {
                    @Override
                    public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getResults().size() > 0) {
                            RecipeResponse.Recipe recipe = response.body().getResults().get(0);
                            recipeTitle.setText(recipe.getTitle());
                            recipeImage.setVisibility(ImageView.VISIBLE);
                            Glide.with(getApplicationContext()).load(recipe.getImage()).into(recipeImage);
                        } else {
                            recipeTitle.setText("No recipe found.");
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeResponse> call, Throwable t) {
                        recipeTitle.setText("Failed to load recipe.");
                    }
                });
    }
}



