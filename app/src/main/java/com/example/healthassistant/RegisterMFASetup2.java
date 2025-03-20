package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import java.util.Objects;

public class RegisterMFASetup2 extends AppCompatActivity {
    TextInputEditText phoneNumber;
    //TextInputLayout phoneNumber;
    CountryCodePicker countryCodePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_mfasetup2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Correct way to reference TextInputLayout and TextInputEditText
        TextInputLayout phoneNumberLayout = findViewById(R.id.signupPhoneNumberTextInputLayout);
        phoneNumber = findViewById(R.id.signupPhoneNumberEditText);
        countryCodePicker = findViewById(R.id.countryCodePick);

        // Check if phoneNumber is null
        if (phoneNumber == null) {
            Log.e("MFA", "phoneNumber EditText is NULL! Check ID in XML.");
        }

        final Button phoneNextButton = findViewById(R.id.signupPhoneNextButton);
        phoneNextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!validatePhoneNumber()) {
                    return;
                }
                Log.d("MFA", "Number validated");

                // Get values passed from previous screens
                String _fullName = getIntent().getStringExtra("fullName");
                String _username = getIntent().getStringExtra("username");

                // Get phone number correctly
                String _getUserEnteredPhoneNumber = Objects.requireNonNull(phoneNumber.getText()).toString().trim();

                if (countryCodePicker != null) {
                    Log.d("MFA", "CountryCodePicker are properly initialized");
                } else {
                    Log.e("MFA", "CountryCodePicker is NULL!");
                }
                // Build full phone number ERROR LIES HERE
                //String _phoneNo = "+" + countryCodePicker.getFullNumber() + _getUserEnteredPhoneNumber;
                String _phoneNo = "+" + countryCodePicker.getSelectedCountryCode() + _getUserEnteredPhoneNumber;
                Log.d("MFA", "Final phone number: " + _phoneNo);

                Intent intent = new Intent(getApplicationContext(), MFAVerify.class);
                intent.putExtra("fullName", _fullName);
                intent.putExtra("username", _username);
                intent.putExtra("phoneNo", _phoneNo);

                startActivity(intent);
                finish();
            }
        });
    }

    public boolean validatePhoneNumber() {
        String phoneNumberString = Objects.requireNonNull(phoneNumber.getText()).toString().trim();
        // Example validation: check if the phone number is empty or not valid
        if (phoneNumberString.isEmpty()) {
            phoneNumber.setError("Phone number cannot be empty");
            return false;
        }
        // Example validation: check if the phone number is valid (simple check for length)
        if (phoneNumberString.length() < 10) {
            phoneNumber.setError("Please enter a valid phone number");
            return false;
        }
        return true; // Valid phone number
    }
}