package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthMultiFactorException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.MultiFactorInfo;
import com.google.firebase.auth.MultiFactorResolver;
import com.google.firebase.auth.PhoneMultiFactorInfo;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    public static MultiFactorResolver multiFactorResolver; // Store resolver statically

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        auth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.logInEmailAddress);
        passwordEditText = findViewById(R.id.logInPassword);
        loginButton = findViewById(R.id.logInButton);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // Sign in with email and password as the first factor
            signInWithEmailAndPassword(email, password);
        });
    }

    private void signInWithEmailAndPassword(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, Homescreen.class));
                            finish();
                        }
                        return;
                    }

                    // Check if Multi-Factor Authentication is required
                    if (task.getException() instanceof FirebaseAuthMultiFactorException) {
                        FirebaseAuthMultiFactorException e = (FirebaseAuthMultiFactorException) task.getException();
                        multiFactorResolver = e.getResolver(); // Store resolver statically

                        MultiFactorInfo selectedHint = multiFactorResolver.getHints().get(0);
                        if (selectedHint instanceof PhoneMultiFactorInfo) {
                            Intent intent = new Intent(LoginActivity.this, MFAVerifyLogin.class);
                            intent.putExtra("selectedHint", selectedHint.getUid()); // Only pass Hint ID
                            startActivity(intent);
                        } else {
                            Toast.makeText(LoginActivity.this, "Unsupported second factor", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}