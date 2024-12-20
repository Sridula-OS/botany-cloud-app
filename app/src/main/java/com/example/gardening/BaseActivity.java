package com.example.gardening;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupNavigationBar() {
        Button homeButton = findViewById(R.id.homeButton);
        Button weatherButton = findViewById(R.id.weatherButton);
        Button chatbotButton = findViewById(R.id.chatbotButton);
        Button plantRecognitionButton = findViewById(R.id.plantRecognitionButton);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaseActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaseActivity.this, WeatherActivity.class);
                startActivity(intent);
            }
        });

        chatbotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaseActivity.this, ChatbotActivity.class);
                startActivity(intent);
            }
        });

        plantRecognitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaseActivity.this, PlantRecognitionActivity.class);
                startActivity(intent);
            }
        });
    }
}
