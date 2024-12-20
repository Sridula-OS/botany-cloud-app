package com.example.gardening;

//--------------------------------------------------------------------------------------------------
//plantnet
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;
import android.widget.TextView;

//--------------------------------------------------------------------------------------------------

import android.content.Context;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutionException;

public class PlantRecognitionActivity extends BaseActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private PreviewView previewView;  // CameraX preview view
    private ProcessCameraProvider cameraProvider;  // Camera provider for binding use cases
    private Preview preview;  // Preview use case for camera display
    private ImageCapture imageCapture;  // Image capture use case for taking pictures

    //----------------------------------------------------------------------------------------------
    //plantnet api
    private static final String API_URL = "https://my-api.plantnet.org/v2/identify/all?include-related-images=false&no-reject=false&nb-results=10&lang=en&api-key=2b103K7gnRMAgSd3jaMbiAw8e";
    //----------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------
    //from lumos
    private ByteBuffer inputBuffer;
    private ByteBuffer buffer;
    private byte[] reusableByteArray; // Reusable array
    ImageView imageView;
    Bitmap bitmap;

    //----------------------------------------------------------------------------------------------
    TextView scientificNameTextView, genusTextView, familyTextView, commonNamesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_recognition);

        // Initialize the preview view
        previewView = findViewById(R.id.previewView);
        scientificNameTextView=findViewById(R.id.scientificNameTextView);
        genusTextView=findViewById(R.id.genusTextView);
        familyTextView=findViewById(R.id.familyTextView);
        commonNamesTextView=findViewById(R.id.commonNamesTextView);


        //------------------------------------------------------------------------------------------
        //from lumos
        imageView = findViewById(R.id.imageView);
        inputBuffer = ByteBuffer.allocateDirect(640 * 640 * 3 * 4); // Reuse this buffer across calls
        inputBuffer.order(ByteOrder.nativeOrder());
        //------------------------------------------------------------------------------------------


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

                //----------------------------------------------------------------------------------
                //from lumos
                imageCapture = new ImageCapture.Builder().build();
                //----------------------------------------------------------------------------------

                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind the camera to lifecycle and start the preview-----modified from lumos
                Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);

                Toast.makeText(this, "Camera started", Toast.LENGTH_SHORT).show();

                Button clickPhotoButton = findViewById(R.id.clickPhotoButton);
                clickPhotoButton.setOnClickListener(v -> {
                    startImageCapture();
                });


            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }



    //----------------------------------------------------------------------------------------------
    //from lumos
    private void startImageCapture() {
        if (imageCapture != null) {
            imageCapture.takePicture(ContextCompat.getMainExecutor(PlantRecognitionActivity.this),
                    new ImageCapture.OnImageCapturedCallback() {
                        @Override
                        public void onCaptureSuccess(@NonNull ImageProxy image) {
                            Log.d("yoohooimagecapture", "Entered image capture");
                            Bitmap localbitmap = imageToBitmap(image);
                            bitmap=localbitmap;
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                                imageView.setVisibility(View.VISIBLE); // Make ImageView visible
                                previewView.setVisibility(View.GONE); // Hide PreviewView
                                Toast.makeText(PlantRecognitionActivity.this, "Image captured!", Toast.LENGTH_SHORT).show();

                                executeSendImageTask(PlantRecognitionActivity.this ,bitmap);

                                // Optional: Hide the captured image after a delay and show live feed again
                                imageView.postDelayed(() -> {
                                    scientificNameTextView.setVisibility(View.GONE);
                                    genusTextView.setVisibility(View.GONE);
                                    familyTextView.setVisibility(View.GONE);
                                    commonNamesTextView.setVisibility(View.GONE);
                                    imageView.setVisibility(View.GONE); // Hide ImageView
                                    previewView.setVisibility(View.VISIBLE); // Show PreviewView
                                }, 20000); // Delay in milliseconds (20 seconds)

                            }
                            image.close();
                        }
                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            exception.printStackTrace();
                        }
                    });
        }

    }

    private Bitmap imageToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();

        reusableByteArray=null;
        buffer = planes[0].getBuffer();
        reusableByteArray = new byte[buffer.remaining()];
        buffer.get(reusableByteArray);
        Bitmap bitmap= BitmapFactory.decodeByteArray(reusableByteArray, 0, reusableByteArray.length);
        if (bitmap == null) {
            Log.e("yoohoo", "Failed to decode bitmap. Possible reasons: invalid or corrupted data.");
        } else {
            Log.d("yoohoo", "Bitmap decoded successfully.");
        }
        buffer.rewind();
        // Rotate bitmap to correct orientation if necessary
        return fixRotation(bitmap);

    }

    private Bitmap fixRotation(Bitmap bitmap) {
        // Assuming you know the correct angle of rotation (90 degrees counterclockwise in your case)
        Matrix matrix = new Matrix();
        matrix.postRotate(90); // Adjust rotation if needed (e.g., 90, 180, 270 degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    //----------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------
    //plantnet api
    //convert bitmap to jpeg
    private static File convertBitmapToJPEG(Context context,Bitmap bitmap) throws Exception {
        // Save the JPEG file in external storage
        File storageDir = context.getExternalFilesDir("PlantImages"); // "PlantImages" is a subdirectory
        if (storageDir != null && !storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                throw new Exception("Failed to create directory.");
            }
        }

        File jpegFile = new File(storageDir, "plant_image.jpeg");
        FileOutputStream fos = new FileOutputStream(jpegFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        return jpegFile;
    }

    public static String sendImageToPlantNetAPI(Context context,Bitmap bitmap) {
        HttpURLConnection connection = null;
        try {
            // Convert Bitmap to JPEG and get the file
            File jpegFile = convertBitmapToJPEG(context, bitmap);

            // Open connection to the PlantNet API
            URL url = new URL(API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=***");

            // Create multipart data
            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            dos.writeBytes("--***\r\n");
            dos.writeBytes("Content-Disposition: form-data; name=\"images\"; filename=\"" + jpegFile.getName() + "\"\r\n");
            dos.writeBytes("Content-Type: image/jpeg\r\n\r\n");

            // Write the JPEG file to the output stream
            FileInputStream fis = new FileInputStream(jpegFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
            fis.close();

            dos.writeBytes("\r\n--***--\r\n");
            dos.flush();
            dos.close();

            // Get the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = connection.getInputStream();
                StringBuilder response = new StringBuilder();
                byte[] responseBuffer = new byte[1024];
                int responseBytes;
                while ((responseBytes = is.read(responseBuffer)) != -1) {
                    response.append(new String(responseBuffer, 0, responseBytes));
                }
                is.close();
                return response.toString();
            } else {
                Log.e("yoohoo", "HTTP Error: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            Log.e("yoohoo", "Error while sending image to PlantNet API: ", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    public void executeSendImageTask(Context context,Bitmap bitmap) {
        // Create a single-threaded executor
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        // Submit the task to the executor
        executorService.execute(() -> {
            try {
                // Call your network function
                String response = sendImageToPlantNetAPI(context, bitmap);


                // Update UI on the main thread with the response
                runOnUiThread(() -> {
                    if (response != null) {
                        // Process and display the response
                        try {
                            // Parse the JSON response
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONArray results = jsonResponse.getJSONArray("results");

                            if (results.length() > 0) {
                                JSONObject bestMatch = results.getJSONObject(0); // Assuming best match is the first result
                                JSONObject species = bestMatch.getJSONObject("species");
                                JSONObject genus = species.getJSONObject("genus");
                                JSONObject family = species.getJSONObject("family");

                                String scientificName = species.getString("scientificNameWithoutAuthor");
                                String genusName = genus.getString("scientificName");
                                String familyName = family.getString("scientificName");
                                String commonNames = TextUtils.join((CharSequence) ", ", new JSONArray[]{species.getJSONArray("commonNames")});

                                // Find the TextViews and update them with the relevant information
                                TextView scientificNameTextView = ((Activity) context).findViewById(R.id.scientificNameTextView);
                                imageView.setVisibility(View.GONE); // Hide ImageVIew
                                previewView.setVisibility(View.GONE);// Hide PreviewView

                                scientificNameTextView.setVisibility(View.VISIBLE);
                                scientificNameTextView.setText("Scientific Name: " + scientificName);

                                TextView genusTextView = ((Activity) context).findViewById(R.id.genusTextView);
                                genusTextView.setVisibility(View.VISIBLE);
                                genusTextView.setText("Genus: " + genusName);

                                TextView familyTextView = ((Activity) context).findViewById(R.id.familyTextView);
                                familyTextView.setVisibility(View.VISIBLE);
                                familyTextView.setText("Family: " + familyName);


                                // Split the string by commas and trim each name
                                commonNames = commonNames.replace("[", "").replace("]", "").replace("\"", "");
                                String[] namesArray = commonNames.split(",");
                                StringBuilder commonNamesText = new StringBuilder();

                                commonNamesText.append("\n");

                                // Loop through each common name and add it to the StringBuilder with a newline
                                for (String commonName : namesArray) {
                                    commonNamesText.append(commonName.trim()).append("\n");
                                }

                                // Set the formatted text to the TextView
                                commonNamesTextView.setText(commonNamesText.toString());

                                TextView commonNamesTextView = ((Activity) context).findViewById(R.id.commonNamesTextView);
                                commonNamesTextView.setVisibility(View.VISIBLE);
                                commonNamesTextView.setText("Common names : " + commonNamesText);
                            } else {
                                Log.e("PlantNetResponse", "No results found.");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("PlantNetResponse", "Error parsing response: " + e.getMessage());
                        }
                    } else {
                        Log.e("PlantNetResponse", "Failed to get a response.");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Handle the exception on the main thread
                runOnUiThread(() -> Log.e("PlantNetResponse", "Error while sending image to PlantNet API: " + e.getMessage()));
            }
        });
        // Shutdown the executor (optional)
        executorService.shutdown();
    }

    //----------------------------------------------------------------------------------------------

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