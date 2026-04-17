package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.UserBMIHistory;
import com.example.fitlife_sumyatnoe.models.UserBodyInfo;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class BodyInfoActivity extends BaseActivity {

    private TextView birthdayText;
    private TextInputEditText heightInput, weightInput;
    private Spinner genderSpinner;
    private MaterialButton nextBtn, backBtn;
    private TextView bmiValue, bmiStatus;
    private MaterialCardView bmiCard;
    private MaterialCardView birthdayCard;

    private FirebaseHelper firebaseHelper;
    private FirebaseUser currentUser;
    private long selectedBirthday = 0;
    private boolean isEditMode = false;
    private String selectedGender = "Male";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_info);

        firebaseHelper = new FirebaseHelper();
        currentUser = firebaseHelper.getCurrentUser();

        isEditMode = getIntent().getBooleanExtra("edit_mode", false);

        initViews();
        setupToolbar(false, isEditMode ? "Edit Body Info" : "Your Body Info");
        setupClickListeners();
        setupGenderSpinner();

        if (isEditMode) {
            if (birthdayCard != null) {
                birthdayCard.setVisibility(View.GONE);
            }
            setToolbarTitle("Edit Body Info");
            // Load existing data for edit mode
            loadExistingBodyInfo();
        } else {
            // First time - show all fields
            if (birthdayCard != null) {
                birthdayCard.setVisibility(View.VISIBLE);
            }
            setToolbarTitle("Your Body Info");
        }
    }

    private void initViews() {
        birthdayText = findViewById(R.id.birthdayText);
        heightInput = findViewById(R.id.heightInput);
        weightInput = findViewById(R.id.weightInput);
        genderSpinner = findViewById(R.id.genderSpinner);
        nextBtn = findViewById(R.id.nextBtn);
        backBtn = findViewById(R.id.backBtn);
        bmiValue = findViewById(R.id.bmiValue);
        bmiStatus = findViewById(R.id.bmiStatus);
        bmiCard = findViewById(R.id.bmiCard);
        birthdayCard = findViewById(R.id.birthdayCard);
    }

    private void setupGenderSpinner() {
        String[] genders = {"Male", "Female", "Other", "Prefer not to say"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genders
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        genderSpinner.setAdapter(adapter);
        genderSpinner.setSelection(0);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGender = genders[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGender = "Male";
            }
        });
    }

    private void setupClickListeners() {
        if (birthdayCard != null) {
            birthdayCard.setOnClickListener(v -> showDatePicker());
        } else if (birthdayText != null) {
            birthdayText.setOnClickListener(v -> showDatePicker());
        }

        nextBtn.setOnClickListener(v -> saveBodyInfo());

        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                calculateAndShowBMI();
            }
        };

        if (heightInput != null) {
            heightInput.addTextChangedListener(watcher);
        }
        if (weightInput != null) {
            weightInput.addTextChangedListener(watcher);
        }
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Birthday")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedBirthday = selection;
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            birthdayText.setText(sdf.format(new Date(selection)));
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void calculateAndShowBMI() {
        String heightStr = heightInput != null ? heightInput.getText().toString() : "";
        String weightStr = weightInput != null ? weightInput.getText().toString() : "";

        if (!TextUtils.isEmpty(heightStr) && !TextUtils.isEmpty(weightStr)) {
            try {
                float heightCm = Float.parseFloat(heightStr);
                float weightKg = Float.parseFloat(weightStr);

                if (heightCm > 0 && weightKg > 0) {
                    float heightM = heightCm / 100;
                    float bmi = weightKg / (heightM * heightM);

                    String bmiStatusText;
                    int statusColor;
                    if (bmi < 18.5) {
                        bmiStatusText = "Underweight";
                        statusColor = getColor(R.color.info);
                    } else if (bmi < 25) {
                        bmiStatusText = "Normal";
                        statusColor = getColor(R.color.success);
                    } else if (bmi < 30) {
                        bmiStatusText = "Overweight";
                        statusColor = getColor(R.color.warning);
                    } else {
                        bmiStatusText = "Obese";
                        statusColor = getColor(R.color.error);
                    }

                    if (bmiValue != null) {
                        bmiValue.setText(String.format("%.1f", bmi));
                    }
                    if (bmiStatus != null) {
                        bmiStatus.setText(bmiStatusText);
                        bmiStatus.setTextColor(statusColor);
                    }

                    if (bmiCard != null) {
                        bmiCard.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (bmiCard != null) {
                        bmiCard.setVisibility(View.GONE);
                    }
                }
            } catch (NumberFormatException e) {
                if (bmiCard != null) {
                    bmiCard.setVisibility(View.GONE);
                }
            }
        } else {
            if (bmiCard != null) {
                bmiCard.setVisibility(View.GONE);
            }
        }
    }

    private void loadExistingBodyInfo() {
        showLoading();
        firebaseHelper.getUserBodyInfo(getUserId(), new FirebaseHelper.FirestoreCallback<UserBodyInfo>() {
            @Override
            public void onSuccess(UserBodyInfo bodyInfo) {
                hideLoading();
                if (bodyInfo != null) {
                    // Load birthday only in first-time mode
                    if (!isEditMode && bodyInfo.getBirthday() > 0) {
                        selectedBirthday = bodyInfo.getBirthday();
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        birthdayText.setText(sdf.format(new Date(bodyInfo.getBirthday())));
                    }

                    // Load height and weight
                    if (heightInput != null) {
                        heightInput.setText(String.valueOf((int) bodyInfo.getHeightCm()));
                    }
                    if (weightInput != null) {
                        weightInput.setText(String.valueOf((int) bodyInfo.getWeightKg()));
                    }

                    // Set gender spinner
                    String gender = bodyInfo.getGender();
                    if (gender != null) {
                        switch (gender) {
                            case "Male":
                                genderSpinner.setSelection(0);
                                break;
                            case "Female":
                                genderSpinner.setSelection(1);
                                break;
                            case "Other":
                                genderSpinner.setSelection(2);
                                break;
                            default:
                                genderSpinner.setSelection(3);
                                break;
                        }
                    }

                    calculateAndShowBMI();
                }
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
            }
        });
    }

    private void saveBodyInfo() {
        String heightStr = heightInput != null ? heightInput.getText().toString().trim() : "";
        String weightStr = weightInput != null ? weightInput.getText().toString().trim() : "";

        if (!isEditMode && selectedBirthday == 0) {
            showWarningToast("Please select your birthday");
            return;
        }

        if (TextUtils.isEmpty(heightStr)) {
            showWarningToast("Please enter your height");
            heightInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(weightStr)) {
            showWarningToast("Please enter your weight");
            weightInput.requestFocus();
            return;
        }

        float height, weight;
        try {
            height = Float.parseFloat(heightStr);
            weight = Float.parseFloat(weightStr);
        } catch (NumberFormatException e) {
            showWarningToast("Please enter valid numbers");
            return;
        }

        if (height <= 0 || height > 300) {
            showWarningToast("Please enter a valid height (50-300 cm)");
            heightInput.requestFocus();
            return;
        }

        if (weight <= 0 || weight > 500) {
            showWarningToast("Please enter a valid weight (1-500 kg)");
            weightInput.requestFocus();
            return;
        }

        showLoading();

        float heightM = height / 100;
        float bmi = weight / (heightM * heightM);

        UserBodyInfo bodyInfo = new UserBodyInfo();
        bodyInfo.setUserId(currentUser.getUid());
        String status;
        if (bmi < 18.5) status = "Underweight";
        else if (bmi < 25) status = "Normal";
        else if (bmi < 30) status = "Overweight";
        else status = "Obese";

        if (!isEditMode) {
            bodyInfo.setBirthday(selectedBirthday);
        }

        bodyInfo.setHeightCm(height);
        bodyInfo.setWeightKg(weight);
        bodyInfo.setGender(selectedGender);
        bodyInfo.setBmi(bmi);
        bodyInfo.setUpdatedAt(System.currentTimeMillis());

        // Create BMI history record
        UserBMIHistory bmiHistory = new UserBMIHistory();
        bmiHistory.setUserId(currentUser.getUid());
        bmiHistory.setBmi(bmi);
        bmiHistory.setStatus(status);
        bmiHistory.setRecordedAt(System.currentTimeMillis());
        bmiHistory.setHeightCm(height);
        bmiHistory.setWeightKg(weight);

        if (!isNetworkAvailable()) {
            saveBodyInfoLocally(bodyInfo);
            saveBMIHistoryLocally(bmiHistory);
            return;
        }

        // Save body info first
        firebaseHelper.updateUserBodyInfo(bodyInfo, new FirebaseHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Then save BMI history
                firebaseHelper.saveBMIHistory(bmiHistory, new FirebaseHelper.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        hideLoading();
                        showSuccessToast("Body info saved successfully!");

                        // Save to SharedPreferences
                        getPrefs().edit()
                                .putBoolean("hasBodyInfo", true)
                                .putFloat("user_height", height)
                                .putFloat("user_weight", weight)
                                .putInt("user_age", bodyInfo.getAge())
                                .putString("user_gender", bodyInfo.getGender())
                                .putFloat("user_bmi", bmi)
                                .putString("user_bmi_status", bodyInfo.getBmiStatus())
                                .apply();

                        boolean hasPreferences = getPrefs().getBoolean("hasPreferences", false);

                        if (!hasPreferences) {
                            Intent intent = new Intent(BodyInfoActivity.this, FitnessPreferencesActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(BodyInfoActivity.this, ProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        hideLoading();
                        showErrorToast("Body info saved but history failed: " + error);
                        navigateToNext();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                showErrorToast("Error: " + error);
            }
        });
    }

    private void saveBMIHistoryLocally(UserBMIHistory bmiHistory) {
        String historyJson = new com.google.gson.Gson().toJson(bmiHistory);
        getPrefs().edit()
                .putString("pending_bmi_history", historyJson)
                .putBoolean("needsBMIHistorySync", true)
                .apply();
    }

    private void navigateToNext() {
        boolean hasPreferences = getPrefs().getBoolean("hasPreferences", false);
        if (!hasPreferences) {
            Intent intent = new Intent(BodyInfoActivity.this, FitnessPreferencesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(BodyInfoActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
    }
    private void saveBodyInfoLocally(UserBodyInfo bodyInfo) {
        getPrefs().edit()
                .putFloat("user_height", bodyInfo.getHeightCm())
                .putFloat("user_weight", bodyInfo.getWeightKg())
                .putString("user_gender", bodyInfo.getGender())
                .putFloat("user_bmi", bodyInfo.getBmi())
                .putBoolean("needsBodyInfoSync", true)
                .apply();

        hideLoading();
        showSuccessToast("Body info saved locally! Will sync when online.");
        navigateToWithTransition(FitnessPreferencesActivity.class);
    }
}