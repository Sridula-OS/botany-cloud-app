package com.example.gardening;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FetchFirebaseDataTask {

    private Context context;
    private DatabaseReference databaseReference;
    private DatabaseReference latestDataNumberRef;
    private int currentLatestNumber = 0;

    public FetchFirebaseDataTask(Context context) {
        this.context = context;
        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Reference to your data location in Firebase
        databaseReference = database.getReference("Sensor/SensorData");
        // Reference to the latest data number
        latestDataNumberRef = database.getReference("Sensor/latestDataNumber");
    }

    public void fetchDataFromFirebase() {
        // First listen for changes in the latest data number
        latestDataNumberRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (!dataSnapshot.exists()) {
                        Toast.makeText(context, "No latest data number available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Integer latestNumber = dataSnapshot.getValue(Integer.class);
                    if (latestNumber != null && latestNumber != currentLatestNumber) {
                        int dataIndex = (latestNumber - 2 + 99) % 99 + 1;
                        currentLatestNumber = dataIndex;
                        fetchSpecificSensorData(dataIndex);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error reading latest number: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("FirebaseError", "Failed to read latest number.", databaseError.toException());
                Toast.makeText(context, "Failed to fetch latest number: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSpecificSensorData(int dataNumber) {
        databaseReference.child(String.valueOf(dataNumber)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (!dataSnapshot.exists()) {
                        Toast.makeText(context, "No data available for number " + dataNumber, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get values from Firebase
                    Double moistureValue = dataSnapshot.child("moisture").getValue(Double.class);
                    Double humidityValue = dataSnapshot.child("humidity").getValue(Double.class);
                    Double temperatureValue = dataSnapshot.child("temperature").getValue(Double.class);
                    Double phValue = dataSnapshot.child("ph").getValue(Double.class);

                    if (moistureValue != null && humidityValue != null && temperatureValue != null && phValue != null) {
                        // Update soil moisture TextView
                        TextView soilMoistureTextView = ((Activity) context).findViewById(R.id.soilMoistureTextView);
                        soilMoistureTextView.setText(String.format("Soil Moisture: %.1f%%", moistureValue));
                        Log.d("ParsedData", "Soil Moisture: " + moistureValue);

                        // Update humidity TextView
                        TextView humidityTextView = ((Activity) context).findViewById(R.id.humidityTextView);
                        humidityTextView.setText(String.format("Humidity: %.1f%%", humidityValue));
                        Log.d("ParsedData", "Humidity: " + humidityValue);

                        // Update temperature TextView
                        TextView temperatureTextView = ((Activity) context).findViewById(R.id.temperatureTextView);
                        temperatureTextView.setText(String.format("Temperature: %.1f°C", temperatureValue));
                        Log.d("ParsedData", "Temperature: " + temperatureValue);

                        // Update ph TextView
                        TextView phTextView = ((Activity) context).findViewById(R.id.phTextView);
                        phTextView.setText(String.format("pH: %.1f", phValue));  // Fixed this line
                        Log.d("ParsedData", "pH: " + phValue);
                    } else {
                        Toast.makeText(context, "Data not complete", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error reading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("FirebaseError", "Failed to read sensor data.", databaseError.toException());
                Toast.makeText(context, "Failed to fetch sensor data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateData(String valueName, String value, UpdateDataCallback callback) {
        // Create a new entry with push ID
        DatabaseReference newRef = databaseReference.push();
        newRef.child(valueName).setValue(value)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseUpdate", "Value updated successfully");
                    callback.onDataUpdated(true);
                })
                .addOnFailureListener(e -> {
                    Log.w("FirebaseUpdate", "Error updating value", e);
                    callback.onDataUpdated(false);
                });
    }

    public interface UpdateDataCallback {
        void onDataUpdated(boolean success);
    }
}