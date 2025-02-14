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

public class SleepLogs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sleep_logs);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Button back = findViewById(R.id.symptomLogsBackButton);
        //back.setOnClickListener(v -> startActivity(new Intent(SleepLogs.this, Homescreen.class)));

        TableLayout tableLayout = findViewById(R.id.tableLayout);

        TableRow tableRow = new TableRow(this);

        TextView dateText = new TextView(this);
        dateText.setText("02/12/2024");
        dateText.setPadding(20, 8, 8, 8);

        TextView hoursSleptText = new TextView(this);
        hoursSleptText.setText("7.5 hrs");
        hoursSleptText.setPadding(20, 8, 8, 8);

        TextView sleepQualityText = new TextView(this);
        sleepQualityText.setText("9");
        sleepQualityText.setPadding(20, 8, 8, 8);

        // Add TextViews to the TableRow
        tableRow.addView(dateText);
        tableRow.addView(hoursSleptText);
        tableRow.addView(sleepQualityText);

        // Add TableRow to TableLayout
        tableLayout.addView(tableRow);
    }
}
