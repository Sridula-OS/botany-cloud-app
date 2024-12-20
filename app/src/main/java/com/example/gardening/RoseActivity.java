package com.example.gardening;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class RoseActivity extends BaseActivity {

    private TextView soilMoistureTextView;
    private TextView humidityTextView;
    private boolean expectingSoilMoisture = true; // Start by expecting soil moisture


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rose);  // Reference the correct XML

        soilMoistureTextView = findViewById(R.id.soilMoistureTextView);
        humidityTextView = findViewById(R.id.humidityTextView);

        // Fetch Soil Moisture Data
        String blynkUrl = "https://blr1.blynk.cloud/external/api/get?token=08HSHBGfNPa53CbXaFQIOWsqQ-c4xDhP&V1&V2&V0";  // For soil moisture
        new FetchBlynkDataTask(this).fetchDataFromBlynk(blynkUrl);

        // Introduce a delay of 1 second (1000 milliseconds) before fetching humidity data
        /*new Handler().postDelayed(() -> {
            String blynkUrlHumidity = "https://blr1.blynk.cloud/external/api/get?token=08HSHBGfNPa53CbXaFQIOWsqQ-c4xDhP&V2";  // For humidity
            new FetchBlynkDataTask(this).fetchDataFromBlynk(blynkUrlHumidity);
        }, 1000); // 1000 milliseconds = 1 second delay  */

        //String blynkUrlHumidity = "https://blr1.blynk.cloud/external/api/get?token=08HSHBGfNPa53CbXaFQIOWsqQ-c4xDhP&V2";  // For humidity
        //new FetchBlynkDataTask(this).fetchDataFromBlynk(blynkUrlHumidity);




        setupNavigationBar(); // Call to set up navigation

    }
}
