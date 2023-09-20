package com.tmynttin.taskulaji;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends AppCompatActivity {
    private static final String TAG = "WebActivity";
    public static final String EXTRA_MESSAGE = "com.tmynttin.taskulaji.extra.MESSAGE";
    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        Intent intent = getIntent();
        String url = intent.getStringExtra(EXTRA_MESSAGE);
        Log.d(TAG, "onCreate: url: " + url);

        WebView web = new WebView(this);
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        web.setWebViewClient(new WebViewClient());
        setContentView(web);
        web.loadUrl(url);
    }
}