package com.example.healthassistant;

import com.google.firebase.firestore.DocumentReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

}