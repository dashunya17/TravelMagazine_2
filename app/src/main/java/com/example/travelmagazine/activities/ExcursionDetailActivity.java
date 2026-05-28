package com.example.travelmagazine.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelmagazine.CloudinaryStorage;
import com.example.travelmagazine.ImagePickerHelper;
import com.example.travelmagazine.R;
import com.example.travelmagazine.attributes.excursion;
import com.example.travelmagazine.attributes.feedback;
import com.example.travelmagazine.attributes.favourites;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExcursionDetailActivity extends AppCompatActivity {
    private TextView textName, textDescription, textRatingValue;
    private RatingBar ratingBar;
    private ImageView imageView, buttonFavorite;
    private RecyclerView reviewsRecyclerView;
    private Button buttonAddReview;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ImagePickerHelper imagePickerHelper;
    private String excursionId;
    private excursion currentExcursion;
    private List<feedback> reviews = new ArrayList<>();
    private ReviewsAdapter reviewsAdapter;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excursion_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        imagePickerHelper = new ImagePickerHelper(this);

        excursionId = getIntent().getStringExtra("excursion_id");

        initViews();
        loadExcursionDetails();
        loadReviews();
        checkIfFavorite();

        buttonFavorite.setOnClickListener(v -> toggleFavorite());
        buttonAddReview.setOnClickListener(v -> showAddReviewDialog());
    }

    private void initViews() {
        textName = findViewById(R.id.textName);
        textDescription = findViewById(R.id.textDescription);
        textRatingValue = findViewById(R.id.textRatingValue);
        ratingBar = findViewById(R.id.ratingBar);
        imageView = findViewById(R.id.imageView);
        buttonFavorite = findViewById(R.id.buttonFavorite);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        buttonAddReview = findViewById(R.id.buttonAddReview);

        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsAdapter = new ReviewsAdapter(reviews);
        reviewsRecyclerView.setAdapter(reviewsAdapter);

        ImageView buttonBack = findViewById(R.id.buttonBack);
        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> finish());
        }
    }

    private void loadExcursionDetails() {
        db.collection("excursion").document(excursionId).addSnapshotListener((doc, error) -> {
            if (error != null) {
                Log.e("ExcursionDetail", "Error loading: " + error.getMessage());
                return;
            }
            if (doc != null && doc.exists()) {
                currentExcursion = doc.toObject(excursion.class);
                if (currentExcursion != null) {
                    currentExcursion.setId(doc.getId());
                    textName.setText(currentExcursion.getName());
                    textDescription.setText(currentExcursion.getDescription());

                    float rating = (float) currentExcursion.getEstimation();
                    ratingBar.setRating(rating);
                    textRatingValue.setText(String.format("%.1f", rating));

                    Log.d("ExcursionDetail", "Current rating: " + rating);

                    if (currentExcursion.getPhoto() != null && !currentExcursion.getPhoto().isEmpty()) {
                        Glide.with(this)
                                .load(currentExcursion.getPhoto())
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .into(imageView);
                    }
                }
            }
        });
    }

    private void loadReviews() {
        db.collection("feedback")
                .whereEqualTo("id_excursion", excursionId)
                .whereEqualTo("approved", true)
                .orderBy("datatime", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Ошибка загрузки отзывов: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        reviews.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            feedback fb = doc.toObject(feedback.class);
                            if (fb != null) {
                                fb.setId(doc.getId());
                                reviews.add(fb);
                            }
                        }
                        reviewsAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void checkIfFavorite() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("favourites")
                .whereEqualTo("id_user", userId)
                .whereEqualTo("id_excursion", excursionId)
                .get()
                .addOnSuccessListener(query -> {
                    isFavorite = !query.isEmpty();
                    updateFavoriteButton();
                })
                .addOnFailureListener(e -> Log.e("ExcursionDetail", "Error checking favorite: " + e.getMessage()));
    }

    private void toggleFavorite() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Войдите в аккаунт", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        if (isFavorite) {
            db.collection("favourites")
                    .whereEqualTo("id_user", userId)
                    .whereEqualTo("id_excursion", excursionId)
                    .get()
                    .addOnSuccessListener(query -> {
                        for (DocumentSnapshot doc : query.getDocuments()) {
                            doc.getReference().delete();
                        }
                        isFavorite = false;
                        updateFavoriteButton();
                        Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    });
        } else {
            favourites fav = new favourites(userId, excursionId);
            db.collection("favourites").add(fav)
                    .addOnSuccessListener(ref -> {
                        isFavorite = true;
                        updateFavoriteButton();
                        Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateFavoriteButton() {
        if (buttonFavorite != null) {
            buttonFavorite.setImageResource(isFavorite ?
                    android.R.drawable.btn_star_big_on :
                    android.R.drawable.btn_star_big_off);
        }
    }

    private void showAddReviewDialog() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Войдите в аккаунт, чтобы оставить отзыв", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogStyle);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_review, null);

        EditText editReviewText = view.findViewById(R.id.editReviewText);
        RatingBar ratingBarReview = view.findViewById(R.id.ratingBarReview);
        EditText editCost = view.findViewById(R.id.editCost);
        Button buttonSelectPhoto = view.findViewById(R.id.buttonSelectPhoto);
        ImageView imageViewPreview = view.findViewById(R.id.imageViewPreview);

        // Устанавливаем цвет звезд для RatingBar
        ratingBarReview.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFC107")));
        ratingBarReview.setSecondaryProgressTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));

        final String[] photoUrl = {null};
        final boolean[] isUploading = {false};

        buttonSelectPhoto.setOnClickListener(v -> {
            imagePickerHelper.pickImage(uri -> {
                if (uri != null) {
                    isUploading[0] = true;
                    buttonSelectPhoto.setEnabled(false);
                    buttonSelectPhoto.setText("Загрузка...");

                    CloudinaryStorage.uploadImageSimple(Uri.parse(uri),
                            new CloudinaryStorage.OnImageUploadListener() {
                                @Override
                                public void onSuccess(String imageUrl) {
                                    photoUrl[0] = imageUrl;
                                    isUploading[0] = false;
                                    Glide.with(ExcursionDetailActivity.this)
                                            .load(imageUrl)
                                            .into(imageViewPreview);
                                    imageViewPreview.setVisibility(View.VISIBLE);
                                    buttonSelectPhoto.setEnabled(true);
                                    buttonSelectPhoto.setText("Выбрать фото");
                                    Toast.makeText(ExcursionDetailActivity.this,
                                            "Фото загружено", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    isUploading[0] = false;
                                    buttonSelectPhoto.setEnabled(true);
                                    buttonSelectPhoto.setText("Выбрать фото");
                                    Toast.makeText(ExcursionDetailActivity.this,
                                            "Ошибка загрузки: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            });
                }
            });
        });

        builder.setView(view)
                .setTitle("Оставить отзыв")
                .setPositiveButton("Отправить", (dialog, which) -> {
                    if (isUploading[0]) {
                        Toast.makeText(this, "Подождите, фото загружается...", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String text = editReviewText.getText().toString().trim();
                    float rating = ratingBarReview.getRating();
                    String costStr = editCost.getText().toString().trim();
                    double cost = 0;

                    if (!costStr.isEmpty()) {
                        try {
                            cost = Double.parseDouble(costStr);
                        } catch (NumberFormatException e) {
                            cost = 0;
                        }
                    }

                    if (text.isEmpty()) {
                        Toast.makeText(this, "Введите текст отзыва", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (rating == 0) {
                        Toast.makeText(this, "Поставьте оценку", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    feedback fb = new feedback(
                            mAuth.getCurrentUser().getUid(),
                            excursionId,
                            text,
                            rating,
                            new Date().toString(),
                            "",
                            cost,
                            photoUrl[0] != null ? photoUrl[0] : ""
                    );

                    db.collection("feedback").add(fb)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(this, "Отзыв отправлен на модерацию", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {
        private List<feedback> reviews;

        ReviewsAdapter(List<feedback> reviews) {
            this.reviews = reviews;
        }

        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ReviewViewHolder(getLayoutInflater().inflate(R.layout.item_review, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            feedback fb = reviews.get(position);
            holder.textReview.setText(fb.getText());

            holder.ratingBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFC107")));
            holder.ratingBar.setSecondaryProgressTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            holder.ratingBar.setRating((float) fb.getEstimation());
            holder.textDate.setText(fb.getDatatime());

            if (fb.getCost() > 0) {
                holder.textCost.setVisibility(View.VISIBLE);
                holder.textCost.setText(String.format("💰 Стоимость: %.2f ₽", fb.getCost()));
            } else {
                holder.textCost.setVisibility(View.GONE);
            }
            if (fb.getPhotoReview() != null && !fb.getPhotoReview().isEmpty()) {
                holder.imageViewReview.setVisibility(View.VISIBLE);
                Glide.with(holder.itemView.getContext())
                        .load(fb.getPhotoReview())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(holder.imageViewReview);
            } else {
                holder.imageViewReview.setVisibility(View.GONE);
            }

            db.collection("user").document(fb.getId_user()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String username = doc.getString("username");
                            holder.textUserName.setText(username != null ? username : "Пользователь");
                        } else {
                            holder.textUserName.setText("Пользователь");
                        }
                    })
                    .addOnFailureListener(e -> holder.textUserName.setText("Пользователь"));
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

        class ReviewViewHolder extends RecyclerView.ViewHolder {
            TextView textUserName, textReview, textDate, textCost;
            RatingBar ratingBar;
            ImageView imageViewReview;

            ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                textUserName = itemView.findViewById(R.id.textUserName);
                textReview = itemView.findViewById(R.id.textReview);
                textDate = itemView.findViewById(R.id.textDate);
                textCost = itemView.findViewById(R.id.textCost);
                ratingBar = itemView.findViewById(R.id.ratingBar);
                imageViewReview = itemView.findViewById(R.id.imageViewReview);
            }
        }
    }
}