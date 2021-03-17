package com.versionhash.watoolkit;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface VolleyCallback {
    void onSuccess(JSONObject result);

    void onError(VolleyError error);
}
