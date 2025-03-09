package com.example.healthassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
    private List<Medication> medicationList;

    // Medication List Adapter
    public MedicationAdapter(List<Medication> medicationList) {
        this.medicationList = medicationList;
    }

    // Inflates medication item to create view holder instances
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.medication_item, parent, false);
        return new ViewHolder(view);
    }

    // Binds data from the medications list to the view holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Medication medication = medicationList.get(position);
        holder.medicationName.setText(medication.getName());
        holder.medicationExpiration.setText("Expires: " + medication.getExpirationDate());
        holder.medicationCount.setText(String.valueOf("0"+"/"+medication.getTotalPills() + " [dosage form] taken"));
    }

    // Returns the total number of medications in the list
    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    // Holds the references to the views in each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView medicationExpiration;
        public TextView medicationCount;
        public TextView medicationName;

        // Initializes view references
        public ViewHolder(View itemView) {
            super(itemView);
            medicationExpiration = itemView.findViewById(R.id.medicationExpiration);
            medicationCount = itemView.findViewById(R.id.medicationCount);
            medicationName = itemView.findViewById(R.id.medicationName);
        }
    }
}
