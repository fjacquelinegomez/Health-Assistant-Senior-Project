package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
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

public class FoodPreferences_PC extends AppCompatActivity {

    private DatabaseReference databaseRef;

    // Declare ALL checkboxes
    private CheckBox breakfast, dinner, lunch, night, snacks;
    private RadioButton none, one_day, three_day, six_day;
    private CheckBox pancakes, eggs, ham, tomatoe, sandwiches, fruits, cheese, olives, croissants;
    private CheckBox salad, meat, pork, cucumber, veggies, sandwich_lunch, snacks_lunch;
    private RadioButton big_eater, quick_eater, light_eater, moderate_eater;
    private CheckBox omnivore, vegetarian, vegan, light_fat, never_full, ketogenic;
    private RadioButton always, usually, sometimes, never;
    private RadioButton healthy, poor, okay, other;
    private CheckBox water, spark_water, milk, soft_drinks, alcohol, juice;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_preferences_pc);


        // imports for the Firebase Authentication + Realtime Database
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();


        // initialize all the checkbox ID's
        // 1st question
        breakfast = findViewById(R.id.checkbox_breakfast);
        dinner = findViewById(R.id.checkbox_dinner);
        lunch = findViewById(R.id.checkbox_lunch);
        night = findViewById(R.id.checkbox_night);
        snacks = findViewById(R.id.checkbox_snacks);

        // 2nd question
        none = findViewById(R.id.radio_none);
        one_day = findViewById(R.id.radio_one_day);
        three_day = findViewById(R.id.radio_three_day);
        six_day = findViewById(R.id.radio_six_day);

        // 3rd question
        pancakes = findViewById(R.id.checkbox_pancakes);
        eggs = findViewById(R.id.checkbox_eggs);
        ham = findViewById(R.id.checkbox_ham);
        tomatoe = findViewById(R.id.checkbox_tomatoes_breakfast);
        sandwiches = findViewById(R.id.checkbox_sandwiches);
        fruits = findViewById(R.id.checkbox_fruits);
        cheese = findViewById(R.id.checkbox_cheese);
        olives = findViewById(R.id.checkbox_olives);
        croissants = findViewById(R.id.checkbox_croissants);

        // 4th question
        salad = findViewById(R.id.checkbox_salad);
        meat = findViewById(R.id.checkbox_meat);
        pork = findViewById(R.id.checkbox_pork);
        cucumber = findViewById(R.id.checkbox_tomatoes);
        veggies = findViewById(R.id.checkbox_veggies);
        sandwich_lunch = findViewById(R.id.checkbox_sandwiches_lunch);
        snacks_lunch = findViewById(R.id.checkbox_snacks_lunch);

        // 5th question
        big_eater = findViewById(R.id.radio_big_eater);
        quick_eater = findViewById(R.id.radio_quick_eater);
        light_eater = findViewById(R.id.radio_light_eater);
        moderate_eater = findViewById(R.id.radio_moderate_eater);

        // 6th question
        omnivore = findViewById(R.id.checkbox_omnivore);
        vegetarian = findViewById(R.id.checkbox_vegetarian);
        vegan = findViewById(R.id.checkbox_vegan);
        light_fat = findViewById(R.id.checkbox_lightfat);
        never_full = findViewById(R.id.checkbox_neverfull);
        ketogenic = findViewById(R.id.checkbox_ketogenic);

        // 7th question
        always = findViewById(R.id.radio_always);
        usually = findViewById(R.id.radio_usually);
        sometimes = findViewById(R.id.radio_sometimes);
        never = findViewById(R.id.radio_never);

        // 8th question
        healthy = findViewById(R.id.radio_healthy);
        poor = findViewById(R.id.radio_poor);
        okay = findViewById(R.id.radio_okay);
        other = findViewById(R.id.radio_other);


        // 9th question
        water = findViewById(R.id.checkbox_water);
        spark_water = findViewById(R.id.checkbox_spark_water);
        milk = findViewById(R.id.checkbox_milk);
        soft_drinks = findViewById(R.id.checkbox_soft_drinks);
        alcohol = findViewById(R.id.checkbox_alcohol);
        juice = findViewById(R.id.checkbox_juice);


        //checks to see if the user is logged in, otherwise it won't save the data as Food_Preferences_PC

        if (user != null){
            String uid = user.getUid(); // captures the user UID from the auth. database
            databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("Food_Preferences_PC");
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setpin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }
    public void onSubmitFPHistory(View view) { // Call this when user submits
        // Create a map to store the selected food preferences
        Map<String, Boolean> foodPreferences = new HashMap<>();

        // Store the selected checkboxes (true if checked, false if unchecked)
        foodPreferences.put("Breakfast", breakfast.isChecked());
        foodPreferences.put("Dinner", dinner.isChecked());
        foodPreferences.put("Lunch", lunch.isChecked());
        foodPreferences.put("Night", night.isChecked());
        foodPreferences.put("Snacks", snacks.isChecked());

        foodPreferences.put("None", none.isChecked());
        foodPreferences.put("One Day", one_day.isChecked());
        foodPreferences.put("Three Day", three_day.isChecked());
        foodPreferences.put("Six Day", six_day.isChecked());

        foodPreferences.put("Pancakes", pancakes.isChecked());
        foodPreferences.put("Eggs", eggs.isChecked());
        foodPreferences.put("Ham", ham.isChecked());
        foodPreferences.put("Tomatoe", tomatoe.isChecked());
        foodPreferences.put("Sandwiches", sandwiches.isChecked());
        foodPreferences.put("Fruits", fruits.isChecked());
        foodPreferences.put("Cheese", cheese.isChecked());
        foodPreferences.put("Olives", olives.isChecked());
        foodPreferences.put("Croissants", croissants.isChecked());

        foodPreferences.put("Salad", salad.isChecked());
        foodPreferences.put("Meat", meat.isChecked());
        foodPreferences.put("Pork", pork.isChecked());
        foodPreferences.put("Cucumber", cucumber.isChecked());
        foodPreferences.put("Veggies", veggies.isChecked());
        foodPreferences.put("Sandwich Lunch", sandwich_lunch.isChecked());
        foodPreferences.put("Snacks Lunch", snacks_lunch.isChecked());


        foodPreferences.put("Big Eater", big_eater.isChecked());
        foodPreferences.put("Quick Eater", quick_eater.isChecked());
        foodPreferences.put("Light Eater", light_eater.isChecked());
        foodPreferences.put("Moderate Eater", moderate_eater.isChecked());

        foodPreferences.put("Omnivore", omnivore.isChecked());
        foodPreferences.put("Vegetarian", vegetarian.isChecked());
        foodPreferences.put("Vegan", vegan.isChecked());
        foodPreferences.put("Light fat", light_fat.isChecked());
        foodPreferences.put("Never Full", never_full.isChecked());
        foodPreferences.put("Ketogenic", ketogenic.isChecked());

        foodPreferences.put("Always", always.isChecked());
        foodPreferences.put("Usually", usually.isChecked());
        foodPreferences.put("Sometimes", sometimes.isChecked());
        foodPreferences.put("Never", never.isChecked());

        foodPreferences.put("Healthy", healthy.isChecked());
        foodPreferences.put("Poor", poor.isChecked());
        foodPreferences.put("Okay", okay.isChecked());
        foodPreferences.put("Other", other.isChecked());
        foodPreferences.put("Water", water.isChecked());
        foodPreferences.put("Spark Water", spark_water.isChecked());
        foodPreferences.put("Milk", milk.isChecked());
        foodPreferences.put("Soft Drink", soft_drinks.isChecked());
        foodPreferences.put("Alcohol", alcohol.isChecked());
        foodPreferences.put("Juice", juice.isChecked());




        String logId = databaseRef.push().getKey();
        if (logId != null) {
            databaseRef.child(logId).setValue(foodPreferences)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(FoodPreferences_PC.this, "Preferences saved!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK, new Intent());
                            finish();
                        } else {
                            Toast.makeText(FoodPreferences_PC.this, "Failed to save preferences.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


        // Indicate that the user successfully completed this step
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish(); // Close this activity and return to ProfileCustomization
    }

}