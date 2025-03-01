package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.MultiFactor;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText logInEmail, logInPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Back button logic
        ImageButton backbutton = findViewById(R.id.back_button);
        backbutton.setOnClickListener(v -> finish());

        // Redirect to the sign-up page if the user doesn't have an account yet
        TextView signUpRedirectButton = findViewById(R.id.signUpRedirect);
        signUpRedirectButton.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Initialize FirebaseAuth and authentication fields
        auth = FirebaseAuth.getInstance();
        logInEmail = findViewById(R.id.logInEmailAddress);
        logInPassword = findViewById(R.id.logInPassword);

        // Logic for login button click
        Button login_btn = findViewById(R.id.logInButton);
        login_btn.setOnClickListener(v -> {
            String email = logInEmail.getText().toString();
            String password = logInPassword.getText().toString();

            // Validate email and password
            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!password.isEmpty()) {
                    // Attempt login with email and password
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> handleLogin(authResult.getUser()))
                            .addOnFailureListener(e -> {
                                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                Log.e("LoginError", "Login failed: ", e);
                            });
                } else {
                    logInPassword.setError("Password cannot be empty.");
                }
            } else if (email.isEmpty()) {
                logInEmail.setError("Email cannot be empty");
            } else {
                logInEmail.setError("Please enter a valid email.");
            }
        });
    }

    private void handleLogin(FirebaseUser user) {
        if (user == null) {
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if MFA is enabled for the user
        MultiFactor multiFactor = user.getMultiFactor();
        if (!multiFactor.getEnrolledFactors().isEmpty()) {
            // MFA is enabled, retrieve MFA session
            multiFactor.getSession()
                    .addOnSuccessListener(multiFactorSession -> {
                        // Redirect to MFA screen
                        Intent intent = new Intent(LoginActivity.this, Login_MFA.class);
                        intent.putExtra("SESSION_ID", multiFactorSession);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MFA", "Failed to get MFA session: " + e.getMessage());
                        Toast.makeText(this, "MFA session error. Try again later.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // MFA not enabled, proceed to the Home screen
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, Homescreen.class));
            finish();
        }
    }
}