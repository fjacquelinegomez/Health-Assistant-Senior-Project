package com.example.healthassistant;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Adapter class for the recycler view that displays the list of searched medications
public class MedicationSearchAdapter extends RecyclerView.Adapter<MedicationSearchAdapter.MedicationViewHolder> {
    private List<Medication> medicationList;

    // Constructs medication list
    public MedicationSearchAdapter(List<Medication> medicationList) {
        this.medicationList = medicationList;
    }

    // Inflates medication item to create view holder instances
    @Override
    public MedicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.medication_search_item, parent, false);
        return new MedicationViewHolder(view);
    }

    // Binds data from the medicationList to the view holder
    @Override
    public void onBindViewHolder(MedicationViewHolder holder, int position) {
        Medication medication = medicationList.get(position);
        Log.d("SearchMedication", "Binding item at position: " + position + " with name: " + medication.getName());
        holder.medicationName.setText(medication.getName());

        // Handles when users click the plus (add medication) button
        // Redirects users to the add medication page
        holder.addMedication.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), AddMedication.class);
            intent.putExtra("medicationName", medication.getName()); // passes medication name to the add medication page
            intent.putExtra("rxcui", medication.getRxcui()); // passes medication's id to the add medication page
            holder.itemView.getContext().startActivity(intent);
        });
    }

    // Returns the total number of medications in the list
    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    // Updates the list of medications and notifies the adapter
    public void updateList(List<Medication> newList) {
        medicationList.clear(); // Corrected variable name
        medicationList.addAll(newList);
        notifyDataSetChanged();
    }

    // Holds the references to the views in each item
    public static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView medicationName;
        ImageView addMedication;

        // Initializes view references
        public MedicationViewHolder(View itemView) {
            super(itemView);
            medicationName = itemView.findViewById(R.id.medicationName);
            addMedication = itemView.findViewById(R.id.addMedication);
        }
    }
}
