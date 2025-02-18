package com.example.healthassistant;

public class LogSleep {
    private String sleepLog;

    // Default constructor (required by Firebase)
    public LogSleep() {
    }

    // Constructor to initialize sleep log
    public LogSleep(String sleepLog) {
        this.sleepLog = sleepLog;
    }

    // Getter and Setter for Firebase to use
    public String getSleepLog() {
        return sleepLog;
    }

    public void setSleepLog(String sleepLog) {
        this.sleepLog = sleepLog;
    }
}
