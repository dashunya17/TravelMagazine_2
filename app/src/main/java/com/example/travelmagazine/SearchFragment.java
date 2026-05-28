package com.example.travelmagazine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchFragment extends Fragment {
    private EditText searchInput;
    private Button switchSearchType;
    private Button searchButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ExcursionAdapter adapter;
    private List<excursion> excursions = new ArrayList<>();
    private List<excursion> allExcursions = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentSearchType = "name";

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchInput = view.findViewById(R.id.searchInput);
        searchButton = view.findViewById(R.id.searchButton);
        switchSearchType = view.findViewById(R.id.switchSearchType);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        searchInput.setHint("Поиск по названию...");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();

        adapter = new ExcursionAdapter(excursions, excursion -> {
            Intent intent = new Intent(getContext(), ExcursionDetailActivity.class);
            intent.putExtra("excursion_id", excursion.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadAllExcursions();

        if (switchSearchType != null) {
            switchSearchType.setText("Город");

            switchSearchType.setOnClickListener(v -> {
                if (currentSearchType.equals("name")) {
                    currentSearchType = "city";
                    searchInput.setHint("Поиск по городу...");
                    switchSearchType.setText("Название");
                    Toast.makeText(getContext(), "Поиск по городу", Toast.LENGTH_SHORT).show();
                } else {
                    currentSearchType = "name";
                    searchInput.setHint("Поиск по названию...");
                    switchSearchType.setText("Город");
                    Toast.makeText(getContext(), "Поиск по названию", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (searchButton != null) {
            searchButton.setOnClickListener(v -> searchExcursions());
        }
    }

    private void loadAllExcursions() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        db.collection("excursion")
                .whereEqualTo("approved", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allExcursions.clear();
                    Set<String> cities = new HashSet<>();

                    for (var doc : queryDocumentSnapshots) {
                        try {
                            excursion exc = doc.toObject(excursion.class);
                            if (exc != null) {
                                exc.setId(doc.getId());
                                allExcursions.add(exc);

                                if (exc.getCity() != null && !exc.getCity().isEmpty()) {
                                    cities.add(exc.getCity());
                                }
                            }
                        } catch (Exception e) {
                        }
                    }

                    excursions.clear();
                    excursions.addAll(allExcursions);
                    adapter.notifyDataSetChanged();

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    if (allExcursions.isEmpty() && getContext() != null) {
                        Toast.makeText(getContext(), "Нет доступных экскурсий", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void searchExcursions() {
        if (searchInput == null) return;

        String query = searchInput.getText().toString().trim().toLowerCase();

        if (query.isEmpty()) {
            excursions.clear();
            excursions.addAll(allExcursions);
            adapter.notifyDataSetChanged();
            return;
        }

        List<excursion> filtered = new ArrayList<>();

        if (currentSearchType.equals("name")) {
            for (excursion exc : allExcursions) {
                if (exc.getName() != null && exc.getName().toLowerCase().contains(query)) {
                    filtered.add(exc);
                }
            }
        } else {
            for (excursion exc : allExcursions) {
                if (exc.getCity() != null && exc.getCity().toLowerCase().contains(query)) {
                    filtered.add(exc);
                }
            }
        }

        excursions.clear();
        excursions.addAll(filtered);
        adapter.notifyDataSetChanged();

        if (getContext() != null) {
            if (filtered.isEmpty()) {
                Toast.makeText(getContext(), "Ничего не найдено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Найдено: " + filtered.size(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}