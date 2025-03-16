package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DietaryRestrictions_PC extends AppCompatActivity {
    
    private DatabaseReference databaseRef;
    
    private CheckBox peanut, treenuts, dairy, egg, shellfish, fish, soy, gluten, wheat, sesame, other1;
    private CheckBox veg, vegan, pesca, low_sodium, low_sugar, low_fat, low_fodmap, renal_diet, keo_diet, other2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dietary_restrictions_pc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // imports for the Firebase Authentication + Realtime Database
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // initialize all the checkbox ID's
        // 1st question
        peanut = findViewById(R.id.checkboxPeanut);
        treenuts = findViewById(R.id.checkboxTreenuts);
        dairy = findViewById(R.id.checkboxDairy);
        egg = findViewById(R.id.checkboxEgg);
        shellfish = findViewById(R.id.checkboxShellfish);
        fish = findViewById(R.id.checkboxFish);
        soy = findViewById(R.id.checkboxSoy);
        gluten = findViewById(R.id.checkboxGlutin);
        wheat = findViewById(R.id.checkboxWheat);
        sesame = findViewById(R.id.checkboxSesame);
        other1 = findViewById(R.id.checkboxOtherAllergies);

        // 2nd question
        veg = findViewById(R.id.checkboxVegetarian);
        vegan = findViewById(R.id.checkboxVegan);
        pesca = findViewById(R.id.checkboxPescatarian);
        low_sodium = findViewById(R.id.checkboxSodium);
        low_sugar = findViewById(R.id.checkboxSugar);
        low_fat = findViewById(R.id.checkboxFat);
        low_fodmap = findViewById(R.id.checkboxFODMAP);
        renal_diet = findViewById(R.id.checkboxRenal);
        keo_diet = findViewById(R.id.checkboxKetogenic);
        other2 = findViewById(R.id.checkboxOtherDiets);


        //checks to see if the user is logged in, otherwise it won't save the data as Food_Preferences_PC

        if (user != null){
            String uid = user.getUid(); // captures the user UID from the auth. database
            databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("Food_Preferences_PC");
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

    }
    public void onSubmitDRHistory(View view) { // Call this when user submits
        Map<String, Boolean> dietaryRestrictions = new HashMap<>();

        //store the selected checkboxes (true if checked, false if unchecked)
        dietaryRestrictions.put("Peanut", peanut.isChecked());
        dietaryRestrictions.put("Tree Nuts", treenuts.isChecked());
        dietaryRestrictions.put("Dairy", dairy.isChecked());
        dietaryRestrictions.put("Egg", egg.isChecked());
        dietaryRestrictions.put("Shellfish", shellfish.isChecked());
        dietaryRestrictions.put("Fish", fish.isChecked());
        dietaryRestrictions.put("Soy", soy.isChecked());
        dietaryRestrictions.put("Gluten", gluten.isChecked());
        dietaryRestrictions.put("What", wheat.isChecked());
        dietaryRestrictions.put("Sesame", sesame.isChecked());
        dietaryRestrictions.put("OtherAllergies", other1.isChecked());

        dietaryRestrictions.put("Vegetarian", veg.isChecked());
        dietaryRestrictions.put("Vegan", vegan.isChecked());
        dietaryRestrictions.put("Pescatarian", pesca.isChecked());
        dietaryRestrictions.put("Low Sodium", low_sodium.isChecked());
        dietaryRestrictions.put("Low Sugar", low_sugar.isChecked());
        dietaryRestrictions.put("Low Fat", low_fat.isChecked());
        dietaryRestrictions.put("Low FODMAP", low_fodmap.isChecked());
        dietaryRestrictions.put("Renal Diet", renal_diet.isChecked());
        dietaryRestrictions.put("Ketogenic Diet", keo_diet.isChecked());
        dietaryRestrictions.put("OtherDiets", other2.isChecked());

        String logId = databaseRef.push().getKey();
        if (logId != null) {
            databaseRef.child(logId).setValue(dietaryRestrictions)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(DietaryRestrictions_PC.this, "Restrictions saved!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK, new Intent());
                            finish();
                        } else {
                            Toast.makeText(DietaryRestrictions_PC.this, "Failed to save restrictions.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }



        // Indicate that the user successfully completed this step
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish(); // Close this activity and return to ProfileCustomization
    }

}