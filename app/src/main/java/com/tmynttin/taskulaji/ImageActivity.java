package com.tmynttin.taskulaji;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.tmynttin.taskulaji.listeners.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageActivity extends AppCompatActivity {

    private final String TAG = "ImageActivity";

    //ImageView to display image selected
    ImageView imageView;

    //edittext for getting the tags input
    EditText editTextTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //initializing views
        imageView = (ImageView) findViewById(R.id.imageView);
        editTextTags = (EditText) findViewById(R.id.editTextTags);

        //checking the permission
        //if the permission is not given we will open setting to add permission
        //else app will not open
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            finish();
            startActivity(intent);
            return;
        }

        //adding click listener to button
        findViewById(R.id.buttonUploadImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*
                //if the tags edittext is empty
                //we will throw input error
                if (editTextTags.getText().toString().trim().isEmpty()) {
                    editTextTags.setError("Enter tags first");
                    editTextTags.requestFocus();
                    return;
                }

                 */

                //if everything is ok we will open image chooser
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {

            //getting the image Uri
            Uri imageUri = data.getData();
            try {
                //getting bitmap object from uri
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                //displaying selected image to imageview
                imageView.setImageBitmap(bitmap);

                //calling the method uploadBitmap to upload image
                Communication.SendImage(bitmap, new RequestListener() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray JSONresponse = new JSONArray(response);
                            String id = JSONresponse.getJSONObject(0).getString("id");
                            SendMetadata(id);
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

    private void SendMetadata(String id) {
        JSONObject metaData = new JSONObject();
        try {
            metaData.put("intellectualRights", "MZ.intellectualRightsCC-BY-SA-4.0");
            //metaData.put("capturerVerbatim", new JSONArray("MA.797"));
            metaData.put("intellectualOwner", "MA.797");


        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        Map<String, String> parameters = new HashMap<String, String>();
        Communication.SendPostRequest("images/" + id, parameters, metaData, new RequestListener() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, "onResponse: " + response);
            }

            @Override
            public void onError(String message) {
                Log.d(TAG, "onError: " + message);
            }
        });

    }


}
