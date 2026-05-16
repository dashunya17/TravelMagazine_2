package com.example.travelmagazine;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelmagazine.attributes.excursion;
import java.util.List;

public class ExcursionAdapter extends RecyclerView.Adapter<ExcursionAdapter.ViewHolder> {
    private List<excursion> excursions;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(excursion excursion);
    }

    public ExcursionAdapter(List<excursion> excursions, OnItemClickListener listener) {
        this.excursions = excursions;
        this.listener = listener;
        Log.d("ExcursionAdapter", "Adapter created with list size: " + (excursions != null ? excursions.size() : 0));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_excursions, parent, false);
        Log.d("ExcursionAdapter", "ViewHolder created");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        excursion exc = excursions.get(position);
        Log.d("ExcursionAdapter", "Binding excursion at position " + position + ": " + exc.getName());

        holder.textName.setText(exc.getName());
        holder.ratingBar.setRating((float) exc.getEstimation());
        holder.textRatingValue.setText(String.format("%.1f", exc.getEstimation()));

        if (exc.getPhoto() != null && !exc.getPhoto().isEmpty()) {
            Log.d("ExcursionAdapter", "Loading image from URL: " + exc.getPhoto());
            Glide.with(holder.itemView.getContext())
                    .load(exc.getPhoto())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.imageView);
        } else {
            Log.d("ExcursionAdapter", "No photo URL for: " + exc.getName());
            holder.imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(exc));
    }

    @Override
    public int getItemCount() {
        int size = excursions != null ? excursions.size() : 0;
        Log.d("ExcursionAdapter", "getItemCount() = " + size);
        return size;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textName, textRatingValue;
        RatingBar ratingBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textName = itemView.findViewById(R.id.textName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            textRatingValue = itemView.findViewById(R.id.textRatingValue);
        }
    }
}