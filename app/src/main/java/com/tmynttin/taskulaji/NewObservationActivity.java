package com.tmynttin.taskulaji;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tmynttin.taskulaji.adapters.UnitAdapter;
import com.tmynttin.taskulaji.document.Document;
import com.tmynttin.taskulaji.document.Gathering;
import com.tmynttin.taskulaji.document.GatheringEvent;
import com.tmynttin.taskulaji.document.Geometry;
import com.tmynttin.taskulaji.document.GeometrySerializer;
import com.tmynttin.taskulaji.document.Unit;
import com.tmynttin.taskulaji.listeners.RecyclerViewClickListener;
import com.tmynttin.taskulaji.listeners.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NewObservationActivity extends AppCompatActivity {
    private static final String TAG = "NewObservationActivity";
    private static final int REQUEST_CODE_1 = 1;
    private static final int REQUEST_CODE_2 = 2;
    private static final int REQUEST_CODE_3 = 3;
    private static final int RESULT_DELETE = 101;

    private SwitchCompat hideUserSwitch;
    private SwitchCompat coarseLocationSwitch;
    private UnitAdapter unitAdapter;
    private RecyclerView recyclerView;
    private ImageButton addUnitButton;
    private MenuItem uploadButton;
    private TextView timeText;
    private TextView dateText;
    private TextView locationTextView;
    private ImageButton mapButton;
    private EditText localityView;
    private EditText localityDescriptionView;

    private Geometry geometry;
    private String region = "";
    private String municipality = "";

    private Unit[] data = new Unit[0];
    private RecyclerViewClickListener recyclerViewClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_observation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        hideUserSwitch = findViewById(R.id.hideUserSwitch);
        coarseLocationSwitch = findViewById(R.id.coarseLocationSwitch);

        recyclerView = findViewById(R.id.unitRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(NewObservationActivity.this));
    
        recyclerViewClickListener = (view, position) -> {
            Log.d(TAG, "onCreate: unit clicked");
            openUnitPage(position);
        };
    
        unitAdapter = new UnitAdapter(data, recyclerViewClickListener);
        recyclerView.setAdapter(unitAdapter);

        Calendar gatheringTime = Calendar.getInstance();
        int hour = gatheringTime.get(Calendar.HOUR_OF_DAY);
        int minute = gatheringTime.get(Calendar.MINUTE);
        int day = gatheringTime.get(Calendar.DAY_OF_MONTH);
        int month = gatheringTime.get(Calendar.MONTH);
        int year = gatheringTime.get(Calendar.YEAR);

        timeText = (TextView) findViewById(R.id.textTime);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        timeText.setText(timeFormat.format(gatheringTime.getTime()));
        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(NewObservationActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int setHour, int setMinute) {
                        gatheringTime.set(year, month, day, setHour, setMinute);
                        timeText.setText(timeFormat.format(gatheringTime.getTime()));
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Set time");
                mTimePicker.show();
            }
        });

        dateText = (TextView) findViewById(R.id.textDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateText.setText(dateFormat.format(gatheringTime.getTime()));
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog datePickerDialog;
                datePickerDialog = new DatePickerDialog(NewObservationActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int setYear, int setMonth, int setDay) {
                        gatheringTime.set(setYear, setMonth, setDay);
                        dateText.setText(dateFormat.format(gatheringTime.getTime()));
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        locationTextView = findViewById(R.id.locationView);
        mapButton = (ImageButton) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMapPage(view);
            }
        });
        mapButton.setBackgroundColor(getResources().getColor(R.color.orange_warning));

        localityView = findViewById(R.id.editTextLocality);
        localityDescriptionView = findViewById(R.id.editTextLocalityDescription);

        addUnitButton = (ImageButton) findViewById(R.id.fab);
        addUnitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUnit(view);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_observation_menu, menu);

        uploadButton = menu.findItem(R.id.action_upload);
        uploadButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                createDocument();
                return false;
            }
        });

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_1:
                    Log.d(TAG, "onActivityResult: returning to NewObservationActivity");
                    String taxoName = dataIntent.getStringExtra("taxoName");
                    String taxoId = dataIntent.getStringExtra("taxoId");
                    int unitIndex = dataIntent.getIntExtra("unitIndex", 0);
                    updateUnit(unitIndex, taxoName, taxoId);
                    openUnitPage(unitIndex);
                    break;
                case REQUEST_CODE_2:
                    Object geometryObject = dataIntent.getSerializableExtra("geometry");
                    geometry = (Geometry) geometryObject;
                    Log.d(TAG, "onActivityResult: " + geometry.toString());
                    //mapButton.setBackgroundColor(Color.GREEN);
                    mapButton.setBackgroundColor(getResources().getColor(R.color.green_ok));
                    //mapButton.setColorFilter(Color.GREEN);
                    getLocation(geometry);
                    break;
                case REQUEST_CODE_3:
                    Object unitObject = dataIntent.getSerializableExtra("unit");
                    Unit unit = (Unit) unitObject;
                    Log.d(TAG, "onActivityResult: Unit: " + unit.identifications[0].taxon);
                    int index = dataIntent.getIntExtra("unitIndex", 0);
                    Unit[] data = unitAdapter.getLocalDataSet();
                    data[index] = unit;
                    unitAdapter.UpdateDataSet(data);
                    break;
                default:
                    Log.d(TAG, "onActivityResult RESULT_OK: DEFAULT!!!");
            }
        }
        if (resultCode == RESULT_DELETE) {
            switch (requestCode) {
                case REQUEST_CODE_1:
                case REQUEST_CODE_3:
                    int index = dataIntent.getIntExtra("unitIndex", 0);
                    deleteUnit(index);
                    break;
                default:
                    Log.d(TAG, "onActivityResult, RESULT_DELETE: DEFAULT!!!");
            }
        }
    }



    private void openSearchPage(int position) {
        Intent intent = new Intent(this, TaxoSearchActivity.class);
        intent.putExtra("unitIndex", position);
        startActivityForResult(intent, REQUEST_CODE_1);
    }

    public void openMapPage(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivityForResult(intent, REQUEST_CODE_2);
    }

    public void getLocation(Geometry geometry) {

        JSONObject jsonGeometry = getJSONDocument(geometry);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("lang", "fi");
        Communication.SendPostRequest("coordinates/location", parameters, jsonGeometry, new RequestListener() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject coordinates = new JSONObject(response);
                    Log.d(TAG, "onResponse: " + response);

                    JSONArray results = coordinates.optJSONArray("results");
                    Log.d(TAG, "onResponse: " + results);
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        Log.d(TAG, "onResponse: " + result);
                        String type = result.optJSONArray("address_components").getJSONObject(0).optJSONArray("types").getString(0);
                        Log.d(TAG, "onResponse: type: " + type);
                        if (type.equals("region")) {
                            region = result.getString("formatted_address");
                        }
                        else if (type.equals("municipality")) {
                            municipality = result.getString("formatted_address");
                        }


                    }
                    String loc = region + "\n" + municipality + "\n" + geometry.coordinates[0][0][0] + " " + geometry.coordinates[0][0][1];
                    Log.d(TAG, "Location is: \n" + loc);
                    locationTextView.setText(loc);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String message) {
                Log.d(TAG, "onError: " + message);
            }
        });

        double longitude = geometry.coordinates[0][0][0];
        double latitude = geometry.coordinates[0][0][1];
        Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                String street = addresses.get(0).getThoroughfare();
                if (street != null) {
                    localityView.setText(street);
                }
            }
        }
        catch (IOException e) {
            Log.d(TAG, "Could not get location.");
        }
    }

    private void openUnitPage(int position) {
        Intent intent = new Intent(this, UnitActivity.class);
        intent.putExtra("unitIndex", position);
        intent.putExtra("unit", unitAdapter.getLocalDataSet()[position]);
        startActivityForResult(intent, REQUEST_CODE_3);
    }
    
    public void addUnit(View view) {
        Unit[] data = unitAdapter.getLocalDataSet();
        Unit unit = new Unit();

        List<Unit> unitsList = new ArrayList<Unit>();
        Collections.addAll(unitsList, data);
        unitsList.add(unit);
        data = unitsList.toArray(new Unit[unitsList.size()]);

        unitAdapter.UpdateDataSet(data);
        int position = unitAdapter.getItemCount()-1;
        openSearchPage(position);
    }

    public void updateUnit(int unitIndex, String taxoName, String taxoID) {
        Log.d(TAG, "updateUnit: index: " + unitIndex + " name: " + taxoName);
        Unit[] data = unitAdapter.getLocalDataSet();

        data[unitIndex].setIdentification(taxoName, taxoID);

        unitAdapter.UpdateDataSet(data);
    }

    public void deleteUnit(int unitIndex) {
        Unit[] data = unitAdapter.getLocalDataSet();

        List<Unit> unitsList = new ArrayList<Unit>();
        Collections.addAll(unitsList, data);
        unitsList.remove(unitIndex);
        data = unitsList.toArray(new Unit[unitsList.size()]);

        unitAdapter.UpdateDataSet(data);
    }

    private String getUserID() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        String userId = sharedPreferences.getString("userId", "");
        return userId;
    }

    private void createDocument() {
        uploadButton.setEnabled(false);
        Document document = new Document();
        if (coarseLocationSwitch.isChecked()) {
            document.setSecureLevel("KM10");
        }
        document.setCreator(getUserID());

        GatheringEvent gatheringEvent = new GatheringEvent();
        gatheringEvent.setLegPublic(!hideUserSwitch.isChecked());
        gatheringEvent.setTime(dateText.getText().toString(), timeText.getText().toString());
        gatheringEvent.addLeg(getUserID());

        document.setGatheringEvent(gatheringEvent);

        Gathering gathering = new Gathering();
        gathering.addGeometry(geometry);
        gathering.setMunicipality(municipality);
        gathering.setLocality(localityView.getText().toString());
        gathering.setLocalityDescription(localityDescriptionView.getText().toString());
        //gathering.setDateBegin((String)dateText.getText());
        gathering.setUnits(unitAdapter.getLocalDataSet());

        document.addGathering(gathering);

        JSONObject jsonDocument = getJSONDocument(document);
        sendDocument(jsonDocument);
    }

    private JSONObject getJSONDocument(Object document) {
        JSONObject jsonDocument = new JSONObject();
        try {

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Geometry.class, new GeometrySerializer());
            Gson gson = gsonBuilder.create();
            String jsonString = gson.toJson(document);

            //String jsonString = new Gson().toJson(document);
            jsonDocument = new JSONObject(jsonString);
            Log.d(TAG, "getJSONDocument: " + jsonDocument.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return jsonDocument;
    }

    private void sendDocument(JSONObject document) {
        Log.d(TAG, "sendDocument: sending");
        Map<String, String> parameters = new HashMap<String, String>();
        Communication.SendPostRequest("documents", parameters, document, new RequestListener() {

            @Override
            public void onResponse(String response) {
                returnToMainActivity();
            }

            @Override
            public void onError(String message) {
                Log.d(TAG, "onError: " + message);
            }
        });
    }

    private void returnToMainActivity() {
        Context context = getApplicationContext();
        CharSequence text = "Document sent";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        finish();
    }
}