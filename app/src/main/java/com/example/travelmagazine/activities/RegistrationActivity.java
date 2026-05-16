package com.example.travelmagazine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelmagazine.R;
import com.example.travelmagazine.attributes.user;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistrationActivity extends AppCompatActivity {

    private EditText userName, userEmail, userPassword;
    private Button buttonRegister, buttonAut;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userPassword = findViewById(R.id.userPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonAut = findViewById(R.id.buttonAut);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        buttonAut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistrationActivity.this, AuthorizationActivity.class));
            }
        });
    }

    private void registerUser() {
        final String username = userName.getText().toString().trim();
        final String email = userEmail.getText().toString().trim();
        final String password = userPassword.getText().toString().trim();

        // Валидация
        if (username.isEmpty()) {
            userName.setError("Введите имя пользователя");
            userName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            userEmail.setError("Введите email");
            userEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            userPassword.setError("Введите пароль");
            userPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            userPassword.setError("Пароль должен быть не менее 6 символов");
            userPassword.requestFocus();
            return;
        }

        buttonRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                user newUser = new user(username, email, password, false);

                                db.collection("user").document(userId).set(newUser)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Всё успешно!
                                                saveLoginStatus(true);
                                                Toast.makeText(RegistrationActivity.this,
                                                        "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                buttonRegister.setEnabled(true);
                                                Toast.makeText(RegistrationActivity.this,
                                                        "Ошибка сохранения данных: " + e.getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                                firebaseUser.delete();
                                            }
                                        });
                            }
                        } else {
                            buttonRegister.setEnabled(true);
                            String errorMessage = task.getException().getMessage();
                            if (errorMessage != null) {
                                if (errorMessage.contains("EMAIL_EXISTS")) {
                                    Toast.makeText(RegistrationActivity.this,
                                            "Этот email уже зарегистрирован. Войдите в аккаунт.",
                                            Toast.LENGTH_LONG).show();
                                } else if (errorMessage.contains("weak password")) {
                                    Toast.makeText(RegistrationActivity.this,
                                            "Пароль слишком слабый",
                                            Toast.LENGTH_LONG).show();
                                } else if (errorMessage.contains("invalid email")) {
                                    Toast.makeText(RegistrationActivity.this,
                                            "Некорректный email",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(RegistrationActivity.this,
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
        editor.apply();
    }
}