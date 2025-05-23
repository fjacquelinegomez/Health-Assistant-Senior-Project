package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HealthGoals_PC extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_health_goals_pc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setpin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }
    public void onSubmitHealthGoalsHistory (View view){ // Call this when user submits
        // Process the form data and save it (optional)

        // Indicate that the user successfully completed this step
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish(); // Close this activity and return to ProfileCustomization
    }

    //public void onSkipHealthGoalsHistory (View view){
      //  Intent resultIntent = new Intent();
        //setResult(RESULT_CANCELED, resultIntent);
        //finish(); // Close this activity and return to ProfileCustomization

    //}
}