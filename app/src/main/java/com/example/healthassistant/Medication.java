package com.example.healthassistant;

import com.google.firebase.firestore.DocumentReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Medication {
    private String Id;
    private String rxcui; // Unique identifier for the medication
    private String name;
    private DocumentReference medicationRef; // Reference to the medication document
    private String medicationForm;
    private int dosageAmount;
    private String frequency;
    private String expirationDate;
    private int pillsTaken;
    private int totalPills;
    private String additionalNotes;
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
    public Medication(String name, String medicationForm, String expirationDate, int pillsTaken, int totalPills, String Id) {
        this.name = name;
        this.medicationForm = medicationForm;
        this.expirationDate = expirationDate;
        this.pillsTaken = pillsTaken;
        this.totalPills = totalPills;
        this.Id = Id;
    }

    // Getter Methods
    public String getName() { return name; }
    public String getRxcui() { return rxcui; }
    public String getId() { return Id; }
    public DocumentReference getMedicationRef() { return medicationRef; }
    public String getMedicationForm() { return medicationForm; }
    public int getDosageAmount() { return dosageAmount; }
    public String getFrequency() { return frequency; }
    public String getExpirationDate() { return expirationDate; }
    public String getFormattedExpirationDate() {
        SimpleDateFormat formattedDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        return formattedDate.format(expirationDate);
    }
    public int getPillsTaken() { return pillsTaken; }
    public int getTotalPills() { return totalPills; }
    public String getAdditionalNotes() { return additionalNotes; }

    // Setter Methods
    public void setName(String name) { this.name = name; }
    public void setMedicationForm(String medicationForm) { this.medicationForm = medicationForm; }
    public void setDosageAmount(int dosageAmount) { this.dosageAmount = dosageAmount; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    public void setPillsTaken(int pillsTaken) {this.pillsTaken = pillsTaken; }
    public void setTotalPills(int totalPills) { this.totalPills = totalPills; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

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

    // Method that calculates if the medication needs to be refilled soon, returns true if there's only 10 pills left
    // FIXME: maybe change it to 80% of total pills instead?
    public boolean isRefillNeeded(int totalPills, int pillsTaken) {
        int pillsLeft = totalPills - pillsTaken;
        return pillsLeft <= 10;
    }
}