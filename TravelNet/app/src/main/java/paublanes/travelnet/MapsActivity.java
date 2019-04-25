package paublanes.travelnet;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //search bar
    private EditText et_search;

    final static String TAG = "MapActivity";
    final static String PLACE_NAME_KEY = "placeName";
    final static String COORDINATES_KEY = "coordinates";
    final static int ACTIVITY_KEY = 3;

    //Data to return
    LatLng latLang;
    String placeName;

    //permisos
    private final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private boolean locationPermissionGranted = false;

    private final float DEFAULT_ZOOM = 15f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

       //Demanar permisos
        getLocationPermission();

        //Link views
        et_search = findViewById(R.id.et_search);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        findViewById(R.id.iv_gps).setOnClickListener(this);

        //Cridar el onMapReady
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getDeviceLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (locationPermissionGranted){
                final Task location  = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Log.e(TAG, "found location");
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null){
                                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(),
                                        currentLocation.getLongitude());
                                moveCamera(currentLatLng,DEFAULT_ZOOM);
                                latLang = currentLatLng;

                                List<android.location.Address> list = new ArrayList<>();
                                Geocoder geocoder = new Geocoder(MapsActivity.this);
                                try{
                                    list = geocoder.getFromLocation(latLang.latitude, latLang.longitude, 1);
                                }catch (IOException e) {
                                    Log.e(TAG, "geolocate: IOExeception: " + e.getMessage());
                                }

                                if (list.size() > 0) {
                                    android.location.Address adress = list.get(0);

                                    Log.d(TAG, "geolocate: found a location: " + adress.toString());
                                    placeName = adress.getAddressLine(0);
                                }

                            }else{
                                Log.e(TAG, "location is null");
                                Toast.makeText(MapsActivity.this, "Location is disabled", Toast.LENGTH_SHORT).show();
                            }

                        }else {
                            Log.e(TAG, "couldnt find location");
                            Toast.makeText(MapsActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "get device location error: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getLocationPermission() {
        String[] permissions = {FINE_LOCATION,COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                locationPermissionGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            locationPermissionGranted = false;
                            return;
                        }
                    }
                    locationPermissionGranted = true;
                    initMap();
                }
                break;
            }
        }

    }

    private void init () {
        Log.d(TAG, "init : initializing");

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                    Toast.makeText(MapsActivity.this, "asd", Toast.LENGTH_SHORT).show();
                    //amagar teclat
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);

                    //Execute method for searching
                    geoLocate();

                    return true;
                }
                return false;
            }
        });

    }

    private void geoLocate() {
        Log.d(TAG, "Geolocating");

        //Mirar si no hem buscat de pillar la posició actual

        String searchString = et_search.getText().toString();
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<android.location.Address> list = new ArrayList<>();

        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e) {
            Log.e(TAG, "geolocate: IOExeception: " + e.getMessage());
        }

        if (list.size() > 0) {
            android.location.Address adress = list.get(0);

            Log.d(TAG, "geolocate: found a location: " + adress.toString());

            latLang = new LatLng(adress.getLatitude(), adress.getLongitude());
            placeName = adress.getAddressLine(0);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang, 10));
            mMap.addMarker(new MarkerOptions().position(latLang).title(placeName));

        }
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        init(); //Set listener per la searchbar

        if (locationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);//pq la barra de búsqueda ho tapa

        }
    }

    private void sendDataBack(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra(PLACE_NAME_KEY, placeName);

        Bundle args = new Bundle();
        args.putParcelable(COORDINATES_KEY, latLang);
        resultIntent.putExtra("bundle", args);

        setResult(ACTIVITY_KEY, resultIntent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                sendDataBack();
                break;
            case R.id.iv_gps:
                getDeviceLocation();
                break;
        }
    }
}
