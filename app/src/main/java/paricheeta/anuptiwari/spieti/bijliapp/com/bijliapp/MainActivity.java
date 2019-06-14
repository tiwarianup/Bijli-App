package paricheeta.anuptiwari.spieti.bijliapp.com.bijliapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import paricheeta.anuptiwari.spieti.bijliapp.com.bijliapp.models.PlaceInfo;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mToolbar;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String TAG = "MainActivity";
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));

    private boolean mLocationPermissionGranted = false;
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private GeoLocation mGeoLocation;
    private double mReportLatitude;
    private double mReportLongitude;

    private MarkerOptions mOptions;
    private Marker mMarker;

    private PlaceInfo mPlace;
    private AutoCompleteTextView mEditText;
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private ImageView mGpsButton;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabaseReferenceOn;

    private Button mReportBtn;
    private GoogleApiClient mGoogleApiClient;
    private String mUid;

    private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";


    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Toast.makeText(this, "RealTime Power Supply Status!", Toast.LENGTH_SHORT).show();
        mGoogleMap = googleMap;


        mDatabaseReferenceOn.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(mMarker != null) mMarker.remove();
                LatLng newLocation = new LatLng(
                        dataSnapshot.child("l").child("0").getValue(Double.class),
                        dataSnapshot.child("l").child("1").getValue(Double.class)
                );

                mGoogleMap.addMarker(new MarkerOptions()
                        .position(newLocation)
                        .title("Status: Off")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_outage)));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(mMarker != null) mMarker.remove();
                LatLng newLocation = new LatLng(
                        dataSnapshot.child("l").child("0").getValue(Double.class),
                        dataSnapshot.child("l").child("1").getValue(Double.class)
                );
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(newLocation)
                        .title("Status: On")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_revival)));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            //mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            initialize();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        mEditText = (AutoCompleteTextView)findViewById(R.id.mapSearchInput);
        mGpsButton = (ImageView)findViewById(R.id.gpsIcon);
        mReportBtn = (Button)findViewById(R.id.reportOutageBtn);
        ImageView clearTextBtn = (ImageView)findViewById(R.id.clearText);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("LocationStatsOff");
        mDatabaseReferenceOn = FirebaseDatabase.getInstance().getReference("LocationStatsOn");

        ImageView logoutBtn = (ImageView)findViewById(R.id.logoutIcon);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class));
                finish();
            }
        });

        clearTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.setText("");
                mEditText.requestFocus();
            }
        });

        getLocationPermission();

        mReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reportBtnTag = (String)mReportBtn.getTag();
                String outageStr = "outage";
                String revivalStr = "revival";

                GeoFire mGeoFire = new GeoFire(mDatabaseReference);
                GeoFire mGeoFire1 = new GeoFire(mDatabaseReferenceOn);


                if(reportBtnTag.equals(revivalStr)){

                    mReportBtn.setText(R.string.report_outage);
                    mReportBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    mReportBtn.setTag(outageStr);
                    Toast toast1 = Toast.makeText(MainActivity.this, "Status Updated: Power Supply Revived.", Toast.LENGTH_SHORT);
                    toast1.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 10, 550);
                    toast1.show();

                    mGeoFire.setLocation(randomAlphaNumeric(14), mGeoLocation, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });

                }else if(reportBtnTag.equals(outageStr)){
                    mReportBtn.setText(R.string.report_revival);
                    mReportBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    mReportBtn.setTag(revivalStr);
                    Toast toast2 = Toast.makeText(MainActivity.this, "Status Update: Power Outage Reported.", Toast.LENGTH_SHORT);
                    toast2.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 10, 550);
                    toast2.show();

                    mGeoFire1.setLocation(randomAlphaNumeric(14), mGeoLocation, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });

                }else{
                    Log.d(TAG, "onClick: Else executed.");
                }
            }
        });

    }

    private void initialize(){
        Log.d(TAG, "init: initializing AutoCompleteTextView..");

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API).enableAutoManage(this, this)
                .build();

        mEditText.setOnItemClickListener(mAutocompleteClickListener);

        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, LAT_LNG_BOUNDS, null);

        mEditText.setAdapter(placeAutocompleteAdapter);

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    geoLocate();
                }
                return false;
            }
        });

        mGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });

        hideSoftKeyboard();
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: Geolocating...");

        String searchString = mEditText.getText().toString();
        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> addressList = new ArrayList<>();

        try{
            addressList = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOError: " + e.toString());
        }

        if(addressList.size() > 0){
            Address address = addressList.get(0);
            Log.d(TAG, "geoLocate: Address found: " + address.toString());

            mReportLatitude = address.getLatitude();
            mReportLongitude = address.getLongitude();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
            //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    public void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MainActivity.this);
    }


    private void getDeviceLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if(mLocationPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: Device location found.");
                            Location currentLocation = (Location) task.getResult();
                            mReportLatitude = currentLocation.getLatitude();
                            mReportLongitude = currentLocation.getLongitude();
                            mGeoLocation = new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");

                        }else{
                            Log.d(TAG, "onComplete: Device location unavailable.");
                            Toast.makeText(MainActivity.this, "Device location unavailable!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }


    private void moveCamera(LatLng latlng, float zoom, String title){
        Log.d(TAG, "moveCamera: Moving camera to Lat: " + latlng.latitude + " , Lng: " + latlng.longitude);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
        mGeoLocation = new GeoLocation(latlng.latitude, latlng.longitude);
        mReportBtn.setText(R.string.report_outage);
        mReportBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mReportBtn.setTag("outage");

        if(!title.equals("My Location")){
            mOptions = new MarkerOptions().position(latlng).title(title);
            mMarker = mGoogleMap.addMarker(mOptions);
        }

        hideSoftKeyboard();
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard(){
        //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(mEditText.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // -------------------------- GooglePlaces API Autocomplete Suggestions -------------------------

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();
            mReportBtn.requestFocusFromTouch();

            final AutocompletePrediction item = placeAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try{
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlace.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                mPlace.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlace.toString());
            }catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage() );
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace.getName());

            places.release();
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        if (isServicesOk()){
            init();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    public void init(){
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){
            startActivity(new Intent(MainActivity.this, StartActivity.class));
            finish();
        }
    }

    //Check for correct version of Google Play Services installation
    public boolean isServicesOk(){
        Log.d(TAG, "isServicesOk: Checking for Google Play Services requirements.");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS){
            Log.d(TAG, "isServicesOk: Google Play Services requirements satisfied.");
            return true;
        }else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "isServicesOk: User resolvable error.");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "Error: Google Play Services couldn't be found!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


}
