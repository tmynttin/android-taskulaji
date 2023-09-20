package com.tmynttin.taskulaji;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tmynttin.taskulaji.adapters.NewsAdapter;
import com.tmynttin.taskulaji.listeners.RecyclerViewClickListener;
import com.tmynttin.taskulaji.listeners.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsPageActivity extends AppCompatActivity {
    private static final String TAG = "NewsPage";

    NewsAdapter adapter;
    RecyclerView recyclerView;

    JSONArray lista = new JSONArray();
    RecyclerViewClickListener listy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_page);

        recyclerView = findViewById(R.id.newsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(NewsPageActivity.this));

        listy = (view, position) -> {
            String title = "";
            String content = "";
            String url = "";
            String external = "false";

            try {
                title = ((JSONObject)adapter.getLocalDataSet().get(position)).getString("title");

                external = ((JSONObject)adapter.getLocalDataSet().get(position)).getString("external");
                if (external == "true") {
                    url = ((JSONObject) adapter.getLocalDataSet().get(position)).getString("externalURL");
                    Intent intent = new Intent(this, WebActivity.class);
                    intent.putExtra(WebActivity.EXTRA_MESSAGE, url);
                    startActivity(intent);
                }
                else {
                    content = ((JSONObject) adapter.getLocalDataSet().get(position)).getString("content");
                    Intent intent = new Intent(this, TextActivity.class);
                    intent.putExtra(TextActivity.EXTRA_NEWS_TITLE, title);
                    intent.putExtra(TextActivity.EXTRA_NEWS_CONTENT, content);
                    startActivity(intent);
                }
            }
            catch (JSONException e) {
                Log.d(TAG, "onCreate: JSON problems" +e.toString());
            }
            Log.d(TAG, "onResponse: lambda kutsuttu: " + url);

        };

        adapter = new NewsAdapter(lista, listy);
        recyclerView.setAdapter(adapter);

        //JSONObject params = new JSONObject();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("lang", "fi");
        JSONArray response = new JSONArray();
        Communication.SendGetRequest("news", parameters, new RequestListener() {

            @Override
            public void onResponse(String responseString) {
                JSONObject response = new JSONObject();
                try {
                    response = new JSONObject(responseString);
                }
                catch (JSONException e){
                    e.printStackTrace();
                }

                List<String> newsTitles = new ArrayList<>();
                try {
                    JSONArray results = response.getJSONArray("results");
                    adapter.UpdateDataSet(results);
                }
                catch (JSONException e) {
                    Log.d(TAG, "onResponse: invalid JSON");
                }
            }

            @Override
            public void onError(String message) {

            }
        });

        Log.d(TAG, "onCreate: " + response.toString());



    }
}