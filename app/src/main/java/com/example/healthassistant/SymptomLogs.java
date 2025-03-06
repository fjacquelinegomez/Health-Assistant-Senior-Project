package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SymptomLogs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_symptom_logs);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Button back = findViewById(R.id.symptomLogsBackButton);
        //back.setOnClickListener(v -> startActivity(new Intent(SymptomLogs.this, Homescreen.class)));

        // Set up new row for symptom log table
        TableLayout tableLayout = findViewById(R.id.tableLayout);

        TableRow tableRow = new TableRow(this);

        TextView textView1 = new TextView(this);
        textView1.setText("11/11/12");
        textView1.setPadding(20, 8, 8, 8);

        TextView textView2 = new TextView(this);
        textView2.setText("Foot feels funny");
        textView2.setPadding(20, 8, 8, 8);

        TextView textView3 = new TextView(this);
        textView3.setText("4");
        textView3.setPadding(20, 8, 8, 8);

        // Add TextViews to the TableRow
        tableRow.addView(textView1);
        tableRow.addView(textView2);
        tableRow.addView(textView3);

        // Add TableRow to TableLayout
        tableLayout.addView(tableRow);
    }
}