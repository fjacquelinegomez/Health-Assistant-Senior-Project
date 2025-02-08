package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Settings extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        Button back = findViewById(R.id.settingsBackButton);
        Button logOut = findViewById(R.id.logOutButton);

        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.settingsBackButton) {
                    startActivity(new Intent(Settings.this, Homescreen.class));
                } else if (v.getId() == R.id.logOutButton) {
                    mAuth.signOut();
                    Toast.makeText(Settings.this, "Log Out Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.this, MainActivity.class));
                    finish();
                }
            }
        };
        back.setOnClickListener(buttonClickListener);
        logOut.setOnClickListener(buttonClickListener);
    }
}