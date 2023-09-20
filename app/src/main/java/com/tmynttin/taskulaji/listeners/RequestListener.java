package com.tmynttin.taskulaji.listeners;

import org.json.JSONObject;

public interface RequestListener {
    void onResponse(String response);
    void onError(String message);
}
