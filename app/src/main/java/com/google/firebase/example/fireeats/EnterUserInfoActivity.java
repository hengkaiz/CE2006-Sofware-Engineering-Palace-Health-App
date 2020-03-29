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
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.R;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class EnterUserInfoActivity extends AppCompatActivity
        implements View.OnClickListener{

    private static final String TAG = "LOG";
    private EditText mUserName;
    private EditText mAge;
    private EditText mHeight;
    private EditText mWeight;
    private EditText mCholesterol;
    private EditText mBloodPressure;
    private String uid;
    private String sex = "Male";
    private String treatedForHBP = "No";
    private String diabetic = "No";

    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_user_info);

        // Views
        mUserName = findViewById(R.id.user_id);
        mAge = findViewById(R.id.user_age);
        mHeight = findViewById(R.id.user_height);
        mWeight = findViewById(R.id.user_weight);
        mCholesterol = findViewById(R.id.user_cholesterol);
        mBloodPressure = findViewById(R.id.user_bloodpressure);

        // Buttons
        findViewById(R.id.user_sex_male_button).setOnClickListener(this);
        findViewById(R.id.user_sex_female_button).setOnClickListener(this);
        findViewById(R.id.enter_info_button_next).setOnClickListener(this);

        findViewById(R.id.user_treated_for_hbp_yes).setOnClickListener(this);
        findViewById(R.id.user_treated_for_hbp_no).setOnClickListener(this);
        findViewById(R.id.user_diabetic_yes).setOnClickListener(this);
        findViewById(R.id.user_diabetic_no).setOnClickListener(this);
        findViewById(R.id.enter_info_button_back).setOnClickListener(this);
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
                sex = "Male";
                break;
            case R.id.user_sex_female_button:
                sex = "Female";
                break;
            case R.id.enter_info_button_next:
                goToSecondPage();
                break;

            // Second page buttons
            case R.id.user_treated_for_hbp_yes:
                treatedForHBP = "Yes";
                break;
            case R.id.user_treated_for_hbp_no:
                treatedForHBP = "No";
                break;
            case R.id.user_diabetic_yes:
                diabetic = "Yes";
                break;
            case R.id.user_diabetic_no:
                diabetic = "No";
                break;
            case R.id.enter_info_button_back:
                goToFirstPage();
                break;
            case R.id.enter_info_button_submit:
                enterUserInfoToDb();
                break;
        }
    }

    // Go to second page of activity
    private void goToSecondPage(){
        if(!validateForm()){ return; }
        updateUI(2);
    }

    // Go back to first page of activity
    private void goToFirstPage(){
        updateUI(1);
    }

    // Update UI of layout
    private void updateUI(int page) {
        if (page == 1) {
            findViewById(R.id.main_layout2).setVisibility(View.GONE);
            findViewById(R.id.main_layout1).setVisibility(View.VISIBLE);

        } else {
            findViewById(R.id.main_layout1).setVisibility(View.GONE);
            findViewById(R.id.main_layout2).setVisibility(View.VISIBLE);
        }
    }

    // Store user information in Firestore
    private void enterUserInfoToDb(){
        Map<String, Object> user = new HashMap<>();
        user.put("name", mUserName.getText().toString());
        user.put("age", Integer.parseInt(mAge.getText().toString()));
        user.put("sex", sex);
        user.put("height", Integer.parseInt(mHeight.getText().toString()));
        user.put("weight", Integer.parseInt(mWeight.getText().toString()));

        if(TextUtils.isEmpty(mCholesterol.getText().toString())){
            user.put("cholesterol", -1);
        }
        else{ user.put("cholesterol", Integer.parseInt(mCholesterol.getText().toString())); }
        if(TextUtils.isEmpty(mBloodPressure.getText().toString())){
            user.put("blood pressure", -1);
        }
        else{ user.put("blood pressure", Integer.parseInt(mBloodPressure.getText().toString())); }
        user.put("treated for hbp", treatedForHBP);
        user.put("diabetic", diabetic);

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

    private boolean validateForm() {
        boolean valid = true;

        String username = mUserName.getText().toString();
        if (TextUtils.isEmpty(username)) {
            mUserName.setError("Required.");
            valid = false;
        } else {
            mUserName.setError(null);
        }

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
