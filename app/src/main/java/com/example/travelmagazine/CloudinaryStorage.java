package com.example.travelmagazine;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CloudinaryStorage {

    private static final String TAG = "CloudinaryStorage";
    private static boolean isInitialized = false;
    private static final String CLOUD_NAME = "dbydikdn5";
    private static final String API_KEY = "992389361163155";
    private static final String API_SECRET = "KByehN7-yn1YMecseO9TwuY2nC0";

    public static void initialize(Context context) {
        if (isInitialized) return;

        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("api_key", API_KEY);
            config.put("api_secret", API_SECRET);

            MediaManager.init(context, config);
            isInitialized = true;
            Log.d(TAG, "Cloudinary initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Cloudinary init error: " + e.getMessage());
        }
    }

    public static void uploadImageSimple(Uri imageUri, OnImageUploadListener listener) {
        if (!isInitialized) {
            Log.e(TAG, "Cloudinary not initialized!");
            if (listener != null) {
                listener.onError("Cloudinary not initialized");
            }
            return;
        }

        String publicId = "excursion_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString();

        // Упрощённая загрузка БЕЗ параметров трансформации
        MediaManager.get()
                .upload(imageUri)
                .option("public_id", publicId)
                .option("folder", "travel_magazine/excursions")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload started: " + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long total) {
                        Log.d(TAG, "Upload progress: " + (bytes * 100 / total) + "%");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        Log.d(TAG, "Upload success! URL: " + url);
                        if (listener != null) {
                            listener.onSuccess(url);
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Upload error: " + error.getDescription());
                        if (listener != null) {
                            listener.onError(error.getDescription());
                        }
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.d(TAG, "Upload rescheduled");
                    }
                })
                .dispatch();
    }

    public interface OnImageUploadListener {
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
    }
}