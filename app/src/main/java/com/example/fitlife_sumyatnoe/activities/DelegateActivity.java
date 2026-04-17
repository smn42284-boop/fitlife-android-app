package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.utils.PermissionHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DelegateActivity extends BaseActivity {

    private TextInputEditText phoneInput, messageInput;
    private MaterialButton sendBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delegate);

        String workoutName = getIntent().getStringExtra("workout_name");

        phoneInput = findViewById(R.id.phoneInput);
        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);

        if (workoutName != null) {
            messageInput.setText(String.format(getString(R.string.delegate_message_format), workoutName));
        }

        sendBtn.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();
            String message = messageInput.getText().toString().trim();

            if (TextUtils.isEmpty(phone)) {
                showWarningToast("Enter phone number");
                return;
            }

            PermissionHelper.requestSmsPermission(this, new PermissionHelper.PermissionCallback() {
                @Override
                public void onPermissionGranted() {
                    sendSms(phone, message);
                }

                @Override
                public void onPermissionDenied() {
                    showWarningToast("SMS permission required");
                }
            });
        });
    }

    private void sendSms(String phone, String message) {
        String workoutName = getIntent().getStringExtra("workout_name");
        List<String> equipment = getIntent().getStringArrayListExtra("equipment_list");
        int duration = getIntent().getIntExtra("workout_duration", 0);
        int exerciseCount = getIntent().getIntExtra("exercise_count", 0);

        StringBuilder smsMessage = new StringBuilder();
        smsMessage.append("🏋️ FitLife Workout\n\n");
        smsMessage.append("Workout: ").append(workoutName).append("\n");
        smsMessage.append("Duration: ").append(duration).append(" min\n");
        smsMessage.append("Exercises: ").append(exerciseCount).append("\n\n");

        if (equipment != null && !equipment.isEmpty()) {
            smsMessage.append("Equipment Needed:\n");

            // Categorize
            Map<String, List<String>> categorized = new LinkedHashMap<>();
            categorized.put("🏋️ Strength", new ArrayList<>());
            categorized.put("🏃 Cardio", new ArrayList<>());
            categorized.put("🧘 Yoga", new ArrayList<>());
            categorized.put("🔧 Accessories", new ArrayList<>());

            for (String eq : equipment) {
                String lower = eq.toLowerCase();
                if (lower.contains("dumbbell") || lower.contains("barbell") || lower.contains("kettlebell")) {
                    categorized.get("🏋️ Strength").add(eq);
                } else if (lower.contains("jump rope") || lower.contains("bike") || lower.contains("treadmill")) {
                    categorized.get("🏃 Cardio").add(eq);
                } else if (lower.contains("yoga") || lower.contains("mat")) {
                    categorized.get("🧘 Yoga").add(eq);
                } else {
                    categorized.get("🔧 Accessories").add(eq);
                }
            }

            for (Map.Entry<String, List<String>> entry : categorized.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    smsMessage.append(entry.getKey()).append(":\n");
                    for (String eq : entry.getValue()) {
                        smsMessage.append("  • ").append(eq).append("\n");
                    }
                }
            }
        } else {
            smsMessage.append("No equipment needed\n");
        }

        smsMessage.append("\nSent from FitLife App 💪");

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, smsMessage.toString(), null, null);
            showSuccessToast("SMS sent");
            finish();
        } catch (Exception e) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + phone));
            intent.putExtra("sms_body", smsMessage.toString());
            startActivity(intent);
        }
    }
}