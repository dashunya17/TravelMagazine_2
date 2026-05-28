package com.example.travelmagazine;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private boolean isGuest = false;

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

        if (getActivity() != null) {
            SharedPreferences sharedPref = getActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
            isGuest = sharedPref.getBoolean("is_guest", false);
            Log.d("HomeFragment", "Guest mode: " + isGuest);
        }
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
        Log.d("HomeFragment", "Loading excursions...");
        db.collection("excursion")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    excursions.clear();
                    List<excursion> tempList = new ArrayList<>();
                    if (queryDocumentSnapshots.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Log.d("HomeFragment", "No excursions in database");
                        Toast.makeText(getContext(), "Нет экскурсий в базе", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.d("HomeFragment", "Found " + queryDocumentSnapshots.size() + " documents");
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            excursion exc = doc.toObject(excursion.class);
                            if (exc != null) {
                                exc.setId(doc.getId());

                                boolean isApproved = exc.isApproved();
                                Log.d("HomeFragment", "Excursion: " + exc.getName() +
                                        ", approved: " + isApproved);

                                if (isApproved) {
                                    tempList.add(exc);
                                    Log.d("HomeFragment", "Added: " + exc.getName());
                                } else {
                                    Log.d("HomeFragment", "Skipped (not approved): " + exc.getName());
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

                    Log.d("HomeFragment", "Final count: " + excursions.size() + " excursions");

                    if (excursions.isEmpty()) {
                        Log.d("HomeFragment", "NO approved excursions to display!");
                        Toast.makeText(getContext(), "Нет доступных экскурсий. Добавьте их через админ-панель.", Toast.LENGTH_LONG).show();
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