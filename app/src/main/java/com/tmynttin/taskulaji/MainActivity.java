package com.tmynttin.taskulaji;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tmynttin.taskulaji.listeners.RequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.tmynttin.taskulaji.MESSAGE";
    public static final String TAG = "MainActivity";
    private boolean loggedIn = false;
    private Timer loginCheckTimer = new Timer();
    private TextView personIdView;
    private TextView personNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        personIdView = (TextView)findViewById(R.id.person_id);
        personNameView = (TextView)findViewById(R.id.person_name);

        getPersonData();
    }

    public void openNewObservationPage(View view) {
        Intent intent = new Intent(this, NewObservationActivity.class);
        startActivity(intent);
    }

    public void openTaxonInfoPage(View view) {
        Intent intent = new Intent(this, TaxonInfoActivity.class);
        startActivity(intent);
    }

    public void openMapPage(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    public void openNewsPage(View view) {
        Intent intent = new Intent(this, NewsPageActivity.class);
        startActivity(intent);
    }

    public void openSettingsPage(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openTestPage(View view) {
        Intent intent = new Intent(this, DispersionMapActivity.class);
        startActivity(intent);
    }

    public void loginToLaji(View view) {
        Map<String, String> parameters = new HashMap<String, String>();
        Communication.SendGetRequest("login", parameters, new RequestListener() {

            @Override
            public void onResponse(String responseString) {
                try {
                    JSONObject response = new JSONObject(responseString);
                    String tmpToken = response.getString("tmpToken");
                    String loginURL = response.getString("loginURL");

                    openLoginPage(loginURL, tmpToken);
                }
                catch (JSONException e) {
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    public void openLoginPage(String loginURL, String tmpToken) {
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra(WebActivity.EXTRA_MESSAGE, loginURL);
        startActivity(intent);
        loginCheckTimer.schedule(new checkLogin(this, tmpToken), 5000, 5000);
    }

    public void finalizeLogin(String token) {
        loginCheckTimer.cancel();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("personToken", token).commit();
        WebActivity.activity.finish();

        getPersonData();
    }

    private void getPersonData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = sharedPreferences.getString("personToken", "");

        if (token != "") {
            /*
            JSONObject parameters = new JSONObject();
            try {
                parameters.put("personToken", token);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

             */
            Map<String, String> parameters = new HashMap<String, String>();
            //parameters.put("personToken", token);
            Communication.SendGetRequest("person/" + token, parameters, new RequestListener() {

                @Override
                public void onResponse(String responseString) {
                    JSONObject response = new JSONObject();
                    try {
                        response = new JSONObject(responseString);
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }

                    String id = response.optString("id");
                    personIdView.setText(id);
                    String name = response.optString("fullName");
                    personNameView.setText(name);

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    sharedPreferences.edit().putString("userId", id).commit();
                    sharedPreferences.edit().putString("userName", name).commit();
                }

                @Override
                public void onError(String message) {
                    Log.d(TAG, "onError: " + message);
                }
            });
        }
    }

    private Context getContext() {
        return this;
    }

    private class checkLogin extends TimerTask {
        private String tmpToken;
        private Context context;

        public checkLogin (Context context, String tmpToken) {
            this.tmpToken = tmpToken;
            this.context = context;
        }

        public void run() {
            Map<String, String> parameters = new HashMap<String, String>();
            //JSONObject parameters = new JSONObject();
            parameters.put("tmpToken", tmpToken);

            Communication.SendPostRequest("login/check", parameters, null, new RequestListener() {

                @Override
                public void onResponse(String responseString) {
                    JSONObject response = new JSONObject();
                    try {
                        response = new JSONObject(responseString);
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }
                    String token = response.optString("token");
                    Log.d(TAG, "onResponse: " + token);
                    finalizeLogin(token);
                }

                @Override
                public void onError(String message) {
                    Log.d(TAG, "onError: " + message);
                }
            });
        }
    }
}