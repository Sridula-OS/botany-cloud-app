package com.example.gardening;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Add Plants button
        Button addPlantsButton = findViewById(R.id.addPlantsButton);
        addPlantsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Navigation buttons
        Button homeButton = findViewById(R.id.homeButton);
        Button weatherButton = findViewById(R.id.weatherButton);
        Button chatbotButton = findViewById(R.id.chatbotButton);
        Button plantRecognitionButton = findViewById(R.id.plantRecognitionButton);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on HomeActivity
            }
        });

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, WeatherActivity.class);
                startActivity(intent);
            }
        });

        chatbotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ChatbotActivity.class);
                startActivity(intent);
            }
        });

        plantRecognitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, PlantRecognitionActivity.class);
                startActivity(intent);
            }
        });
    }
}

