package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.MultiFactorAssertion;
import com.google.firebase.auth.MultiFactorSession;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneMultiFactorGenerator;  // Import the correct class

import java.util.concurrent.TimeUnit;

public class Login_MFA extends AppCompatActivity {
    private MultiFactorSession multiFactorSession;
    private String phoneNumber;
    private String verificationId;
    private EditText editTextCode;
    private Button btnVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_mfa);

        editTextCode = findViewById(R.id.editTextCode);
        btnVerify = findViewById(R.id.btnVerify);

        multiFactorSession = getIntent().getParcelableExtra("SESSION_ID");

        if (multiFactorSession != null) {
            sendVerificationCode();
        } else {
            Toast.makeText(this, "MFA session error.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnVerify.setOnClickListener(v -> {
            String code = editTextCode.getText().toString().trim();
            if (!code.isEmpty()) {
                verifyCode(code);
            }
        });
    }

    private void sendVerificationCode() {
        phoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder()
                .setPhoneNumber(phoneNumber)
                .setTimeout(30L, TimeUnit.SECONDS)
                .setMultiFactorSession(multiFactorSession)
                .setCallbacks(callbacks)
                .setActivity(this)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    completeMFA(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.e("MFA", "Verification failed: " + e.getMessage());
                    Toast.makeText(Login_MFA.this, "Verification failed.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    Login_MFA.this.verificationId = verificationId;
                    Toast.makeText(Login_MFA.this, "Code sent!", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifyCode(String verificationCode) {
        if (verificationId == null) {
            Toast.makeText(Login_MFA.this, "Verification ID is missing. Request a new code.", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, verificationCode);

        completeMFA(credential);
    }

    private void completeMFA(PhoneAuthCredential credential) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if MFA is already enrolled for the user
        if (!currentUser.getMultiFactor().getEnrolledFactors().isEmpty()) {
            Toast.makeText(Login_MFA.this, "MFA already set up.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Login_MFA.this, Homescreen.class));
            finish();
            return;
        }

        // If MFA is not set up, proceed to enroll the user
        MultiFactorAssertion multiFactorAssertion = PhoneMultiFactorGenerator.getAssertion(credential);

        currentUser.getMultiFactor()
                .enroll(multiFactorAssertion, "My Personal Phone")
                .addOnSuccessListener(aVoid -> {
                    // MFA successfully enrolled
                    Toast.makeText(Login_MFA.this, "MFA enrolled successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login_MFA.this, Homescreen.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Failed to enroll MFA
                    Log.e("MFA", "MFA enrollment failed: " + e.getMessage());
                    Toast.makeText(Login_MFA.this, "MFA enrollment failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }
}