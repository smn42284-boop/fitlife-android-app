package com.example.fitlife_sumyatnoe.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    public static final int SMS_PERMISSION_CODE = 100;
    public static final int CAMERA_PERMISSION_CODE = 200;
    public static final int STORAGE_PERMISSION_CODE = 201;

    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    public static void requestSmsPermission(Activity activity, PermissionCallback callback) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            callback.onPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }
    public static void requestCameraPermission(Activity activity, PermissionCallback callback) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            callback.onPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        }
    }

    public static void requestStoragePermission(Activity activity, PermissionCallback callback) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            callback.onPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }
}