package com.offlineupi.app.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.offlineupi.app.R;
import com.offlineupi.app.adapters.FavoriteAdapter;
import com.offlineupi.app.models.Favorite;
import com.offlineupi.app.models.Transaction;
import com.offlineupi.app.utils.AppPreferences;
import com.offlineupi.app.utils.USSDBuilder;

import java.util.List;

public class HomeFragment extends Fragment {

    private AppPreferences prefs;
    private RecyclerView rvFavorites;
    private FavoriteAdapter favoriteAdapter;
    private TextView tvGreeting, tvSubtitle;
    private TextView toggleUssd, toggleIvr;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = AppPreferences.getInstance(requireContext());

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        toggleUssd = view.findViewById(R.id.toggleUssd);
        toggleIvr = view.findViewById(R.id.toggleIvr);
        rvFavorites = view.findViewById(R.id.rvFavorites);

        setupGreeting();
        setupModeToggle();
        setupFavorites();
        setupQuickActions(view);
        updateUIForMode();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupGreeting();
        refreshFavorites();
        updateUIForMode();
    }

    private void setupGreeting() {
        String name = prefs.getUserName();
        String greeting = getGreeting();
        tvGreeting.setText(name.isEmpty() ? greeting + "! 👋" : greeting + ", " + name + "! 👋");
    }

    private String getGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }

    private void setupModeToggle() {
        toggleUssd.setOnClickListener(v -> setAppMode(false));
        toggleIvr.setOnClickListener(v -> setAppMode(true));
        updateToggleUI();
    }

    private void setAppMode(boolean isJio) {
        if (prefs.isJioMode() == isJio) return;
        
        prefs.setJioMode(isJio);
        updateToggleUI();
        updateUIForMode();
        
        if (getActivity() instanceof com.offlineupi.app.MainActivity) {
            ((com.offlineupi.app.MainActivity) getActivity()).updateFabVisibility(false);
        }

        String msg = isJio ? "Switched to Jio IVR Mode" : "Switched to Standard USSD";
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void updateToggleUI() {
        boolean isJio = prefs.isJioMode();
        toggleUssd.setBackgroundResource(isJio ? 0 : R.drawable.bg_toggle_active);
        toggleUssd.setTextColor(isJio ? 0xCCFFFFFF : 0xFF5C35D9);
        toggleIvr.setBackgroundResource(isJio ? R.drawable.bg_toggle_active : 0);
        toggleIvr.setTextColor(isJio ? 0xFF5C35D9 : 0xCCFFFFFF);
    }

    private void updateUIForMode() {
        boolean isJio = prefs.isJioMode();
        tvSubtitle.setText(isJio ? "Pay offline using IVR Call" : "Pay offline using USSD");
        
        View view = getView();
        if (view == null) return;
        
        // Hide USSD-only actions if in Jio mode
        int visibility = isJio ? View.GONE : View.VISIBLE;
        View cardPending = view.findViewById(R.id.cardPending);
        View cardRequest = view.findViewById(R.id.cardRequestMoney);
        View cardStatement = view.findViewById(R.id.cardMiniStatement);

        if (cardPending != null) cardPending.setVisibility(visibility);
        if (cardRequest != null) cardRequest.setVisibility(visibility);
        if (cardStatement != null) cardStatement.setVisibility(visibility);
    }

    private void setupFavorites() {
        List<Favorite> favorites = prefs.getFavorites();
        favoriteAdapter = new FavoriteAdapter(favorites, fav -> {
            Bundle args = new Bundle();
            args.putString("prefill_identifier", fav.getIdentifier());
            args.putString("prefill_name", fav.getName());
            args.putBoolean("prefill_is_upi", fav.isUpiId());

            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.payFragment);
                Navigation.findNavController(requireView()).navigate(R.id.payFragment, args);
            } else {
                Navigation.findNavController(requireView()).navigate(R.id.payFragment, args);
            }
        });
        rvFavorites.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFavorites.setAdapter(favoriteAdapter);
    }

    private void refreshFavorites() {
        List<Favorite> favorites = prefs.getFavorites();
        if (favoriteAdapter != null) {
            favoriteAdapter.updateData(favorites);
        }
        View noFavText = getView() != null ? getView().findViewById(R.id.tvNoFavorites) : null;
        if (noFavText != null) {
            noFavText.setVisibility(favorites.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void setupQuickActions(View view) {
        setupCard(view.findViewById(R.id.cardBalance), "💰", "Balance", () -> executeUssd(Transaction.Type.CHECK_BALANCE, USSDBuilder.checkBalance()));
        setupCard(view.findViewById(R.id.cardMiniStatement), "📋", "Statement", () -> executeUssd(Transaction.Type.MINI_STATEMENT, USSDBuilder.miniStatement()));
        setupCard(view.findViewById(R.id.cardPending), "⏳", "Pending", () -> executeUssd(Transaction.Type.PENDING_REQUESTS, USSDBuilder.pendingRequests()));
        setupCard(view.findViewById(R.id.cardRequestMoney), "📲", "Request", () -> executeUssd(Transaction.Type.REQUEST_MONEY, USSDBuilder.requestMoney()));

        view.findViewById(R.id.btnPayNow).setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.payFragment);
            } else {
                Navigation.findNavController(view).navigate(R.id.payFragment);
            }
        });
    }

    private void setupCard(View card, String icon, String label, Runnable action) {
        if (card == null) return;
        TextView tvIcon = card.findViewById(R.id.tvCardIcon);
        TextView tvLabel = card.findViewById(R.id.tvCardLabel);
        if (tvIcon != null) tvIcon.setText(icon);
        if (tvLabel != null) tvLabel.setText(label);
        card.setOnClickListener(v -> action.run());
    }

    private void executeUssd(Transaction.Type type, String ussdCode) {
        boolean isJio = prefs.isJioMode();
        String telUri;
        Transaction.Mode mode;

        if (isJio) {
            telUri = USSDBuilder.ivrNumber();
            mode = Transaction.Mode.IVR;
        } else {
            telUri = USSDBuilder.buildTelUri(ussdCode);
            mode = Transaction.Mode.USSD;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse(telUri));
            startActivity(intent);

            Transaction tx = new Transaction(type, mode, null, null, 0, ussdCode);
            prefs.addTransaction(tx);
        } else {
            Toast.makeText(requireContext(), "Permission required", Toast.LENGTH_SHORT).show();
        }
    }
}
