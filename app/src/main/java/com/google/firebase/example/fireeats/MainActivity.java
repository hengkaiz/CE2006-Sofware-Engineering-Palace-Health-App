package com.google.firebase.example.fireeats;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.adapter.RestaurantAdapter;
import com.google.firebase.example.fireeats.model.Restaurant;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main activity for Palace Health
 * Shows a list of filtered restaurants based on the user's current location and health info
 */
public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        FilterDialogFragment.FilterListener,
        RestaurantAdapter.OnRestaurantSelectedListener {

    private static final String TAG = "MainActivity";

    private static final int RC_SIGN_IN = 9001;
    private static final int RC_ADD_USER_INFO = 9002;

    private static final int LIMIT = 50;

    private Toolbar mToolbar;
    private TextView mCurrentSearchView;
    private TextView mCurrentSortByView;
    private RecyclerView mRestaurantsRecycler;
    private ViewGroup mEmptyView;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private FilterDialogFragment mFilterDialog;
    private RestaurantAdapter mAdapter;

    private MainActivityViewModel mViewModel;
    private BottomNavigationView bottomNavigation;

    private LocationManager locMag;
    private LocationListener locList;
    private double uLat = 1.369053;
    private double uLng = 103.960883;
    //private Filters filtersD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mCurrentSearchView = findViewById(R.id.text_current_search);
        mCurrentSortByView = findViewById(R.id.text_current_sort_by);
        mRestaurantsRecycler = findViewById(R.id.recycler_restaurants);
        mEmptyView = findViewById(R.id.view_empty);

        bottomNavigation = findViewById(R.id.nav_bot);
        bottomNavigation.setOnNavigationItemSelectedListener(navListener);
        bottomNavigation.setSelectedItemId(R.id.restaurant_page);

        findViewById(R.id.filter_bar).setOnClickListener(this);
        findViewById(R.id.button_clear_filter).setOnClickListener(this);

        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Initialize Firestore and the main RecyclerView
        initFirestore();
        initRecyclerView();

        // Filter Dialog
        mFilterDialog = new FilterDialogFragment();

        locMag = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locList = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //uLat = location.getLatitude();
                //uLng = location.getLongitude();
                Log.d(TAG, "onLocationChanged: userLat" + location.getLatitude());
                Log.d(TAG, "onLocationChanged: userLng" + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locMag.requestLocationUpdates("gps", 5000, 0, locList);
    }

    /**
     * Initialize firestore
     */
    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();

        Filters filters = mViewModel.getFilters();
        /*Log.d(TAG, "initFirestore: uLatB" + filters.getUserCoordinatesLat());
        Log.d(TAG, "initFirestore: ulngB" + filters.getUserCoordinatesLon());
        filters.setUserCoordinatesLat(uLat);
        filters.setUserCoordinatesLon(uLng);
        Log.d(TAG, "initFirestore: uLat" + filters.getUserCoordinatesLat());
        Log.d(TAG, "initFirestore: ulng" + filters.getUserCoordinatesLon());*/

        // Get the 50 highest rated restaurants
        mQuery = mFirestore.collection("restaurants");

        mQuery = mQuery.whereLessThanOrEqualTo("x_y", filters.getUpperLimit()).whereGreaterThanOrEqualTo("x_y", filters.getLowerLimit())
                .orderBy("x_y", Query.Direction.DESCENDING).limit(LIMIT);
    }

    /**
     * Initialize recycler view for the restaurant fragments
     */
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

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }

        while (FirebaseAuth.getInstance().getCurrentUser() == null) ;

        needEnterUserInfo();

        // Apply filters
        Filters filters = new Filters(uLat,uLng);
        filters.setSortBy(Restaurant.FIELD_AVG_RATING);
        filters.setSortDirection(Query.Direction.DESCENDING);
        filters.setCity("Nearby");
        /*filters.setUserCoordinatesLat(uLat);
        filters.setUserCoordinatesLon(uLng);
        filters.setUpperLimit(uLat,uLng);
        filters.setLowerLimit(uLat,uLng);*/
        Log.d(TAG, "initFirestore: uLat" + filters.getUserCoordinatesLat());
        Log.d(TAG, "initFirestore: ulng" + filters.getUserCoordinatesLon());
        Log.d(TAG, "onStart: upperLimit " + filters.getUpperLimit());
        Log.d(TAG, "onStart: lowerlimit " + filters.getLowerLimit());
        onFilter(filters);


        //Apply filters
        //onFilter(mViewModel.getFilters());*/

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

    /**
     * Check if user have already entered their health info
     * Required. In order for the user to use the app
     */
    private void needEnterUserInfo(){
        DocumentReference docRef = mFirestore.collection("users")
                                             .document(FirebaseAuth.getInstance()
                                                     .getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "user data exist" + document.getData());
                    } else {
                        Log.d(TAG, "No user data");
                        addUserData();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     * Opens enter user info activity for new users to enter their health information
     */
    private void addUserData(){
        Intent intent = new Intent(this, EnterUserInfoActivity.class);
        startActivityForResult(intent, RC_ADD_USER_INFO);
    }

    @Override
    public void onFilter(Filters filters) {

        Log.d(TAG, "onFilterB: uLat " + filters.getUserCoordinatesLat());
        Log.d(TAG, "onFilterB: uLng " + filters.getUserCoordinatesLon());

        // Construct query basic query
        Query query = mFirestore.collection("restaurants");

        // Category (equality filter)
        if (filters.hasCategory()) {
            query = query.whereEqualTo("category", filters.getCategory());
        }

        // Price (equality filter)
        if (filters.hasPrice()) {
            query = query.whereEqualTo("price", filters.getPrice());
        }

        // City (equality filter)
        query = query.whereLessThanOrEqualTo("x_y", filters.getUpperLimit())
                .whereGreaterThanOrEqualTo("x_y", filters.getLowerLimit())
                .orderBy("x_y", Query.Direction.DESCENDING);


        // Sort by (orderBy with direction)
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
        }

        // Limit items
        query = query.limit(LIMIT);

        // Update the query
        mQuery = query;
        mAdapter.setQuery(query);

        // Set header
        mCurrentSearchView.setText(Html.fromHtml(filters.getSearchDescription(this)));
        mCurrentSortByView.setText(filters.getOrderDescription(this));

        // Save filters
        mViewModel.setFilters(filters);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            mViewModel.setIsSigningIn(false);

            if (resultCode != RESULT_OK && shouldStartSignIn()) {
                startSignIn();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_bar:
                onFilterClicked();
                break;
            case R.id.button_clear_filter:
                onClearFilterClicked();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch(item.getItemId()){
                        case R.id.restaurant_page:
                            break;
                        case R.id.favorites_page:
                            startActivity(new Intent(getBaseContext(), FavoritesActivity.class));
                            overridePendingTransition(0,0);
                            break;
                        case R.id.profile_page:
                            startActivity(new Intent(getBaseContext(), ProfileActivity.class));
                            overridePendingTransition(0,0);
                            break;
                    }
                return true;
                }
            };

    public void onFilterClicked() {
        // Show the dialog containing filter options
        mFilterDialog.show(getSupportFragmentManager(), FilterDialogFragment.TAG);
    }

    public void onClearFilterClicked() {
        mFilterDialog.resetFilters();

        onFilter(Filters.getDefault());
    }

    @Override
    public void onRestaurantSelected(DocumentSnapshot restaurant) {
        // Go to the details page for the selected restaurant
        Intent intent = new Intent(this, RestaurantDetailActivity.class);
        intent.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, restaurant.getId());
        intent.putExtra(RestaurantDetailActivity.USER_LAT, uLat);
        intent.putExtra(RestaurantDetailActivity.USER_LNG, uLng);

        startActivity(intent);
    }

    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    /**
     * Opens sign in activity for uesrs to create/log in to the app
     */
    private void startSignIn() {
        // Sign in with FirebaseUI
            Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(Collections.singletonList(
                            new AuthUI.IdpConfig.EmailBuilder().build()))
                    .setIsSmartLockEnabled(false)
                    .build();

        startActivityForResult(intent, RC_SIGN_IN);
        mViewModel.setIsSigningIn(true);
    }
}
