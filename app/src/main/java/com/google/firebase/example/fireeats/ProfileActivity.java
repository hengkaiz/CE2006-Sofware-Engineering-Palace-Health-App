package com.google.firebase.example.fireeats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.model.Restaurant;
import com.google.firebase.example.fireeats.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileActivity extends AppCompatActivity implements
        EventListener<DocumentSnapshot> {

    private static final String TAG = "ProfilePage";

    private BottomNavigationView bottomNavigation;

    private TextView mProfileName;
    private TextView mProfileAge;
    private TextView mProfileSex;
    private TextView mProfileHeight;
    private TextView mProfileWeight;
    private TextView mProfileBMI;
    private TextView mProfileChol;
    private TextView mProfileBP;
    private TextView mProfileRL;

    private TextView mProfileRLTitle;
    private RelativeLayout mProfileRLLinear;

    private String uID;
    private DocumentReference mUserRef;
    private FirebaseFirestore mFirestore;

    private ListenerRegistration mUserRegistration;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bottomNavigation = findViewById(R.id.nav_bot);
        bottomNavigation.setOnNavigationItemSelectedListener(navListener);
        bottomNavigation.setSelectedItemId(R.id.profile_page);

        mProfileName = findViewById(R.id.profile_page_name);
        mProfileAge = findViewById(R.id.profile_page_age);
        mProfileSex = findViewById(R.id.profile_page_sex);
        mProfileHeight = findViewById(R.id.profile_page_height);
        mProfileWeight = findViewById(R.id.profile_page_weight);
        mProfileBMI = findViewById(R.id.profile_page_bmi);
        mProfileChol = findViewById(R.id.profile_page_cholesterol);
        mProfileBP = findViewById(R.id.profile_page_bp);
        mProfileRL = findViewById(R.id.profile_page_risk_level);
        mProfileRLTitle = findViewById(R.id.profile_page_risk_level_title);

        mProfileRLLinear = findViewById(R.id.profile_page_risk_level_layout);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        uID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mFirestore = FirebaseFirestore.getInstance();

        mUserRef = mFirestore.collection("users").document(uID);
    }

    @Override
    public void onStart() {
        super.onStart();

        mUserRegistration = mUserRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e);
            return;
        }
        onUserLoaded(snapshot.toObject(User.class));
    }

    private void onUserLoaded(User user){
        mProfileName.setText(user.getName());
        mProfileAge.setText(getString(R.string.fmt_num,user.getAge()).toUpperCase());
        mProfileSex.setText(user.getSex());
        mProfileHeight.setText(getString(R.string.fmt_num,user.getHeight()));
        mProfileWeight.setText(getString(R.string.fmt_num,user.getWeight()));

        mProfileBMI.setText(getString(R.string.fmt_double, user.getBmi()));
        mProfileChol.setText(getString(R.string.fmt_num, user.getCholesterol()));
        mProfileBP.setText(getString(R.string.fmt_num, user.getBloodPressure()));

        if(user.getTotalRisk() > 9) {
            mProfileRL.setText(R.string.very_high_risk);
            mProfileRLTitle.setTextColor(Color.rgb(139,0,0));
            mProfileRLLinear.setBackgroundColor(Color.rgb(139,0,0));
        }
        else if (user.getTotalRisk() <= 9 && user.getTotalRisk() > 7) {
            mProfileRL.setText(R.string.high_risk);
            mProfileRLTitle.setTextColor(Color.rgb(255,99,71));
            mProfileRLLinear.setBackgroundColor(Color.rgb(255,99,71));
        }
        else if (user.getTotalRisk() <= 7 && user.getTotalRisk() > 5) {
            mProfileRL.setText(R.string.at_risk);
            mProfileRLTitle.setTextColor(Color.rgb(204,204,0));
            mProfileRLLinear.setBackgroundColor(Color.rgb(204,204,0));
        }
        else if (user.getTotalRisk() <= 5) {
            mProfileRL.setText(R.string.low_risk);
            mProfileRLTitle.setTextColor(Color.rgb(0,100,0));
            mProfileRLLinear.setBackgroundColor(Color.rgb(0,100,0));
        }
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch(item.getItemId()){
                        case R.id.restaurant_page:
                            startActivity(new Intent(getBaseContext(), MainActivity.class));
                            overridePendingTransition(0,0);
                            break;
                        case R.id.favorites_page:
                            startActivity(new Intent(getBaseContext(), FavoritesActivity.class));
                            overridePendingTransition(0,0);
                            break;
                        case R.id.profile_page:
                            break;
                    }
                    return true;
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_details:
                startActivity(new Intent(this, EnterUserInfoActivity.class));
                break;
            case R.id.menu_sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
