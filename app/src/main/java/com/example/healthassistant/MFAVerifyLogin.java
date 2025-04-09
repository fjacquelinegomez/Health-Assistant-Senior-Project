package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.MultiFactorAssertion;
import com.google.firebase.auth.MultiFactorInfo;
import com.google.firebase.auth.MultiFactorResolver;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneMultiFactorGenerator;
import com.google.firebase.auth.PhoneMultiFactorInfo;

import java.util.concurrent.TimeUnit;

public class MFAVerifyLogin extends AppCompatActivity {
    private FirebaseAuth auth;
    private PinView pinFromUser;
    private String verificationId;
    private PhoneMultiFactorInfo selectedHint;
    private MultiFactorResolver multiFactorResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mfaverify);
//        setContentView(R.layout.activity_mfaverify_login);


        auth = FirebaseAuth.getInstance();
        pinFromUser = findViewById(R.id.pinView);
        Button verifyButton = findViewById(R.id.verifyCodeButton);

        // Retrieve MultiFactorResolver from static storage
        multiFactorResolver = LoginActivity.multiFactorResolver;
        if (multiFactorResolver == null) {
            Toast.makeText(this, "Error retrieving multi-factor session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get the selectedHint using UID
        String hintId = getIntent().getStringExtra("selectedHint");
        for (MultiFactorInfo info : multiFactorResolver.getHints()) {
            if (info.getUid().equals(hintId) && info instanceof PhoneMultiFactorInfo) {
                selectedHint = (PhoneMultiFactorInfo) info;
                startPhoneVerification();
                break;
            }
        }

        verifyButton.setOnClickListener(v -> verifyCode());
    }

    private void startPhoneVerification() {
        PhoneAuthOptions phoneAuthOptions = PhoneAuthOptions.newBuilder()
                .setActivity(this)
                .setMultiFactorSession(multiFactorResolver.getSession()) // FIX: Now it won’t be null
                .setMultiFactorHint(selectedHint)
                .setCallbacks(generatePhoneAuthCallbacks())
                .setTimeout(30L, TimeUnit.SECONDS)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks generatePhoneAuthCallbacks() {
        return new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                // Successfully received the verification code
                resolveMultiFactorSignIn(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(MFAVerifyLogin.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                MFAVerifyLogin.this.verificationId = verificationId; // Store verification ID
                Toast.makeText(MFAVerifyLogin.this, "Verification code sent", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void verifyCode() {
        String code = pinFromUser.getText().toString().trim();
        if (code.isEmpty() || code.length() < 6) {
            pinFromUser.setError("Enter a valid code");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        resolveMultiFactorSignIn(credential);
    }

    private void resolveMultiFactorSignIn(PhoneAuthCredential phoneAuthCredential) {
        MultiFactorAssertion multiFactorAssertion = PhoneMultiFactorGenerator.getAssertion(phoneAuthCredential);

        // Resolve multi-factor authentication
        multiFactorResolver.resolveSignIn(multiFactorAssertion)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MFAVerifyLogin.this, "Multi-factor authentication successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MFAVerifyLogin.this, Homescreen.class));
                        finish();
                    } else {
                        Toast.makeText(MFAVerifyLogin.this, "Failed to resolve multi-factor authentication", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}