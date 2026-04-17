package com.example.fitlife_sumyatnoe.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import java.util.List;

public class ShareHelper {

    public interface ShareCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public static void shareEquipment(Context context, List<String> equipment, ShareCallback callback) {
        StringBuilder message = new StringBuilder();
        message.append("📋 Equipment needed for today's workout:\n\n");

        for (String item : equipment) {
            message.append("• ").append(item).append("\n");
        }

        message.append("\nSent from FitLife App 💪");

        shareViaIntent(context, message.toString(), callback);
    }

    public static void shareWorkout(Context context, String workoutName,
                                    List<String> exercises, ShareCallback callback) {
        StringBuilder message = new StringBuilder();
        message.append("🏋️ Workout: ").append(workoutName).append("\n\n");
        message.append("Exercises:\n");

        for (String exercise : exercises) {
            message.append("• ").append(exercise).append("\n");
        }

        message.append("\nSent from FitLife App 💪");

        shareViaIntent(context, message.toString(), callback);
    }

    private static void shareViaIntent(Context context, String message, ShareCallback callback) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);

        // Create chooser with multiple apps
        Intent chooser = Intent.createChooser(shareIntent, "Share via");

        // Add specific apps if needed
        // You can add specific package names for Viber, Telegram, etc.

        try {
            context.startActivity(chooser);
            callback.onSuccess();
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    // Share via SMS specifically
    public static void shareViaSms(Context context, String message, String phoneNumber) {
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
        smsIntent.putExtra("sms_body", message);
        context.startActivity(smsIntent);
    }

    // Share via WhatsApp
    public static void shareViaWhatsApp(Context context, String message) {
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.setPackage("com.whatsapp");
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            context.startActivity(whatsappIntent);
        } catch (Exception e) {
            // WhatsApp not installed
        }
    }

    // Share via Telegram
    public static void shareViaTelegram(Context context, String message) {
        Intent telegramIntent = new Intent(Intent.ACTION_SEND);
        telegramIntent.setType("text/plain");
        telegramIntent.setPackage("org.telegram.messenger");
        telegramIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            context.startActivity(telegramIntent);
        } catch (Exception e) {
            // Telegram not installed
        }
    }
}