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
import com.google.firebase.auth.FirebaseUser;

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

        // Logic for the back button
        ImageButton back_btn = findViewById(R.id.backButton);
        back_btn.setOnClickListener(v -> finish());

        // Redirect users to the login page if they already have an account
        TextView logInRedirectButton = findViewById(R.id.logInRedirect);
        logInRedirectButton.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        // Initializing registration/authentication fields
        auth = FirebaseAuth.getInstance();
        emailAddress = findViewById(R.id.signUpEmailAddress);
        password = findViewById(R.id.signUpPassword);

        // Logic for once the user presses the next button (sign up)
        Button next_btn = findViewById(R.id.nextButton);
        next_btn.setOnClickListener(v -> {
            // Takes in email address and password
            String user = emailAddress.getText().toString().trim();
            String pass = password.getText().toString().trim();

            // Fields can't be empty
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
                            Toast.makeText(RegisterActivity.this, "Registration Successful. Please check your inbox to verify your email.", Toast.LENGTH_SHORT).show();

                            FirebaseUser user = task.getResult().getUser();

                            if (user != null) {
                                // Send email verification
                                user.sendEmailVerification().addOnCompleteListener(emailVerificationTask -> {
                                    if (emailVerificationTask.isSuccessful()) {
                                        // Inform user to verify email
                                        Toast.makeText(RegisterActivity.this, "Verification email sent. Please verify your email to continue.", Toast.LENGTH_LONG).show();

                                        // Redirect user to the login screen or inform them to check their inbox
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        // Error while sending verification email
                                        Toast.makeText(RegisterActivity.this, "Error sending verification email: " + emailVerificationTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("RegisterError", "Registration Failed: ", task.getException());
                        }
                    }
                });
            }
        });
    }
}