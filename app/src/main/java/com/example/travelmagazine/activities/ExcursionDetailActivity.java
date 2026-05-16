package com.example.travelmagazine.activities;

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
        excursionId = getIntent().getStringExtra("excursion_id");

        initViews();
        loadExcursionDetails();  // ← Вызов метода здесь
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

    // ★★★ ЭТОТ МЕТОД НУЖНО ВСТАВИТЬ СЮДА ★★★
    private void loadExcursionDetails() {
        db.collection("excursion").document(excursionId).addSnapshotListener((doc, error) -> {
            if (error != null) return;
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
                        Toast.makeText(this, "Ошибка загрузки отзывов", Toast.LENGTH_SHORT).show();
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
                });
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
        buttonFavorite.setImageResource(isFavorite ?
                android.R.drawable.btn_star_big_on :
                android.R.drawable.btn_star_big_off);
    }

    private void showAddReviewDialog() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Войдите в аккаунт, чтобы оставить отзыв", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_review, null);

        EditText editReviewText = view.findViewById(R.id.editReviewText);
        RatingBar ratingBarReview = view.findViewById(R.id.ratingBarReview);

        builder.setView(view)
                .setTitle("Оставить отзыв")
                .setPositiveButton("Отправить", (dialog, which) -> {
                    String text = editReviewText.getText().toString().trim();
                    float rating = ratingBarReview.getRating();

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
                            ""
                    );

                    db.collection("feedback").add(fb)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(this, "Отзыв отправлен на модерацию", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // Адаптер для отзывов
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
            holder.ratingBar.setRating((float) fb.getEstimation());
            holder.textDate.setText(fb.getDatatime());

            db.collection("user").document(fb.getId_user()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String username = doc.getString("username");
                            holder.textUserName.setText(username != null ? username : "Пользователь");
                        } else {
                            holder.textUserName.setText("Пользователь");
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

        class ReviewViewHolder extends RecyclerView.ViewHolder {
            TextView textUserName, textReview, textDate;
            RatingBar ratingBar;

            ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                textUserName = itemView.findViewById(R.id.textUserName);
                textReview = itemView.findViewById(R.id.textReview);
                textDate = itemView.findViewById(R.id.textDate);
                ratingBar = itemView.findViewById(R.id.ratingBar);
            }
        }
    }
}