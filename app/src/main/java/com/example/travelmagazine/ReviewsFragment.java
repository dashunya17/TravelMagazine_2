package com.example.travelmagazine;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelmagazine.R;
import com.example.travelmagazine.attributes.feedback;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ReviewsFragment extends Fragment {

    private RecyclerView recyclerReviews;
    private FirebaseFirestore db;
    private List<feedback> reviews = new ArrayList<>();
    private ReviewsAdminAdapter adapter;

    public ReviewsFragment() {
        super(R.layout.fragment_reviews);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        recyclerReviews = view.findViewById(R.id.recyclerReviews);
        recyclerReviews.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ReviewsAdminAdapter(reviews);
        recyclerReviews.setAdapter(adapter);

        loadReviews();
    }

    private void loadReviews() {
        // Используем "approved" (как в Firebase), а не "isApproved"
        db.collection("feedback")
                .whereEqualTo("approved", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reviews.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        feedback fb = doc.toObject(feedback.class);
                        if (fb != null) {
                            fb.setId(doc.getId());
                            reviews.add(fb);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (reviews.isEmpty()) {
                        Toast.makeText(getContext(), "Нет отзывов на модерации", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateExcursionRating(String excursionId) {
        db.collection("feedback")
                .whereEqualTo("id_excursion", excursionId)
                .whereEqualTo("approved", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRating = 0;
                    int count = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        feedback fb = doc.toObject(feedback.class);
                        if (fb != null) {
                            totalRating += fb.getEstimation();
                            count++;
                        }
                    }

                    if (count > 0) {
                        double averageRating = totalRating / count;
                        averageRating = Math.round(averageRating * 10) / 10.0;

                        db.collection("excursion").document(excursionId)
                                .update("estimation", averageRating)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("ReviewsFragment", "Rating updated: " );
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ReviewsFragment", "Rating update failed: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ReviewsFragment", "Failed to get reviews: " + e.getMessage());
                });
    }

    private void approveReview(feedback fb) {
        if (fb.getId() != null) {
            db.collection("feedback").document(fb.getId())
                    .update("approved", true)
                    .addOnSuccessListener(ref -> {
                        updateExcursionRating(fb.getId_excursion());
                        Toast.makeText(getContext(), "Отзыв опубликован", Toast.LENGTH_SHORT).show();
                        loadReviews();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void deleteReview(feedback fb) {
        if (fb.getId() != null) {
            db.collection("feedback").document(fb.getId()).delete()
                    .addOnSuccessListener(ref -> {
                        updateExcursionRating(fb.getId_excursion());
                        Toast.makeText(getContext(), "Отзыв удален", Toast.LENGTH_SHORT).show();
                        loadReviews();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    class ReviewsAdminAdapter extends RecyclerView.Adapter<ReviewsAdminAdapter.ViewHolder> {
        private List<feedback> reviews;

        ReviewsAdminAdapter(List<feedback> reviews) {
            this.reviews = reviews;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_review_admin, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            feedback fb = reviews.get(position);
            holder.textReview.setText(fb.getText());
            holder.textRating.setText("Оценка: " + fb.getEstimation());

            if (fb.getPhoto() != null && !fb.getPhoto().isEmpty()) {
                Glide.with(requireContext())
                        .load(fb.getPhoto())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(holder.imageView);
                holder.imageView.setVisibility(View.VISIBLE);
            } else {
                holder.imageView.setVisibility(View.GONE);
            }

            db.collection("user").document(fb.getId_user()).get()
                    .addOnSuccessListener(doc -> {
                        String username = doc.getString("username");
                        holder.textUserName.setText(username != null ? username : "Пользователь");
                    })
                    .addOnFailureListener(e -> {
                        holder.textUserName.setText("Пользователь");
                    });

            holder.buttonApprove.setOnClickListener(v -> approveReview(fb));
            holder.buttonDelete.setOnClickListener(v -> deleteReview(fb));
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textReview, textRating, textUserName;
            ImageView imageView;
            Button buttonApprove, buttonDelete;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textReview = itemView.findViewById(R.id.textReview);
                textRating = itemView.findViewById(R.id.textRating);
                textUserName = itemView.findViewById(R.id.textUserName);
                imageView = itemView.findViewById(R.id.imageView);
                buttonApprove = itemView.findViewById(R.id.buttonApprove);
                buttonDelete = itemView.findViewById(R.id.buttonDelete);
            }
        }
    }
}