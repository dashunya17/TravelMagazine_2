package com.example.travelmagazine.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.travelmagazine.R;
import com.example.travelmagazine.attributes.user;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActivityProfile extends AppCompatActivity {
    private TextView textUsername, textEmail, textTotalReviews, textTotalFavorites;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        textUsername = findViewById(R.id.textUsername);
        textEmail = findViewById(R.id.textEmail);
        textTotalReviews = findViewById(R.id.textTotalReviews);
        textTotalFavorites = findViewById(R.id.textTotalFavorites);

        loadUserData();
        loadStats();
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("user").document(userId).get()
                .addOnSuccessListener(doc -> {
                    user currentUser = doc.toObject(user.class);
                    if (currentUser != null) {
                        textUsername.setText(currentUser.getUsername());
                        textEmail.setText(currentUser.getEmail());
                    }
                });
    }

    private void loadStats() {
        String userId = mAuth.getCurrentUser().getUid();

        // Количество отзывов
        db.collection("feedback").whereEqualTo("id_user", userId).get()
                .addOnSuccessListener(query -> textTotalReviews.setText(String.valueOf(query.size())));

        // Количество в избранном
        db.collection("favourites").whereEqualTo("id_user", userId).get()
                .addOnSuccessListener(query -> textTotalFavorites.setText(String.valueOf(query.size())));
    }
}