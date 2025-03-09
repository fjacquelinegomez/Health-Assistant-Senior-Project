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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button login_btn;
    private Button register_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("HELLO TEST123!");
        myRef.setValue("This is me testing, testing, testing again. ");

        //logic for once the user presses the login button
        login_btn = findViewById(R.id.login_button);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        //logic for once the user presses the register button
        register_btn = findViewById(R.id.register_button);
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Keeps the user signed in if they already have
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("AuthUser", "User is already signed in: " + currentUser.getEmail());
            startActivity(new Intent(MainActivity.this, Homescreen.class));
            finish();
        }

        /* Testing firestore connection
        FirebaseFirestore firestore_db = FirebaseFirestore.getInstance();
        firestore_db.collection("test")
                .add(new HashMap<String, Object>() {{
                    put("message", "Hello Firestore!");
                }})
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding document", e);
                });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Upload medications to medication database (still a WIP)
        //uploadMedicationsToFirestore();
    }
*/
        // Upload medications to medication database by parsing through JSON file (still a WIP)
    /*
    private void uploadMedicationsToFirestore() {
        try {
            FirebaseFirestore firestoreDatabase = FirebaseFirestore.getInstance();
            // Initializing tools
            InputStream inputStream = getAssets().open("medicationInfo_01.json");
            ObjectMapper objectMapper = new ObjectMapper();
            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(inputStream);
            parser.setCodec(objectMapper);

            // Parse/stream through the JSON file
            while (!parser.isClosed()) {
                JsonToken token = parser.nextToken();

                // Check if it's a new medication object
                if (token == JsonToken.START_OBJECT) {
                    // Create a JsonNode of current object
                    JsonNode medicationNode = objectMapper.readTree(parser);
                    JsonNode openfdaNode = medicationNode.get("openfda");

                    // Extract relevant fields from the OpenFDA data
                    String brandName = openfdaNode.has("brand_name") ? openfdaNode.get("brand_name").get(0).asText() : "Unknown";
                    String genericName = openfdaNode.has("generic_name") ? openfdaNode.get("generic_name").get(0).asText() : "Unknown";
                    String manufacturerName = openfdaNode.has("manufacturer_name") ? openfdaNode.get("manufacturer_name").get(0).asText() : "Unknown";
                    String route = openfdaNode.has("route") ? openfdaNode.get("route").get(0).asText() : "Unknown";

                    // Create a map of the medication data to upload to Firestore
                    Map<String, Object> medData = new HashMap<>();
                    medData.put("brand_name", brandName);
                    medData.put("generic_name", genericName);
                    medData.put("manufacturer_name", manufacturerName);
                    medData.put("route", route);

                    // Upload the medication data to Firestore
                    DocumentReference medRef = firestoreDatabase.collection("Medications").document(brandName);
                    medRef.set(medData)
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Medication upload successful"))
                            .addOnFailureListener(e -> Log.e("Firestore", "Error uploading medication", e));
                }
            }

        } catch (Exception e) {
            Log.e("Firestore", "Error uploading medication", e);
        }
    }
     */
    }
}