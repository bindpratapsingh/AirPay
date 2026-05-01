package com.offlineupi.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.offlineupi.app.utils.AppPreferences;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 1600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AppPreferences prefs = AppPreferences.getInstance(this);

            if (!prefs.isJioSetupDone()) {
                showJioQuestion(prefs);
            } else {
                proceed(prefs);
            }
        }, SPLASH_DELAY_MS);
    }

    private void showJioQuestion(AppPreferences prefs) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Network")
                .setMessage("Are you a Jio user? (USSD codes don't work on Jio, we will use IVR calls instead)")
                .setCancelable(false)
                .setPositiveButton("Yes, I'm a Jio User", (dialog, which) -> {
                    prefs.setJioMode(true);
                    prefs.setJioSetupDone(true);
                    proceed(prefs);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    prefs.setJioMode(false);
                    prefs.setJioSetupDone(true);
                    proceed(prefs);
                })
                .show();
    }

    private void proceed(AppPreferences prefs) {
        Intent intent;
        if (prefs.isFirstLaunch()) {
            intent = new Intent(this, AuthActivity.class);
            intent.putExtra(AuthActivity.MODE, AuthActivity.MODE_SETUP);
        } else if (prefs.isPinEnabled() || prefs.isBiometricEnabled()) {
            intent = new Intent(this, AuthActivity.class);
            intent.putExtra(AuthActivity.MODE, AuthActivity.MODE_UNLOCK);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
