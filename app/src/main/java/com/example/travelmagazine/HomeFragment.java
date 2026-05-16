package com.example.travelmagazine;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelmagazine.activities.ExcursionDetailActivity;
import com.example.travelmagazine.attributes.excursion;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ExcursionAdapter adapter;
    private List<excursion> excursions = new ArrayList<>();
    private FirebaseFirestore db;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        loadExcursions();
    }

    private void loadExcursions() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("excursion")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    excursions.clear();
                    List<excursion> tempList = new ArrayList<>();
                    if (queryDocumentSnapshots.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Нет экскурсий в базе", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            excursion exc = doc.toObject(excursion.class);
                            if (exc != null) {
                                exc.setId(doc.getId());
                                if (exc.isApproved()) {
                                    tempList.add(exc);
                                } else {
                                    Log.d("HomeFragment", "SKIPPED (not approved): " + exc.getName());
                                }
                            } else {
                                Log.e("HomeFragment", "excursion object is NULL for doc: " + doc.getId());
                            }
                        } catch (Exception e) {
                            Log.e("HomeFragment", "Error parsing document: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    Collections.shuffle(tempList);
                    excursions.addAll(tempList);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    if (excursions.isEmpty()) {
                        Log.d("HomeFragment", "NO excursions to display!");
                        Toast.makeText(getContext(), "Нет доступных экскурсий", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("HomeFragment", "SUCCESS! Displaying " + excursions.size() + " excursions");
                        }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Error loading: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }
}