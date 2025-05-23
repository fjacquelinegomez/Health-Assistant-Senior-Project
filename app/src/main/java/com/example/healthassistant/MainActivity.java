package com.example.healthassistant;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button login_btn;
    private Button register_btn;
    private String storedPin = "";
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        /*
        DatabaseReference myRef = database.getReference("message");
        myRef.setValue("HELLO TEST123!");
        myRef.setValue("This is me testing, testing, testing again. ");
        */

        //logic for once the user presses the login button
        login_btn = findViewById(R.id.login_button);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        //logic for once the user presses the register button
        register_btn = findViewById(R.id.register_button);
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Keeps the user signed in if they already have
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("AuthUser", "User is already signed in: " + currentUser.getEmail());
            String uid = currentUser.getUid();
            databaseRef = database.getReference("users").child(uid);

            databaseRef.child("pin").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    storedPin = dataSnapshot.getValue(String.class);
                    if (storedPin == null || storedPin.isEmpty()) {
                        startActivity(new Intent(MainActivity.this, Homescreen.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, PinTest.class));
                    }
                    finish(); // move finish inside here too
                }

                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Failed to retrieve PIN", Toast.LENGTH_SHORT).show();
                }
            });
        }


        /* Testing firestore connection
        FirebaseFirestore firestore_db = FirebaseFirestore.getInstance();
        firestore_db.collection("test")
                .add(new HashMap<String, Object>() {{
                    put("message", "Hello Firestore!");
                }})
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding document", e);
                });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        }); */
    }
}