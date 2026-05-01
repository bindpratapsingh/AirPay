package com.offlineupi.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.offlineupi.app.R;
import com.offlineupi.app.adapters.TransactionAdapter;
import com.offlineupi.app.models.Transaction;
import com.offlineupi.app.utils.AppPreferences;

import java.util.List;

public class HistoryFragment extends Fragment {

    private AppPreferences prefs;
    private RecyclerView rvHistory;
    private View tvEmpty;
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = AppPreferences.getInstance(requireContext());

        rvHistory = view.findViewById(R.id.rvHistory);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        view.findViewById(R.id.btnClearHistory).setOnClickListener(v -> confirmClear());

        loadHistory();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        List<Transaction> transactions = prefs.getTransactions();
        if (transactions.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        rvHistory.setVisibility(View.VISIBLE);

        adapter = new TransactionAdapter(transactions);
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setAdapter(adapter);
    }

    private void confirmClear() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear all transaction history?")
                .setPositiveButton("Clear", (d, w) -> {
                    prefs.clearTransactions();
                    loadHistory();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
