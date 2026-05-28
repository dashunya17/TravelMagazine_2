package com.example.travelmagazine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelmagazine.R;
import com.example.travelmagazine.attributes.user;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthorizationActivity extends AppCompatActivity {
    private EditText emailUser, passwordUser;
    private Button buttonAut, buttonRegister;
    private TextView buttonGuest;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_authorization);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailUser = findViewById(R.id.email);
        passwordUser = findViewById(R.id.password);
        buttonAut = findViewById(R.id.buttonAut);
        buttonRegister = findViewById(R.id.button);
        buttonGuest = findViewById(R.id.buttonGuest);

        buttonAut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthorizationActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        buttonGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLoginStatus(false);
                startActivity(new Intent(AuthorizationActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void loginUser() {
        String email = emailUser.getText().toString().trim();
        String password = passwordUser.getText().toString().trim();

        if (email.isEmpty()) {
            emailUser.setError("Введите email");
            emailUser.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordUser.setError("Введите пароль");
            passwordUser.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordUser.setError("Пароль должен быть не менее 6 символов");
            passwordUser.requestFocus();
            return;
        }

        buttonAut.setEnabled(false);
        buttonAut.setText("Вход...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();

                                db.collection("user").document(userId).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> userTask) {
                                                buttonAut.setEnabled(true);
                                                buttonAut.setText("Войти");

                                                if (userTask.isSuccessful()) {
                                                    DocumentSnapshot document = userTask.getResult();
                                                    if (document.exists()) {
                                                        user currentUser = document.toObject(user.class);
                                                        saveLoginStatus(true);

                                                        Toast.makeText(AuthorizationActivity.this,
                                                                "Добро пожаловать, " + (currentUser != null ? currentUser.getUsername() : firebaseUser.getEmail()),
                                                                Toast.LENGTH_SHORT).show();

                                                        if (currentUser != null && currentUser.isAdmin()) {
                                                            Intent intent = new Intent(AuthorizationActivity.this, AdminActivity.class);
                                                            startActivity(intent);
                                                        } else {
                                                            Intent intent = new Intent(AuthorizationActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                        }
                                                        finish();
                                                    } else {
                                                        Toast.makeText(AuthorizationActivity.this,
                                                                "Данные пользователя не найдены",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                } else {
                                                    Toast.makeText(AuthorizationActivity.this,
                                                            "Ошибка загрузки данных пользователя",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            buttonAut.setEnabled(true);
                            buttonAut.setText("Войти");

                            String errorMessage = task.getException().getMessage();
                            if (errorMessage != null) {
                                if (errorMessage.contains("user-not-found")) {
                                    Toast.makeText(AuthorizationActivity.this,
                                            "Пользователь не найден. Зарегистрируйтесь.",
                                            Toast.LENGTH_LONG).show();
                                } else if (errorMessage.contains("wrong-password")) {
                                    Toast.makeText(AuthorizationActivity.this,
                                            "Неверный пароль",
                                            Toast.LENGTH_LONG).show();
                                } else if (errorMessage.contains("network")) {
                                    Toast.makeText(AuthorizationActivity.this,
                                            "Ошибка сети. Проверьте подключение к интернету.",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(AuthorizationActivity.this,
                                            "Ошибка: " + errorMessage,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                });
    }

    private void saveLoginStatus(boolean isLoggedIn) {
        SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("is_logged_in", isLoggedIn);
        editor.putBoolean("is_guest", !isLoggedIn);
        editor.apply();
    }
}