package com.example.healthassistant;

import static com.example.healthassistant.R.id.picture;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;



public class PinTest extends AppCompatActivity {

    private String storedPin = "";
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.pin_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button back = findViewById(R.id.searchBackButton);
        Button set = findViewById(R.id.button2);
        Button test = findViewById(R.id.button3);
        Button test2 = findViewById(picture);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        // Check if the user is signed in
        if (user != null) {
            String uid = user.getUid(); // Get the user UID from the Firebase Auth database
            databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            set.setOnClickListener(v -> setPin());
            test.setOnClickListener(v -> checkPin());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        back.setOnClickListener(v -> startActivity(new Intent(PinTest.this, Homescreen.class)));
        test2.setOnClickListener(v -> startActivity(new Intent(PinTest.this, BarcodeScannerActivity.class)));
    }
    private void setPin() {
        // Get user input from EditText
        EditText setter = findViewById(R.id.setter);
        TextView show = findViewById(R.id.textView9);
        String numberInput = setter.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(numberInput)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numberInput.length() == 4 && numberInput.matches("\\d+")) {
            storedPin = numberInput;
            show.setText("New Pin Set: " + storedPin);

            // Store pin in Firebase
            databaseRef.child("pin").setValue(hashPin(numberInput))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(PinTest.this, "New Pin saved!", Toast.LENGTH_SHORT).show();
                            setter.setText(""); // Clear input field
                        } else {
                            Toast.makeText(PinTest.this, "Failed to save Pin", Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            Toast.makeText(PinTest.this, "Pin must be four numbers", Toast.LENGTH_SHORT).show();
        }
    }
    private void checkPin() {
        // Retrieve and test stored PIN
        databaseRef.child("pin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Retrieve the stored PIN from Firebase
                storedPin = dataSnapshot.getValue(String.class);

                if (storedPin == null || storedPin.isEmpty()) {
                    Toast.makeText(PinTest.this, "No PIN found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If stored PIN is found, validate the entered PIN
                EditText tester = findViewById(R.id.editTextNumber2);
                TextView show2 = findViewById(R.id.textView10);
                String enteredPin = tester.getText().toString().trim();

                if (enteredPin.length() == 4 && enteredPin.matches("\\d+")) {
                    enteredPin = hashPin(enteredPin);
                    if (enteredPin.equals(storedPin)) {
                        show2.setText("PIN Matched");
                    } else {
                        show2.setText("Incorrect PIN! Try again.");
                    }
                } else {
                    show2.setText("Enter a valid 4-digit PIN!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PinTest.this, "Failed to retrieve PIN", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));

            // Ensure the hash has 64 characters (padding with leading zeros if needed)
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
