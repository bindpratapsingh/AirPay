package com.offlineupi.app.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.offlineupi.app.AuthActivity;
import com.offlineupi.app.MainActivity;
import com.offlineupi.app.R;
import com.offlineupi.app.utils.AppPreferences;
import com.offlineupi.app.utils.SecurityUtils;
import com.offlineupi.app.utils.USSDBuilder;

public class SettingsFragment extends Fragment {

    private AppPreferences prefs;
    private TextInputEditText etUserName;
    private SwitchMaterial switchJio, switchBiometric, switchPin;
    private AutoCompleteTextView etTheme, etSim;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = AppPreferences.getInstance(requireContext());

        bindViews(view);
        loadCurrentSettings();
        setupListeners(view);
    }

    private void bindViews(View view) {
        etUserName = view.findViewById(R.id.etUserName);
        switchJio = view.findViewById(R.id.switchJio);
        switchBiometric = view.findViewById(R.id.switchBiometric);
        switchPin = view.findViewById(R.id.switchPin);
        etTheme = view.findViewById(R.id.etTheme);
        etSim = view.findViewById(R.id.etSimPreference);
    }

    private void loadCurrentSettings() {
        etUserName.setText(prefs.getUserName());
        switchJio.setChecked(prefs.isJioMode());
        switchBiometric.setChecked(prefs.isBiometricEnabled());
        switchPin.setChecked(prefs.isPinEnabled());

        // Theme dropdown
        String[] themes = {"System Default", "Light", "Dark"};
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, themes);
        etTheme.setAdapter(themeAdapter);
        int themeIdx = prefs.getTheme().equals("light") ? 1 : prefs.getTheme().equals("dark") ? 2 : 0;
        etTheme.setText(themes[themeIdx], false);

        // SIM dropdown
        String[] sims = {"Auto (Default)", "SIM 1", "SIM 2"};
        ArrayAdapter<String> simAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, sims);
        etSim.setAdapter(simAdapter);
        etSim.setText(sims[prefs.getDefaultSim()], false);

        // Biometric visibility
        switchBiometric.setEnabled(SecurityUtils.isBiometricAvailable(requireContext()));
        if (!SecurityUtils.isBiometricAvailable(requireContext())) {
            switchBiometric.setText("Fingerprint (Not Available)");
        }
    }

    private void setupListeners(View view) {
        // Save name on every text change
        etUserName.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                prefs.setUserName(s.toString().trim());
            }
        });

        switchJio.setOnCheckedChangeListener((btn, checked) -> {
            prefs.setJioMode(checked);
            
            // Notify MainActivity to update FAB
            if (getActivity() instanceof com.offlineupi.app.MainActivity) {
                ((com.offlineupi.app.MainActivity) getActivity()).updateFabVisibility(checked);
            }

            Toast.makeText(requireContext(),
                    checked ? "Jio IVR Mode enabled" : "Standard USSD mode enabled",
                    Toast.LENGTH_SHORT).show();
        });

        switchPin.setOnCheckedChangeListener((btn, checked) -> {
            if (checked && !prefs.isPinSet()) {
                // Launch PIN setup
                Intent intent = new Intent(requireContext(), AuthActivity.class);
                intent.putExtra(AuthActivity.MODE, AuthActivity.MODE_SETUP);
                startActivity(intent);
            } else if (!checked) {
                prefs.clearPin();
                Toast.makeText(requireContext(), "PIN lock disabled", Toast.LENGTH_SHORT).show();
            }
        });

        switchBiometric.setOnCheckedChangeListener((btn, checked) -> {
            prefs.setBiometricEnabled(checked);
            Toast.makeText(requireContext(),
                    checked ? "Biometric unlock enabled" : "Biometric unlock disabled",
                    Toast.LENGTH_SHORT).show();
        });

        etTheme.setOnItemClickListener((parent, v, pos, id) -> {
            String[] keys = {"system", "light", "dark"};
            prefs.setTheme(keys[pos]);
            MainActivity.applyTheme(keys[pos]);
            Toast.makeText(requireContext(), "Theme updated", Toast.LENGTH_SHORT).show();
        });

        etSim.setOnItemClickListener((parent, v, pos, id) -> {
            prefs.setDefaultSim(pos);
            Toast.makeText(requireContext(), "Default SIM updated", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnViewUpiProfile).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse(USSDBuilder.buildTelUri(USSDBuilder.viewProfile())));
            startActivity(intent);
        });
    }
}
