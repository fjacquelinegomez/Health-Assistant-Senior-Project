package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import java.util.HashMap;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity2 extends AppCompatActivity {

    private EditText fullname, username;
    private Button btnSaveNames;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register2);

        //logic so the name and username are stored in the realtimedatabase
        fullname = findViewById(R.id.fullNameTextBox);
        username = findViewById(R.id.usernameTextbox);
        btnSaveNames = findViewById(R.id.signUpButton);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // checks if the user is signed in
        if (user != null){
            String uid = user.getUid(); // captures the user UID from the auth. database
            databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            btnSaveNames.setOnClickListener(v -> saveNames());

            //nNEW
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Inform the user to check their email and verify
                            Log.d("MFA", "Verification email sent.");
                        } else {
                            Log.e("MFA", "Failed to send verification email.");
                        }
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        //back button logic
        ImageButton backbutton = findViewById(R.id.back_button);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                finish();
            }
        });


//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }


    //method for capturing the data from the input fields (new encrypted version)
    private void saveNames(){
        String uName = fullname.getText().toString().trim();
        String uUsername = username.getText().toString().trim();

        if (TextUtils.isEmpty(uName) || (TextUtils.isEmpty(uUsername))){
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }


        // Encrypt the user input before saving, (new)
        String encryptedName = EncryptionUtils.encrypt(uName);
        String encryptedUsername = EncryptionUtils.encrypt(uUsername);

        // Data structure to save name and username (encryption new)
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("fullName", encryptedName);
        userData.put("username", encryptedUsername);

        // Save data to Firebase
        databaseRef.setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Data saved securely! 🔐", Toast.LENGTH_SHORT).show();

                    // Navigate to Home Screen after data is saved
                    //Intent intent = new Intent(RegisterActivity2.this, ProfileCustomization.class);
                    Intent intent = new Intent(RegisterActivity2.this, RegisterMFASetup.class);
                    startActivity(intent);
                    finish();  // Close RegisterActivity2
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }



    }



