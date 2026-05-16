package com.example.travelmagazine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelmagazine.ExcursionAdapter;
import com.example.travelmagazine.R;
import com.example.travelmagazine.attributes.excursion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ExcursionAdapter adapter;
    private List<excursion> favorites = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExcursionAdapter(favorites, excursion -> {
            Intent intent = new Intent(this, ExcursionDetailActivity.class);
            intent.putExtra("excursion_id", excursion.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadFavorites();
    }

    private void loadFavorites() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("favourites")
                .whereEqualTo("id_user", userId)
                .get()
                .addOnSuccessListener(favDocs -> {
                    favorites.clear();
                    for (var doc : favDocs) {
                        String excursionId = doc.getString("id_excursion");
                        if (excursionId != null) {
                            loadExcursionById(excursionId);
                        }
                    }
                    if (favDocs.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Нет избранных экскурсий", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadExcursionById(String excursionId) {
        db.collection("excursion").document(excursionId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        excursion exc = doc.toObject(excursion.class);
                        exc.setId(doc.getId());
                        favorites.add(exc);
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}