package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText emailAddress, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //logic for once the user presses the back button
        ImageButton back_btn = findViewById(R.id.backButton);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                finish();
            }
        });

        // Logic that redirects users to the log in page if they already have an account
        TextView logInRedirectButton = findViewById(R.id.logInRedirect);
        logInRedirectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        // Initializing registration/authentication fields
        auth = FirebaseAuth.getInstance();
        emailAddress = findViewById(R.id.signUpEmailAddress);
        password = findViewById(R.id.signUpPassword);

        //logic for once the user presses the next button (signs up)
        Button next_btn = findViewById(R.id.nextButton);
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Takes in email address and password
                String user = emailAddress.getText().toString().trim();
                String pass = password.getText().toString().trim();

                // fields can't be empty
                if(user.isEmpty()) {
                    emailAddress.setError("Email cannot be empty.");
                }
                if(pass.isEmpty()) {
                    password.setError("Password cannot be empty.");
                } else {
                    // Firebase Authentication database adds account
                    // Checks if account exists already and if the fields are valid (must be an email and at least 5 characters long)
                    auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, RegisterActivity2.class));
                            } else {
                                Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.e("RegisterError", "Registration Failed: ", task.getException());
                            }
                        }
                    });
                }
            }
        });
    }
}