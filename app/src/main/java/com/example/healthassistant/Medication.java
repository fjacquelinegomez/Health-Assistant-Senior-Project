package com.example.healthassistant;

import android.os.Build;

import com.google.firebase.firestore.DocumentReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Medication {
    private String Id;
    private String rxcui; // Unique identifier for the medication
    private String name;
    private DocumentReference medicationRef; // Reference to the medication document
    private String medicationForm;
    private int dosageAmount;
    private String frequency;
    private String medicationTime;
    private String expirationDate;
    private int pillsTaken;
    private int totalPills;
    private String additionalNotes;
    private Map<String, Boolean> takenToday;
    /*
    private String expireDate;
    private int totalPills;
    private int dosesTaken;
    private String dosageForm;
    */

    // Default constructor (needed for Firebase)
    public Medication() {}

    // Constructor
    //public Medication(String name, String expireDate, int totalPills, int dosesTaken, String dosageForm, String key) {
    public Medication(String name, String medicationForm, String medicationTime, String expirationDate, int pillsTaken, int totalPills, String Id, Map<String, Boolean> takenToday) {
        this.name = name;
        this.medicationForm = medicationForm;
        this.medicationTime = medicationTime;
        this.expirationDate = expirationDate;
        this.pillsTaken = pillsTaken;
        this.totalPills = totalPills;
        this.Id = Id;
        this.takenToday = takenToday;
    }

    // Getter Methods
    public String getName() { return name; }
    public String getRxcui() { return rxcui; }
    public String getId() { return Id; }
    public DocumentReference getMedicationRef() { return medicationRef; }
    public String getMedicationForm() { return medicationForm; }
    public int getDosageAmount() { return dosageAmount; }
    public String getFrequency() { return frequency; }
    public String getMedicationTime() { return medicationTime; }
    public String getExpirationDate() { return expirationDate; }
    public String getFormattedExpirationDate() {
        SimpleDateFormat formattedDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        return formattedDate.format(expirationDate);
    }
    public int getPillsTaken() { return pillsTaken; }
    public int getTotalPills() { return totalPills; }
    public String getAdditionalNotes() { return additionalNotes; }
    public Map<String, Boolean> getTakenToday() { return takenToday; }


    // Setter Methods
    public void setName(String name) { this.name = name; }
    public void setMedicationForm(String medicationForm) { this.medicationForm = medicationForm; }
    public void setDosageAmount(int dosageAmount) { this.dosageAmount = dosageAmount; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setMedicationTime(String medicationTime) { this.medicationTime = medicationTime; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    public void setPillsTaken(int pillsTaken) {this.pillsTaken = pillsTaken; }
    public void setTotalPills(int totalPills) { this.totalPills = totalPills; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    public void setId(String Id) { this.Id = Id; }
    public void setTakenToday(Map<String, Boolean> takenToday) { this.takenToday = takenToday; }


    // Method that calculates if the medication is expiring soon, returns true if it's about to expire in 7 days
    public boolean isExpiringSoon(String expirationDate) {
        // Matches the date format to the way the expiration date is stored on Firestore
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        // .parse requires us to catch the exception btw
        try {
            // Finds how much time is between the expiration date and the current date
            Date formattedExpirationDate = sdf.parse(expirationDate);
            Date currentDate = new Date();
            long diffInMillis = formattedExpirationDate.getTime() - currentDate.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            // If the date is less than or equal to 7 days, it's expiring soon (or already is) returns true (is expiring)
            return diffInDays <= 7;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false; // If date can't be parsed
    }

    // Method that calculates if the medication needs to be refilled soon, returns true if user has taken at least 80% of total pills
    public boolean isRefillNeeded(int totalPills, int pillsTaken) {
        if (totalPills == 0) { return false; } // so it won't divide by 0
        double takenPercentage = (double) pillsTaken / totalPills;
        return takenPercentage >= 0.8;
    }

    // Method that checks if the medication time is coming up (15 min before)
    public boolean isTimeToTake(String medicationTime) {
        try {
            // Matches the date format to the way the expiration date is stored on Firestore
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            // Grabs the current time and formats it to HH:mm format
            Date currentTime = new Date();
            String currentTimeString = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime);
            // Parses and converts formatted times to Date object
            Date currentTime_Date = sdf.parse(currentTimeString);
            Date medicationTime_Date = sdf.parse(medicationTime);

            // Finds the difference between medication time and current time (in milliseconds)
            long diffInMillis = medicationTime_Date.getTime() - currentTime_Date.getTime();
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis); // Converts to minutes

            // Returns true if current time is within 15 minutes of medicationTime
            return diffInMinutes >= 0 && diffInMinutes <= 15;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}