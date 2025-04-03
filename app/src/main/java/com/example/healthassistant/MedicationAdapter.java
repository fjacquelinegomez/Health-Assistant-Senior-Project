package com.example.healthassistant;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;


import java.util.List;


public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
    private List<Medication> medicationList;
    private Context context;


    public MedicationAdapter(List<Medication> medicationList, Context context) {
        this.medicationList = medicationList;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.medication_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Medication medication = medicationList.get(position);
        holder.medicationName.setText(medication.getName());
        holder.expirationDate.setText("Expires: " + medication.getExpirationDate());
        //holder.medicationCount.setText(medication.getDosesTaken() + "/" + medication.getTotalPills() + " " + medication.getDosageForm() + " taken");
        holder.medicationCount.setText("0/" + medication.getTotalPills() + " [dosage form] taken");

        // Edit button functionality
        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddMedication.class);
            intent.putExtra("medicationId", medication.getId()); // passes in the userMed document
            context.startActivity(intent);
        });

        /*
        // Delete button functionality
        holder.deleteButton.setOnClickListener(v -> {
            // Remove item from Firebase and the list
            ((MedicationManager2) context).deleteMedicationFromFirestore(medication, position);
        });
         */
    }


    @Override
    public int getItemCount() {
        return medicationList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView medicationName;
        public TextView expirationDate;
        public TextView medicationCount;
        public ImageView editButton;


        public ViewHolder(View itemView) {
            super(itemView);
            medicationName = itemView.findViewById(R.id.medicationName);
            expirationDate = itemView.findViewById(R.id.medicationExpiration);
            medicationCount = itemView.findViewById(R.id.medicationCount);
            editButton = itemView.findViewById(R.id.medicationEdit);
        }
    }
}

/**
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
**/