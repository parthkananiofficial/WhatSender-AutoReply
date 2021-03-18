package com.versionhash.watoolkit.model.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class PreferencesManager {
    public static final String KEY_WEB_SERVER_URL = "web_server_url";
    public static final String KEY_POST_HEADER_KEY = "web_server_post_header_key";
    public static final String KEY_POST_HEADER_VALUE = "web_server_post_header_value";
    public static final String KEY_ANYTHING_REPLY_CONFIG = "anything_reply_config";
    private static PreferencesManager _instance;
    public final String KEY_SERVICE_ENABLED = "pref_service_enabled";
    public final String KEY_GROUP_REPLY_ENABLED = "pref_group_reply_enabled";
    public final String KEY_AUTO_REPLY_THROTTLE_TIME_MS = "pref_auto_reply_throttle_time_ms";
    private final SharedPreferences _sharedPrefs;

    private PreferencesManager(Context context) {
        _sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferencesManager getPreferencesInstance(Context context) {
        if (_instance == null) {
            _instance = new PreferencesManager(context.getApplicationContext());
        }
        return _instance;
    }

    public boolean isServiceEnabled() {
        return _sharedPrefs.getBoolean(KEY_SERVICE_ENABLED, false);
    }

    public void setServicePref(boolean enabled) {
        SharedPreferences.Editor editor = _sharedPrefs.edit();
        editor.putBoolean(KEY_SERVICE_ENABLED, enabled);
        editor.apply();
    }

    public boolean isGroupReplyEnabled() {
        return _sharedPrefs.getBoolean(KEY_GROUP_REPLY_ENABLED, false);
    }

    public void setGroupReplyPref(boolean enabled) {
        SharedPreferences.Editor editor = _sharedPrefs.edit();
        editor.putBoolean(KEY_GROUP_REPLY_ENABLED, enabled);
        editor.apply();
    }

    public long getAutoReplyDelay() {
        return _sharedPrefs.getLong(KEY_AUTO_REPLY_THROTTLE_TIME_MS, 0);
    }

    public void setAutoReplyDelay(long delay) {
        SharedPreferences.Editor editor = _sharedPrefs.edit();
        editor.putLong(KEY_AUTO_REPLY_THROTTLE_TIME_MS, delay);
        editor.apply();
    }

    public String getWebServer() {
        return _sharedPrefs.getString(KEY_WEB_SERVER_URL, "");
    }

    public void setWebServer(String url) {
        SharedPreferences.Editor editor = _sharedPrefs.edit();
        editor.putString(KEY_WEB_SERVER_URL, url);
        editor.apply();
    }

    public String getWebServerPostKey() {
        return _sharedPrefs.getString(KEY_POST_HEADER_KEY, "");
    }

    public void setWebServerPostKey(String key) {
        SharedPreferences.Editor editor = _sharedPrefs.edit();
        editor.putString(KEY_POST_HEADER_KEY, key);
        editor.apply();
    }

    public String getWebServerPostValue() {
        return _sharedPrefs.getString(KEY_POST_HEADER_VALUE, "");
    }

    public void setWebServerPostValue(String value) {
        SharedPreferences.Editor editor = _sharedPrefs.edit();
        editor.putString(KEY_POST_HEADER_VALUE, value);
        editor.apply();
    }

}
