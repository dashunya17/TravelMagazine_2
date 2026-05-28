package com.example.travelmagazine;

import android.os.Bundle;
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
import com.example.travelmagazine.attributes.appeal;
import com.example.travelmagazine.attributes.excursion;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AppealsFragment extends Fragment {

    private RecyclerView recyclerAppeals;
    private FirebaseFirestore db;
    private List<appeal> appeals = new ArrayList<>();
    private AppealsAdapter adapter;

    public AppealsFragment() {
        super(R.layout.fragment_appeals);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        recyclerAppeals = view.findViewById(R.id.recyclerAppeals);
        recyclerAppeals.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AppealsAdapter(appeals);
        recyclerAppeals.setAdapter(adapter);

        loadAppeals();
    }

    private void loadAppeals() {
        db.collection("appeal").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), "Ошибка загрузки: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null) {
                appeals.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    appeal a = doc.toObject(appeal.class);
                    if (a != null) {
                        a.setId(doc.getId());
                        appeals.add(a);
                    }
                }
                adapter.notifyDataSetChanged();

                if (appeals.isEmpty()) {
                    Toast.makeText(getContext(), "Нет заявок", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class AppealsAdapter extends RecyclerView.Adapter<AppealsAdapter.ViewHolder> {
        private List<appeal> appeals;

        AppealsAdapter(List<appeal> appeals) {
            this.appeals = appeals;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.item_appeal_admin, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            appeal a = appeals.get(position);
            holder.textName.setText(a.getName());
            holder.textDescription.setText(a.getDescription());

            if (a.getCity() != null && !a.getCity().isEmpty()) {
                holder.textCity.setVisibility(View.VISIBLE);
                holder.textCity.setText("📍 " + a.getCity());
            } else {
                holder.textCity.setVisibility(View.GONE);
            }

            if (a.getPhoto() != null && !a.getPhoto().isEmpty()) {
                Glide.with(requireContext())
                        .load(a.getPhoto())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(holder.imageView);
                holder.imageView.setVisibility(View.VISIBLE);
            } else {
                holder.imageView.setVisibility(View.GONE);
            }

            holder.buttonApprove.setOnClickListener(v -> approveAppeal(a));
            holder.buttonReject.setOnClickListener(v -> rejectAppeal(a));
        }

        @Override
        public int getItemCount() {
            return appeals.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textName, textDescription, textCity;
            ImageView imageView;
            Button buttonApprove, buttonReject;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.textName);
                textDescription = itemView.findViewById(R.id.textDescription);
                textCity = itemView.findViewById(R.id.textCity);
                imageView = itemView.findViewById(R.id.imageView);
                buttonApprove = itemView.findViewById(R.id.buttonApprove);
                buttonReject = itemView.findViewById(R.id.buttonReject);
            }
        }
    }

    private void approveAppeal(appeal a) {
        excursion newExcursion = new excursion(
                a.getName(),
                0,
                a.getPhoto() != null ? a.getPhoto() : "",
                a.getDescription(),
                true,
                a.getCity() != null ? a.getCity() : ""
        );

        db.collection("excursion").add(newExcursion)
                .addOnSuccessListener(ref -> {
                    if (a.getId() != null) {
                        db.collection("appeal").document(a.getId()).delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getContext(), "Экскурсия добавлена, заявка удалена", Toast.LENGTH_SHORT).show();
                                    loadAppeals();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Ошибка удаления заявки: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    } else {
                        Toast.makeText(getContext(), "Экскурсия добавлена", Toast.LENGTH_SHORT).show();
                        loadAppeals();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Ошибка добавления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void rejectAppeal(appeal a) {
        if (a.getId() != null) {
            db.collection("appeal").document(a.getId()).delete()
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(getContext(), "Заявка отклонена", Toast.LENGTH_SHORT).show();
                        loadAppeals();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "Заявка отклонена", Toast.LENGTH_SHORT).show();
        }
    }
}