package com.offlineupi.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.offlineupi.app.utils.AppPreferences;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PHONE = 101;
    private NavController navController;

    private final ActivityResultLauncher<Intent> scanLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String upiId = result.getData().getStringExtra("upi_id");
                    String name = result.getData().getStringExtra("name");
                    if (upiId != null) {
                        navigateToPay(upiId, name);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply saved theme preference
        AppPreferences prefs = AppPreferences.getInstance(this);
        applyTheme(prefs.getTheme());

        setContentView(R.layout.activity_main);

        // Setup Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Handle QR scan button
        findViewById(R.id.fabScan).setOnClickListener(v -> {
            scanLauncher.launch(new Intent(this, ScannerActivity.class));
        });

        // FAB is now always visible to avoid awkward layout shifts
        updateFabVisibility(false);

        // Request CALL_PHONE permission on startup
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
        }
    }

    public void updateFabVisibility(boolean isJio) {
        View fab = findViewById(R.id.fabScan);
        if (fab != null) {
            // Scanner stays visible in both modes to keep UI consistent
            fab.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToPay(String upiId, String name) {
        Bundle args = new Bundle();
        args.putString("prefill_identifier", upiId);
        args.putString("prefill_name", name);
        args.putBoolean("prefill_is_upi", true);
        
        navController.navigate(R.id.payFragment, args);
        
        // Sync bottom nav selection
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.payFragment);
        }
    }

    public static void applyTheme(String theme) {
        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Phone permission granted ✓", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "Phone permission required to make USSD calls",
                        Snackbar.LENGTH_LONG)
                        .setAction("Retry", v ->
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE))
                        .show();
            }
        }
    }

    public NavController getNavController() {
        return navController;
    }
}
