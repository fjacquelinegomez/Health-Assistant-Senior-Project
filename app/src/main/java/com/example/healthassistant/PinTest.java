package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PinTest extends AppCompatActivity {

    private String storedPin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.pin_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button back = findViewById(R.id.searchBackButton);
        Button set = findViewById(R.id.button2);
        Button test = findViewById(R.id.button3);
        EditText setter = findViewById(R.id.setter);
        EditText tester = findViewById(R.id.editTextNumber2);
        TextView show = findViewById(R.id.textView9);
        TextView show2 = findViewById(R.id.textView10);

        back.setOnClickListener(v -> startActivity(new Intent(PinTest.this, Homescreen.class)));

        // Set PIN
        set.setOnClickListener(v -> {
            String numberInput = setter.getText().toString().trim();

            if (!numberInput.isEmpty() && numberInput.length() == 4 && numberInput.matches("\\d+")) {
                storedPin = numberInput;
                show.setText("New Pin Set: " + storedPin);
            } else {
                show.setText("Enter a 4-digit numeric PIN!");
            }
        });

        // Test PIN
        test.setOnClickListener(v -> {
            String enteredPin = tester.getText().toString().trim();

            if (!enteredPin.isEmpty() && enteredPin.length() == 4 && enteredPin.matches("\\d+")) {
                if (enteredPin.equals(storedPin)) {
                    show2.setText("PIN Matched: " + enteredPin);
                } else {
                    show2.setText("Incorrect PIN! Try again.");
                }
            } else {
                show2.setText("Enter a valid 4-digit PIN!");
            }
        });
    }
}
