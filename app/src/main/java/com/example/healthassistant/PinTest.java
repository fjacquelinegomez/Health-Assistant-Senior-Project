package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


public class PinTest extends AppCompatActivity {

    private String storedPin = "";
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.pin_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setpin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button test = findViewById(R.id.button3);
        Button mybutton = (Button) findViewById(R.id.FoodREB);
        mybutton.setOnClickListener(v -> {
            addRestrict();
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Check if the user is signed in
        if (user != null) {
            String uid = user.getUid(); // Get the user UID from the Firebase Auth database
            databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            test.setOnClickListener(v -> checkPin());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
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
                        Toast.makeText(PinTest.this, "Pin Matched!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PinTest.this, Homescreen.class));
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
    private void addRestrict() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && user.getUid() != null) {
            // Access Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Get the value of "FoodRe" from the EditText
            EditText adder2 = findViewById(R.id.setter2);
            String foodRe = adder2.getText().toString().trim();

            // Debug the value of foodRe
            Log.d("AddFoodRe", "FoodRe value: " + foodRe);

            if (!foodRe.isEmpty()) {
                // Create a map to store the "FoodRe" data

                // Save the data in Firestore under the user's UID
                databaseRef.child("Refood").child(foodRe).setValue("Restricted")
                        .addOnSuccessListener(aVoid -> {
                            Log.d("AddFoodRe", "FoodRe saved successfully");
                            Toast.makeText(this, "FoodRe saved successfully! " + user.getUid(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("AddFoodRe", "Error saving FoodRe: ", e);
                            Toast.makeText(this, "Error saving FoodRe", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Please enter a valid value for FoodRe", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("AddFoodRe", "User is not authenticated or UID is null");
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
    }



}
