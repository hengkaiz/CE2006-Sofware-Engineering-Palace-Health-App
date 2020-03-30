package com.google.firebase.example.fireeats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.adapter.RestaurantAdapter;
import com.google.firebase.example.fireeats.model.Restaurant;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity implements
        View.OnClickListener,
        RestaurantAdapter.OnRestaurantSelectedListener {

    private static final String TAG = "favorite restaurants";

    private BottomNavigationView bottomNavigation;
    private RecyclerView mRestaurantsRecycler;
    private ViewGroup mEmptyView;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private RestaurantAdapter mAdapter;

    private List<Integer> favList = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        bottomNavigation = findViewById(R.id.nav_bot);
        bottomNavigation.setOnNavigationItemSelectedListener(navListener);
        bottomNavigation.setSelectedItemId(R.id.favorites_page);

        mRestaurantsRecycler = findViewById(R.id.recycler_restaurants);
        mEmptyView = findViewById(R.id.view_empty);

        FirebaseFirestore.setLoggingEnabled(true);

        // Initialize Firestore and main RecyclerView
        initFirestore();
        initRecyclerView();
    }

    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();
        String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mQuery = mFirestore.collection("restaurants").whereArrayContains("liked", uID);
    }

    private void initRecyclerView() {
        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
        }

        mAdapter = new RestaurantAdapter(mQuery, this) {

            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mRestaurantsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mRestaurantsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mRestaurantsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRestaurantsRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    public void onClick(View v){}

    // Restaurant is selected
    @Override
    public void onRestaurantSelected(DocumentSnapshot restaurant){
        // Go to the details page for the selected restaurant
        Intent intent = new Intent(this, RestaurantDetailActivity.class);
        intent.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, restaurant.getId());

        startActivity(intent);
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
                            break;
                        case R.id.profile_page:
                            break;
                    }
                    return true;
                }
            };
}
