package com.example.gardening;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class ChatbotActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Add Plants button logic remains here
        setupNavigationBar(); // Call to set up navigation

        // Views
        TextInputEditText queryEditText = findViewById(R.id.queryEditText);
        Button sendQueryButton = findViewById(R.id.sendPromptButton);
        TextView responseTextView = findViewById(R.id.modelResponseTextView);
        ProgressBar progressBar = findViewById(R.id.sendPromptProgressBar);

        // Focus and show the keyboard when the user clicks the input box
        queryEditText.requestFocus(); // Request focus for the input field
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(queryEditText, InputMethodManager.SHOW_IMPLICIT); // Show keyboard

        sendQueryButton.setOnClickListener(v -> {
            String query = queryEditText.getText().toString();

            if (query.isEmpty()) {
                Toast.makeText(this, "Please enter a query", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            responseTextView.setText(""); // Clear previous response
            queryEditText.setText(""); // Clear the input field


            // Create GeminiPro instance and get the response
            GeminiPro geminiPro = new GeminiPro();
            geminiPro.getResponse(query, new ResponseCallback() {
                @Override
                public void onResponse(String response) {
                    responseTextView.setText(response); // Set the response text
                    progressBar.setVisibility(View.GONE); // Hide progress bar
                }

                @Override
                public void onError(Throwable throwable) {
                    Toast.makeText(ChatbotActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE); // Hide progress bar
                }
            });

        });
    }

}
