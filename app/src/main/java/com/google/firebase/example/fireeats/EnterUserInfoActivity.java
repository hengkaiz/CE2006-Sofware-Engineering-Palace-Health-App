package com.google.firebase.example.fireeats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.model.User;
import com.google.firebase.example.fireeats.util.HealthRiskUtil;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Enter user info activity containing health information fields for new users to fill in
 */
public class EnterUserInfoActivity extends AppCompatActivity
        implements View.OnClickListener{

    private static final String TAG = "LOG";
    private EditText mAge;
    private EditText mHeight;
    private EditText mWeight;
    private EditText mCholesterol;
    private EditText mBloodPressure;
    private String uid;
    private String mSex = "Male";
    private String mTreatedForHBP = "No";
    private String mDiabetic = "No";
    private String mSmoke = "No";
    private int mActivityLevel = 0;
    private String mHistoryHeartDisease = "No";

    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_enter_info);

        // Views
        mAge = findViewById(R.id.user_age);
        mHeight = findViewById(R.id.user_height);
        mWeight = findViewById(R.id.user_weight);
        mCholesterol = findViewById(R.id.user_cholesterol);
        mBloodPressure = findViewById(R.id.user_bloodpressure);

        // Buttons
        findViewById(R.id.user_sex_male_button).setOnClickListener(this);
        findViewById(R.id.user_sex_female_button).setOnClickListener(this);

        findViewById(R.id.user_treated_for_hbp_yes).setOnClickListener(this);
        findViewById(R.id.user_treated_for_hbp_no).setOnClickListener(this);
        findViewById(R.id.user_diabetic_yes).setOnClickListener(this);
        findViewById(R.id.user_diabetic_no).setOnClickListener(this);

        findViewById(R.id.user_smoke_yes).setOnClickListener(this);
        findViewById(R.id.user_smoke_no).setOnClickListener(this);
        findViewById(R.id.user_activity_level_none).setOnClickListener(this);
        findViewById(R.id.user_activity_level_low).setOnClickListener(this);
        findViewById(R.id.user_activity_level_medium).setOnClickListener(this);
        findViewById(R.id.user_activity_level_high).setOnClickListener(this);
        findViewById(R.id.enter_info_button_submit).setOnClickListener(this);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_sex_male_button:
                mSex = "Male";
                break;
            case R.id.user_sex_female_button:
                mSex = "Female";
                break;

            // Second page buttons
            case R.id.user_treated_for_hbp_yes:
                mTreatedForHBP = "Yes";
                break;
            case R.id.user_treated_for_hbp_no:
                mTreatedForHBP = "No";
                break;
            case R.id.user_diabetic_yes:
                mDiabetic = "Yes";
                break;
            case R.id.user_diabetic_no:
                mDiabetic = "No";
                break;

            // Third page buttons
            case R.id.user_smoke_yes:
                mSmoke = "Yes";
                break;
            case R.id.user_smoke_no:
                mSmoke = "No";
                break;
            case R.id.user_activity_level_none:
                mActivityLevel = 0;
                break;
            case R.id.user_activity_level_low:
                mActivityLevel = 1;
                break;
            case R.id.user_activity_level_medium:
                mActivityLevel = 2;
                break;
            case R.id.user_activity_level_high:
                mActivityLevel = 3;
                break;
            case R.id.enter_info_button_submit:
                enterUserInfoToDb();
                break;

            default:
                break;
        }
    }

    /**
     * Stores the information that the user have entered into firestore
     */
    private void enterUserInfoToDb(){
        if(!validateForm()){ return; }
        Map<String, Object> user = new HashMap<>();
        user.put("name", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        user.put("age", Integer.parseInt(mAge.getText().toString()));
        user.put("sex", mSex);
        user.put("height", Integer.parseInt(mHeight.getText().toString()));
        user.put("weight", Integer.parseInt(mWeight.getText().toString()));

        if(TextUtils.isEmpty(mCholesterol.getText().toString())){
            user.put("cholesterol", 6);
        }
        else{ user.put("cholesterol", Integer.parseInt(mCholesterol.getText().toString())); }

        if(TextUtils.isEmpty(mBloodPressure.getText().toString())){
            user.put("bloodPressure", 120);
        }
        else{ user.put("bloodPressure", Integer.parseInt(mBloodPressure.getText().toString())); }

        user.put("treatedHBP", mTreatedForHBP);
        user.put("diabetic", mDiabetic);
        user.put("smoke", mSmoke);
        user.put("activityLevel", mActivityLevel);
        user.put("historyHeartDisease", mHistoryHeartDisease);

        user = calculateData(user);

        mFirestore.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", "success!");
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    /**
     * User HealthRiskUtil to calculate user's BMI and Risk levels
     * @param user_hash is a hash map containing the user's information
     */
    private Map calculateData(Map user_hash){
        User user = new User(user_hash.get("name").toString(),
                (int)user_hash.get("age"),
                (int)user_hash.get("bloodPressure"),
                (int)user_hash.get("cholesterol"),
                user_hash.get("diabetic").toString(),
                (int)user_hash.get("height"),
                user_hash.get("sex").toString(),
                user_hash.get("treatedHBP").toString(),
                (int)user_hash.get("weight"),
                user_hash.get("smoke").toString(),
                (int)user_hash.get("activityLevel"),
                user_hash.get("historyHeartDisease").toString(),
                0,
                0,
                0,
                0);

        HealthRiskUtil healthRiskUtil = new HealthRiskUtil(user);

        user_hash.put("bmi", healthRiskUtil.calBMI());
        user_hash.put("riskRF", healthRiskUtil.calRiskRF());
        user_hash.put("riskAge", healthRiskUtil.calRiskAge());
        user_hash.put("totalRisk", healthRiskUtil.calTotalRisk());

        return user_hash;
    }

    /**
     * Checks if the information that the user entered is valid
     */
    private boolean validateForm() {
        boolean valid = true;

        String age = mAge.getText().toString();
        if (TextUtils.isEmpty(age)) {
            mAge.setError("Required.");
            valid = false;
        } else {
            mAge.setError(null);
        }

        String weight = mWeight.getText().toString();
        if (TextUtils.isEmpty(weight)) {
            mWeight.setError("Required.");
            valid = false;
        } else {
            mWeight.setError(null);
        }

        String height = mHeight.getText().toString();
        if (TextUtils.isEmpty(age)) {
            mHeight.setError("Required.");
            valid = false;
        } else {
            mHeight.setError(null);
        }

        return valid;
    }
}
