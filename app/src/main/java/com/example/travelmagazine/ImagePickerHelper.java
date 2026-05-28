package com.example.travelmagazine;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import java.util.function.Consumer;

public class ImagePickerHelper implements DefaultLifecycleObserver {
    private AppCompatActivity activity;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private Consumer<String> onImageSelected;
    private boolean isLauncherReady = false;

    public ImagePickerHelper(AppCompatActivity activity) {
        this.activity = activity;
        registerLauncher();
    }

    private void registerLauncher() {
        if (galleryLauncher != null) return;

        try {
            galleryLauncher = activity.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                            Uri selectedImageUri = result.getData().getData();
                            if (selectedImageUri != null && onImageSelected != null) {
                                onImageSelected.accept(selectedImageUri.toString());
                            } else {
                                Toast.makeText(activity, "Фото не выбрано", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
            isLauncherReady = true;
        } catch (IllegalStateException e) {
            activity.getLifecycle().addObserver(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        if (!isLauncherReady && galleryLauncher == null) {
            registerLauncher();
        }
        owner.getLifecycle().removeObserver(this);
    }

    public void pickImage(Consumer<String> callback) {
        this.onImageSelected = callback;

        if (!isLauncherReady && galleryLauncher == null) {
            registerLauncher();
        }

        if (galleryLauncher == null) {
            Toast.makeText(activity, "Ошибка: фото-помощник не инициализирован", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        try {
            galleryLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(activity, "Ошибка открытия галереи: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}