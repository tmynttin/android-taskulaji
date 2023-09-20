package com.tmynttin.taskulaji;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tmynttin.taskulaji.document.Helpers;
import com.tmynttin.taskulaji.document.Unit;
import com.tmynttin.taskulaji.listeners.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UnitActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "UnitActivity";
    private static final int REQUEST_CODE_1 = 1;
    private static final int RESULT_DELETE = 101;

    private Unit unit;
    private int unitIndex;

    private TextView taxonView;
    private TextView taxonIDView;
    private EditText countView;
    private EditText notesView;
    private Spinner recodBasisSpinner;
    private Spinner taxonConfidenceSpinner;
    private ImageButton imageButton;
    private ImageView unitImageView;
    private ImageButton acceptUnitButton;
    private ImageButton deleteUnitButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit);

        Intent intent = getIntent();

        unitIndex = intent.getIntExtra("unitIndex", 0);
        Object unitObject = intent.getSerializableExtra("unit");
        unit = (Unit) unitObject;

        taxonView = findViewById(R.id.taxonTextView);
        taxonIDView = findViewById(R.id.taxonIDTextView);
        countView = findViewById(R.id.editTextCount);
        notesView = findViewById(R.id.editTextNotes);
        recodBasisSpinner = findViewById(R.id.recordBasisSpinner);
        taxonConfidenceSpinner = findViewById(R.id.taxonConfidenceSpinner);
        imageButton = findViewById(R.id.unitImageButton);
        unitImageView = findViewById(R.id.unitImageView);
        acceptUnitButton = findViewById(R.id.acceptUnitButton);
        deleteUnitButton = findViewById(R.id.deleteUnitButton);
        progressBar = findViewById(R.id.progressBar2);

        taxonView.setText(unit.identifications[0].taxon);
        taxonIDView.setText(unit.unitFact.autocompleteSelectedTaxonID);

        countView.setText(unit.count);
        TextWatcher countWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                return;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                unit.count = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                return;
            }
        };
        countView.addTextChangedListener(countWatcher);


        notesView.setText(unit.notes);
        TextWatcher notesWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                return;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                unit.notes = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                return;
            }
        };
        notesView.addTextChangedListener(notesWatcher);

        ArrayAdapter<CharSequence> recordBasisAdapter = new ArrayAdapter<CharSequence>(this, R.layout.item_spinner, R.id.spinnerLabel, Helpers.getLabels(R.raw.record_basis));
        recodBasisSpinner.setAdapter(recordBasisAdapter);
        recodBasisSpinner.setOnItemSelectedListener(this);
        recodBasisSpinner.setSelection(Helpers.getIndex(R.raw.record_basis, unit.recordBasis));

        ArrayAdapter<CharSequence> taxonConfidenceAdapter = new ArrayAdapter<CharSequence>(this, R.layout.item_spinner, R.id.spinnerLabel, Helpers.getLabels(R.raw.taxon_confidence));
        taxonConfidenceSpinner.setAdapter(taxonConfidenceAdapter);
        taxonConfidenceSpinner.setOnItemSelectedListener(this);
        taxonConfidenceSpinner.setSelection(Helpers.getIndex(R.raw.taxon_confidence, unit.taxonConfidence));

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageGallery();
            }
        });

        unitImageView.setVisibility(View.GONE);

        acceptUnitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitUnit();
            }
        });

        deleteUnitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUnit();
            }
        });



        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Log.d(TAG, "onItemSelected: " + parent.getId() + " " + recodBasisSpinner.getId());
        if (parent.getId() == recodBasisSpinner.getId()) {
            unit.setRecordBasis(pos);
        }
        else if (parent.getId() == taxonConfidenceSpinner.getId()) {
            unit.setTaxonConfidence(pos);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "onNothingSelected: " + parent.toString());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {

            //getting the image Uri
            Uri imageUri = data.getData();
            float photoRotation = getOrientation(this, imageUri);
            Log.d(TAG, "onActivityResult: Rotation is: " + photoRotation);


            try {


                progressBar.setVisibility(View.VISIBLE);
                //getting bitmap object from uri
                Bitmap rawBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                Bitmap bitmap = rotateBitmap(rawBitmap, photoRotation);

                //displaying selected image to imageview
                //unitImageView.setImageBitmap(bitmap);


                //calling the method uploadBitmap to upload image
                Communication.SendImage(bitmap, new RequestListener() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray JSONresponse = new JSONArray(response);
                            String id = JSONresponse.getJSONObject(0).getString("id");
                            sendImageMetadata(id);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(String message) {

                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private float getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        int result = -1;
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0);
            }
            cursor.close();
        }

        return (float)result;
    }

    private void openSearchPage(int position) {
        Intent intent = new Intent(this, TaxoSearchActivity.class);
        intent.putExtra("unitIndex", position);
        startActivityForResult(intent, REQUEST_CODE_1);
    }

    private void openImageGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            finish();
            startActivity(intent);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    private void sendImageMetadata(String id) {
        JSONObject metaData = new JSONObject();
        try {
            metaData.put("intellectualRights", "MZ.intellectualRightsCC-BY-SA-4.0");
            metaData.put("intellectualOwner", getUserName());


        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        Map<String, String> parameters = new HashMap<String, String>();
        Communication.SendPostRequest("images/" + id, parameters, metaData, new RequestListener() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, "onResponse: " + response);
                try {
                    JSONObject JSONresponse = new JSONObject(response);
                    String id = JSONresponse.getString("id");
                    addImage(id);
                    String thumbnailUrl = JSONresponse.getString("thumbnailURL");
                    Picasso.get().load(thumbnailUrl).into(unitImageView);
                    progressBar.setVisibility(View.GONE);
                    unitImageView.setVisibility(View.VISIBLE);
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

    }

    private void addImage(String id) {
        unit.addImage(id);
        Log.d(TAG, "addImage: " + unit.images[0]);
    }

    private String getUserName() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sharedPreferences.getString("userName", "defaultName");
        return name;
    }



    private void exitUnit() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("unitIndex", unitIndex);
        returnIntent.putExtra("unit", unit);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private void deleteUnit() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("unitIndex", unitIndex);
        returnIntent.putExtra("unit", unit);
        setResult(RESULT_DELETE, returnIntent);
        finish();
    }
}