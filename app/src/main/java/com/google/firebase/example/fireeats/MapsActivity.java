package com.google.firebase.example.fireeats;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.example.fireeats.adapter.StepAdapter;
import com.google.firebase.example.fireeats.model.PolylineData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.type.LatLngOrBuilder;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnPolylineClickListener{
        //GoogleMap.OnInfoWindowClickListener{
        ///GoogleMap.OnMyLocationButtonClickListener,
        //GoogleMap.OnMyLocationClickListener {
    //GoogleMap.OnMarkerClickListener {

    private static final String TAG = "Map";
    public static final String RESTAURANT_LNG = "restaurant_lat";
    public static final String RESTAURANT_LAT = "restaurant_lng";
    public static final String RESTAURANT_NAME = "restaurant_name";

    private GoogleMap mMap;

    private FirebaseFirestore mFirestore;
    private LocationManager locationManager;
    private LocationListener locationListener;
    /*private Button mBtnDriving;
    private Button mBtnTransit;
    private Button mBtnWalk;*/

    private double restaurantLat;
    private double restaurantLng;
    private String restaurantName;
    private double userLat = 1.353286;
    private double userLng = 103.682812;
    private LatLng restaurantCoor;
    private LatLng userCoor;
    private Marker restaurantMarker;
    private Marker userMarker;

    private GeoApiContext geoApiContext = null;
    private ArrayList<PolylineData> polylineData = new ArrayList<>();

    private SupportMapFragment mapFragment;
    private Button mBtnGetDirections;
    private RecyclerView mDirectionsRecycler;
    private RecyclerView.Adapter adapter;
    private List<DirectionsStep> stepList = new ArrayList<>();;


    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();
        }

        mFirestore = FirebaseFirestore.getInstance();
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            throw new IllegalArgumentException("Must pass extra ");
        } else {
            restaurantLat = bundle.getDouble(RESTAURANT_LAT);
            restaurantLng = bundle.getDouble(RESTAURANT_LNG);
            restaurantName = bundle.getString(RESTAURANT_NAME);
            Log.d(TAG, "onCreate: rlat " + restaurantLat);
            Log.d(TAG, "onCreate: rlng " + restaurantLng);
            Log.d(TAG, "onCreate: name " + restaurantName);
        }

        //set up button
        mBtnGetDirections = findViewById(R.id.btnGetDirections);
        mBtnGetDirections.setOnClickListener(this);
        findViewById(R.id.map_back_button).setOnClickListener(this);
        /*mBtnDriving = findViewById(R.id.btnDriving);
        mBtnTransit = findViewById(R.id.btnTransit);
        mBtnWalk = findViewById(R.id.btnWalk);*/

        mDirectionsRecycler = findViewById(R.id.directionsRecycler);
        mDirectionsRecycler.setHasFixedSize(false);
        mDirectionsRecycler.setLayoutManager(new LinearLayoutManager(this)) ;

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnPolylineClickListener(this);

        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        restaurantCoor = new LatLng(restaurantLat,restaurantLng);
        Log.d(TAG, "lat = " + restaurantLat);
        Log.d(TAG, "lng = " + restaurantLng);
        Log.d(TAG, "name = " + restaurantName);

        //hardcoded userLoc until fixed
        userCoor = new LatLng(userLat,userLng);
        Log.d(TAG, "onMapReady: userLat = " + userLat);
        Log.d(TAG, "onMapReady: userLng = " + userLng);

        float results[] = new float[10];
        Location.distanceBetween(userLat,userLng,restaurantLat,restaurantLng,results);

        restaurantMarker = mMap.addMarker(new MarkerOptions()
                .position(restaurantCoor)
                .title(restaurantName)
                .snippet("Distance = " + (int)(results[0]/1000) + "km"));
        userMarker = mMap.addMarker(new MarkerOptions()
                .position(userCoor)
                .title("You are here!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantCoor, 15));
        restaurantMarker.showInfoWindow();
        userMarker.showInfoWindow();;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGetDirections:
                calcDirections();
                //shows multiple markers in one screen
                LatLngBounds bounds = new LatLngBounds.Builder().include(restaurantCoor).include(userCoor).build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                mBtnGetDirections.setVisibility(View.GONE);
                /*mBtnDriving.setVisibility(View.VISIBLE);
                mBtnTransit.setVisibility(View.VISIBLE);
                mBtnWalk.setVisibility(View.VISIBLE);*/
                break;
            case R.id.map_back_button:
                Log.d(TAG, "onClick: back arrow pressed");
                onBackArrowClicked(v);
                break;
        }
    }

    public void calcDirections(){
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.origin(new com.google.maps.model.LatLng(userLat,userLng));
        directions.destination(new com.google.maps.model.LatLng(restaurantLat,restaurantLng));
        directions.alternatives(true); //return alternative routes
       // directions.mode("driving")
        directions.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "onResult: directions calculated");
                Log.d(TAG, "onResult: distance = " + result.routes[0].legs[0].distance);
                traceRoute(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.d(TAG, "onFailure: failed to get directions " + e.getMessage());
            }
        });
    }

    public void traceRoute(DirectionsResult result){
        //post method to run on the main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            double shortestDuration = 99999999;
            @Override
            public void run() {
                //check if any polylines in the list. if yes, clear
                if(polylineData.size()>0){
                    polylineData.clear();
                }
                for(DirectionsRoute route : result.routes){
                    List<com.google.maps.model.LatLng> gDecodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    List<LatLng> aDecodedPath = new ArrayList<>();

                    //looping through all the latlng in one polyline
                    for(com.google.maps.model.LatLng latLng : gDecodedPath){
                        aDecodedPath.add(new LatLng(latLng.lat,latLng.lng));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(aDecodedPath));
                    polyline.setClickable(true);
                    polyline.setColor(Color.DKGRAY);
                    polylineData.add(new PolylineData(polyline,route.legs[0]));

                    if(route.legs[0].duration.inSeconds < shortestDuration){
                        shortestDuration = route.legs[0].duration.inSeconds;
                        onPolylineClick(polyline);
                    }
                }
            }
        });
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        for(PolylineData mPolylineData : polylineData){
            if(polyline.getId().equals(mPolylineData.getPolyline().getId())){
                polyline.setColor(Color.BLUE);
                polyline.setZIndex(1); //set elevation of selected polyline

                if(stepList.size()>0){
                    stepList.clear();
                }
                for(int i=0; i<mPolylineData.getDirectionsLeg().steps.length; i++){
                    stepList.add(mPolylineData.getDirectionsLeg().steps[i]);
                }

                Log.d(TAG, "onPolylineClick: step = " + mPolylineData.getDirectionsLeg().steps);
                restaurantMarker.setTag(mPolylineData);
                restaurantMarker.setSnippet("Duration = " + mPolylineData.getDirectionsLeg().duration);
                //restaurantMarker.setSnippet("Distance = " + mPolylineData.getDirectionsLeg().distance);
                restaurantMarker.showInfoWindow();
                Log.d(TAG, "onPolylineClick: tag =" + restaurantMarker.getTag());

            }
            else{
                mPolylineData.getPolyline().setColor(Color.DKGRAY);
                mPolylineData.getPolyline().setZIndex(0);
            }
        }
        mBtnGetDirections.setVisibility(View.GONE);
        mDirectionsRecycler.setVisibility(View.VISIBLE);
        adapter = new StepAdapter(stepList,this);
        mDirectionsRecycler.setAdapter(adapter);
    }

    public void onBackArrowClicked(View view) {
        onBackPressed();
    }
}
