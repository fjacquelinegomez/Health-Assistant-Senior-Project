package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sleep_logs);

        editSleepLogs = findViewById(R.id.sleep_log_tester);
        Button buttonSaveSleepLog = findViewById(R.id.idBtnSendData);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null){
            String uid = user.getUid();
            databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("sleep_time");
            buttonSaveSleepLog.setOnClickListener(v -> saveSleepLog());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button back = findViewById(R.id.sleepLogsBackButton);
        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.sleepLogsBackButton) {
                    startActivity(new Intent(SleepLogs.this, Homescreen.class));
                }
            }
        };
        back.setOnClickListener(buttonClickListener);
    }

        private void saveSleepLog() {
            String sleepLog = editSleepLogs.getText().toString().trim();

            if (TextUtils.isEmpty(sleepLog)){
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String logId = databaseRef.push().getKey();
            LogSleep logsleep = new LogSleep(sleepLog);

            if (logId != null){
                databaseRef.child(logId).setValue(logsleep)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(SleepLogs.this, "Sleep log saved!", Toast.LENGTH_SHORT).show();
                                editSleepLogs.setText("");  // Clear input fields
                            } else {
                                Toast.makeText(SleepLogs.this, "Failed to save log", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }


}