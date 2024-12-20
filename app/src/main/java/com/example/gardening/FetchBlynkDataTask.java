package com.example.gardening;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FetchBlynkDataTask {

    private Context context;


    public FetchBlynkDataTask(Context context) {
        this.context = context;
    }

    // Use Executor for background task
    public void fetchDataFromBlynk(String url) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String result = fetchData(url);

            // Use Handler to update UI from background thread
            new Handler(Looper.getMainLooper()).post(() -> {
                if (result != null) {
                    try {
                        // Parse the result as a JSON object
                        JSONObject jsonResponse = new JSONObject(result);

                        // Assuming the JSON response contains keys "soil_moisture" and "humidity"
                        String soilMoistureValue = jsonResponse.optString("V2");
                        String humidityValue = jsonResponse.optString("V1");
                        String temperatureValue = jsonResponse.optString("V0");


                        if (!soilMoistureValue.isEmpty() && !humidityValue.isEmpty() &&!temperatureValue.isEmpty()) {
                            // Update soil moisture TextView
                            TextView soilMoistureTextView = ((Activity) context).findViewById(R.id.soilMoistureTextView);
                            soilMoistureTextView.setText("Soil Moisture: " + soilMoistureValue);
                            Log.d("ParsedData", "Soil Moisture: " + soilMoistureValue);

                            // Update humidity TextView
                            TextView humidityTextView = ((Activity) context).findViewById(R.id.humidityTextView);
                            humidityTextView.setText("Humidity: " + humidityValue);
                            Log.d("ParsedData", "Humidity: " + humidityValue);

                            // Update temperature TextView
                            TextView temperatureTextView = ((Activity) context).findViewById(R.id.temperatureTextView);
                            temperatureTextView.setText("Temperature: " + temperatureValue);
                            Log.d("ParsedData", "Temperature: " + temperatureValue);
                        } else {
                            Toast.makeText(context, "Unexpected response format or empty values", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error parsing JSON data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error
                    Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String fetchData(String urlString) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Read the response from Blynk Cloud
            InputStream inputStream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            String result = response.toString(); // Convert the response to a string
            Log.d("BlynkResponse", result); // Log the raw response to see what you're getting

            return result; // Return the response
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) connection.disconnect();
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
