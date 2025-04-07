package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.MultiFactorSession;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RegisterMFASetup2 extends AppCompatActivity {
    private TextInputEditText phoneNumber;
    private CountryCodePicker countryCodePicker;
    private FirebaseAuth auth;
    private MultiFactorSession multiFactorSession;
    private String phoneNo;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_mfasetup2);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Initialize UI components
        TextInputLayout phoneNumberLayout = findViewById(R.id.signupPhoneNumberTextInputLayout);
        phoneNumber = findViewById(R.id.signupPhoneNumberEditText);
        countryCodePicker = findViewById(R.id.countryCodePick);
        Button phoneNextButton = findViewById(R.id.signupPhoneNextButton);

        phoneNextButton.setOnClickListener(v -> {
            if (!validatePhoneNumber()) return;

            String userEnteredPhoneNumber = Objects.requireNonNull(phoneNumber.getText()).toString().trim();
            phoneNo = "+" + countryCodePicker.getSelectedCountryCode() + userEnteredPhoneNumber;
            Log.d("MFA", "Final phone number: " + phoneNo);

            if (user != null) {
                user.getMultiFactor().getSession()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                multiFactorSession = task.getResult();
                                sendVerificationCode(phoneNo);
                            } else {
                                Log.e("MFA", "Failed to get MultiFactorSession", task.getException());
                                Toast.makeText(this, "Error: Unable to get MFA session", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setMultiFactorSession(multiFactorSession)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        Log.d("MFA", "Auto verification completed");
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.e("MFA", "Verification failed", e);
                        Toast.makeText(RegisterMFASetup2.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        RegisterMFASetup2.this.verificationId = verificationId;
                        resendingToken = token;

                        Log.d("MFA", "Code sent successfully");

                        Intent intent = new Intent(RegisterMFASetup2.this, MFAVerify.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("phoneNo", phoneNo);
                        startActivity(intent);
                        finish();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private boolean validatePhoneNumber() {
        String phoneNumberString = Objects.requireNonNull(phoneNumber.getText()).toString().trim();
        if (phoneNumberString.isEmpty()) {
            phoneNumber.setError("Phone number cannot be empty");
            return false;
        }
        if (phoneNumberString.length() < 10) {
            phoneNumber.setError("Please enter a valid phone number");
            return false;
        }
        return true;
    }
}

