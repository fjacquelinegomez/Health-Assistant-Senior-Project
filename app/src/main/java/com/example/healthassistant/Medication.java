package com.example.healthassistant;

public class Medication {
    private String name;
    private String expirationDate;
    private int totalPills;
    private String rxcui; // takes in the medication's unique identifier

    // Constructs medication
    public Medication(String name, String expirationDate, int totalPills) {
        this.name = name;
        this.expirationDate = expirationDate;
        this.totalPills = totalPills;}

    // Getters
    public String getName() {
        return name;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public int getTotalPills() {
        return totalPills;
    }
    public String getRxcui() { return rxcui; }

}
