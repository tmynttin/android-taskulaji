package com.tmynttin.taskulaji;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.tmynttin.taskulaji.listeners.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispersionMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "DispersionMapActivity";
    private GoogleMap map;
    private String taxonID;

    List<WeightedLatLng> latLngs;
    LatLng mapPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.taxonID = "MX.34567";
        latLngs = new ArrayList<>();
        mapPosition = new LatLng(65, 23);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispersion_map);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_dispersion_map);

        mapFragment.getMapAsync(this);

        fetchHeatMapData();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        //map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        try {
            MapStyleOptions options;
            options = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style);
            map.setMapStyle(options);

        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }



        map.getUiSettings().setZoomControlsEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapPosition, 5.0f));
    }

    private void addHeatMap() {
        // Create a heat map tile provider, passing it the latlngs of the police stations.
        HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                .radius(10)
                .weightedData(latLngs)
                .build();

        // Add a tile overlay to the map, using the heat map tile provider.
        TileOverlay overlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
    }

    private void fetchHeatMapData() {

        Map<String, String> parameters = new HashMap<>();
        parameters.put("aggregateBy", "gathering.conversions.wgs84Grid01.lat,gathering.conversions.wgs84Grid01.lon,gathering.conversions.month");
        parameters.put("taxonId", this.taxonID);
        parameters.put("pageSize", "10000");
        parameters.put("page", "1");
        parameters.put("time", "-7300/0");
        parameters.put("area", "finland");

        Communication.SendGetRequest("warehouse/query/unit/aggregate", parameters, new RequestListener() {
            @Override
            public void onResponse(String stringResponse) {
                try {
                    JSONObject response = new JSONObject(stringResponse);
                    JSONArray results = response.optJSONArray("results");
                    int len = results.length();

                    for (int i=0; i < len; i++) {
                        JSONObject result = results.getJSONObject(i);
                        double lat = Double.parseDouble(result.getJSONObject("aggregateBy").getString("gathering.conversions.wgs84Grid01.lat"));
                        double lon = Double.parseDouble(result.getJSONObject("aggregateBy").getString("gathering.conversions.wgs84Grid01.lon"));
                        double count = result.getInt("count");
                        double intensity = 0;

                        if (count > 0) {
                            intensity = 1;
                        }

                        LatLng latLng = new LatLng(lat, lon);

                        latLngs.add(new WeightedLatLng(latLng, intensity));
                    }

                    addHeatMap();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String message) {
                Log.d(TAG, "onError: fetchHeatMapData " + message);
            }
        });
    }


}