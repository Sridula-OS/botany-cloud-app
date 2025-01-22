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
import java.io.IOException;

public class RoseActivity extends BaseActivity {
    private TextView soilMoistureTextView;
    private TextView humidityTextView;
    private TextView temperatureTextView;
    private EditText precipitationEditText;
    private TextView resultTextView;
    private Spinner daySpinner, potSizeSpinner;
    private String potSizeCategory;
    private String soiltype;
    private float waterNeeded=0.0f;

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

        // Fetch Soil Moisture Data
        String blynkUrl = "https://blr1.blynk.cloud/external/api/get?token=08HSHBGfNPa53CbXaFQIOWsqQ-c4xDhP&V1&V2&V0";  // For soil moisture
        new FetchBlynkDataTask(this).fetchDataFromBlynk(blynkUrl);


        // Setup Navigation Bar
        setupNavigationBar();

        // Calculate Water Requirement on button click
        findViewById(R.id.calculateButton).setOnClickListener(v -> calculateWaterRequirement());

        findViewById(R.id.waterplantButton).setOnClickListener(v -> waterplant());
    }

    private void waterplant(){
        if(waterNeeded>0)
        {
            float time=20 * waterNeeded* 1000;

            //to turn the relay on
            String relayONUrl = "https://blr1.blynk.cloud/external/api/update?token=08HSHBGfNPa53CbXaFQIOWsqQ-c4xDhP&V12=1";  // For soil moisture

            // Call the updateData method and pass the callback to handle the result
            new FetchBlynkDataTask(this).updateData(relayONUrl,  new FetchBlynkDataTask.UpdateDataCallback() {
                @Override
                public void onDataUpdated(boolean success) {
                    if (success) {
                        // Successfully updated data
                        Log.d("RoseActivity", "Relay turned ON");
                        // Do something after success, like updating UI
                    } else {
                        // Failed to update data
                        Log.d("RoseActivity", "Failed to turn the relay ON");
                        // Handle failure, show an error message, etc.
                    }
                }
            });

            //to turn the relay off after time period
            new Handler().postDelayed(() -> {
                String relayOFFUrl = "https://blr1.blynk.cloud/external/api/update?token=08HSHBGfNPa53CbXaFQIOWsqQ-c4xDhP&V12=0";  // For soil moisture

                // Call the updateData method and pass the callback to handle the result
                new FetchBlynkDataTask(this).updateData(relayOFFUrl, new FetchBlynkDataTask.UpdateDataCallback() {
                    @Override
                    public void onDataUpdated(boolean success) {
                        if (success) {
                            // Successfully updated data
                            Log.d("RoseActivity", "Relay turned OFF");
                            // Do something after success, like updating UI
                        } else {
                            // Failed to update data
                            Log.d("RoseActivity", "Failed to turn the relay OFF");
                            // Handle failure, show an error message, etc.
                        }
                    }
                });
                Toast.makeText(RoseActivity.this, "Relay turned OFF after"+time+ "milliseconds", Toast.LENGTH_SHORT).show();
            }, (long) time);
            waterNeeded=0.0f;

        }
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
