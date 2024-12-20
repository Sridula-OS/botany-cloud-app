package com.example.gardening;

import android.content.Context;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraX;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;
import androidx.camera.view.PreviewView;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class PlantRecognitionActivity extends BaseActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private PreviewView previewView;  // CameraX preview view
    private ProcessCameraProvider cameraProvider;  // Camera provider for binding use cases
    private Preview preview;  // Preview use case for camera display
    private ImageCapture imageCapture;  // Image capture use case for taking pictures

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_recognition);

        // Initialize the preview view
        previewView = findViewById(R.id.previewView);

        // Find the open camera button
        Button openCameraButton = findViewById(R.id.openCameraButton);

        // Set up button click listener to open the camera
        openCameraButton.setOnClickListener(v -> {
            // Check for camera permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                // Open the camera using CameraX
                startCamera();
            } else {
                // Request camera permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            }
        });

        // Add Plants button logic remains here
        setupNavigationBar(); // Call to set up navigation
    }

    // Start CameraX
    private void startCamera() {
        // Get the camera provider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Camera provider is now available
                cameraProvider = cameraProviderFuture.get();

                // Create a camera selector to choose which camera to use (front or back)
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)  // Use rear camera, change to FRONT for front camera
                        .build();

                // Create a preview instance
                preview = new Preview.Builder().build();

                // Bind the preview to the lifecycle
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind the camera to lifecycle and start the preview
                Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);

                Toast.makeText(this, "Camera started", Toast.LENGTH_SHORT).show();

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (cameraProvider != null) {
            cameraProvider.unbind(preview, imageCapture); // Unbind use cases explicitly
        }
    }


}
