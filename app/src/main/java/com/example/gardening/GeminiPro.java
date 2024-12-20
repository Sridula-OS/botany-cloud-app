package com.example.gardening;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.concurrent.Executor;

public class GeminiPro {

    private static final String API_KEY = "AIzaSyAFfDu8m9NcqtmM2wCB4dAFhx1Linrpli4"; // Move your API Key to a secure location later!

    public void getResponse(String query, ResponseCallback callback) {
        GenerativeModelFutures model = getModel();

        Content content = new Content.Builder().addText(query).build();
        Executor executor = Runnable::run;

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                callback.onResponse(resultText); // Pass the response to callback
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
                callback.onError(throwable); // Pass error to callback
            }
        }, executor);
    }

    private GenerativeModelFutures getModel() {
        SafetySetting harassmentSafety = new SafetySetting(HarmCategory.HARASSMENT,
                BlockThreshold.ONLY_HIGH);

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.9f; // Customize as needed
        configBuilder.topK = 16;
        configBuilder.topP = 0.1f;
        GenerationConfig generationConfig = configBuilder.build();

        GenerativeModel gm = new GenerativeModel(
                "gemini-pro",
                API_KEY,
                generationConfig,
                Collections.singletonList(harassmentSafety)
        );

        return GenerativeModelFutures.from(gm);
    }
}
