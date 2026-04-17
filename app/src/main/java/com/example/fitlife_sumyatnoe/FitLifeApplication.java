package com.example.fitlife_sumyatnoe;

import android.app.Application;

import com.example.fitlife_sumyatnoe.database.AppDatabase;
import com.example.fitlife_sumyatnoe.utils.ImageLoader;

public class FitLifeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Room database
        AppDatabase.getInstance(this);

        // Clear Glide cache to remove any existing large images
        ImageLoader.clearCache(this);
    }
}