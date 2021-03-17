package com.versionhash.watoolkit;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class APIHelper {
    private RequestQueue requestQueue;
    Context context;
    private String TAG = "API_HELPER";
    private String url = "";
    private String header_key = "";
    private String header_value = "";
    private JSONObject postData = null;
    public APIHelper(Context context) {
        this.context = context;
    }

    public void volleyPost(String postUrl, String header_key, String header_value, JSONObject postData, final VolleyCallback callback) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, postUrl, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                if(isTest)
//                {
//                    if(isValidResponse(response)){
//                        CommonMethod.showAlert("Success",(Activity) context);
//                    }else{
//                        CommonMethod.showAlert("Failed : Response format is not valid",(Activity) context);
//                    }
//                }else{
                callback.onSuccess(response);
//                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //error.printStackTrace();
                callback.onError(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (!header_key.isEmpty() && !header_value.isEmpty())
                    headers.put(header_key, header_value);
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }


    public JSONObject simplePost(String postUrl, String header_key, String header_value, JSONObject postData)
    {
        JSONObject response = null;
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl,  postData, future, future){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (!header_key.isEmpty() && !header_value.isEmpty())
                    headers.put(header_key, header_value);
                return headers;
            }
        };
        requestQueue.add(request);
        try {
            response = future.get(3, TimeUnit.SECONDS); // Blocks for at most 10 seconds.
        } catch (InterruptedException e) {
            Log.d(TAG,"interrupted");
        } catch (ExecutionException e) {
            Log.d(TAG,"execution");
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return response;
    }

    public JSONObject newThreadPost(String postUrl, String header_key, String header_value, JSONObject postData)
    {
        this.url = postUrl;
        this.header_key = header_key;
        this.header_value = header_value;
        this.postData = postData;

        requestQueue = Volley.newRequestQueue(context);
        ThreadA threadA = new ThreadA();
        JSONObject result =null;
        try {
            try {

                try {
                    result = threadA.execute().get(10, TimeUnit.SECONDS);
                    assert result != null;
                } catch (TimeoutException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            } catch (InterruptedException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        } catch (ExecutionException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return result;
    }

    public boolean isValidResponse(JSONObject response) {
        //validation conditions will be here
        if (response != null && response.has("reply") && !response.isNull("reply")) {
            return true;
        } else {
            return false;
        }
    }

    class ThreadA extends AsyncTask<Void, Void, JSONObject> {

        public ThreadA() {
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    url,
                    postData,
                    future, future){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    if (!header_key.isEmpty() && !header_value.isEmpty())
                        headers.put(header_key, header_value);
                    return headers;
                }
            };

            requestQueue.add(request);

            try {
                try {
                    int REQUEST_TIMEOUT = 100;
                    try {
                        return future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                } catch (ExecutionException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            } catch (InterruptedException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            return null;
        }
    }
}
