package com.example.healthassistant;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SleepLogs extends AppCompatActivity {
    private EditText editSleepLogs;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_logs);

        editSleepLogs = findViewById(R.id.sleep_log_tester);
        View buttonSaveSleepLog = findViewById(R.id.idBtnSendData);
        TableLayout tableLayout = findViewById(R.id.tableLayout);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Check if the user is signed in
        if (user != null) {
            String uid = user.getUid(); // Get the user UID from the Firebase Auth database
            databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("sleep_time");
            buttonSaveSleepLog.setOnClickListener(v -> saveSleepLog());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Adding a sample sleep log row
        TableRow tableRow = new TableRow(this);

        TextView dateText = new TextView(this);
        dateText.setText("02/12/2024");
        dateText.setPadding(20, 8, 8, 8);

        TextView hoursSleptText = new TextView(this);
        hoursSleptText.setText("7.5 hrs");
        hoursSleptText.setPadding(20, 8, 8, 8);

        TextView sleepQualityText = new TextView(this);
        sleepQualityText.setText("9");
        sleepQualityText.setPadding(20, 8, 8, 8);

        tableRow.addView(dateText);
        tableRow.addView(hoursSleptText);
        tableRow.addView(sleepQualityText);
        tableLayout.addView(tableRow);


        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setpin), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });
    }

    private void saveSleepLog() {
        // Logic for when the user submits their sleep time, saving it in Firebase Realtime Database
        String sleepLog = editSleepLogs.getText().toString().trim();

        if (TextUtils.isEmpty(sleepLog)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String logId = databaseRef.push().getKey();
        LogSleep logsleep = new LogSleep(sleepLog); // Ensure LogSleep is correctly defined elsewhere

        if (logId != null) {
            databaseRef.child(logId).setValue(logsleep)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SleepLogs.this, "Sleep log saved!", Toast.LENGTH_SHORT).show();
                            editSleepLogs.setText(""); // Clear input field
                        } else {
                            Toast.makeText(SleepLogs.this, "Failed to save log", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
