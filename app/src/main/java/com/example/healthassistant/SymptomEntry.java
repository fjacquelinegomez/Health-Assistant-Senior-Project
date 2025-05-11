package com.example.healthassistant;

public class SymptomEntry {

    public String date;
    public String symptom;
    public int rating;

    public SymptomEntry(String date, String symptom, int rating) {
        this.date = date;
        this.symptom = symptom;
        this.rating = rating;
    }
}
