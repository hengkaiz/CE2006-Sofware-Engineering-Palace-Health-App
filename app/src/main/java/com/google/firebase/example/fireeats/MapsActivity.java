package com.google.firebase.example.fireeats;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.example.fireeats.model.PolylineData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;

import java.util.ArrayList;
import java.util.List;

import com.google.maps.model.DirectionsRoute;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnInfoWindowClickListener {
        //GoogleMap.OnMyLocationButtonClickListener,
        //GoogleMap.OnMyLocationClickListener,
        //GoogleMap.OnMarkerClickListener {

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
    private LatLng restaurantCoor;
    private LatLng userCoor;
    private Marker restaurantMarker;
    private Marker userMarker;

    private GeoApiContext geoApiContext = null;
    private ArrayList<PolylineData> polylineData = new ArrayList<>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(geoApiContext == null){
            geoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();
        }

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

        //set up button
        findViewById(R.id.btnGetDirections).setOnClickListener(this);

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

        mMap.setOnPolylineClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        /*mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Context mContext = null;
                LinearLayout info = new LinearLayout(mContext);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(mContext);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(mContext);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });*/

        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        restaurantCoor = new LatLng(restaurantLat,restaurantLng);
        Log.d(TAG, "lat = " + restaurantLat);
        Log.d(TAG, "lng = " + restaurantLng);
        Log.d(TAG, "name = " + restaurantName);

        //hardcoded userLoc until fixed
        userCoor = new LatLng(userLat,userLng);
        Log.d(TAG, "onMapReady: userLat = " + userLat);
        Log.d(TAG, "onMapReady: userLng = " + userLng);

        // Add a marker to restaurant and move the camera
        /*restaurantMarker = new MarkerOptions();
        restaurantMarker.position(restaurantCoor).title(restaurantName);

        userMarker = new MarkerOptions();
        userMarker.position(userCoor);*/
        //mMap.addMarker(new MarkerOptions().position(restaurantCoor).title(restaurantName));
        //mMap.addMarker(new MarkerOptions().position(userCoor).title("You are here!"));
        //Toast.makeText(this, "name = " + restaurantName, Toast.LENGTH_SHORT).show();
        //mMap.setOnMarkerClickListener(this);

        /*float results[] = new float[10];
        Location.distanceBetween(userLat,userLng,restaurantLat,restaurantLng,results);*/

        restaurantMarker = mMap.addMarker(new MarkerOptions()
                .position(restaurantCoor)
                .title(restaurantName));
        userMarker = mMap.addMarker(new MarkerOptions()
                .position(userCoor)
                .title("You are here!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantCoor, 15));
        restaurantMarker.showInfoWindow();



        /*userLoc();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            return;
        }*/

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGetDirections:
                calcDirections();
                //shows multiple markers in one screen
                LatLngBounds bounds = new LatLngBounds.Builder().include(restaurantCoor).include(userCoor).build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                break;
        }
    }

    public void calcDirections(){
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.origin(new com.google.maps.model.LatLng(userLat,userLng));
        directions.destination(new com.google.maps.model.LatLng(restaurantLat,restaurantLng));
        directions.alternatives(true); //return alternative routes
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

                restaurantMarker.setSnippet("Duration = " + mPolylineData.getDirectionsLeg().duration + "\n" + "Distance = " + mPolylineData.getDirectionsLeg().distance);
                //restaurantMarker.setSnippet("Distance = " + mPolylineData.getDirectionsLeg().distance);
                restaurantMarker.showInfoWindow();
            }
            else{
                mPolylineData.getPolyline().setColor(Color.DKGRAY);
                mPolylineData.getPolyline().setZIndex(0);
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(TAG, "onInfoWindowClick: clicked");
    }

    /*private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(), R.color.darkGrey));
                    polyline.setClickable(true);

                }
            }
        });
    }*/

    /*private String getUrl(){
        String str_origin = "origin=" + userCoor.latitude + "," + userCoor.longitude;
        String str_destination = "destination" + restaurantCoor.latitude + "," + restaurantCoor.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String param = str_origin + "&" + str_destination + "&" + sensor + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/directions/" + output + "?" + param;
        return url;
    }

    private String requestDirections(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while((line = bufferedReader.readLine()) != null){
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "requestDirections: " + e.getMessage());
        } finally{
            if (inputStream != null){
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }*/

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

    /*@Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }*/

}
