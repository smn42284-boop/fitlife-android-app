package com.example.fitlife_sumyatnoe;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class FitLifeGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        int memoryCacheSizeBytes = 1024 * 1024 * 20;
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));

        builder.setDiskCache(new DiskLruCacheFactory(
                context.getCacheDir().getAbsolutePath(),
                "glide_cache",
                100 * 1024 * 1024
        ));    }
}