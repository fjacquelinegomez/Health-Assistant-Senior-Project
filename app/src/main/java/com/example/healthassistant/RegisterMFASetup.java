package com.example.healthassistant;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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

public class RegisterMFASetup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_mfasetup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //setup
        final Button setupButton = findViewById(R.id.mfaSetupButton);
//        setupButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                startActivity(new Intent(RegisterMFASetup.this, RegisterMFASetup2.class));
//                finish();
//            }
//        });
        setupButton.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                user.reload().addOnCompleteListener(task -> { // Ensure latest verification status
                    if (user.isEmailVerified()) {
                        // Proceed to MFA Setup
                        Intent intent = new Intent(RegisterMFASetup.this, RegisterMFASetup2.class);
                        startActivity(intent);
                    } else {
                        // Show AlertDialog if email is not verified
                        showEmailNotVerifiedDialog();
                    }
                });
            }
        });

        //skip
        final Button skipButton = findViewById(R.id.mfaSkipButton);
        skipButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(RegisterMFASetup.this, ProfileCustomization.class));
                finish();
            }
        });
    }
    private void showEmailNotVerifiedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Email Verification Required")
                .setMessage("You need to verify your email before setting up MFA. Please check your email for the verification link.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Resend Email", (dialog, which) -> {
                    resendVerificationEmail();
                    dialog.dismiss();
                })
                .show();
    }
    private void resendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to send verification email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}