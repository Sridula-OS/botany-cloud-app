package com.example.gardening;

public interface ResponseCallback {
    void onResponse(String response);
    void onError(Throwable throwable);
}
