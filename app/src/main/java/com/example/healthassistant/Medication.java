package com.example.healthassistant;

import android.os.Build;

import com.google.firebase.firestore.DocumentReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Medication {
    private String rxcui; // Unique identifier for the medication
    private String name;
    private String userId;
    //private DocumentReference medicationRef; // Reference to the medication document
    private String medicationForm;
    private int frequencyCount;
    private String frequencyUnit;
    private String medicationTime;
    private List<String> alternateNames;
    private int dosageAmount;
    private String expirationDate;
    private int pillsTaken;
    private int totalPills;
    private String additionalNotes;
    private Map<String, Boolean> takenToday;
    private String type;
    private String tty;

    // Default constructor (needed for Firebase)
    public Medication() {}
    // Constructor of all attributes
    public Medication(String rxcui, String name, String userId, String medicationForm, int frequencyCount, String frequencyUnit,
                      String medicationTime, int dosageAmount, String expirationDate, int pillsTaken, int totalPills, String additionalNotes, Map<String, Boolean> takenToday) {
        this.rxcui = rxcui;
        this.name = name;
        this.userId = userId;
        this.medicationForm = medicationForm;
        this.frequencyCount = frequencyCount;
        this.frequencyUnit = frequencyUnit;
        this.medicationTime = medicationTime;
        this.dosageAmount = dosageAmount;
        this.expirationDate = expirationDate;
        this.pillsTaken = pillsTaken;
        this.totalPills = totalPills;
        this.additionalNotes = additionalNotes;
        this.takenToday = takenToday;
    }

    // Constructor with parameters that uses UI components
    public Medication(String name, String userId, String medicationForm, String medicationTime, String expirationDate, int pillsTaken, int totalPills, Map<String, Boolean> takenToday) {
        this.name = name;
        this.userId = userId;
        this.medicationForm = medicationForm;
        this.medicationTime = medicationTime;
        this.expirationDate = expirationDate;
        this.pillsTaken = pillsTaken;
        this.totalPills = totalPills;
        this.takenToday = takenToday;
    }

    // Constructor of strictly medication information (whatever will go in the medication Firestore collection)
    public Medication(String rxcui, String name) {
        this.rxcui = rxcui;
        this.name = name;
    }

    // Constructor of user's medication information (whatever will go in the userMedication Firestore collection)
    public Medication(String rxcui, String name, String userId, String medicationForm, int frequencyCount, String frequencyUnit,
                      String medicationTime, int dosageAmount, String expirationDate, int totalPills, String additionalNotes) {
        this.rxcui = rxcui;
        this.name = name;
        this.userId = userId;
        this.medicationForm = medicationForm;
        this.frequencyCount = frequencyCount;
        this.frequencyUnit = frequencyUnit;
        this.medicationTime = medicationTime;
        this.dosageAmount = dosageAmount;
        this.expirationDate = expirationDate;
        this.totalPills = totalPills;
        this.additionalNotes = additionalNotes;
    }



    // Getter and Setter Methods
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getAlternateNames() { return alternateNames; }
    private void setAlternateNames() { this.alternateNames = alternateNames; }

    public String getRxcui() { return rxcui; }
    public void setRxcui(String rxcui) { this.rxcui = rxcui; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    //public DocumentReference getMedicationRef() { return medicationRef; }
    //public void setMedicationRef(DocumentReference medicationRef) { this.medicationRef = medicationRef; }

    public int getFrequencyCount() { return frequencyCount; }
    public void setFrequencyCount(int frequencyCount) { this.frequencyCount = frequencyCount; }

    public String getFrequencyUnit() { return frequencyUnit; }
    public void setFrequencyUnit(String frequencyUnit) { this.frequencyUnit = frequencyUnit; }

    public String getMedicationForm() { return medicationForm; }
    public void setMedicationForm(String medicationForm) { this.medicationForm = medicationForm; }

    public int getDosageAmount() { return dosageAmount; }
    public void setDosageAmount(int dosageAmount) { this.dosageAmount = dosageAmount; }

    public String getMedicationTime() { return medicationTime; }
    public void setMedicationTime(String medicationTime) { this.medicationTime = medicationTime; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    // formats the expiration date to be MM/dd/yyyy
    public String getFormattedExpirationDate() {
        SimpleDateFormat formattedDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        return formattedDate.format(expirationDate);
    }

    public int getPillsTaken() { return pillsTaken; }
    public void setPillsTaken(int pillsTaken) {this.pillsTaken = pillsTaken; }

    public int getTotalPills() { return totalPills; }
    public void setTotalPills(int totalPills) { this.totalPills = totalPills; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public Map<String, Boolean> getTakenToday() { return takenToday; }
    public void setTakenToday(Map<String, Boolean> takenToday) { this.takenToday = takenToday; }

    public String getType() {return type;}
    public void setType(String type) {this.type = type;}

    public String getTty() {return tty;}
    public void setTty(String tty) {this.tty = tty;}


    // Helper methods
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