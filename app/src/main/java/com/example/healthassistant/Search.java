package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import medication.RetrofitService;
import medication.RxNormApiResponse;
import medication.RxNormApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.healthassistant.databinding.ActivityFoodManagerBinding;
import com.example.healthassistant.databinding.ActivitySearchBinding;


public class Search extends AppCompatActivity {
    private SearchView search;
    private RecyclerView recyclerView;
    private MedicationSearchAdapter adapter;
    private List<Medication> medicationList = new ArrayList<>();

    ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        /**bottom bar navigation functionality**/
        binding = ActivitySearchBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.home:
                            startActivity(new Intent(Search.this, Homescreen.class));
                            break;
                        case R.id.searchMedication:
                            startActivity(new Intent(Search.this, Search.class));
                            break;
                        case R.id.foodManager:
                            startActivity(new Intent(Search.this, FoodManager.class));
                            break;
                        case R.id.healthGoals:
                            startActivity(new Intent(Search.this, HealthGoals.class));
                            break;
                        case R.id.medicationManager:
                            startActivity(new Intent(Search.this, MedicationManager.class));
                            break;

                    }
            return true;
        });

        // Initializes the search and recycler view (+ adapter)
        search = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.searchRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);
        adapter = new MedicationSearchAdapter(medicationList);
        recyclerView.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //searchMedication("ibuprofen"); //Used to test if searching with RxNormApi is working as intended

        // Search bar functionality
        // https://developer.android.com/reference/android/widget/SearchView -> Helpful resource for this widget
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // When user hits 'Enter' after typing their medication, they will get their results
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    searchMedication(query);
                    Log.d("SearchMedication", "Adapter item count: " + adapter.getItemCount());
                    return true;
                }
                return false;
            }

            // FIXME: Results will pop up while users are typing
            // WIP, it kept crashing everytime I tried working with this :((
            @Override
            public boolean onQueryTextChange(String query) {
                return false;
            }
        });

        // Back button functionality
        // FIXME: temporary spot, find a better spot to put this
        /*Button back = findViewById(R.id.back);
        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.back) {
                    startActivity(new Intent(Search.this, Homescreen.class));
                }
            }
        };
        back.setOnClickListener(buttonClickListener);*/
    }

    // Grabs the user's inputted search and requests information about the medication from RxNorm
    private void searchMedication(String drugName) {
        Log.d("SearchMedication", "Hello: ");
        // Creates an instance of the RxNormApi using Retrofit
        RxNormApiService service = RetrofitService.getClient().create(RxNormApiService.class);

        // Requests information about the medication from RxNorm
        service.searchDrugs(drugName).enqueue(new Callback<RxNormApiResponse>() {
            @Override
            public void onResponse(Call<RxNormApiResponse> call, Response<RxNormApiResponse> response) {
                // Checks if the request was successful and actually returns something
                // If it's successful then it goes through the formatted JSON file and stores the medication(s) in a list
                if (response.isSuccessful() && response.body() != null) {
                    List<Medication> medications = new ArrayList<>();
                    for (RxNormApiResponse.ConceptGroup group: response.body().getDrugGroup().getConceptGroup()) {
                        if (group.getConceptProperties() != null) {
                            medications.addAll(group.getConceptProperties());
                        }
                    }

                    // Adds medication to recycler view
                    medicationList.clear();
                    medicationList.addAll(medications);
                    adapter.notifyDataSetChanged();
                    Log.d("SearchMedication", "Medications list size after population: " + medicationList.size());

                    // Logcat message for debugging purposes
                    for (Medication med : medications) {
                        Log.d("SearchMedication", "Medication: " + med.getName() + med.getRxcui());
                    }
                }
            }
            // Called if the request fails
            @Override
            public void onFailure(Call<RxNormApiResponse> call, Throwable t) {
                Log.d("SearchMedication", "Error: " + t.getMessage());
            }
        });
    }
}