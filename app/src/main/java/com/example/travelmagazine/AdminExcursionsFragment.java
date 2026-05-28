package com.example.travelmagazine;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelmagazine.CloudinaryStorage;
import com.example.travelmagazine.R;
import com.example.travelmagazine.attributes.excursion;
import com.example.travelmagazine.ImagePickerHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AdminExcursionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private ImagePickerHelper imagePickerHelper;
    private List<excursion> excursionsList = new ArrayList<>();
    private ExcursionsAdapter adapter;
    private boolean isImagePickerInitialized = false;

    public AdminExcursionsFragment() {
        super(R.layout.fragment_admin_excursions);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerExcursions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ExcursionsAdapter(excursionsList);
        recyclerView.setAdapter(adapter);

        loadExcursions();
    }

    private void initImagePicker() {
        if (!isImagePickerInitialized && getActivity() != null && getActivity() instanceof AppCompatActivity) {
            imagePickerHelper = new ImagePickerHelper((AppCompatActivity) getActivity());
            isImagePickerInitialized = true;
        }
    }

    private void loadExcursions() {
        db.collection("excursion").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                excursionsList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    excursion exc = doc.toObject(excursion.class);
                    if (exc != null) {
                        exc.setId(doc.getId());
                        excursionsList.add(exc);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    class ExcursionsAdapter extends RecyclerView.Adapter<ExcursionsAdapter.ViewHolder> {
        private List<excursion> excursions;

        ExcursionsAdapter(List<excursion> excursions) {
            this.excursions = excursions;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_excursion_admin, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            excursion exc = excursions.get(position);
            holder.textName.setText(exc.getName());
            holder.textDescription.setText(exc.getDescription());
            holder.textRating.setText("⭐ " + exc.getEstimation());

            if (exc.getPhoto() != null && !exc.getPhoto().isEmpty()) {
                Glide.with(requireContext())
                        .load(exc.getPhoto())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(holder.imageView);
            }

            holder.buttonEdit.setOnClickListener(v -> showEditDialog(exc));
            holder.buttonDelete.setOnClickListener(v -> confirmDelete(exc));
        }

        @Override
        public int getItemCount() {
            return excursions.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textName, textDescription, textRating;
            ImageView imageView;
            Button buttonEdit, buttonDelete;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.textName);
                textDescription = itemView.findViewById(R.id.textDescription);
                textRating = itemView.findViewById(R.id.textRating);
                imageView = itemView.findViewById(R.id.imageView);
                buttonEdit = itemView.findViewById(R.id.buttonEdit);
                buttonDelete = itemView.findViewById(R.id.buttonDelete);
            }
        }
    }

    private void showEditDialog(excursion exc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogStyle);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_excursion, null);

        EditText editName = view.findViewById(R.id.editName);
        EditText editDescription = view.findViewById(R.id.editDescription);
        EditText editRating = view.findViewById(R.id.editRating);
        ImageView imageViewPreview = view.findViewById(R.id.imageViewPreview);
        Button buttonSelectPhoto = view.findViewById(R.id.buttonSelectPhoto);

        editName.setText(exc.getName());
        editDescription.setText(exc.getDescription());
        editRating.setText(String.valueOf(exc.getEstimation()));
        editRating.setVisibility(View.VISIBLE);

        if (exc.getPhoto() != null && !exc.getPhoto().isEmpty()) {
            Glide.with(requireContext()).load(exc.getPhoto()).into(imageViewPreview);
            imageViewPreview.setVisibility(View.VISIBLE);
        }

        final String[] newPhotoUrl = {exc.getPhoto()};

        if (imagePickerHelper == null && getActivity() != null) {
            imagePickerHelper = new ImagePickerHelper((AppCompatActivity) requireActivity());
        }

        buttonSelectPhoto.setOnClickListener(v -> {
            if (imagePickerHelper != null) {
                imagePickerHelper.pickImage(uri -> {
                    if (uri != null) {
                        CloudinaryStorage.uploadImageSimple(Uri.parse(uri),
                                new CloudinaryStorage.OnImageUploadListener() {
                                    @Override
                                    public void onSuccess(String imageUrl) {
                                        newPhotoUrl[0] = imageUrl;
                                        Glide.with(requireContext()).load(imageUrl).into(imageViewPreview);
                                        imageViewPreview.setVisibility(View.VISIBLE);
                                        Toast.makeText(getContext(), "Фото загружено", Toast.LENGTH_SHORT).show();
                                    }
                                    @Override
                                    public void onError(String errorMessage) {
                                        Toast.makeText(getContext(), "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            } else {
                Toast.makeText(getContext(), "Ошибка инициализации", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(view)
                .setTitle("Редактировать экскурсию")
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String description = editDescription.getText().toString().trim();
                    double rating = 0;
                    try {
                        rating = Double.parseDouble(editRating.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        rating = exc.getEstimation();
                    }

                    db.collection("excursion").document(exc.getId())
                            .update("name", name, "description", description, "estimation", rating, "photo", newPhotoUrl[0])
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(getContext(), "Обновлено", Toast.LENGTH_SHORT).show();
                                loadExcursions();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void confirmDelete(excursion exc) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Удалить")
                .setMessage("Удалить \"" + exc.getName() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    db.collection("excursion").document(exc.getId()).delete()
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(getContext(), "Удалено", Toast.LENGTH_SHORT).show();
                                loadExcursions();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}