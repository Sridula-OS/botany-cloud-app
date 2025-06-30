package com.example.gardening;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroqChat {

    private static final String API_KEY = "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"; // 🔐 Replace with your real key from https://console.groq.com
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    public void getResponse(String query, ResponseCallback callback) {
        try {
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", query);

            JSONArray messages = new JSONArray();
            messages.put(userMessage);

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("model", "llama3-8b-8192"); // ✅ You can change this to other Groq models
            bodyJson.put("messages", messages);

            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                Handler handler = new Handler(Looper.getMainLooper());

                @Override
                public void onFailure(Call call, IOException e) {
                    handler.post(() -> callback.onError(e));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        handler.post(() -> callback.onError(new IOException("Unexpected code " + response)));
                        return;
                    }

                    String responseStr = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseStr);
                        String reply = json
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        handler.post(() -> callback.onResponse(reply));
                    } catch (Exception e) {
                        handler.post(() -> callback.onError(e));
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e);
        }
    }
}
