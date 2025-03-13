package com.example.healthassistant;

public class Medication {
    private String name;
    private String expireDate;
    private int totalPills;
    private int dosesTaken;
    private String dosageForm;
    private String key;  // Unique key for each medication (Firestore document ID)
    private String rxcui; // Unique identifier for the medication

    // Default constructor (needed for Firebase)
    public Medication() {}

    // Constructor
    //public Medication(String name, String expireDate, int totalPills, int dosesTaken, String dosageForm, String key) {
    public Medication(String name, String expireDate, int totalPills, String key) {
        this.name = name;
        this.expireDate = expireDate;
        this.totalPills = totalPills;
//        this.dosesTaken = dosesTaken;
//        this.dosageForm = dosageForm;
        this.key = key;
    }

    // Getter and setter for key
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    // Getter Methods
    public String getName() {
        return name;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public int getTotalPills() {
        return totalPills;
    }

//    public int getDosesTaken() {
//        return dosesTaken;
//    }
//
//    public String getDosageForm() {
//        return dosageForm;
//    }

    public String getRxcui() {
        return rxcui;
    }

    // Setter Methods
    public void setName(String name) {
        this.name = name;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public void setTotalPills(int totalPills) {
        this.totalPills = totalPills;
    }

    public void setDosesTaken(int dosesTaken) {
        this.dosesTaken = dosesTaken;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public void setRxcui(String rxcui) {
        this.rxcui = rxcui;
    }
}