package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthassistant.databinding.ActivitySearchBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import medication.RetrofitService;
import medication.RxNormApiResponse;
import medication.RxNormApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Search extends AppCompatActivity {
    private SearchView search;
    private RecyclerView recyclerView;
    private MedicationSearchAdapter adapter;
    private List<Medication> medicationList = new ArrayList<>();
    private List<Medication> filteredList = new ArrayList<>();
    ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Spinner filterSpinner = findViewById(R.id.filterSpinner);

        // Create an ArrayAdapter using the string array and a simple spinner layout
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.medication_filters,
                android.R.layout.simple_spinner_item
        );
        Button test2 = findViewById(R.id.picture);
        Intent intent = getIntent();
        String str = intent.getStringExtra("message_key");

        search = findViewById(R.id.searchView); // <-- initialize BEFORE using it
        search.setQuery(str, true);
        test2.setOnClickListener(v -> startActivity(new Intent(Search.this, BarcodeScannerActivity.class)));

        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        filterSpinner.setAdapter(spinnerAdapter);

        // Handle spinner selection
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedFilter = parentView.getItemAtPosition(position).toString();
                applyFilter(selectedFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        // Bottom Navigation functionality
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(this, Homescreen.class));
                    break;
                case R.id.searchMedication:
                    startActivity(new Intent(this, Search.class));
                    break;
                case R.id.foodManager:
                    startActivity(new Intent(this, FoodManager.class));
                    break;
                case R.id.healthGoals:
                    startActivity(new Intent(this, HealthGoals.class));
                    break;
                case R.id.medicationManager:
                    startActivity(new Intent(this, MedicationManager.class));
                    break;
            }
            return true;
        });

        // Initialize views
        search = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.searchRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);
        adapter = new MedicationSearchAdapter(medicationList);
        recyclerView.setAdapter(adapter);

        // Handle system insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Search bar functionality
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    searchMedication(query);
                    Log.d("SearchMedication", "Query submitted: " + query);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return false; // Not implementing real-time results for now
            }
        });
    }

    private void searchMedication(String drugName) {
        Log.d("SearchMedication", "Searching for: " + drugName);

        RxNormApiService service = RetrofitService.getClient().create(RxNormApiService.class);
        service.searchDrugs(drugName).enqueue(new Callback<RxNormApiResponse>() {
            @Override
            public void onResponse(Call<RxNormApiResponse> call, Response<RxNormApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Medication> medications = new ArrayList<>();
                    Set<String> uniqueMedications = new HashSet<>();

                    RxNormApiResponse.DrugGroup groupResponse = response.body().getDrugGroup();
                    if (groupResponse == null || groupResponse.getConceptGroup() == null) {
                        Toast.makeText(Search.this, "Medication not found!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    for (RxNormApiResponse.ConceptGroup group : groupResponse.getConceptGroup()) {
                        if (group.getConceptProperties() != null) {
                            for (Medication med : group.getConceptProperties()) {
                                String cleanedName = cleanMedicationName(med.getName());
                                med.setName(cleanedName);

                                // Set the type
                                med.setType(med.getTty());

                                if (!uniqueMedications.contains(cleanedName)) {
                                    uniqueMedications.add(cleanedName);
                                    medications.add(med);
                                }
                            }
                        }
                    }

                    medicationList.clear();
                    medicationList.addAll(medications);
                    applyFilter(((Spinner) findViewById(R.id.filterSpinner)).getSelectedItem().toString());
                    Log.d("SearchMedication", "Loaded " + medicationList.size() + " medications");
                }
            }

            @Override
            public void onFailure(Call<RxNormApiResponse> call, Throwable t) {
                Log.e("SearchMedication", "API failure: " + t.getMessage());
            }
        });
    }


    private String cleanMedicationName(String medication) {
        String cleanedName = medication.replaceAll(
                "\\d+ (MG/ML)?(/\\d+ MG?)?\\s*(Oral Tablet|Oral Powder|Effervescent Oral Tablet|Reformulated\\s*\\w*)?",
                "").trim();

        if (cleanedName.contains("[")) {
            int start = cleanedName.indexOf("[");
            int end = cleanedName.indexOf("]", start);
            if (start != -1 && end != -1) {
                String productName = cleanedName.substring(start + 1, end).trim();
                if (!productName.isEmpty()) {
                    return productName;
                }
            }
        }

        return cleanedName;
    }

    private void applyFilter(String filterType) {
        List<Medication> filteredList = new ArrayList<>();

        if (filterType == null || filterType.equalsIgnoreCase("All")) {
            filteredList.addAll(medicationList);
        } else {
            for (Medication med : medicationList) {
                if (filterType.equalsIgnoreCase("Ingredient") && "IN".equalsIgnoreCase(med.getType())) {
                    filteredList.add(med);
                } else if (filterType.equalsIgnoreCase("Brand Name") && "BN".equalsIgnoreCase(med.getType())) {
                    filteredList.add(med);
                } else if (filterType.equalsIgnoreCase("Clinical Drug") && "SCD".equalsIgnoreCase(med.getType())) {
                    filteredList.add(med);
                }
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No medications found for this filter", Toast.LENGTH_SHORT).show();
        }

        adapter.updateList(filteredList);
    }

}
