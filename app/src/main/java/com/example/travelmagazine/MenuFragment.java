package com.example.travelmagazine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.travelmagazine.activities.ActivityProfile;
import com.example.travelmagazine.activities.AdminActivity;
import com.example.travelmagazine.activities.AuthorizationActivity;
import com.example.travelmagazine.activities.FavoritesActivity;
import com.example.travelmagazine.activities.SuggestPlaceActivity;
import com.example.travelmagazine.attributes.user;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MenuFragment extends Fragment {
    private Button buttonProfile, buttonFavorites, buttonSuggest, buttonLogout;
    private Button buttonAdminPanel; // Добавляем кнопку для админ-панели
    private TextView textViewUsername, textViewEmail;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isAdmin = false;

    public MenuFragment() {
        super(R.layout.fragment_menu);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        buttonProfile = view.findViewById(R.id.buttonProfile);
        buttonFavorites = view.findViewById(R.id.buttonFavorites);
        buttonSuggest = view.findViewById(R.id.buttonSuggest);
        buttonLogout = view.findViewById(R.id.buttonLogout);

        // Инициализируем кнопку админ-панели (если она есть в layout)
        View adminButton = view.findViewById(R.id.buttonAdminPanel);
        if (adminButton != null) {
            buttonAdminPanel = (Button) adminButton;
        }

        textViewUsername = view.findViewById(R.id.textViewUsername);
        textViewEmail = view.findViewById(R.id.textViewEmail);

        loadUserData();

        if (buttonProfile != null) {
            buttonProfile.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ActivityProfile.class);
                startActivity(intent);
            });
        }

        if (buttonFavorites != null) {
            buttonFavorites.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), FavoritesActivity.class);
                startActivity(intent);
            });
        }

        if (buttonSuggest != null) {
            buttonSuggest.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), SuggestPlaceActivity.class);
                startActivity(intent);
            });
        }

        if (buttonAdminPanel != null) {
            buttonAdminPanel.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AdminActivity.class);
                startActivity(intent);
            });
        }

        if (buttonLogout != null) {
            buttonLogout.setOnClickListener(v -> logout());
        }
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) {
            textViewUsername.setText("Не авторизован");
            textViewEmail.setText("");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("user").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        user currentUser = doc.toObject(user.class);
                        if (currentUser != null) {
                            textViewUsername.setText(currentUser.getUsername());
                            textViewEmail.setText(currentUser.getEmail());
                            isAdmin = currentUser.isAdmin();

                            // Показываем кнопку админ-панели только если пользователь - админ
                            if (buttonAdminPanel != null) {
                                buttonAdminPanel.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                            }
                        }
                    } else {
                        textViewUsername.setText(mAuth.getCurrentUser().getEmail());
                        textViewEmail.setText(mAuth.getCurrentUser().getEmail());
                    }
                })
                .addOnFailureListener(e -> {
                    textViewUsername.setText(mAuth.getCurrentUser().getEmail());
                    textViewEmail.setText(mAuth.getCurrentUser().getEmail());
                });
    }

    private void logout() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("is_logged_in", false);
        editor.apply();

        FirebaseAuth.getInstance().signOut();

        Toast.makeText(getContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), AuthorizationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}