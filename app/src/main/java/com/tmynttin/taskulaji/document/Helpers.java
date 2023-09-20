package com.tmynttin.taskulaji.document;

import android.util.Log;

import com.tmynttin.taskulaji.App;
import com.tmynttin.taskulaji.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Helpers {
    private static final String TAG = "Helpers";

    public static JSONArray createJSONArrayFromFile(int fileID) {

        JSONArray result = null;

        try {
            // Read file into string builder
            InputStream inputStream = App.getContext().getResources().openRawResource(fileID);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();

            for (String line = null; (line = reader.readLine()) != null ; ) {
                builder.append(line).append("\n");
            }

            // Parse into JSONObject
            String resultStr = builder.toString();
            JSONTokener tokener = new JSONTokener(resultStr);
            result = new JSONArray(tokener);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static JSONObject createJSONObjectFromFile(int fileID) {
        JSONObject result = null;

        try {
            // Read file into string builder
            InputStream inputStream = App.getContext().getResources().openRawResource(fileID);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();

            for (String line = null; (line = reader.readLine()) != null ; ) {
                builder.append(line).append("\n");
            }

            // Parse into JSONObject
            String resultStr = builder.toString();
            JSONTokener tokener = new JSONTokener(resultStr);
            result = new JSONObject(tokener);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static String[] getLabels(int resource) {
        JSONArray resourceArray = Helpers.createJSONArrayFromFile(resource);
        int len = resourceArray.length();
        List<String> labelsList = new ArrayList<String>();
        for (int i=0; i < len; i++) {
            try {
                String label = resourceArray.getJSONObject(i).getJSONObject("label").getString("fi");
                labelsList.add(label);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String[] labels = labelsList.toArray(new String[len]);
        return labels;
    }

    public static String[] getValues(int resource) {
        JSONObject resourceObject = Helpers.createJSONObjectFromFile(resource);

        JSONArray names = resourceObject.names();

        int len = names.length();
        String[] values = new String[len];

        for (int i=0; i< len; i++) {
            try {
                values[i] = resourceObject.getString(names.getString(i));
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return values;
    }

    public static String getName(int resource, String value) {
        JSONObject resourceObject = Helpers.createJSONObjectFromFile(resource);

        JSONArray names = resourceObject.names();

        int len = names.length();

        for (int i=0; i< len; i++) {
            try {
                String iterName = names.getString(i);
                String iterValue = resourceObject.getString(iterName);
                if (value == iterValue) {
                    return iterName;
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String getProperty(int resource, int index) {
        String property = "";
        try {
            JSONArray resourceArray = Helpers.createJSONArrayFromFile(resource);
            property = resourceArray.getJSONObject(index).getString("property");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return property;
    }

    public static String getValueFromId(int resource, String id) {
        String value = "";

        try {
            JSONArray resourceArray = Helpers.createJSONArrayFromFile(resource);

            int len = resourceArray.length();

            for (int i=0; i < len; i++) {
                JSONObject currentObject = resourceArray.getJSONObject(i);
                String currentId = currentObject.getString("id");

                Log.d(TAG, "getValueFromId: currentId is " + currentId);

                if (currentId.equals(id)) {
                    value = currentObject.getString("value");
                    break;
                }
            }
        }

        catch (JSONException e) {
            e.printStackTrace();
        }

        return value;
    }

    public static int getIndex(int resource, String property) {
        int ret = -1;
        JSONArray resourceArray = Helpers.createJSONArrayFromFile(resource);
        int len = resourceArray.length();
        for (int i=0; i < len; i++) {
            try {
                String propertyFromResource = resourceArray.getJSONObject(i).getString("property");
                if (property.equals(propertyFromResource)) {
                    ret = i;
                }

            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static JSONArray concatArray(JSONArray... arrs)
            throws JSONException {
        JSONArray result = new JSONArray();
        for (JSONArray arr : arrs) {
            for (int i = 0; i < arr.length(); i++) {
                result.put(arr.get(i));
            }
        }
        return result;
    }

}
