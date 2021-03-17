package com.versionhash.watoolkit.model;


import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.versionhash.watoolkit.APIHelper;
import com.versionhash.watoolkit.model.helpers.RuleHelper;
import com.versionhash.watoolkit.model.preferences.PreferencesManager;
import com.versionhash.watoolkit.model.rules.Rule;

import org.json.JSONException;
import org.json.JSONObject;

import static com.versionhash.watoolkit.model.preferences.PreferencesManager.WEBSERVER;

/**
 * Manages user entered custom auto reply text data.
 */
public class CustomRepliesData {
    private static SharedPreferences _sharedPrefs;
    private static CustomRepliesData _INSTANCE;
    private Boolean responseFlag = false;

    private Context context;

    private CustomRepliesData() {
    }

    private CustomRepliesData(Context context) {
        _sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        //_sharedPrefs = context.getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.context = context;
    }

    public static CustomRepliesData getInstance(Context context) {
        if (_INSTANCE == null) {
            _INSTANCE = new CustomRepliesData(context);
        }
        return _INSTANCE;
    }


    public String get(String message_from_friend) {
        RuleHelper ruleHelper = new RuleHelper();
        Rule rule = ruleHelper.identifyRule(message_from_friend);
        responseFlag = false;
        final String[] replyMessage = {""};
        if (rule != null) {
            //all the case will be here to get the message from the server or generate  from the local database
            if (rule.getConditionType().equals(Rule.ANYTHING)) {
                String anything_reply_config = _sharedPrefs.getString(PreferencesManager.KEY_ANYTHING_REPLY_CONFIG, PreferencesManager.STATIC);
                //check the shared preferences for if anything then check which type of the reply ? static or server integration
                if (anything_reply_config.equals(PreferencesManager.STATIC)) {
                    replyMessage[0] = rule.getReplyMsg();
                } else if (anything_reply_config.equals(WEBSERVER)) {
                    //send this message to server
                    JSONObject postData = new JSONObject();
                    try {
                        postData.put("number", "917405136746");
                        postData.put("message", message_from_friend);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    APIHelper apiHelper = new APIHelper(this.context);
                    JSONObject response = apiHelper.newThreadPost(
                            _sharedPrefs.getString(PreferencesManager.KEY_WEB_SERVER_URL, ""),
                            _sharedPrefs.getString(PreferencesManager.KEY_POST_HEADER_KEY, ""),
                            _sharedPrefs.getString(PreferencesManager.KEY_POST_HEADER_VALUE, ""),
                            postData
                    );
                    if (apiHelper.isValidResponse(response)) {
                        try {
                            return response.get("reply").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


//                    apiHelper.volleyPost(_sharedPrefs.getString(PreferencesManager.KEY_WEB_SERVER_URL, ""),
//                            _sharedPrefs.getString(PreferencesManager.KEY_POST_HEADER_KEY, ""),
//                            _sharedPrefs.getString(PreferencesManager.KEY_POST_HEADER_VALUE, ""),
//                            postData,
//                            new VolleyCallback() {
//                                @Override
//                                public void onSuccess(JSONObject response) {
//                                    if (apiHelper.isValidResponse(response)) {
//                                        try {
//                                            replyMessage[0] =  response.get("reply").toString();
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                    } else {
//
//                                    }
//                                    responseFlag = true;
//                                }
//
//                                @Override
//                                public void onError(VolleyError error) {
//                                    responseFlag = true;
//                                }
//                            });
//
//                    waitUntilServerRespond();
                }
            } else {
                return rule.getReplyMsg();
            }
        }
        return "";
    }

    private Boolean waitUntilServerRespond()
    {
        for (int iWait = 0; iWait < 2500;iWait++)
        {
            try{
                Thread.sleep(20);
            }catch (InterruptedException ie)
            {
                ie.printStackTrace();
                break;
            }
            if(responseFlag)
                return responseFlag;
        }
        return responseFlag;
    }

    public String getNew(String message_from_friend) {
        RuleHelper ruleHelper = new RuleHelper();
        Rule rule = ruleHelper.identifyRule(message_from_friend);
        responseFlag = false;
        final String[] replyMessage = {""};
        if (rule != null) {
            //all the case will be here to get the message from the server or generate  from the local database
            if (rule.getConditionType().equals(Rule.ANYTHING)) {
                String anything_reply_config = _sharedPrefs.getString(PreferencesManager.KEY_ANYTHING_REPLY_CONFIG, PreferencesManager.STATIC);
                //check the shared preferences for if anything then check which type of the reply ? static or server integration
                if (anything_reply_config.equals(PreferencesManager.STATIC)) {
                    replyMessage[0] = rule.getReplyMsg();
                } else if (anything_reply_config.equals(WEBSERVER)) {
                    //send this message to server
                    JSONObject postData = new JSONObject();
                    try {
                        postData.put("number", "917405136746");
                        postData.put("message", message_from_friend);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    APIHelper apiHelper = new APIHelper(this.context);
                    JSONObject response = apiHelper.newThreadPost(
                            _sharedPrefs.getString(PreferencesManager.KEY_WEB_SERVER_URL, ""),
                            _sharedPrefs.getString(PreferencesManager.KEY_POST_HEADER_KEY, ""),
                            _sharedPrefs.getString(PreferencesManager.KEY_POST_HEADER_VALUE, ""),
                            postData
                    );
                    if (apiHelper.isValidResponse(response)) {
                        try {
                            return response.get("reply").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


//                    apiHelper.volleyPost(_sharedPrefs.getString(PreferencesManager.KEY_WEB_SERVER_URL, ""),
//                            _sharedPrefs.getString(PreferencesManager.KEY_POST_HEADER_KEY, ""),
//                            _sharedPrefs.getString(PreferencesManager.KEY_POST_HEADER_VALUE, ""),
//                            postData,
//                            new VolleyCallback() {
//                                @Override
//                                public void onSuccess(JSONObject response) {
//                                    if (apiHelper.isValidResponse(response)) {
//                                        try {
//                                            replyMessage[0] =  response.get("reply").toString();
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                    } else {
//
//                                    }
//                                    responseFlag = true;
//                                }
//
//                                @Override
//                                public void onError(VolleyError error) {
//                                    responseFlag = true;
//                                }
//                            });
//
//                    waitUntilServerRespond();
                }
            } else {
                return rule.getReplyMsg();
            }
        }
        return "";
    }
}
