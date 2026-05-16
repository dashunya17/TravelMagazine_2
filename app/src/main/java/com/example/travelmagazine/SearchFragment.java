package com.example.travelmagazine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelmagazine.activities.ExcursionDetailActivity;
import com.example.travelmagazine.attributes.excursion;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private EditText searchInput;
    private ImageView searchButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ExcursionAdapter adapter;
    private List<excursion> excursions = new ArrayList<>();
    private List<excursion> allExcursions = new ArrayList<>();
    private FirebaseFirestore db;

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchInput = view.findViewById(R.id.searchInput);
        searchButton = view.findViewById(R.id.searchButton);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();

        adapter = new ExcursionAdapter(excursions, excursion -> {
            Intent intent = new Intent(getContext(), ExcursionDetailActivity.class);
            intent.putExtra("excursion_id", excursion.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadAllExcursions();

        searchButton.setOnClickListener(v -> searchExcursions());
    }

    private void loadAllExcursions() {
        progressBar.setVisibility(View.VISIBLE);

        // УБИРАЕМ whereEqualTo, просто загружаем все экскурсии
        db.collection("excursion")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allExcursions.clear();
                    for (var doc : queryDocumentSnapshots) {
                        try {
                            excursion exc = doc.toObject(excursion.class);
                            exc.setId(doc.getId());
                            allExcursions.add(exc);
                        } catch (Exception e) {
                        }
                    }
                    excursions.clear();
                    excursions.addAll(allExcursions);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (allExcursions.isEmpty()) {
                        Toast.makeText(getContext(), "Нет доступных экскурсий", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void searchExcursions() {
        String query = searchInput.getText().toString().trim().toLowerCase();

        if (query.isEmpty()) {
            excursions.clear();
            excursions.addAll(allExcursions);
            adapter.notifyDataSetChanged();
            return;
        }

        List<excursion> filtered = new ArrayList<>();
        for (excursion exc : allExcursions) {
            if (exc.getName() != null && exc.getName().toLowerCase().contains(query)) {
                filtered.add(exc);
            }
        }

        excursions.clear();
        excursions.addAll(filtered);
        adapter.notifyDataSetChanged();

        if (filtered.isEmpty()) {
            Toast.makeText(getContext(), "Ничего не найдено", Toast.LENGTH_SHORT).show();
        }
    }
}