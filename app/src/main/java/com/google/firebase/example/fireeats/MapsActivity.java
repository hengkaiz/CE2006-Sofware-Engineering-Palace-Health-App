package com.google.firebase.example.fireeats;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.example.fireeats.adapter.StepAdapter;
import com.google.firebase.example.fireeats.model.PolylineData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.TravelMode;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnPolylineClickListener{

    private static final String TAG = "Map";
    public static final String RESTAURANT_LNG = "restaurant_lat";
    public static final String RESTAURANT_LAT = "restaurant_lng";
    public static final String RESTAURANT_NAME = "restaurant_name";

    private GoogleMap mMap;
    private FirebaseFirestore mFirestore;
    private GeoApiContext geoApiContext = null;

    private double restaurantLat;
    private double restaurantLng;
    private String restaurantName;
    private double userLat = 1.405256; //1.348326;//1.353286;
    private double userLng = 103.902334; //103.683129;//103.682812;
    private LatLng restaurantCoor;
    private LatLng userCoor;
    private Marker restaurantMarker;
    private Marker userMarker;
    private ArrayList<PolylineData> polylineData = new ArrayList<>();
    private List<DirectionsStep> stepList = new ArrayList<>();;

    private SupportMapFragment mapFragment;
    private RecyclerView mDirectionsRecycler;
    private RecyclerView.Adapter adapter;
    private LinearLayout mModeLayout;
    private Button mBtnGetDirections;
    private Button mBtnDriving;
    private Button mBtnTransit;
    private Button mBtnWalk;


    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //setup GeoApiContext for directions api
        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();
        }

        //set up Firestore
        mFirestore = FirebaseFirestore.getInstance();

        //get extras
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

        //set up layouts
        mModeLayout = findViewById(R.id.modeLayout);
        mDirectionsRecycler = findViewById(R.id.directionsRecycler);
        mDirectionsRecycler.setHasFixedSize(false);
        mDirectionsRecycler.setLayoutManager(new LinearLayoutManager(this));

        //set up buttons
        mBtnGetDirections = findViewById(R.id.btnGetDirections);
        mBtnGetDirections.setOnClickListener(this);
        findViewById(R.id.map_back_button).setOnClickListener(this);
        mBtnDriving = findViewById(R.id.btnDriving);
        mBtnDriving.setOnClickListener(this);
        mBtnTransit = findViewById(R.id.btnTransit);
        mBtnTransit.setOnClickListener(this);
        mBtnWalk = findViewById(R.id.btnWalk);
        mBtnWalk.setOnClickListener(this);
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

        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        }*/
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

        //get distance between the 2 coordinates
        float results[] = new float[10];
        Location.distanceBetween(userLat,userLng,restaurantLat,restaurantLng,results);

        //add markers
        restaurantMarker = mMap.addMarker(new MarkerOptions()
                .position(restaurantCoor)
                .title(restaurantName)
                .snippet("Distance = " + (int)(results[0]/1000) + "km"));
        userMarker = mMap.addMarker(new MarkerOptions()
                .position(userCoor)
                .title("You are here!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantCoor, 15));
        restaurantMarker.showInfoWindow();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGetDirections:
                calcDirections("driving");
                //shows multiple markers in one screen
                LatLngBounds bounds = new LatLngBounds.Builder().include(restaurantCoor).include(userCoor).build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 500));

                //setting up layout
                mBtnDriving.setBackgroundColor(Color.GRAY);
                mBtnTransit.setBackgroundColor(Color.LTGRAY);
                mBtnWalk.setBackgroundColor(Color.LTGRAY);
                break;
            case R.id.map_back_button:
                Log.d(TAG, "onClick: back arrow pressed");
                onBackArrowClicked(v);
                break;
            case R.id.btnDriving:
                calcDirections("driving");
                mBtnDriving.setBackgroundColor(Color.GRAY);
                mBtnTransit.setBackgroundColor(Color.LTGRAY);
                mBtnWalk.setBackgroundColor(Color.LTGRAY);
                break;
            case R.id.btnTransit:
                calcDirections("transit");
                mBtnDriving.setBackgroundColor(Color.LTGRAY);
                mBtnTransit.setBackgroundColor(Color.GRAY);
                mBtnWalk.setBackgroundColor(Color.LTGRAY);
                break;
            case R.id.btnWalk:
                calcDirections("walk");
                mBtnDriving.setBackgroundColor(Color.LTGRAY);
                mBtnTransit.setBackgroundColor(Color.LTGRAY);
                mBtnWalk.setBackgroundColor(Color.GRAY);
                break;
        }
    }

    public void calcDirections(String mode){
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.origin(new com.google.maps.model.LatLng(userLat,userLng));
        directions.destination(new com.google.maps.model.LatLng(restaurantLat,restaurantLng));
        directions.alternatives(true); //return alternative routes

        //set the different mode based on parameter
        switch (mode) {
            case "driving":
                directions.mode(TravelMode.DRIVING);
                break;
            case "transit":
                directions.mode(TravelMode.TRANSIT);
                break;
            case "walk":
                directions.mode(TravelMode.WALKING);
                break;
        }
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
                    for(PolylineData polylineD: polylineData){
                        polylineD.getPolyline().remove();
                    }
                    polylineData.clear();
                    Log.d(TAG, "run: cleared");
                }
                for(DirectionsRoute route : result.routes){
                    List<com.google.maps.model.LatLng> gDecodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    List<LatLng> aDecodedPath = new ArrayList<>();

                    //looping through all the latlng in one polyline
                    for(com.google.maps.model.LatLng latLng : gDecodedPath){
                        aDecodedPath.add(new LatLng(latLng.lat,latLng.lng));
                    }

                    //creating the polyline based on all the latlng
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(aDecodedPath));
                    polyline.setClickable(true);
                    polyline.setColor(Color.DKGRAY);
                    polylineData.add(new PolylineData(polyline,route.legs[0]));

                    //get fastest route and highlight it
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

        //iterating through all the polylines in the polylineData to find selected polyline
        for(PolylineData mPolylineData : polylineData){
            if(polyline.getId().equals(mPolylineData.getPolyline().getId())){
                polyline.setColor(Color.BLUE);
                polyline.setZIndex(1); //set elevation of selected polyline

                if(stepList.size()>0){
                    stepList.clear();
                }
                stepList.addAll(Arrays.asList(mPolylineData.getDirectionsLeg().steps));

                Log.d(TAG, "onPolylineClick: step = " + mPolylineData.getDirectionsLeg().steps[0]);
                restaurantMarker.setTag(mPolylineData);
                restaurantMarker.setSnippet("Duration = " + mPolylineData.getDirectionsLeg().duration);
                restaurantMarker.showInfoWindow();
                Log.d(TAG, "onPolylineClick: tag =" + restaurantMarker.getTag());

            }
            else{
                mPolylineData.getPolyline().setColor(Color.DKGRAY);
                mPolylineData.getPolyline().setZIndex(0);
            }
        }

        //setting layout
        mModeLayout.setVisibility(View.VISIBLE);
        mBtnGetDirections.setVisibility(View.GONE);
        mDirectionsRecycler.setVisibility(View.VISIBLE);
        adapter = new StepAdapter(stepList,this);
        mDirectionsRecycler.setAdapter(adapter);
    }

    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    public boolean inNearby(LatLng coor){
        Circle circle = mMap.addCircle(new CircleOptions().center(userCoor).radius(5000));
        float results[] = new float[10];
        Location.distanceBetween(userLat,userLng,coor.latitude,coor.longitude,results);
        if(results[0]<=5000){
            return true;
        }
        else{
            return false;
        }
    }
}
