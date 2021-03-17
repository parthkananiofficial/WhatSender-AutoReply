package com.versionhash.watoolkit.activity.integration;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.versionhash.watoolkit.APIHelper;
import com.versionhash.watoolkit.CommonMethod;
import com.versionhash.watoolkit.R;
import com.versionhash.watoolkit.VolleyCallback;
import com.versionhash.watoolkit.model.preferences.PreferencesManager;

import org.json.JSONException;
import org.json.JSONObject;


public class WebIntegrationActivity extends AppCompatActivity {

    TextInputEditText webURL, headerKey, headerValue;
    MaterialButton testWebConfigBtn, saveWebConfigBtn;
    String number, message, reply;
    APIHelper apiHelper;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_integration);
        apiHelper = new APIHelper(this);
        preferencesManager = PreferencesManager.getPreferencesInstance(this);

        webURL = findViewById(R.id.webURL);
        headerKey = findViewById(R.id.headerKey);
        headerValue = findViewById(R.id.headerValue);

        webURL.setText(preferencesManager.getWebServer());
        headerKey.setText(preferencesManager.getWebServerPostKey());
        headerValue.setText(preferencesManager.getWebServerPostValue());

        testWebConfigBtn = findViewById(R.id.testWebConfigBtn);
        testWebConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //test the with sample data
                //apiHelper.sampleServerAPI(webURL.getText().toString(),headerKey.getText().toString(),headerValue.getText().toString());
                JSONObject postData = new JSONObject();
                try {
                    postData.put("number", "917405136746");
                    postData.put("message", "This is the sample Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                apiHelper.volleyPost(webURL.getText().toString(), headerKey.getText().toString(), headerValue.getText().toString(), postData, new VolleyCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        if (apiHelper.isValidResponse(response)) {
                            CommonMethod.showAlert("Success", WebIntegrationActivity.this);
                        } else {
                            CommonMethod.showAlert("Failed : Response format is not valid", WebIntegrationActivity.this);
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        CommonMethod.showAlert("Failed : " + error.getMessage(), WebIntegrationActivity.this);
                    }
                });
            }
        });


        saveWebConfigBtn = findViewById(R.id.saveWebConfigBtn);
        saveWebConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save to preferenceManager
                preferencesManager.setWebServer(webURL.getText().toString());
                preferencesManager.setWebServerPostKey(headerKey.getText().toString());
                preferencesManager.setWebServerPostValue(headerValue.getText().toString());
                finish();
            }
        });

    }
}