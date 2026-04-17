package com.example.fitlife_sumyatnoe.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitlife_sumyatnoe.R;

public class IntroViewPagerAdapter extends RecyclerView.Adapter<IntroViewPagerAdapter.ViewHolder> {

    private int[] images;
    private String[] titles;
    private String[] descriptions;

    public IntroViewPagerAdapter(int[] images, String[] titles, String[] descriptions) {
        this.images = images;
        this.titles = titles;
        this.descriptions = descriptions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_intro, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.titleText.setText(titles[position]);
        holder.descriptionText.setText(descriptions[position]);

        holder.illustration.setImageResource(images[position]);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView illustration;
        TextView titleText, descriptionText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            illustration = itemView.findViewById(R.id.illustration);
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
        }
    }
}