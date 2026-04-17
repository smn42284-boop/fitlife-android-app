package com.example.fitlife_sumyatnoe.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.fitlife_sumyatnoe.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageLoader {
    private static final int MAX_IMAGE_WIDTH = 800;
    private static final int MAX_IMAGE_HEIGHT = 800;
    private static final int COMPRESSION_QUALITY = 70;

    private static RequestOptions getWorkoutOptions() {
        return new RequestOptions()
                .placeholder(R.drawable.workout_placeholder)
                .error(R.drawable.workout_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .override(MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT)  // ✅ Force resize to 800x800
                .encodeQuality(COMPRESSION_QUALITY)
                .timeout(10000);
    }
    public static void loadImageFromUri(Context context, Uri uri, ImageView imageView) {
        if (uri == null) {
            imageView.setImageResource(R.drawable.workout_placeholder);
            return;
        }

        Glide.with(context)
                .load(uri)
                .apply(getWorkoutOptions())
                .into(imageView);
    }

    private static RequestOptions getExerciseOptions() {
        return new RequestOptions()
                .placeholder(R.drawable.ic_fitness)
                .error(R.drawable.ic_fitness)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .override(400, 400)  // ✅ Smaller for exercises
                .encodeQuality(COMPRESSION_QUALITY)
                .timeout(10000);
    }

    private static RequestOptions getProfileOptions() {
        return new RequestOptions()
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .override(200, 200)
                .encodeQuality(COMPRESSION_QUALITY)
                .timeout(10000);
    }

    // ========== WORKOUT IMAGE ==========
    public static void loadWorkoutImage(Context context, String imageSource, ImageView imageView) {
        Log.d("ImageLoaderDebug", "Loading image: " + imageSource);

        if (imageSource == null || imageSource.isEmpty()) {
            Log.d("ImageLoaderDebug", "Image source is null or empty");
            imageView.setImageResource(R.drawable.workout_placeholder);
            return;
        }

        int resId = context.getResources().getIdentifier(imageSource, "drawable", context.getPackageName());
        if (resId != 0) {
            Log.d("ImageLoaderDebug", "Loading from drawable: " + imageSource);
            Glide.with(context)
                    .load(resId)
                    .apply(getWorkoutOptions())
                    .into(imageView);
            return;
        }

        File imageFile = new File(imageSource);
        if (imageFile.exists()) {
            long fileSize = imageFile.length();
            Log.d("ImageLoaderDebug", "Loading from file, size: " + fileSize / 1024 + "KB");

            Glide.with(context)
                    .load(imageFile)
                    .apply(getWorkoutOptions())
                    .into(imageView);
            return;
        }

        if (imageSource.startsWith("http://") || imageSource.startsWith("https://")) {
            Log.d("ImageLoaderDebug", "Loading from URL: " + imageSource);
            Glide.with(context)
                    .load(imageSource)
                    .apply(getWorkoutOptions())
                    .into(imageView);
            return;
        }

        Log.e("ImageLoaderDebug", "No valid image source found for: " + imageSource);
        imageView.setImageResource(R.drawable.workout_placeholder);
    }

    public static void loadExerciseImage(Context context, String imageSource, ImageView imageView) {
        Log.d("ImageLoaderDebug", "Loading exercise image: " + imageSource);

        if (imageSource == null || imageSource.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_fitness);
            return;
        }

        int resId = context.getResources().getIdentifier(imageSource, "drawable", context.getPackageName());
        if (resId != 0) {
            Log.d("ImageLoaderDebug", "Found drawable: " + imageSource);
            Glide.with(context)
                    .load(resId)
                    .placeholder(R.drawable.ic_fitness)
                    .error(R.drawable.ic_fitness)
                    .centerCrop()
                    .into(imageView);
            return;
        }

        resId = context.getResources().getIdentifier("exercise_" + imageSource, "drawable", context.getPackageName());
        if (resId != 0) {
            Log.d("ImageLoaderDebug", "Found drawable with prefix: exercise_" + imageSource);
            Glide.with(context)
                    .load(resId)
                    .placeholder(R.drawable.ic_fitness)
                    .error(R.drawable.ic_fitness)
                    .centerCrop()
                    .into(imageView);
            return;
        }

        // Fallback
        Log.e("ImageLoaderDebug", "No drawable found for: " + imageSource);
        imageView.setImageResource(R.drawable.ic_fitness);
    }
    public static String compressAndSaveImage(Context context, Uri imageUri, String fileName) {
        Bitmap bitmap = null;
        FileOutputStream out = null;

        try {
            File dir = new File(context.getFilesDir(), "compressed_images");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            int sampleSize = 1;
            while (options.outWidth / sampleSize > MAX_IMAGE_WIDTH * 2 &&
                    options.outHeight / sampleSize > MAX_IMAGE_HEIGHT * 2) {
                sampleSize *= 2;
            }

            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            inputStream = context.getContentResolver().openInputStream(imageUri);
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            if (bitmap == null) {
                return null;
            }

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > MAX_IMAGE_WIDTH || height > MAX_IMAGE_HEIGHT) {
                float ratio = Math.min((float) MAX_IMAGE_WIDTH / width,
                        (float) MAX_IMAGE_HEIGHT / height);
                int newWidth = Math.round(width * ratio);
                int newHeight = Math.round(height * ratio);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                bitmap.recycle();
                bitmap = resizedBitmap;
            }

            File outputFile = new File(dir, fileName + ".jpg");
            out = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out);
            out.flush();

            Log.d("ImageLoader", "Saved compressed image: " + outputFile.getAbsolutePath() +
                    " Size: " + outputFile.length() / 1024 + "KB");

            return outputFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e("ImageLoader", "Error compressing image", e);
            return null;
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void clearCache(Context context) {
        new Thread(() -> {
            try {
                // Clear Glide cache
                Glide.get(context).clearDiskCache();

                // Clear compressed images
                File dir = new File(context.getFilesDir(), "compressed_images");
                if (dir.exists()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            file.delete();
                        }
                    }
                    dir.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Clear memory cache on main thread
        Glide.get(context).clearMemory();
    }
}