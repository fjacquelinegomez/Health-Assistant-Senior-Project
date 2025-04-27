package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Settings extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private String storedPin;

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
        databaseRef = FirebaseDatabase.getInstance().getReference("users")
                .child(mAuth.getCurrentUser().getUid());

        ImageButton back = findViewById(R.id.settingsBackButton);
        Button logOut = findViewById(R.id.logOutButton);
        TextView profileCustom = findViewById(R.id.clickablePC);
        Button set = findViewById(R.id.Adder); // Button for setting PIN

        View.OnClickListener buttonClickListener = v -> {
            switch (v.getId()) {
                case R.id.settingsBackButton:
                    startActivity(new Intent(Settings.this, Homescreen.class));
                    break;
                case R.id.logOutButton:
                    mAuth.signOut();
                    Toast.makeText(Settings.this, "Log Out Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.this, MainActivity.class));
                    finish();
                    break;
                case R.id.clickablePC:
                    Intent intent = new Intent(Settings.this, ProfileCustomization.class);
                    intent.putExtra("FROM_SETTINGS", true);
                    startActivity(intent);
                    break;
            }
        };

        back.setOnClickListener(buttonClickListener);
        logOut.setOnClickListener(buttonClickListener);
        profileCustom.setOnClickListener(buttonClickListener);
        set.setOnClickListener(v -> setPin());
    }

    private void setPin() {
        EditText setter = findViewById(R.id.setter);
        TextView show = findViewById(R.id.textView2);
        String numberInput = setter.getText().toString().trim();

        if (TextUtils.isEmpty(numberInput)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numberInput.length() == 4 && numberInput.matches("\\d+")) {
            storedPin = numberInput;
            show.setText("New Pin Set: " + storedPin);

            databaseRef.child("pin").setValue(hashPin(numberInput))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Settings.this, "New Pin saved!", Toast.LENGTH_SHORT).show();
                            setter.setText("");
                        } else {
                            Toast.makeText(Settings.this, "Failed to save Pin", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(Settings.this, "Pin must be four numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));

            while (hexString.length() < 64) {
                hexString.insert(0, '0');
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
