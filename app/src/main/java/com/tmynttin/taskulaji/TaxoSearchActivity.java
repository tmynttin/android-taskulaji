package com.tmynttin.taskulaji;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.SearchView;

import com.tmynttin.taskulaji.adapters.TaxoResultAdapter;
import com.tmynttin.taskulaji.listeners.RecyclerViewClickListener;
import com.tmynttin.taskulaji.listeners.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TaxoSearchActivity extends AppCompatActivity {
    private final String TAG = "TaxoSearchActivity";

    private TaxoResultAdapter taxoAdapter;
    private RecyclerView recyclerView;
    private JSONArray data = new JSONArray();
    private RecyclerViewClickListener recyclerViewClickListener;

    private int unitIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxo_search);

        Intent intent = getIntent();
        unitIndex = intent.getIntExtra("unitIndex", 0);

        SearchView taxoSearchView = (SearchView) findViewById(R.id.taxoSearchView);
        taxoSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length()>2) {
                    ShowTaxons(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return false;
            }
        });
        taxoSearchView.setFocusable(true);
        taxoSearchView.setIconified(false);
        if(taxoSearchView.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        recyclerView = findViewById(R.id.taxoResultRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(TaxoSearchActivity.this));

        recyclerViewClickListener = (view, position) -> {
            Log.d(TAG, "onCreate: unit clicked");
            String taxoName = "";
            String taxoId = "";
            try {
                JSONObject taxo = (JSONObject) taxoAdapter.getLocalDataSet().get(position);
                taxoName = taxo.getString("value");
                taxoId = taxo.getString("key");
                Log.d(TAG, "tried and succeeded");
            }
            catch (JSONException e) {
                Log.d(TAG, "tried and failed");
                e.printStackTrace();
            }

            Intent returnIntent = new Intent();
            returnIntent.putExtra("unitIndex", unitIndex);
            returnIntent.putExtra("taxoName", taxoName);
            returnIntent.putExtra("taxoId", taxoId);
            setResult(RESULT_OK, returnIntent);
            finish();
        };

        taxoAdapter = new TaxoResultAdapter(data, recyclerViewClickListener);
        recyclerView.setAdapter(taxoAdapter);
    }

    public void ShowTaxons(String query) {
        /*
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("q", query);
        }
        catch (JSONException e) {
            Log.d(TAG, "ShowTaxons: JSON error");
            e.printStackTrace();
        }

         */
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("q", query);
        Communication.SendGetRequest("autocomplete/taxon", parameters, new RequestListener() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: " + response);
                JSONArray results = new JSONArray();
                try {
                    results = new JSONArray(response);
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
                taxoAdapter.UpdateDataSet(results);
            }

            @Override
            public void onError(String message) {
                Log.d(TAG, "onError: " + message);
            }
        });
    }
}