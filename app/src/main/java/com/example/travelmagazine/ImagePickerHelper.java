package com.example.travelmagazine;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.util.function.Consumer;

public class ImagePickerHelper {
    private AppCompatActivity activity;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private Consumer<String> onImageSelected;
    private boolean isLauncherInitialized = false;

    public ImagePickerHelper(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void initialize() {
        if (isLauncherInitialized) return;

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
            isLauncherInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "Ошибка инициализации: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void pickImage(Consumer<String> callback) {
        this.onImageSelected = callback;

        if (!isLauncherInitialized) {
            initialize();
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (galleryLauncher != null) {
            try {
                galleryLauncher.launch(intent);
            } catch (Exception e) {
                Toast.makeText(activity, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, "Ошибка открытия галереи", Toast.LENGTH_SHORT).show();
        }
    }
}