package com.example.healthassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
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

        ImageButton back = findViewById(R.id.settingsBackButton);
        Button logOut = findViewById(R.id.logOutButton);
        //profile customization
        TextView profileCustom = findViewById(R.id.clickablePC);


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
                else if (v.getId() == R.id.clickablePC) {
                    // Set the flag in SharedPreferences before starting the activity
//                    SharedPreferences sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPref.edit();
//                    editor.putBoolean("FROM_SETTINGS", true);
//                    editor.apply();

                    // Now start the ProfileCustomizationActivity
                    Intent intent = new Intent(Settings.this, ProfileCustomization.class);
                    intent.putExtra("FROM_SETTINGS", true); // Pass the flag
                    startActivity(intent);
                }
            }
        };
        back.setOnClickListener(buttonClickListener);
        logOut.setOnClickListener(buttonClickListener);
        profileCustom.setOnClickListener(buttonClickListener);
    }
}