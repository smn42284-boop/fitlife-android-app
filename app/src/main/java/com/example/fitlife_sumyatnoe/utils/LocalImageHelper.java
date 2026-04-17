package com.example.fitlife_sumyatnoe.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalImageHelper {

    private Context context;

    // Image dimensions
    private static final int MAX_PROFILE_WIDTH = 200;
    private static final int MAX_PROFILE_HEIGHT = 200;
    private static final int MAX_WORKOUT_WIDTH = 400;
    private static final int MAX_WORKOUT_HEIGHT = 400;
    private static final int MAX_EXERCISE_WIDTH = 200;
    private static final int MAX_EXERCISE_HEIGHT = 200;
    private static final int QUALITY = 75;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public LocalImageHelper(Context context) {
        this.context = context;
    }


    public String saveWorkoutImage(Uri imageUri, String workoutId) {
        Bitmap bitmap = null;
        try {
            File workoutDir = new File(context.getFilesDir(), "workout_images");
            if (!workoutDir.exists()) workoutDir.mkdirs();

            String fileName = "workout_" + workoutId + ".jpg";
            File destFile = new File(workoutDir, fileName);

            if (destFile.exists()) {
                destFile.delete();
            }

            bitmap = compressImage(imageUri, 800, 800);

            if (bitmap != null) {
                FileOutputStream out = new FileOutputStream(destFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
                out.close();


                return destFile.getAbsolutePath();
            }
        } catch (Exception e) {
            Log.e("LocalImageHelper", "Error saving workout image", e);
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        return null;
    }

    public String saveExerciseImage(Uri imageUri, String exerciseId) {
        Bitmap compressedBitmap = null;
        try {
            File exerciseDir = new File(context.getFilesDir(), "exercise_images");
            if (!exerciseDir.exists()) exerciseDir.mkdirs();

            String fileName = "exercise_" + exerciseId + ".jpg";
            File destFile = new File(exerciseDir, fileName);

            compressedBitmap = compressImage(imageUri, MAX_EXERCISE_WIDTH, MAX_EXERCISE_HEIGHT);

            if (compressedBitmap != null) {
                FileOutputStream out = new FileOutputStream(destFile);
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, out);
                out.flush();
                out.close();
                return destFile.getAbsolutePath();
            }
        } catch (IOException e) {
            Log.e("LocalImageHelper", "Error saving exercise image", e);
        } finally {
            if (compressedBitmap != null && !compressedBitmap.isRecycled()) {
                compressedBitmap.recycle();
            }
        }
        return null;
    }

    // ========== GENERIC METHOD ==========


    // ========== COMPRESSION HELPER ==========

    private Bitmap compressImage(Uri imageUri, int reqWidth, int reqHeight) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        int sampleSize = 1;
        while (options.outWidth / sampleSize > reqWidth * 2 ||
                options.outHeight / sampleSize > reqHeight * 2) {
            sampleSize *= 2;
        }
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;  // ✅ Uses less memory

        inputStream = context.getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        return bitmap;
    }


    public void saveProfileImageAsync(Uri imageUri, String userId, ImageSaveCallback callback) {
        executor.execute(() -> {
            String path = saveProfileImage(imageUri, userId);
            if (callback != null) {
                callback.onImageSaved(path);
            }
        });
    }

    public String saveProfileImage(Uri imageUri, String userId) {
        Bitmap compressedBitmap = null;
        try {
            File profileDir = new File(context.getFilesDir(), "profile_images");
            if (!profileDir.exists()) profileDir.mkdirs();

            String fileName = "profile_" + userId + ".jpg";
            File destFile = new File(profileDir, fileName);

            compressedBitmap = compressImageFast(imageUri, MAX_PROFILE_WIDTH, MAX_PROFILE_HEIGHT);

            if (compressedBitmap != null) {
                FileOutputStream out = new FileOutputStream(destFile);
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, out);
                out.flush();
                out.close();
                return destFile.getAbsolutePath();
            }
        } catch (IOException e) {
            Log.e("LocalImageHelper", "Error saving profile image", e);
        } finally {
            if (compressedBitmap != null && !compressedBitmap.isRecycled()) {
                compressedBitmap.recycle();
            }
        }
        return null;
    }

    private Bitmap compressImageFast(Uri imageUri, int reqWidth, int reqHeight) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        // More aggressive sampling
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565; // Uses less memory

        inputStream = context.getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        return bitmap;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        // More aggressive scaling
        while (height / inSampleSize > reqHeight * 2 && width / inSampleSize > reqWidth * 2) {
            inSampleSize *= 2;
        }

        return inSampleSize;
    }

    public interface ImageSaveCallback {
        void onImageSaved(String path);
    }

    public String getProfileImagePath(String userId) {
        File directory = new File(context.getFilesDir(), "profile_images");
        File imageFile = new File(directory, "profile_" + userId + ".jpg");
        return imageFile.exists() ? imageFile.getAbsolutePath() : null;
    }

    // ========== DELETE METHODS ==========

    public void deleteWorkoutImage(String workoutId) {
        File workoutDir = new File(context.getFilesDir(), "workout_images");
        File imageFile = new File(workoutDir, "workout_" + workoutId + ".jpg");
        if (imageFile.exists()) imageFile.delete();
    }

    public void deleteExerciseImage(String exerciseId) {
        File exerciseDir = new File(context.getFilesDir(), "exercise_images");
        File imageFile = new File(exerciseDir, "exercise_" + exerciseId + ".jpg");
        if (imageFile.exists()) imageFile.delete();
    }

    public void deleteProfileImage(String userId) {
        File profileDir = new File(context.getFilesDir(), "profile_images");
        File imageFile = new File(profileDir, "profile_" + userId + ".jpg");
        if (imageFile.exists()) imageFile.delete();
    }

    // ========== GET METHODS ==========

    public File getWorkoutImage(String workoutId) {
        File workoutDir = new File(context.getFilesDir(), "workout_images");
        File imageFile = new File(workoutDir, "workout_" + workoutId + ".jpg");
        return imageFile.exists() ? imageFile : null;
    }

    public File getExerciseImage(String exerciseId) {
        File exerciseDir = new File(context.getFilesDir(), "exercise_images");
        File imageFile = new File(exerciseDir, "exercise_" + exerciseId + ".jpg");
        return imageFile.exists() ? imageFile : null;
    }

    public File getProfileImage(String userId) {
        File profileDir = new File(context.getFilesDir(), "profile_images");
        File imageFile = new File(profileDir, "profile_" + userId + ".jpg");
        return imageFile.exists() ? imageFile : null;
    }

    public File getImageFile(String imagePath) {
        if (imagePath != null) {
            File imageFile = new File(imagePath);
            return imageFile.exists() ? imageFile : null;
        }
        return null;
    }

    public boolean imageExists(String imagePath) {
        if (imagePath != null) {
            File imageFile = new File(imagePath);
            return imageFile.exists();
        }
        return false;
    }
}