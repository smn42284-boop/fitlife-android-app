package com.example.fitlife_sumyatnoe.utils;

import static com.example.fitlife_sumyatnoe.utils.PermissionHelper.CAMERA_PERMISSION_CODE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.fitlife_sumyatnoe.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImagePickerHelper {

    private static final int CAMERA_REQUEST = 101;
    private static final int PICK_IMAGE_REQUEST = 100;

    private static final int MAX_IMAGE_SIZE = 800;
    private static final int COMPRESSION_QUALITY = 70;

    private AppCompatActivity activity;
    private Uri currentImageUri;
    private ImagePickerCallback callback;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public interface ImagePickerCallback {
        void onImageSelected(Uri imageUri);
        void onError(String error);
    }

    public ImagePickerHelper(AppCompatActivity activity, ImagePickerCallback callback) {
        this.activity = activity;
        this.callback = callback;
        setupLaunchers();
    }
    private void compressAndSaveImage(Uri originalUri) {
        new Thread(() -> {
            try {
                InputStream inputStream = activity.getContentResolver().openInputStream(originalUri);
                int originalSize = inputStream.available();
                inputStream.close();

                // Create compressed file
                File compressDir = new File(activity.getFilesDir(), "compressed_images");
                if (!compressDir.exists()) compressDir.mkdirs();

                String fileName = "img_" + System.currentTimeMillis() + ".jpg";
                File compressedFile = new File(compressDir, fileName);

                // Compress
                boolean success = Boolean.parseBoolean(compressImage(originalUri, compressedFile));

                if (success && compressedFile.exists()) {
                    String compressedPath = compressedFile.getAbsolutePath();

                    Uri compressedUri = Uri.fromFile(compressedFile);
                    activity.runOnUiThread(() -> {
                        if (callback != null) {
                            callback.onImageSelected(compressedUri);
                        }
                    });
                } else {
                    activity.runOnUiThread(() -> {
                        if (callback != null) {
                            // Fallback to original if compression fails
                            callback.onImageSelected(originalUri);
                        }
                    });
                }

            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    if (callback != null) {
                        callback.onError("Error: " + e.getMessage());
                    }
                });
            }
        }).start();
    }
    private void setupLaunchers() {
        // Camera launcher
        cameraLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && currentImageUri != null) {
                        Log.d("ImagePickerHelper", "Camera result OK, URI: " + currentImageUri);
                        compressAndCallback(currentImageUri);
                    } else if (callback != null) {
                        callback.onError("Camera cancelled");
                    }
                }
        );

        galleryLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        Log.d("ImagePickerHelper", "Gallery result OK, URI: " + imageUri);
                        if (imageUri != null && callback != null) {
                            compressAndCallback(imageUri);
                        }
                    } else if (callback != null) {
                        callback.onError("Gallery cancelled");
                    }
                }
        );
    }

    private void compressAndCallback(Uri originalUri) {
        new Thread(() -> {
            try {
                InputStream inputStream = activity.getContentResolver().openInputStream(originalUri);
                int originalSize = inputStream.available();
                inputStream.close();
                Log.d("ImagePickerHelper", "Original image size: " + originalSize / 1024 + "KB");

                // Compress image
                File compressedFile = null;
                String compressedPath = compressImage(originalUri, compressedFile);

                if (compressedPath != null) {
                    compressedFile = new File(compressedPath);
                    Log.d("ImagePickerHelper", "Compressed image size: " + compressedFile.length() / 1024 + "KB");

                    Uri compressedUri = Uri.fromFile(compressedFile);
                    activity.runOnUiThread(() -> {
                        if (callback != null) {
                            callback.onImageSelected(compressedUri);
                        }
                    });
                } else {
                    activity.runOnUiThread(() -> {
                        if (callback != null) {
                            callback.onImageSelected(originalUri);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("ImagePickerHelper", "Compression error", e);
                activity.runOnUiThread(() -> {
                    if (callback != null) {
                        callback.onImageSelected(originalUri);
                    }
                });
            }
        }).start();
    }

    private String compressImage(Uri imageUri, File compressedFile) {
        Bitmap bitmap = null;
        FileOutputStream out = null;

        try {
            // Create directory for compressed images
            File compressDir = new File(activity.getCacheDir(), "compressed");
            if (!compressDir.exists()) {
                compressDir.mkdirs();
            }

            // Get image dimensions without loading full image
            InputStream inputStream = activity.getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // Calculate sample size to reduce dimensions
            int sampleSize = 1;
            while (options.outWidth / sampleSize > MAX_IMAGE_SIZE * 2 &&
                    options.outHeight / sampleSize > MAX_IMAGE_SIZE * 2) {
                sampleSize *= 2;
            }

            // Decode with sample size
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;  // Uses less memory

            inputStream = activity.getContentResolver().openInputStream(imageUri);
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            if (bitmap == null) {
                return null;
            }

            // Resize if still too large
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE) {
                float ratio = Math.min((float) MAX_IMAGE_SIZE / width,
                        (float) MAX_IMAGE_SIZE / height);
                int newWidth = Math.round(width * ratio);
                int newHeight = Math.round(height * ratio);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                bitmap.recycle();
                bitmap = resizedBitmap;
            }

            // Save compressed image
            String fileName = "compressed_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(compressDir, fileName);
            out = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out);
            out.flush();

            return outputFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e("ImagePickerHelper", "Error compressing image", e);
            return null;
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        // This method is kept for backward compatibility
        // New implementation uses ActivityResultLauncher, so this may not be needed
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                if (callback != null) {
                    compressAndCallback(imageUri);
                }
            } else if (requestCode == CAMERA_REQUEST && data != null && data.getExtras() != null) {
                android.graphics.Bitmap bitmap = (android.graphics.Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    Uri imageUri = saveBitmapToUri(bitmap);
                    if (imageUri != null && callback != null) {
                        compressAndCallback(imageUri);
                    }
                }
            }
        } else if (resultCode != Activity.RESULT_CANCELED) {
            if (callback != null) {
                callback.onError("Image selection cancelled");
            }
        }
    }

    private Uri saveBitmapToUri(Bitmap bitmap) {
        // Save bitmap to app's cache directory
        FileOutputStream fos = null;
        try {
            File cacheDir = activity.getCacheDir();
            File imageFile = new File(cacheDir, "temp_image_" + System.currentTimeMillis() + ".jpg");
            fos = new FileOutputStream(imageFile);

            // ✅ Compress bitmap before saving
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, fos);
            fos.flush();

            return Uri.fromFile(imageFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showImagePickerDialog() {
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_image_picker, null);
        LinearLayout cameraOption = dialogView.findViewById(R.id.cameraOption);
        LinearLayout galleryOption = dialogView.findViewById(R.id.galleryOption);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        ImageView closeBtn = dialogView.findViewById(R.id.closeBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();

        if (cameraOption != null) {
            cameraOption.setOnClickListener(v -> {
                Log.d("ImagePickerHelper", "Camera option clicked");
                dialog.dismiss();
                openCamera();
            });
        }

        if (galleryOption != null) {
            galleryOption.setOnClickListener(v -> {
                Log.d("ImagePickerHelper", "Gallery option clicked");
                dialog.dismiss();
                openGallery();
            });
        }

        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> dialog.dismiss());
        }

        if (cancelBtn != null) {
            cancelBtn.setOnClickListener(v -> dialog.dismiss());
        }
    }

    public void openCamera() {
        // Check if camera permission is granted
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    100);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                currentImageUri = FileProvider.getUriForFile(activity,
                        "com.example.fitlife_sumyatnoe.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }
    private void openGallery() {
        Log.d("ImagePickerHelper", "Opening gallery");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "IMG_" + timeStamp + "_";
            File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            Log.d("ImagePickerHelper", "Created image file: " + image.getAbsolutePath());
            return image;
        } catch (IOException e) {
            Log.e("ImagePickerHelper", "Error creating image file", e);
            return null;
        }
    }

    public void displayImage(Uri imageUri, ImageView imageView) {
        if (imageUri != null) {
            Glide.with(activity)
                    .load(imageUri)
                    .override(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                    .centerCrop()
                    .placeholder(R.drawable.ic_fitness)
                    .error(R.drawable.ic_fitness)
                    .into(imageView);
        }
    }

    public Uri getSelectedImageUri() {
        return currentImageUri;
    }
}