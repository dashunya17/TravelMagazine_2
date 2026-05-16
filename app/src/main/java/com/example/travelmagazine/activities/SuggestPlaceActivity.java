package com.example.travelmagazine.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.travelmagazine.CloudinaryStorage;
import com.example.travelmagazine.R;
import com.example.travelmagazine.attributes.appeal;
import com.example.travelmagazine.ImagePickerHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SuggestPlaceActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 100;

    private EditText editName, editDescription;
    private ImageView imageViewPreview;
    private Button buttonSelectImage, buttonSubmit;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ImagePickerHelper imagePickerHelper;
    private String uploadedImageUrl = null;
    private String selectedImageUri = null;
    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_place);

        // Проверяем авторизацию перед загрузкой UI
        checkAuthAndProceed();

        checkPermission();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        imagePickerHelper = new ImagePickerHelper(this);
        imagePickerHelper.initialize();

        editName = findViewById(R.id.editName);
        editDescription = findViewById(R.id.editDescription);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());

        buttonSelectImage.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                showLoginDialog();
                return;
            }
            Toast.makeText(this, "Открытие галереи...", Toast.LENGTH_SHORT).show();
            imagePickerHelper.pickImage(uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).into(imageViewPreview);
                    imageViewPreview.setVisibility(android.view.View.VISIBLE);
                    Toast.makeText(this, "Фото выбрано. Нажмите 'Отправить' для загрузки", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Не удалось выбрать фото", Toast.LENGTH_SHORT).show();
                }
            });
        });

        buttonSubmit.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                showLoginDialog();
                return;
            }
            submitSuggestion();
        });
    }

    private void checkAuthAndProceed() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Показываем диалог входа
            showLoginDialog();
        }
    }

    private void showLoginDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Требуется авторизация")
                .setMessage("Для предложения места необходимо войти в аккаунт.")
                .setPositiveButton("Войти", (dialog, which) -> {
                    Intent intent = new Intent(SuggestPlaceActivity.this, AuthorizationActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Отмена", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение получено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Нужно разрешение для выбора фото", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void submitSuggestion() {
        String name = editName.getText().toString().trim();
        String description = editDescription.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Введите название");
            return;
        }

        if (description.isEmpty()) {
            editDescription.setError("Введите описание");
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            showLoginDialog();
            return;
        }

        if (isUploading) {
            Toast.makeText(this, "Идет загрузка, подождите...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null && uploadedImageUrl == null) {
            isUploading = true;
            buttonSubmit.setEnabled(false);
            buttonSubmit.setText("Загрузка фото...");

            CloudinaryStorage.uploadImageSimple(Uri.parse(selectedImageUri),
                    new CloudinaryStorage.OnImageUploadListener() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            uploadedImageUrl = imageUrl;
                            isUploading = false;
                            buttonSubmit.setEnabled(true);
                            buttonSubmit.setText("Отправить предложение");
                            saveAppealToFirestore();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            isUploading = false;
                            buttonSubmit.setEnabled(true);
                            buttonSubmit.setText("Отправить предложение");
                            Toast.makeText(SuggestPlaceActivity.this,
                                    "Ошибка загрузки фото: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            saveAppealToFirestore();
        }
    }

    private void saveAppealToFirestore() {
        buttonSubmit.setEnabled(false);
        buttonSubmit.setText("Отправка...");

        appeal newAppeal = new appeal(
                mAuth.getCurrentUser().getUid(),
                editName.getText().toString().trim(),
                uploadedImageUrl != null ? uploadedImageUrl : "",
                editDescription.getText().toString().trim()
        );

        db.collection("appeal").add(newAppeal)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Заявка отправлена", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    buttonSubmit.setEnabled(true);
                    buttonSubmit.setText("Отправить предложение");
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}