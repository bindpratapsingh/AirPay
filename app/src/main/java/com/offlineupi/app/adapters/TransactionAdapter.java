package com.offlineupi.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineupi.app.R;
import com.offlineupi.app.models.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction tx = transactions.get(position);
        holder.tvTitle.setText(tx.getDisplayTitle());
        holder.tvSubtitle.setText(tx.getDisplaySubtitle());

        // Set icon based on type
        String icon;
        switch (tx.getType()) {
            case SEND_MONEY: icon = "↗"; break;
            case REQUEST_MONEY: icon = "📩"; break;
            case CHECK_BALANCE: icon = "💰"; break;
            case PENDING_REQUESTS: icon = "⏳"; break;
            case MINI_STATEMENT: icon = "📋"; break;
            case VIEW_PROFILE: icon = "👤"; break;
            case CHANGE_LANGUAGE: icon = "🌐"; break;
            case IVR_CALL: icon = "📞"; break;
            default: icon = "•";
        }
        holder.tvIcon.setText(icon);

        // Mode & Status badge
        String modeStr = tx.getMode() == Transaction.Mode.IVR ? "IVR" : "USSD";
        holder.tvMode.setText(modeStr + " • Dialed");
    }

    @Override
    public int getItemCount() { return transactions.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle, tvSubtitle, tvMode;

        ViewHolder(View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvTxIcon);
            tvTitle = itemView.findViewById(R.id.tvTxTitle);
            tvSubtitle = itemView.findViewById(R.id.tvTxSubtitle);
            tvMode = itemView.findViewById(R.id.tvTxMode);
        }
    }
}
