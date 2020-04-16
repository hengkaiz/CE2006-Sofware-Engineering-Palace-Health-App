package com.google.firebase.example.fireeats;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.adapter.RatingAdapter;
import com.google.firebase.example.fireeats.model.Rating;
import com.google.firebase.example.fireeats.model.Restaurant;
import com.google.firebase.example.fireeats.util.RestaurantUtil;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * This class controls the activity for restaurant details
 */

public class RestaurantDetailActivity extends AppCompatActivity implements
        View.OnClickListener,
        EventListener<DocumentSnapshot>,
        RatingDialogFragment.RatingListener{

    private static final String TAG = "RestaurantDetail";

    public static final String KEY_RESTAURANT_ID = "key_restaurant_id";
    public static final String USER_LAT = "user_lat";
    public static final String USER_LNG = "user_lng";

    private ImageView mImageView;
    private TextView mNameView;
    private MaterialRatingBar mRatingIndicator;
    private TextView mNumRatingsView;
    private TextView mCityView;
    private TextView mCategoryView;
    private TextView mPriceView;
    private ViewGroup mEmptyView;
    private RecyclerView mRatingsRecycler;

    private TextView mRestaurantDetails;
    private TextView mRestaurantOpeningHours;
    private TextView mRestaurantGoodForDetails;

    private RatingDialogFragment mRatingDialog;

    private FirebaseFirestore mFirestore;
    private DocumentReference mRestaurantRef;
    private ListenerRegistration mRestaurantRegistration;

    private RatingAdapter mRatingAdapter;

    private String restaurantId;
    private double restaurantLat;
    private double restaurantLng;
    private String restaurantName;
    private double uLat;
    private double uLng;

    private String uID;
    private FusedLocationProviderClient client;
    private LocationManager locMag;
    private LocationListener locList;
    /**
     * When the activity is created
     * @param savedInstanceState points to the saved state of the previous activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);
        
        mImageView = findViewById(R.id.restaurant_image);
        mNameView = findViewById(R.id.restaurant_name);
        mRatingIndicator = findViewById(R.id.restaurant_rating);
        mNumRatingsView = findViewById(R.id.restaurant_num_ratings);
        mCityView = findViewById(R.id.restaurant_city);
        mCategoryView = findViewById(R.id.restaurant_category);
        mPriceView = findViewById(R.id.restaurant_price);
        mEmptyView = findViewById(R.id.view_empty_ratings);
        mRatingsRecycler = findViewById(R.id.recycler_ratings);

        mRestaurantDetails = findViewById(R.id.restaurant_details);
        mRestaurantOpeningHours = findViewById(R.id.restaurant_opening_hours);
        mRestaurantGoodForDetails = findViewById(R.id.restaurant_good_for_details);


        findViewById(R.id.restaurant_button_back).setOnClickListener(this);
        findViewById(R.id.fab_show_rating_dialog).setOnClickListener(this);
        findViewById(R.id.show_directions).setOnClickListener(this);
        findViewById(R.id.favorite_restaurant).setOnClickListener(this);
        findViewById(R.id.unfavorite_restaurant).setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            throw new IllegalArgumentException("Must pass extra ");
        } else {
            restaurantId = bundle.getString(KEY_RESTAURANT_ID);
            uLat = bundle.getDouble(USER_LAT);
            uLng = bundle.getDouble(USER_LNG);
            Log.d(TAG, "onCreate: rlat " + restaurantLat);
            Log.d(TAG, "onCreate: rlng " + restaurantLng);
            Log.d(TAG, "onCreate: name " + restaurantName);
        }

        uID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        CollectionReference resRef = FirebaseFirestore.getInstance().collection("restaurants");

        resRef.whereEqualTo("postal", Integer.parseInt(restaurantId)).whereArrayContains("liked", uID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                updateUI(0);
                            }
                        } else {
                            updateUI(1);
                        }
                    }
                });

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the restaurant
        mRestaurantRef = mFirestore.collection("restaurants").document(restaurantId);

        // Get ratings
        Query ratingsQuery = mRestaurantRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        mRatingAdapter = new RatingAdapter(ratingsQuery) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    mRatingsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mRatingsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        };

        mRatingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRatingsRecycler.setAdapter(mRatingAdapter);

        mRatingDialog = new RatingDialogFragment();
    }

    /**
     * When the activity starts
     */
    @Override
    public void onStart() {
        super.onStart();
        try {
            //set time in mili
            Thread.sleep(800);

        }catch (Exception e){
            e.printStackTrace();
        }

        mRatingAdapter.startListening();
        mRestaurantRegistration = mRestaurantRef.addSnapshotListener(this);
    }

    /**
     * Show unfavorite/ favorite button depending on whether user already favorited it
     * @param fav is 0 if the favorite = false, and 1 if true
     */
    private void updateUI(int fav){
        switch(fav){
            case 0:
                findViewById(R.id.favorite_restaurant).setVisibility(View.GONE);
                findViewById(R.id.unfavorite_restaurant).setVisibility(View.VISIBLE);
                break;

            case 1:
                findViewById(R.id.favorite_restaurant).setVisibility(View.VISIBLE);
                findViewById(R.id.unfavorite_restaurant).setVisibility(View.GONE);
                break;
        }
    }

    /**
     * When the activity stops
     */
    @Override
    public void onStop() {
        super.onStop();

        mRatingAdapter.stopListening();

        if (mRestaurantRegistration != null) {
            mRestaurantRegistration.remove();
            mRestaurantRegistration = null;
        }
    }

    /**
     * When the a button on the activity is pressed
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restaurant_button_back:
                onBackArrowClicked(v);
                break;
            case R.id.fab_show_rating_dialog:
                onAddRatingClicked(v);
                break;
            case R.id.show_directions:
                /*final Task location = client.getLastLocation();
                location.addOnCompleteListener( new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful()){
                            uLat = task.getResult().getLatitude();
                            uLng = task.getResult().getLongitude();
                        }
                        else{
                            Toast.makeText(RestaurantDetailActivity.this, "failed to get loc", Toast.LENGTH_SHORT).show();
                        }
                    }
                }); */

                Intent intent = new Intent(RestaurantDetailActivity.this, MapsActivity.class);
                intent.putExtra(MapsActivity.RESTAURANT_LAT, restaurantLat);
                intent.putExtra(MapsActivity.RESTAURANT_LNG, restaurantLng);
                intent.putExtra(MapsActivity.RESTAURANT_NAME, restaurantName);
                intent.putExtra(MapsActivity.USER_LAT, uLat);
                intent.putExtra(MapsActivity.USER_LNG, uLng);
                startActivity(intent);
                break;
            case R.id.favorite_restaurant:
                addToFavorites();
                break;
            case R.id.unfavorite_restaurant:
                removeFromFavorites();
                break;
        }
    }

    /**
     * Remove restaurant from user's favorites list
     */
    private void removeFromFavorites(){
        String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        CollectionReference resRef = FirebaseFirestore.getInstance().collection("restaurants");

        resRef.document(restaurantId)
                .update("liked", FieldValue.arrayRemove(uID))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        Toast.makeText(RestaurantDetailActivity.this, "Restaurant removed from Favorites!", Toast.LENGTH_SHORT).show();
                        updateUI(1);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Toast.makeText(RestaurantDetailActivity.this, "Error removing from Favorites!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Add restaurant from user's favorites list
     */
    private void addToFavorites(){
        String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        CollectionReference resRef = FirebaseFirestore.getInstance().collection("restaurants");

        resRef.document(restaurantId)
                .update("liked", FieldValue.arrayUnion(uID))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Toast.makeText(RestaurantDetailActivity.this, "Restaurant added to Favorites!", Toast.LENGTH_SHORT).show();
                        updateUI(0);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Toast.makeText(RestaurantDetailActivity.this, "Error adding to Favorites!", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    /**
     * Add new ratings to the selected restaurant
     * @param restaurantRef is the selected restaurant
     * @param rating is the rating/ review by the user
     */
    private Task<Void> addRating(final DocumentReference restaurantRef,
                                 final Rating rating) {
        // Create reference for new rating, for use inside the transaction
        final DocumentReference ratingRef = restaurantRef.collection("ratings")
                .document();

        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction)
                    throws FirebaseFirestoreException {

                Restaurant restaurant = transaction.get(restaurantRef)
                        .toObject(Restaurant.class);

                // Compute new number of ratings
                int newNumRatings = restaurant.getNumRatings() + 1;

                // Compute new average rating
                double oldRatingTotal = restaurant.getAvgRating() *
                        restaurant.getNumRatings();
                double newAvgRating = (oldRatingTotal + rating.getRating()) /
                        newNumRatings;

                // Set new restaurant info
                restaurant.setNumRatings(newNumRatings);
                restaurant.setAvgRating(newAvgRating);

                // Commit to Firestore
                transaction.set(restaurantRef, restaurant);
                transaction.set(ratingRef, rating);

                return null;
            }
        });
    }

    /**
     * Listener for the Restaurant document ({@link #mRestaurantRef}).
     */
    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e);
            return;
        }

        onRestaurantLoaded(snapshot.toObject(Restaurant.class));
    }

    /**
     * Fill in the restaurant info for the activity
     * @param restaurant is the class containing the details for the selected restaurant
     */
    private void onRestaurantLoaded(Restaurant restaurant) {
        mNameView.setText(restaurant.getName());
        mRatingIndicator.setRating((float) restaurant.getAvgRating());
        mNumRatingsView.setText(getString(R.string.fmt_num_ratings, restaurant.getNumRatings()));
        mCityView.setText(restaurant.getCity());
        mCategoryView.setText(restaurant.getCategory());
        mPriceView.setText(RestaurantUtil.getPriceString(restaurant));
        mRestaurantDetails.setText(restaurant.getDescription());
        mRestaurantOpeningHours.setText(restaurant.getOpeningHours());

        String lowIn ="";
        Boolean firstString = true;

        if(restaurant.getSugar() == 1){
            lowIn = "Sugar";
            firstString = false;
        }
        if(restaurant.getSalt() == 1){
            lowIn = (firstString) ? "" : lowIn.concat(", ");
            lowIn = lowIn.concat("Salt");
            firstString = false;
        }
        if(restaurant.getFat() == 1){
            lowIn = (firstString) ? "" : lowIn.concat(", ");
            lowIn = lowIn.concat("Fat");
        }

        mRestaurantGoodForDetails.setText(lowIn);

        restaurantLat = restaurant.getX();
        restaurantLng = restaurant.getY();
        restaurantName = restaurant.getName();

        // Background image
        Glide.with(mImageView.getContext())
                .load(restaurant.getPhoto())
                .into(mImageView);
    }

    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    public void onAddRatingClicked(View view) {
        mRatingDialog.show(getSupportFragmentManager(), RatingDialogFragment.TAG);
    }

    @Override
    public void onRating(Rating rating) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(mRestaurantRef, rating)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Rating added");

                        // Hide keyboard and scroll to top
                        hideKeyboard();
                        mRatingsRecycler.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Add rating failed", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
