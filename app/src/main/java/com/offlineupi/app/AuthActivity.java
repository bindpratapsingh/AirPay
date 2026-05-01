package com.offlineupi.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.offlineupi.app.utils.AppPreferences;
import com.offlineupi.app.utils.SecurityUtils;

import java.util.concurrent.Executor;

public class AuthActivity extends AppCompatActivity {

    public static final String MODE = "mode";
    public static final String MODE_SETUP = "setup";
    public static final String MODE_UNLOCK = "unlock";
    public static final String MODE_CHANGE = "change";

    private String currentMode;
    private StringBuilder pinInput = new StringBuilder();
    private String firstPin = null; // used during setup (confirm step)
    private boolean isConfirmStep = false;

    private TextView tvTitle, tvSubtitle, tvDots;
    private AppPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        prefs = AppPreferences.getInstance(this);
        currentMode = getIntent().getStringExtra(MODE);
        if (currentMode == null) currentMode = MODE_UNLOCK;

        tvTitle = findViewById(R.id.tvAuthTitle);
        tvSubtitle = findViewById(R.id.tvAuthSubtitle);
        tvDots = findViewById(R.id.tvPinDots);

        setupModeUI();
        setupKeypad();

        // Offer biometric on unlock if enabled
        if (MODE_UNLOCK.equals(currentMode) && prefs.isBiometricEnabled()
                && SecurityUtils.isBiometricAvailable(this)) {
            showBiometricPrompt();
        }
    }

    private void setupModeUI() {
        if (MODE_SETUP.equals(currentMode)) {
            tvTitle.setText("Create Your PIN");
            tvSubtitle.setText("Set a 4-digit PIN to secure the app");
        } else if (MODE_UNLOCK.equals(currentMode)) {
            tvTitle.setText("Welcome Back");
            tvSubtitle.setText("Enter your PIN to continue");
        } else if (MODE_CHANGE.equals(currentMode)) {
            tvTitle.setText("Change PIN");
            tvSubtitle.setText("Enter your current PIN first");
        }
    }

    private void setupKeypad() {
        int[] btnIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
                R.id.btn8, R.id.btn9
        };
        String[] values = {"0","1","2","3","4","5","6","7","8","9"};

        for (int i = 0; i < btnIds.length; i++) {
            final String val = values[i];
            MaterialButton btn = findViewById(btnIds[i]);
            if (btn != null) {
                btn.setOnClickListener(v -> appendDigit(val));
            }
        }

        MaterialButton btnBack = findViewById(R.id.btnPinBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> deleteDigit());
        }
    }

    private void appendDigit(String digit) {
        if (pinInput.length() >= 4) return;
        pinInput.append(digit);
        updateDots();
        if (pinInput.length() == 4) {
            processPin(pinInput.toString());
        }
    }

    private void deleteDigit() {
        if (pinInput.length() > 0) {
            pinInput.deleteCharAt(pinInput.length() - 1);
            updateDots();
        }
    }

    private void updateDots() {
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            dots.append(i < pinInput.length() ? "●" : "○");
            if (i < 3) dots.append("  ");
        }
        tvDots.setText(dots.toString());
    }

    private void processPin(String pin) {
        if (MODE_SETUP.equals(currentMode)) {
            handleSetup(pin);
        } else if (MODE_UNLOCK.equals(currentMode)) {
            handleUnlock(pin);
        } else if (MODE_CHANGE.equals(currentMode)) {
            handleChange(pin);
        }
    }

    private void handleSetup(String pin) {
        if (!isConfirmStep) {
            firstPin = pin;
            isConfirmStep = true;
            pinInput.setLength(0);
            tvSubtitle.setText("Confirm your PIN");
            updateDots();
        } else {
            if (pin.equals(firstPin)) {
                prefs.setPin(pin);
                prefs.setPinEnabled(true);
                prefs.setFirstLaunch(false);
                Toast.makeText(this, "PIN set successfully!", Toast.LENGTH_SHORT).show();
                goToMain();
            } else {
                Toast.makeText(this, "PINs don't match. Try again.", Toast.LENGTH_SHORT).show();
                isConfirmStep = false;
                firstPin = null;
                pinInput.setLength(0);
                tvSubtitle.setText("Set a 4-digit PIN");
                updateDots();
            }
        }
    }

    private void handleUnlock(String pin) {
        if (prefs.verifyPin(pin)) {
            goToMain();
        } else {
            Toast.makeText(this, "Incorrect PIN. Try again.", Toast.LENGTH_SHORT).show();
            pinInput.setLength(0);
            updateDots();
        }
    }

    private void handleChange(String pin) {
        // Implementation for PIN change would go here
        Toast.makeText(this, "PIN change feature coming soon.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        goToMain();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // Fall back to PIN entry — already visible
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(AuthActivity.this,
                                "Biometric failed. Use PIN.", Toast.LENGTH_SHORT).show();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock AirPay")
                .setSubtitle("Use your fingerprint to unlock")
                .setNegativeButtonText("Use PIN")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void onSkipSetup(View view) {
        // User skips PIN setup — still mark first launch done
        prefs.setFirstLaunch(false);
        goToMain();
    }
}
