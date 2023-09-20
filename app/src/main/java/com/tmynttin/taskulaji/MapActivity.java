package com.tmynttin.taskulaji;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.tmynttin.taskulaji.document.Geometry;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener {

    private Button setLocationButton;

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Polygon polygon;
    private Marker marker;
    private Marker currentLocation;
    private List<LatLng> polyArray = new ArrayList<LatLng>();
    private static final String TAG = "MapActivity";

    private boolean fristLocationZoomDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setLocationButton = findViewById(R.id.setLocationButton);
        setLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnLocation(view);
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);

        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //getLocation();

        createLocationRequest();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "onLocationResult: got a result");
                Location lastLocation = locationResult.getLocations().get(0);
                double lat = lastLocation.getLatitude();
                double lon = lastLocation.getLongitude();
                setCurrentLocationMarker(lat, lon);
            }
        };

        startLocationUpdates();



    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

    }

/*
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "getLocation: NO PERMISSIONS!");
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d(TAG, "onSuccess: locaition: " + location.toString());
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    setCurrentLocationMarker(lat, lon);
                }
            }
        });
    }
*/

    private void startLocationUpdates() {
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
        Log.d(TAG, "startLocationUpdates: starting updates");
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }


    public void setCurrentLocationMarker(double lat, double lon) {
        LatLng latLng = new LatLng(lat, lon);
        if (currentLocation != null) {
            currentLocation.setPosition(latLng);
        }
        else {
            currentLocation = map.addMarker(new MarkerOptions()
            .position(latLng)
            .title("Current location"));
        }
        if (!fristLocationZoomDone) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
            fristLocationZoomDone = true;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng point) {

        Log.d(TAG, "onMapClick: " + point.toString());
        polyArray.add(point);

        if(polyArray.size()>1) {
            if(polygon != null) {
                polygon.remove();
            }
            polygon = map.addPolygon(new PolygonOptions().addAll(polyArray)
                    .strokeColor(Color.RED)
                    .fillColor(Color.BLUE));
        }
        else if (polyArray.size()==1) {
            if(polygon != null) {
                polygon.remove();
            }
            if(marker != null) {
                marker.remove();
            }
            marker = map.addMarker(new MarkerOptions().position(polyArray.get(0)));
        }

    }

    public void resetPolygon(View view) {
        polyArray = new ArrayList<LatLng>();
        if (polygon != null) {
            polygon.remove();
        }
    }

    public void returnLocation(View view) {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Intent returnIntent = new Intent();
        double[][] coordinates;

        if(polyArray.size() < 1) {
            Log.d(TAG, "returnLocation: using current location");
            coordinates = new double[1][2];
            double lon = currentLocation.getPosition().longitude;
            double lat = currentLocation.getPosition().latitude;
            coordinates[0][0] = lon;
            coordinates[0][1] = lat;
        }

        else if(polyArray.size() == 1) {
            Log.d(TAG, "returnLocation: using point");
            coordinates = new double[1][2];
            double lon = polyArray.get(0).longitude;
            double lat = polyArray.get(0).latitude;
            coordinates[0][0] = lon;
            coordinates[0][1] = lat;
        }


        else {
            Log.d(TAG, "returnLocation: using polygon");
            coordinates = new double[polyArray.size() + 1][2];
            for (int i = 0; i < polyArray.size(); i++) {
                double lon = polyArray.get(i).longitude;
                double lat = polyArray.get(i).latitude;
                coordinates[i][0] = lon;
                coordinates[i][1] = lat;
            }
            coordinates[coordinates.length - 1] = coordinates[0];
        }

        Geometry geometry = new Geometry(coordinates);
        Log.d(TAG, "returnLocation: " + geometry.toString());
        returnIntent.putExtra("geometry", geometry);
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}