package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.MultiFactorAssertion;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneMultiFactorGenerator;

public class MFAVerify extends AppCompatActivity {
    private FirebaseAuth auth;
    private PinView pinFromUser;
    private String verificationId;
    private String phoneNo;
    private MultiFactorAssertion multiFactorAssertion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mfaverify);

        auth = FirebaseAuth.getInstance();
        pinFromUser = findViewById(R.id.pinView);
        Button verifyButton = findViewById(R.id.verifyCodeButton);

        verificationId = getIntent().getStringExtra("verificationId");
        phoneNo = getIntent().getStringExtra("phoneNo");

        verifyButton.setOnClickListener(v -> verifyCode());
    }

    private void verifyCode() {
        String code = pinFromUser.getText().toString().trim();
        if (code.isEmpty() || code.length() < 6) {
            pinFromUser.setError("Enter a valid code");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        multiFactorAssertion = PhoneMultiFactorGenerator.getAssertion(credential);
        enrollSecondFactor();
    }

    private void enrollSecondFactor() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && multiFactorAssertion != null) {
            user.getMultiFactor().enroll(multiFactorAssertion, phoneNo)
                    .addOnSuccessListener(task -> {
                        Toast.makeText(MFAVerify.this, "MFA Enrollment Successful!", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(MFAVerify.this, Homescreen.class));
                        startActivity(new Intent(MFAVerify.this, ProfileCustomization.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MFAVerify.this, "MFA Enrollment Failed", Toast.LENGTH_SHORT).show();
                        Log.e("MFA", "MFA Enrollment failed", e);
                    });
        }
    }
}

