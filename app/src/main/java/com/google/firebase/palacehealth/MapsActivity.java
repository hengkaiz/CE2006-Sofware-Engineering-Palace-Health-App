package com.google.firebase.palacehealth;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.palacehealth.fireeats.R;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        //GoogleMap.OnMyLocationButtonClickListener,
        //GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener {

    private static final String TAG = "Map";
    public static final String KEY_RESTAURANT_ID = "key_restaurant_id";
    public static final String RESTAURANT_LNG = "restaurant_lat";
    public static final String RESTAURANT_LAT = "restaurant_lng";
    public static final String RESTAURANT_NAME = "restaurant_name";

    private GoogleMap mMap;

    private FirebaseFirestore mFirestore;
    private DocumentReference mRestaurantRef;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private double restaurantLat;
    private double restaurantLng;
    private String restaurantName;
    private double userLat = 1.353286;
    private double userLng = 103.682812;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFirestore = FirebaseFirestore.getInstance();
        String restaurantId;
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_RESTAURANT_ID);
        } else {
            restaurantId = bundle.getString(KEY_RESTAURANT_ID);
            restaurantLat = bundle.getDouble(RESTAURANT_LAT);
            restaurantLng = bundle.getDouble(RESTAURANT_LNG);
            restaurantName = bundle.getString(RESTAURANT_NAME);
            Log.d(TAG, "onCreate: lat " + restaurantLat);
            Log.d(TAG, "onCreate: lng " + restaurantLng);
            Log.d(TAG, "onCreate: name " + restaurantName);
        }

        // Get reference to the restaurant
        mRestaurantRef = mFirestore.collection("restaurants").document(restaurantId);
     }

    /*private void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case 10:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates("gps", 1000, 0, locationListener);
        }
    }*/


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

        /*mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);*/

        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: hi");
        LatLng restaurantCoor = new LatLng(restaurantLat,restaurantLng);
        Log.d(TAG, "lat = " + restaurantLat);
        Log.d(TAG, "lng = " + restaurantLng);
        Log.d(TAG, "name = " + restaurantName);

        //hardcoded userLoc until fixed
        LatLng userCoor = new LatLng(userLat,userLng);
        Log.d(TAG, "onMapReady: userLat = " + userLat);
        Log.d(TAG, "onMapReady: userLng = " + userLng);

        // Add a marker to restaurant and move the camera
        mMap.addMarker(new MarkerOptions().position(restaurantCoor).title(restaurantName));
        mMap.addMarker(new MarkerOptions().position(userCoor).title("You are here!"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantCoor, 15));
        mMap.setOnMarkerClickListener(this);

        LatLngBounds bounds = new LatLngBounds.Builder().include(restaurantCoor).include(userCoor).build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,50));

        /*userLoc();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            return;
        }*/

    }

   /* @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this,"Current location", Toast.LENGTH_LONG).show();
    }

    //initialises map
    private void initMap(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //get user current location
    private void userLoc(){
        //get user current location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                userLat = location.getLatitude();
                userLng = location.getLongitude();
                Log.d(TAG, "onLocationChanged: userLat = " + userLat);
                Log.d(TAG, "onLocationChanged: userLng = " + userLng);
                //initMap();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
    }*/

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

}
