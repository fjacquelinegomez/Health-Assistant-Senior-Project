package com.example.healthassistant;

public class Medication {
    private String name;
    private String expirationDate;
    private int totalPills;

    public Medication(String name, String expirationDate, int totalPills) {
        this.name = name;
        this.expirationDate = expirationDate;
        this.totalPills = totalPills;}

    public String getName() {
        return name;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public int getTotalPills() {
        return totalPills;
    }

}
