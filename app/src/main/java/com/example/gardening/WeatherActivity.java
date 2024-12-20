package com.example.gardening;

import android.os.Bundle;

import android.content.Intent;
import android.net.Uri;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WeatherActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Initialize the weather forecast button
        Button weatherForecastButton = findViewById(R.id.weatherForecastButton);

        weatherForecastButton.setOnClickListener(v -> {
            // Replace the URL with your desired link
            String url = "https://www.foreca.com/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        // Add Plants button logic remains here
        setupNavigationBar(); // Call to set up navigation

    }
}