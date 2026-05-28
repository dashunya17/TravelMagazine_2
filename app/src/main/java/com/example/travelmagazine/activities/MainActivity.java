package com.example.travelmagazine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.travelmagazine.CloudinaryStorage;
import com.example.travelmagazine.HomeFragment;
import com.example.travelmagazine.MenuFragment;
import com.example.travelmagazine.R;
import com.example.travelmagazine.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CloudinaryStorage.initialize(getApplicationContext());
        boolean isLoggedIn = checkIfUserLoggedIn();

        if (!isLoggedIn) {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new HomeFragment())
                    .commit();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
                animateFragment(selectedFragment, false);
            } else if (itemId == R.id.navigation_search) {
                selectedFragment = new SearchFragment();
                animateFragment(selectedFragment, false);
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new MenuFragment();
                animateFragment(selectedFragment, true);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    private void animateFragment(Fragment fragment, boolean slideFromRight) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (slideFromRight) {
            transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_in_left
            );
        } else {
            transaction.setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
        }
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private boolean checkIfUserLoggedIn() {
        SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean("is_logged_in", false);
        boolean isGuest = sharedPref.getBoolean("is_guest", false);
        return isLoggedIn || isGuest;
    }
}