package com.example.healthassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
    private List<Medication> medicationList;

    public MedicationAdapter(List<Medication> medicationList) {
        this.medicationList = medicationList;
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
        holder.medicationExpiration.setText("Expires: " + medication.getExpirationDate());
        holder.medicationCount.setText(String.valueOf("0"+"/"+medication.getTotalPills() + " [dosage form] taken"));
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView medicationExpiration;
        public TextView medicationCount;

        public ViewHolder(View itemView) {
            super(itemView);
            medicationExpiration = itemView.findViewById(R.id.medicationExpiration);
            medicationCount = itemView.findViewById(R.id.medicationCount);
        }
    }
}
