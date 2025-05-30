package com.example.gardening;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class RoseActivity extends BaseActivity {
    private TextView soilMoistureTextView;
    private TextView humidityTextView;
    private TextView temperatureTextView;
    private TextView phTextView;
    private EditText precipitationEditText;
    private TextView resultTextView;
    private Spinner daySpinner, potSizeSpinner;
    private String potSizeCategory;
    private String soiltype;
    private float waterNeeded=0.0f;
    private DatabaseReference pumpStatusRef;
    private DatabaseReference servoStatusRef;
    private boolean isSprinklerOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rose);

        // Get the pot size category from intent
        potSizeCategory = getIntent().getStringExtra("POT_SIZE_CATEGORY");
        soiltype=getIntent().getStringExtra("SOIL_TYPE");

        // Initialize views
        soilMoistureTextView = findViewById(R.id.soilMoistureTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        temperatureTextView=findViewById(R.id.temperatureTextView);
        phTextView = findViewById(R.id.phTextView);
        precipitationEditText = findViewById(R.id.precipitationEditText);
        resultTextView = findViewById(R.id.resultTextView);

        // Initialize Spinners
        daySpinner = findViewById(R.id.daySpinner);
        potSizeSpinner = findViewById(R.id.potSizeSpinner);

        // Set up day spinner
        String[] dayOptions = {"<28", ">=28"};
        daySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dayOptions));

        // Set up pot size spinner based on category
        String[] potSizes;
        switch (potSizeCategory.toLowerCase()) {
            case "small":
                potSizes = new String[]{"0.01", "0.015", "0.02", "0.025"};
                break;
            case "medium":
                potSizes = new String[]{"0.03", "0.05", "0.06", "0.08"};
                break;
            case "large":
                potSizes = new String[]{"0.10", "0.25", "0.5", "1.0"};
                break;
            default:
                potSizes = new String[]{"0.25", "0.5", "0.75", "1.0"};
                break;
        }
        potSizeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, potSizes));

        // Initialize Firebase Database references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        pumpStatusRef = database.getReference("pumpStatus");
        servoStatusRef = database.getReference("ServoControl/servo0/status");
// Add listener for sprinkler status changes
        servoStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer status = dataSnapshot.getValue(Integer.class);
                    if (status != null) {
                        isSprinklerOn = status == 1;

                        // Show message when sprinkler turns off automatically
                        if (status == 0 && isSprinklerOn) {
                            Toast.makeText(RoseActivity.this,
                                    "Sprinkler turned off automatically",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("RoseActivity", "Failed to read servo status", databaseError.toException());
            }
        });


        // Fetch sensor data
        FetchFirebaseDataTask fetchTask = new FetchFirebaseDataTask(this);
        fetchTask.fetchDataFromFirebase();
        // Setup Navigation Bar
        setupNavigationBar();

        // Calculate Water Requirement on button click
        findViewById(R.id.calculateButton).setOnClickListener(v -> calculateWaterRequirement());

        findViewById(R.id.waterplantButton).setOnClickListener(v -> waterplant());
        findViewById(R.id.sprinklerButton).setOnClickListener(v -> toggleSprinkler());
    }

    private void waterplant() {
        if (waterNeeded > 0) {
            // Calculate pump duration in milliseconds (20ms per unit of waterNeeded)
            long pumpDuration = (long) (20 * waterNeeded * 1000);

            // Turn the pump ON
            pumpStatusRef.setValue(1)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("RoseActivity", "Pump turned ON");
                        Toast.makeText(RoseActivity.this,
                                "Watering plant for " + (pumpDuration/1000) + " seconds",
                                Toast.LENGTH_SHORT).show();

                        // Schedule to turn the pump OFF after the calculated duration
                        new Handler().postDelayed(() -> {
                            pumpStatusRef.setValue(0)
                                    .addOnSuccessListener(aVoid1 -> {
                                        Log.d("RoseActivity", "Pump turned OFF");
                                        Toast.makeText(RoseActivity.this,
                                                "Watering completed",
                                                Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("RoseActivity", "Failed to turn pump OFF", e);
                                        Toast.makeText(RoseActivity.this,
                                                "Failed to turn pump OFF",
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }, pumpDuration);

                        waterNeeded = 0.0f;
                    })
                    .addOnFailureListener(e -> {
                        Log.e("RoseActivity", "Failed to turn pump ON", e);
                        Toast.makeText(RoseActivity.this,
                                "Failed to start watering",
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Please calculate water requirement first", Toast.LENGTH_SHORT).show();
        }
    }



    private void toggleSprinkler() {
        int newStatus = isSprinklerOn ? 0 : 1;
        servoStatusRef.setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    String message = newStatus == 1 ?
                            "Sprinkler turned ON" : "Sprinkler turned OFF";
                    Toast.makeText(RoseActivity.this, message, Toast.LENGTH_SHORT).show();
                    Log.d("RoseActivity", message);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RoseActivity.this,
                            "Failed to control sprinkler",
                            Toast.LENGTH_SHORT).show();
                    Log.e("RoseActivity", "Failed to control sprinkler", e);
                });
    }

    private void calculateWaterRequirement() {
        float baseWaterRequirement=2.0f;
        try {
            String daySelection = daySpinner.getSelectedItem().toString();
            String potSizeSelection = potSizeSpinner.getSelectedItem().toString();
            String precipitationInput = precipitationEditText.getText().toString();
            String soilMoistureText = soilMoistureTextView.getText().toString();

            if (TextUtils.isEmpty(precipitationInput)) {
                Toast.makeText(this, "Please enter precipitation value", Toast.LENGTH_SHORT).show();
                return;
            }

            float soilMoisture = extractSoilMoistureValue(soilMoistureText);
            float precipitation = Float.parseFloat(precipitationInput);
            float potArea = Float.parseFloat(potSizeSelection);

            // Adjust daily water requirement based on pot size category
            switch (soiltype.toLowerCase()) {
                case "loamy":baseWaterRequirement = daySelection.equals("<28") ? 2.25f : 1.45f;break;
                case "black": baseWaterRequirement = daySelection.equals("<28") ? 2f : 1.25f;break;
                case "sandy loam": baseWaterRequirement = daySelection.equals("<28") ? 2.5f : 1.65f;break;
            }

            float sizeMultiplier;
            switch (potSizeCategory.toLowerCase()) {
                case "medium":
                    sizeMultiplier = 1.5f;
                    break;
                case "large":
                    sizeMultiplier = 2.0f;
                    break;
                default: // small
                    sizeMultiplier = 1.0f;
                    break;
            }

            float dailyWaterRequirement = baseWaterRequirement * sizeMultiplier;
            float precipitationContribution = precipitation * potArea;
            float soilMoistureAdjustment = (100 - soilMoisture) / 100.0f;
            waterNeeded = (dailyWaterRequirement * potArea * soilMoistureAdjustment - precipitationContribution)*10;
            waterNeeded = Math.max(0, waterNeeded);

            resultTextView.setText(String.format("Water Needed: %.2f Litres", waterNeeded));

        } catch (Exception e) {
            Toast.makeText(this, "Error calculating water requirement", Toast.LENGTH_SHORT).show();
        }
    }

    private float extractSoilMoistureValue(String text) {
        try {
            if (text.contains(":") && text.contains("%")) {
                String[] parts = text.split(":");
                return Float.parseFloat(parts[1].replace("%", "").trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 50.0f;
    }
}
