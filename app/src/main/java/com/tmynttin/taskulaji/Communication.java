package com.tmynttin.taskulaji;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tmynttin.taskulaji.listeners.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.internal.http.HttpMethod;

public class Communication {
    private static final String TAG = "Communication";
    public static final String API_URL = "https://apitest.laji.fi/v0/";
    public static final String ACCESS_TOKEN = "Q1cVCk7I8sc2PCqIbhMHt1rib2FyZwJF9OhUXmxIIAy6R0bSeKEMWgtq47ecYVYo";
    //public static final String API_URL = "https://api.laji.fi/v0/";
    //public static final String ACCESS_TOKEN = "DFUyMwPOXOBeOrj00dcKGscJqFVgTWsUggikIgU2pPIB9KPjITn94Ly45VOo5C2s";
    public static String test;

    public static void SendGetRequest(String endPoint, Map<String, String> parameters, RequestListener listener) {
        Log.d(TAG, "SendGetRequest: ");
        RequestQueue queue = Volley.newRequestQueue(App.getContext());
        String requestUrl = API_URL + endPoint;// + "?access_token=" + ACCESS_TOKEN;
        parameters.put("access_token", ACCESS_TOKEN);
        parameters.put("personToken", getPersonToken());

        requestUrl += parameterMapToString(parameters);

        Log.d(TAG, "SendGetRequest: " + requestUrl);

        StringRequest request = new StringRequest(Request.Method.GET,
                requestUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        listener.onResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d(TAG, "onErrorResponse: failed to get response");
            }
        }) {
            @Override
            protected Map<String, String> getParams()
            {
                return parameters;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");

                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(0,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }

    public static void SendPostRequest(String endPoint, Map<String, String> parameters, JSONObject data, RequestListener listener) {
        RequestQueue queue = Volley.newRequestQueue(App.getContext());
        String requestUrl = API_URL + endPoint;// + "?access_token=" + ACCESS_TOKEN;
        parameters.put("access_token", ACCESS_TOKEN);
        parameters.put("personToken", getPersonToken());

        requestUrl += parameterMapToString(parameters);

        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        listener.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, "onErrorResponse: " + new String(error.networkResponse.data));
                    }
                }) {
            @Override
            protected Map<String, String> getParams()
            {
                return parameters;
            }

            @Override
            public byte[] getBody() {
                if (data != null) {
                    String stringData = data.toString();
                    try {
                        return stringData == null ? null : stringData.getBytes("utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                else {
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");

                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(0,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }

    public static void SendImage(final Bitmap bitmap, RequestListener listener) {

        Map<String, String> parameters = new HashMap<>();
        String requestUrl = API_URL + "images?" + "access_token=" + ACCESS_TOKEN + "&personToken=" + getPersonToken();

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, requestUrl,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        listener.onResponse(new String(response.data));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        error.printStackTrace();
                        Log.d(TAG, "onErrorResponse: " + new String(error.networkResponse.data));
                    }
                }) {


            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("pic", new DataPart(imagename + ".jpg", getFileDataFromDrawable(bitmap)));
                return params;
            }

        };

        //adding the request to volley
        Volley.newRequestQueue(App.getContext()).add(volleyMultipartRequest);
    }

    /*
     * The method is taking Bitmap as an argument
     * then it will return the byte[] array for the given bitmap
     * and we will send this array to the server
     * here we are using PNG Compression with 80% quality
     * you can give quality between 0 to 100
     * 0 means worse quality
     * 100 means best quality
     * */
    private static byte[] getFileDataFromDrawable(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private static String parametersToRequestString(JSONObject JSONparameters) {
        Log.d(TAG, "parametersToRequestString: into the function");
        int len = JSONparameters.length();
        String returnString = "&";
        for (int i=0; i<len; i++) {
            String key;
            String value;
            try {
                key = JSONparameters.names().getString(i);
                value = JSONparameters.getString(key);
                returnString += key + "=" + value;
                if (i+1 != len) {
                    returnString += "&";
                }
            }
            catch (JSONException e) {
                Log.d(TAG, "parametersToRequestString: something went wrong with JSON");
            }
        }
        Log.d(TAG, "parametersToRequestString: " + returnString);
        return returnString;
    }

    private static String parameterMapToString(Map<String, String> parameterMap) {
        String returnString = "";

        int size = parameterMap.size();

        if (size > 0) {
            returnString += "?";
        }

        int i = size;
        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            if (i < size) {
                returnString += "&";
            }
            returnString += entry.getKey() + "=" + entry.getValue();
            i--;
        }

        return returnString;
    }

    private static String getPersonToken() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        String token = sharedPreferences.getString("personToken", "");
        return token;
    }

    public static void getXenoCantoAudio(String scientificName, RequestListener listener) {
        RequestQueue queue = Volley.newRequestQueue(App.getContext());

        String xenoCantoApiUrl = "https://www.xeno-canto.org/api/2/recordings?query=";
        String requestUrl = xenoCantoApiUrl + scientificName + "+q:A" + "+box:53.956,-7.383,71.856,42.539";

        StringRequest request = new StringRequest(Request.Method.GET, requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: xonoo" + response);
                listener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("query", scientificName);
                return parameters;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");

                return params;
            }
        };

        request.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 3;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        queue.add(request);
    }
}
