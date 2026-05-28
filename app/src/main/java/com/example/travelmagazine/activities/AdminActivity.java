package com.example.travelmagazine.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.example.travelmagazine.CloudinaryStorage;
import com.example.travelmagazine.R;
import com.example.travelmagazine.AdminPagerAdapter;
import com.example.travelmagazine.attributes.excursion;
import com.example.travelmagazine.ImagePickerHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private AdminPagerAdapter pagerAdapter;
    private Button buttonAddExcursion;
    private ImagePickerHelper imagePickerHelper;
    private String tempExcursionPhotoUrl = null;
    private String selectedImageUri = null;
    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();
        imagePickerHelper = new ImagePickerHelper(this);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        buttonAddExcursion = findViewById(R.id.buttonAddExcursion);

        pagerAdapter = new AdminPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Экскурсии");
                    } else if (position == 1) {
                        tab.setText("Заявки");
                    } else {
                        tab.setText("Отзывы");
                    }
                }
        ).attach();

        buttonAddExcursion.setOnClickListener(v -> showAddExcursionDialog());
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }

    private void showAddExcursionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_excursion, null);

        EditText editName = view.findViewById(R.id.editName);
        EditText editDescription = view.findViewById(R.id.editDescription);
        ImageView imageViewPreview = view.findViewById(R.id.imageViewPreview);
        Button buttonSelectPhoto = view.findViewById(R.id.buttonSelectPhoto);

        tempExcursionPhotoUrl = null;
        selectedImageUri = null;
        imageViewPreview.setVisibility(View.GONE);

        buttonSelectPhoto.setOnClickListener(v ->
                imagePickerHelper.pickImage(uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Glide.with(this).load(uri).into(imageViewPreview);
                        imageViewPreview.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Фото выбрано. Нажмите 'Добавить' для загрузки", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Не удалось выбрать фото", Toast.LENGTH_SHORT).show();
                    }
                }));

        builder.setView(view)
                .setTitle("Добавить экскурсию")
                .setPositiveButton("Добавить", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String description = editDescription.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedImageUri != null && tempExcursionPhotoUrl == null) {
                        if (isUploading) {
                            Toast.makeText(this, "Идет загрузка, подождите...", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        isUploading = true;
                        Toast.makeText(this, "Загрузка фото...", Toast.LENGTH_SHORT).show();

                        CloudinaryStorage.uploadImageSimple(Uri.parse(selectedImageUri),
                                new CloudinaryStorage.OnImageUploadListener() {
                                    @Override
                                    public void onSuccess(String imageUrl) {
                                        tempExcursionPhotoUrl = imageUrl;
                                        isUploading = false;
                                        saveExcursionToFirestore(name, description, dialog);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        isUploading = false;
                                        Toast.makeText(AdminActivity.this,
                                                "Ошибка загрузки фото: " + errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        saveExcursionToFirestore(name, description, dialog);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void saveExcursionToFirestore(String name, String description, android.content.DialogInterface dialog) {
        excursion newExcursion = new excursion(
                name,
                0,
                tempExcursionPhotoUrl != null ? tempExcursionPhotoUrl : "",
                description,
                true
        );

        db.collection("excursion").add(newExcursion)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Экскурсия добавлена", Toast.LENGTH_SHORT).show();
                    if (dialog != null) dialog.dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}