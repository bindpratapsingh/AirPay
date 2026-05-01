package com.offlineupi.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineupi.app.R;
import com.offlineupi.app.models.Favorite;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Favorite favorite);
    }

    private List<Favorite> favorites;
    private final OnFavoriteClickListener listener;

    public FavoriteAdapter(List<Favorite> favorites, OnFavoriteClickListener listener) {
        this.favorites = favorites;
        this.listener = listener;
    }

    public void updateData(List<Favorite> newData) {
        this.favorites = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Favorite fav = favorites.get(position);
        holder.tvInitials.setText(fav.getInitialsDisplay());
        holder.tvName.setText(fav.getName());
        holder.itemView.setOnClickListener(v -> listener.onFavoriteClick(fav));
    }

    @Override
    public int getItemCount() { return favorites.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName;

        ViewHolder(View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tvFavInitials);
            tvName = itemView.findViewById(R.id.tvFavName);
        }
    }
}
