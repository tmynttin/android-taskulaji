package com.tmynttin.taskulaji;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.squareup.picasso.Picasso;
import com.tmynttin.taskulaji.adapters.DescriptionAdapter;
import com.tmynttin.taskulaji.document.Helpers;
import com.tmynttin.taskulaji.listeners.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class TaxonInfoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "TaxonInfoActivity";
    private static final int REQUEST_CODE_1 = 1;

    private TextView scientificNameView;
    private TextView vernacularNameView;
    private TextView primaryHabitatView;
    private TextView secondaryHabitatView;

    private ImageView imageView;
    private JSONArray imageDataArray;
    private ImageButton previousImageButton;
    private ImageButton nextImageButton;
    private int imageIndex;
    private ConstraintLayout mapContainer;

    private RecyclerView descriptionRecyclerView;
    private ImageButton searchButton;

    private AudioPlayerView audioPlayer;

    private String taxonID;
    private JSONObject taxoInformation;

    private DescriptionAdapter descriptionAdapter;
    private JSONArray descriptions;

    private GoogleMap map;
    List<WeightedLatLng> latLngs;
    LatLng mapPosition;
    private HeatmapTileProvider provider;
    private TileOverlay overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxon_info);

        latLngs = new ArrayList<>();
        mapPosition = new LatLng(65, 25);

        scientificNameView = findViewById(R.id.scientificName);
        vernacularNameView = findViewById(R.id.vernacularName);
        primaryHabitatView = findViewById(R.id.primaryHabitat);
        secondaryHabitatView = findViewById(R.id.secondaryHabitat);

        imageView = findViewById(R.id.taxoImageView);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            float startPoint = 0;
            float endPoint = 0;
            float treshold = 50;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: " + event.getAction());
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        startPoint = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        endPoint = event.getX();
                        Log.d(TAG, "onTouch: " + (startPoint-endPoint));
                        if (endPoint < (startPoint - treshold)) {
                            Log.d(TAG, "onTouch: LEFT " + (startPoint-endPoint));

                            if (imageIndex < imageDataArray.length() -1) {
                                imageIndex++;
                                setImage(imageIndex);
                            }
                        }
                        else if (endPoint > (startPoint + treshold)) {
                            Log.d(TAG, "onTouch: RIGHT " + (startPoint-endPoint));
                            if (imageIndex > 0) {
                                imageIndex--;
                                setImage(imageIndex);
                            }
                        }
                        return true;
                    default:
                        Log.d(TAG, "onTouch: default");
                }
                return true;
            }
        });

        previousImageButton = findViewById(R.id.previousImageButton);
        previousImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageIndex > 0) {
                    imageIndex--;
                    setImage(imageIndex);
                }
            }
        });
        nextImageButton = findViewById(R.id.nextImageButton);
        nextImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageIndex < imageDataArray.length() -1) {
                    imageIndex++;
                    setImage(imageIndex);
                }
            }
        });
        setImageVisibility(GONE);

        searchButton = findViewById(R.id.fab);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchPage();
            }
        });

        audioPlayer = findViewById(R.id.audioPlayer);

        descriptionRecyclerView = findViewById(R.id.descriptionRecyclerView);
        descriptionRecyclerView.setLayoutManager(new LinearLayoutManager(TaxonInfoActivity.this));
        descriptions = new JSONArray();
        descriptionAdapter = new DescriptionAdapter(descriptions);
        descriptionRecyclerView.setAdapter(descriptionAdapter);

        mapContainer = findViewById(R.id.map_fragment_container);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_dispersion_map);

        mapFragment.getMapAsync(this);




        openSearchPage();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        try {
            MapStyleOptions options;
            options = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style);
            map.setMapStyle(options);

        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        //map.getUiSettings().setZoomControlsEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapPosition, 4f));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_1:
                    Log.d(TAG, "onActivityResult: returning to TaxonInfoActivity");
                    String taxoId = dataIntent.getStringExtra("taxoId");
                    updateTaxon(taxoId);
                    break;
                default:
                    Log.d(TAG, "onActivityResult: DEFAULT!!!");
            }
        }
    }


    private void openSearchPage() {
        audioPlayer.pause();
        Intent intent = new Intent(this, TaxoSearchActivity.class);
        startActivityForResult(intent, REQUEST_CODE_1);
    }

    private void updateTaxon(String taxonID) {
        this.taxonID = taxonID;
        imageView.setVisibility(GONE);
        if (overlay != null) {
            overlay.setVisible(false);
        }
        fetchInformation();
        fetchImage();
        fetchDescription();
        fetchHeatMapData();
    }

    private void addHeatMap() {
        if (latLngs.size() > 0) {
            if (overlay == null) {

                provider = new HeatmapTileProvider.Builder().radius(10).weightedData(latLngs).build();
                overlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
            } else {
                provider.setWeightedData(latLngs);
                overlay.clearTileCache();
            }
            overlay.setVisible(true);
        }
    }

    private String getPrimaryHabitat(JSONObject information) {
        String value = "";
        Log.d(TAG, "getPrimaryHabitat: LOOKING FOR IT...");
        try {
            String id = information.getJSONObject("primaryHabitat").getString("habitat");
            Log.d(TAG, "getPrimaryHabitat: id is " + id);
            value = Helpers.getValueFromId(R.raw.habitats, id);
            Log.d(TAG, "getPrimaryHabitat: Now at end of trying. Value is " + value);
        }

        catch (JSONException e) {
            Log.d(TAG, "getPrimaryHabitat: Something went WRONG!!!!!!!!!!!!!!!!");
            e.printStackTrace();
        }

        return value;
    }

    private String getSecondaryHabitats(JSONObject information) {
        String value = "";
        Log.d(TAG, "getSecondaryHabitat: LOOKING FOR IT...");
        try {
            JSONArray secondaryHabitats = information.getJSONArray("secondaryHabitats");
            int len = secondaryHabitats.length();
            for (int i = 0; i < len; i++) {
                String id = secondaryHabitats.getJSONObject(i).getString("habitat");
                Log.d(TAG, "getSecondaryHabitat: id is " + id);
                value = value + Helpers.getValueFromId(R.raw.habitats, id) + "\n";
                Log.d(TAG, "getSecondaryHabitat: Now at end of trying. Value is " + value);
            }
        }

        catch (JSONException e) {
            Log.d(TAG, "getPrimaryHabitat: Something went WRONG!!!!!!!!!!!!!!!!");
            e.printStackTrace();
        }

        return value;
    }

    private void fetchHeatMapData() {
        latLngs.clear();

        Map<String, String> parameters = new HashMap<>();
        parameters.put("aggregateBy", "gathering.conversions.wgs84Grid01.lat,gathering.conversions.wgs84Grid01.lon");
        parameters.put("taxonId", this.taxonID);
        parameters.put("pageSize", "10000");
        parameters.put("page", "1");
        parameters.put("cache", "true");
        parameters.put("time", "-7300/0");
        parameters.put("area", "finland");
        parameters.put("recordQuality", "EXPERT_VERIFIED,COMMUNITY_VERIFIED,NEUTRAL");

        Communication.SendGetRequest("warehouse/query/unit/aggregate", parameters, new RequestListener() {
            @Override
            public void onResponse(String stringResponse) {
                try {
                    JSONObject response = new JSONObject(stringResponse);
                    JSONArray results = response.optJSONArray("results");
                    int len = results.length();

                    LatLng scalingPoint = new LatLng(0, 0);
                    latLngs.add(new WeightedLatLng(scalingPoint, 6));

                    for (int i=0; i < len; i++) {
                        JSONObject result = results.getJSONObject(i);
                        double lat = Double.parseDouble(result.getJSONObject("aggregateBy").getString("gathering.conversions.wgs84Grid01.lat"));
                        double lon = Double.parseDouble(result.getJSONObject("aggregateBy").getString("gathering.conversions.wgs84Grid01.lon"));
                        double count = result.getInt("count");
                        double intensity = 0;
                        if (count > 0 && count < 1) {
                            intensity = 1;
                        }
                        if (count >= 1 && count < 10) {
                            intensity = 2;
                        }

                        if (count >= 10 && count < 100) {
                            intensity = 3;
                        }
                        if (count >= 100 && count < 1000) {
                            intensity = 4;
                        }

                        if (count >= 1000 && count < 10000) {
                            intensity = 5;
                        }

                        if(count >= 10000) {
                            intensity = 6;
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

    private void fetchInformation() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("lang", "fi");
        Communication.SendGetRequest("taxa/" + this.taxonID, parameters, new RequestListener() {
            @Override
            public void onResponse(String response) {
                try {
                    taxoInformation = new JSONObject(response);

                    String scientificName = taxoInformation.optString("scientificName");
                    scientificNameView.setText(scientificName);

                    String vernacularName = taxoInformation.optString("vernacularName");
                    vernacularNameView.setText(vernacularName);

                    String primaryHabitat = getPrimaryHabitat(taxoInformation);
                    primaryHabitatView.setText("Ensisijainen elinympäristö:\n" + primaryHabitat);

                    String secondaryHabitat = getSecondaryHabitats(taxoInformation);
                    secondaryHabitatView.setText("Toissijainen elinympäristö:\n" + secondaryHabitat);

                    audioPlayer.updateDataset(taxoInformation);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String message) {
                Log.d(TAG, "onError: fetchInformation " + message);
            }
        });
    }

    private void fetchImage() {
        imageDataArray = new JSONArray();
        imageIndex = 0;
        Map<String, String> parameters = new HashMap<>();
        parameters.put("lang", "fi");
        Communication.SendGetRequest("taxa/" + this.taxonID + "/media", parameters, new RequestListener() {

            @Override
            public void onResponse(String response) {
                try {
                    imageDataArray = new JSONArray(response);
                    setImage(imageIndex);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    setImageVisibility(GONE);
                }
            }

            @Override
            public void onError(String message) {
                Log.d(TAG, "onError: ");
            }
        });
    }

    private void setImage(int index) {
        String fullImage = "";
        try {
            JSONObject media_data = imageDataArray.getJSONObject(index);
            fullImage = media_data.getString("largeURL");
            Picasso.get().load(fullImage).into(imageView);
            setImageVisibility(VISIBLE);
        }

        catch (JSONException e) {
            e.printStackTrace();
            setImageVisibility(GONE);
        }
    }

    private void setImageVisibility(int visibility) {
        imageView.setVisibility(visibility);
        previousImageButton.setVisibility(visibility);
        nextImageButton.setVisibility(visibility);
    }

    private void fetchDescription() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("lang", "fi");
        Communication.SendGetRequest("taxa/" + this.taxonID + "/descriptions", parameters, new RequestListener() {

            @Override
            public void onResponse(String response) {

                JSONArray descriptionData = new JSONArray();
                try {
                    JSONArray JSONresponse = new JSONArray(response);
                    JSONArray groups = JSONresponse.getJSONObject(0).getJSONArray("groups");
                    for (int i = 0; i < groups.length(); i++) {
                        JSONObject group = groups.getJSONObject(i);
                        String groupTitle = group.getString("title");
                        JSONArray variables = group.getJSONArray("variables");
                        for(int j = 0; j < variables.length(); j++) {
                            JSONObject variable = variables.getJSONObject(j);
                            String variableTitle = variable.getString("title");
                            String variableContent = variable.getString("content");

                            JSONObject description = new JSONObject();
                            description.put("groupTitle", groupTitle);
                            description.put("title", variableTitle);
                            description.put("content", variableContent);
                            descriptionData.put(description);
                        }

                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                descriptionAdapter.UpdateDataSet(descriptionData);
            }

            @Override
            public void onError(String message) {
                Log.d(TAG, "onError: ");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioPlayer.stop();
    }
}