package com.offlineupi.app.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.offlineupi.app.R;
import com.offlineupi.app.models.Favorite;
import com.offlineupi.app.models.Transaction;
import com.offlineupi.app.utils.AppPreferences;
import com.offlineupi.app.utils.USSDBuilder;

import java.util.Locale;

public class PayFragment extends Fragment {

    private enum PayMode { MOBILE, UPI_ID, ACCOUNT }
    private PayMode currentMode = PayMode.MOBILE;

    private MaterialButtonToggleGroup toggleRecipientType;
    private TextInputLayout tilIdentifier, tilAmount;
    private TextInputEditText etIdentifier, etAmount;
    private AutoCompleteTextView etSimSelect;
    private MaterialButton btnPay, btnAddFavorite;
    private View formFieldsContainer;
    private TextView tvMenuMessage;

    private AppPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pay, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = AppPreferences.getInstance(requireContext());

        bindViews(view);
        setupRecipientToggle();
        setupQuickAmounts(view);
        setupSimDropdown();
        setupPayButton();
        setupAddFavorite();

        // Handle pre-fill
        Bundle args = getArguments();
        if (args != null && args.containsKey("prefill_identifier")) {
            applyPrefill(args);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setPayMode(currentMode);
    }

    private void bindViews(View view) {
        toggleRecipientType = view.findViewById(R.id.toggleRecipientType);
        tilIdentifier = view.findViewById(R.id.tilIdentifier);
        etIdentifier = view.findViewById(R.id.etIdentifier);
        tilAmount = view.findViewById(R.id.tilAmount);
        etAmount = view.findViewById(R.id.etAmount);
        etSimSelect = view.findViewById(R.id.etSimSelect);
        btnPay = view.findViewById(R.id.btnPay);
        btnAddFavorite = view.findViewById(R.id.btnAddFavorite);
        formFieldsContainer = view.findViewById(R.id.formFieldsContainer);
        tvMenuMessage = view.findViewById(R.id.tvMenuMessage);
    }

    private void applyPrefill(Bundle args) {
        String identifier = args.getString("prefill_identifier");
        boolean isUpi = args.getBoolean("prefill_is_upi", false);
        
        if (isUpi) {
            toggleRecipientType.check(R.id.btnUpiId);
            setPayMode(PayMode.UPI_ID);
        } else {
            toggleRecipientType.check(R.id.btnMobile);
            setPayMode(PayMode.MOBILE);
            etIdentifier.setText(identifier);
        }
    }

    private void setupRecipientToggle() {
        toggleRecipientType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnMobile) {
                setPayMode(PayMode.MOBILE);
            } else if (checkedId == R.id.btnUpiId) {
                setPayMode(PayMode.UPI_ID);
            } else if (checkedId == R.id.btnAccount) {
                setPayMode(PayMode.ACCOUNT);
            }
        });
        toggleRecipientType.check(R.id.btnMobile);
    }

    private void setPayMode(PayMode mode) {
        currentMode = mode;
        boolean isJio = prefs != null && prefs.isJioMode();
        
        if (isJio) {
            formFieldsContainer.setVisibility(View.GONE);
            tvMenuMessage.setVisibility(View.VISIBLE);
            btnAddFavorite.setVisibility(View.GONE);
            tvMenuMessage.setText("Jio mode uses IVR. You will need to enter all details via voice prompts after the call connects.");
            btnPay.setText("Call NPCI IVR");
            return;
        }

        if (mode == PayMode.MOBILE) {
            formFieldsContainer.setVisibility(View.VISIBLE);
            tvMenuMessage.setVisibility(View.GONE);
            btnPay.setText("Pay Offline  ↗");
            btnAddFavorite.setVisibility(View.VISIBLE);
        } else {
            formFieldsContainer.setVisibility(View.GONE);
            tvMenuMessage.setVisibility(View.VISIBLE);
            btnAddFavorite.setVisibility(View.GONE);
            
            if (mode == PayMode.UPI_ID) {
                String msg = "This will dial the UPI ID menu. Enter the ID in the system popup.";
                if (getArguments() != null && getArguments().containsKey("prefill_identifier")) {
                    msg += "\n\n(UPI ID has been copied to your clipboard. Long press and paste in the popup!)";
                }
                tvMenuMessage.setText(msg);
                btnPay.setText("Open UPI Menu");
            } else {
                tvMenuMessage.setText("This will dial the Account + IFSC menu. Enter details in the system popup.");
                btnPay.setText("Open Account Menu");
            }
        }
    }

    private void setupQuickAmounts(View view) {
        int[] chipIds = {R.id.chip100, R.id.chip200, R.id.chip500, R.id.chip1000, R.id.chip2000};
        int[] amounts = {100, 200, 500, 1000, 2000};
        for (int i = 0; i < chipIds.length; i++) {
            final int amount = amounts[i];
            View chip = view.findViewById(chipIds[i]);
            if (chip != null) {
                chip.setOnClickListener(v -> etAmount.setText(String.valueOf(amount)));
            }
        }
    }

    private void setupSimDropdown() {
        String[] simOptions = {"Auto (Default SIM)", "SIM 1", "SIM 2"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, simOptions);
        etSimSelect.setAdapter(adapter);
        etSimSelect.setText(simOptions[prefs.getDefaultSim()], false);
    }

    private void setupPayButton() {
        btnPay.setOnClickListener(v -> {
            if (prefs.isJioMode()) {
                dialIvr();
                return;
            }

            if (currentMode == PayMode.MOBILE) {
                validateAndPayMobile();
            } else if (currentMode == PayMode.UPI_ID) {
                dialUssd(USSDBuilder.sendMoneyByUpiIdMenu(), "UPI Menu", 0);
            } else {
                dialUssd(USSDBuilder.sendMoneyByAccountMenu(), "Account Menu", 0);
            }
        });
    }

    private void validateAndPayMobile() {
        String identifier = etIdentifier.getText() != null ? etIdentifier.getText().toString().trim() : "";
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";

        if (identifier.isEmpty()) {
            tilIdentifier.setError("Enter mobile number");
            return;
        }
        tilIdentifier.setError(null);

        if (amountStr.isEmpty()) {
            tilAmount.setError("Enter amount");
            return;
        }
        tilAmount.setError(null);

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            tilAmount.setError("Invalid amount");
            return;
        }

        String ussdCode = USSDBuilder.sendMoneyByMobile(identifier, amountStr);
        String displayAmount = String.format(Locale.getDefault(), "₹%.0f", amount);
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Payment")
                .setMessage("Send " + displayAmount + " to " + identifier + "?")
                .setPositiveButton("Confirm & Call", (d, w) -> dialUssd(ussdCode, identifier, amount))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupAddFavorite() {
        btnAddFavorite.setOnClickListener(v -> {
            String identifier = etIdentifier.getText() != null ? etIdentifier.getText().toString().trim() : "";
            if (identifier.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a number first", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddFavoriteDialog(identifier);
        });
    }

    private void showAddFavoriteDialog(String identifier) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_favorite, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etFavName);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Save as Favourite")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : identifier;
                    Favorite fav = new Favorite(name, identifier, false);
                    prefs.addFavorite(fav);
                    Toast.makeText(requireContext(), "Saved ⭐", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void dialUssd(String ussdCode, String recipient, double amount) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(USSDBuilder.buildTelUri(ussdCode)));
        startActivity(intent);

        Transaction tx = new Transaction(
                Transaction.Type.SEND_MONEY,
                Transaction.Mode.USSD,
                recipient, null, amount, ussdCode);
        prefs.addTransaction(tx);
    }

    private void dialIvr() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(USSDBuilder.ivrNumber()));
        startActivity(intent);

        Transaction tx = new Transaction(
                Transaction.Type.IVR_CALL,
                Transaction.Mode.IVR,
                "NPCI IVR", null, 0, "IVR");
        prefs.addTransaction(tx);
    }
}
