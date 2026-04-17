package com.example.fitlife_sumyatnoe.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.activities.TwoFactorAuthActivity;

public class IntroAdapter extends RecyclerView.Adapter<IntroAdapter.IntroViewHolder> {

    private Context context;
    private int[] layouts = {
            R.layout.intro_page1,
            R.layout.intro_page2,
            R.layout.intro_page3
    };

    public IntroAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public IntroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layouts[viewType], parent, false);
        return new IntroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IntroViewHolder holder, int position) {
        if (position == 2) {
            Button enableBtn = holder.itemView.findViewById(R.id.enable2FABtn);
            TextView remindLater = holder.itemView.findViewById(R.id.remindLaterBtn);

            enableBtn.setOnClickListener(v -> {
                // Go to 2FA settings
                context.startActivity(new Intent(context, TwoFactorAuthActivity.class));
            });

            remindLater.setOnClickListener(v -> {
                // Just continue
            });
        }
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class IntroViewHolder extends RecyclerView.ViewHolder {
        IntroViewHolder(View itemView) {
            super(itemView);
        }
    }
}