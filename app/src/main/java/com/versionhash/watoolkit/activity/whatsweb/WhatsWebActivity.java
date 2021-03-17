package com.versionhash.watoolkit.activity.whatsweb;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.versionhash.watoolkit.R;

import java.util.Arrays;
import java.util.Locale;

public class WhatsWebActivity extends AppCompatActivity {

    private static final String CHROME_FULL = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36";
    private static final String USER_AGENT = CHROME_FULL;
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA; // "android.permission.CAMERA";
    private static final String AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO; // "android.permission.RECORD_AUDIO";
    private static final String[] VIDEO_PERMISSION = {CAMERA_PERMISSION, AUDIO_PERMISSION};
    private static final String WHATSAPP_HOMEPAGE_URL = "https://www.whatsapp.com/";
    private static final String WHATSAPP_WEB_BASE_URL = "web.whatsapp.com";
    private static final String WORLD_ICON = "\uD83C\uDF10";
    private static final String WHATSAPP_WEB_URL = "https://" + WHATSAPP_WEB_BASE_URL
            + "/" + WORLD_ICON + "/"
            + Locale.getDefault().getLanguage();
    private static final int FILECHOOSER_RESULTCODE = 200;
    private static final int CAMERA_PERMISSION_RESULTCODE = 201;
    private static final int AUDIO_PERMISSION_RESULTCODE = 202;
    private static final int VIDEO_PERMISSION_RESULTCODE = 203;
    private static final int STORAGE_PERMISSION_RESULTCODE = 204;
    private static final String DEBUG_TAG = "WAWEBTOGO";
    private final Activity activity = this;
    boolean mKeyboardEnabled = true;
    boolean mDarkMode;
    private WebView mWebView;
    private SharedPreferences mSharedPrefs;
    private ViewGroup mMainView;
    private long mLastBackClick = 0;
    private ValueCallback<Uri[]> mUploadMessage;
    private PermissionRequest mCurrentPermissionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats_web);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        mSharedPrefs = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        mDarkMode = mSharedPrefs.getBoolean("darkMode", false);

        mMainView = findViewById(R.id.layout);

        // webview stuff

        mWebView = findViewById(R.id.webview);

        mWebView.getSettings().setJavaScriptEnabled(true); //for wa web
        mWebView.getSettings().setAllowContentAccess(true); // for camera
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false); //for audio messages

        mWebView.getSettings().setDomStorageEnabled(true); //for html5 app
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(false); // deprecated
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);

        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);

        mWebView.getSettings().setSaveFormData(true);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setBlockNetworkImage(false);
        mWebView.getSettings().setBlockNetworkLoads(false);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setNeedInitialFocus(false);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setScrollbarFadingEnabled(true);

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                Toast.makeText(getApplicationContext(), "OnCreateWindow", Toast.LENGTH_LONG).show();
                return true;
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (request.getResources()[0].equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_DENIED
                            && ContextCompat.checkSelfPermission(activity, AUDIO_PERMISSION) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(activity, VIDEO_PERMISSION, VIDEO_PERMISSION_RESULTCODE);
                        mCurrentPermissionRequest = request;
                    } else if (ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(activity, new String[]{CAMERA_PERMISSION}, CAMERA_PERMISSION_RESULTCODE);
                        mCurrentPermissionRequest = request;
                    } else if (ContextCompat.checkSelfPermission(activity, AUDIO_PERMISSION) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(activity, new String[]{AUDIO_PERMISSION}, AUDIO_PERMISSION_RESULTCODE);
                        mCurrentPermissionRequest = request;
                    } else {
                        request.grant(request.getResources());
                    }
                } else if (request.getResources()[0].equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(activity, AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(request.getResources());
                    } else {
                        ActivityCompat.requestPermissions(activity, new String[]{AUDIO_PERMISSION}, AUDIO_PERMISSION_RESULTCODE);
                        mCurrentPermissionRequest = request;
                    }
                } else {
                    try {
                        request.grant(request.getResources());
                    } catch (RuntimeException e) {
                        Log.d(DEBUG_TAG, "Granting permissions failed", e);
                    }
                }
            }

            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(DEBUG_TAG, "WebView console message: " + cm.message());
                return super.onConsoleMessage(cm);
            }

            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadMessage = filePathCallback;
                Intent chooserIntent = fileChooserParams.createIntent();
                WhatsWebActivity.this.startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                return true;
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (mDarkMode) {
                    addDarkMode(view);
                }
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                if (mDarkMode) {
                    addDarkMode(view);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.scrollTo(0, 0);

                if (mDarkMode) {
                    addDarkMode(view);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                Log.d(DEBUG_TAG, url.toString());

                if (url.toString().equals(WHATSAPP_HOMEPAGE_URL)) {
                    // when whatsapp somehow detects that waweb is running on a phone (e.g. trough
                    // the user agent, but apparently somehow else), it automatically redicts to the
                    // WHATSAPP_HOMEPAGE_URL. It's higly unlikely that a user wants to visit the
                    // WHATSAPP_HOMEPAGE_URL from within waweb.
                    // -> block the request and reload waweb
                    showToast("WA Web has to be reloaded to keep the app running");
                    loadWhatsapp();
                    return true;
                } else if (url.getHost().equals(WHATSAPP_WEB_BASE_URL)) {
                    // whatsapp web request -> fine
                    return super.shouldOverrideUrlLoading(view, request);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, url);
                    startActivity(intent);
                    return true;
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                String msg = String.format("Error: %s - %s", error.getErrorCode(), error.getDescription());
                Log.d(DEBUG_TAG, msg);
            }

            public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
                Log.d(DEBUG_TAG, "Unhandled key event: " + event.toString());
            }
        });

        if (savedInstanceState == null) {
            loadWhatsapp();
        } else {
            Log.d(DEBUG_TAG, "savedInstanceState is present");
        }

        mWebView.getSettings().setUserAgentString(USER_AGENT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        mKeyboardEnabled = mSharedPrefs.getBoolean("keyboardEnabled", true);
        setAppbarEnabled(mSharedPrefs.getBoolean("appbarEnabled", true));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case VIDEO_PERMISSION_RESULTCODE:
                if (permissions.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mCurrentPermissionRequest.grant(mCurrentPermissionRequest.getResources());
                    } catch (RuntimeException e) {
                        Log.e(DEBUG_TAG, "Granting permissions failed", e);
                    }
                } else {
                    showSnackbar("Permission not granted, can't use video.");
                    mCurrentPermissionRequest.deny();
                }
                break;
            case CAMERA_PERMISSION_RESULTCODE:
            case AUDIO_PERMISSION_RESULTCODE:
                //same same
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mCurrentPermissionRequest.grant(mCurrentPermissionRequest.getResources());
                    } catch (RuntimeException e) {
                        Log.e(DEBUG_TAG, "Granting permissions failed", e);
                    }
                } else {
                    showSnackbar("Permission not granted, can't use " +
                            (requestCode == CAMERA_PERMISSION_RESULTCODE ? "camera" : "microphone"));
                    mCurrentPermissionRequest.deny();
                }
                break;
            case STORAGE_PERMISSION_RESULTCODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO: check for current download and enqueue it
                } else {
                    showSnackbar("Permission not granted, can't download to storage");
                }
                break;
            default:
                Log.d(DEBUG_TAG, "Got permission result with unknown request code " +
                        requestCode + " - " + Arrays.asList(permissions).toString());
        }
        mCurrentPermissionRequest = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mWebView.restoreState(savedInstanceState);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILECHOOSER_RESULTCODE:
                if (resultCode == RESULT_CANCELED || data.getData() == null) {
                    mUploadMessage.onReceiveValue(null);
                } else {
                    Uri result = data.getData();
                    Uri[] results = new Uri[1];
                    results[0] = result;
                    mUploadMessage.onReceiveValue(results);
                }
                break;
            default:
                Log.d(DEBUG_TAG, "Got activity result with unknown request code " +
                        requestCode + " - " + data.toString());
        }
    }

    private void showPopupDialog(String message) {
        final SpannableString msg = new SpannableString(message);
        Linkify.addLinks(msg, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Ok", null);
        AlertDialog alert = builder.create();
        alert.show();
        ((TextView) alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void showPopupDialog(int resId) {
        showPopupDialog(getString(resId));
    }

    private void showToast(String msg) {
        this.runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
    }

    private void showSnackbar(String msg) {
        this.runOnUiThread(() -> {
            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg, 900);
            snackbar.setAction("dismiss", (View view) -> snackbar.dismiss());
            snackbar.setActionTextColor(Color.parseColor("#075E54"));
            snackbar.show();
        });
    }

    private void setAppbarEnabled(boolean enable) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (enable) {
                actionBar.show();
            } else {
                actionBar.hide();
            }
            mSharedPrefs.edit().putBoolean("appbarEnabled", enable).apply();
        }
    }

    private void toggleDarkMode() {
        boolean currentState = mSharedPrefs.getBoolean("darkMode", false);
        mSharedPrefs.edit().putBoolean("darkMode", !currentState).apply();

        Log.d(DEBUG_TAG, "Dark Mode Enabled: " + !currentState);
        recreate();
    }


    @Override
    public void onBackPressed() {
        //close drawer if open and impl. press back again to leave

        if (System.currentTimeMillis() - mLastBackClick < 1100) {
            finishAffinity();
        } else {
            mWebView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE));
            showToast("Click back again to close");
            mLastBackClick = System.currentTimeMillis();
        }
    }

    private void loadWhatsapp() {
        mWebView.getSettings().setUserAgentString(USER_AGENT);
        mWebView.loadUrl(WHATSAPP_WEB_URL);
    }

    public void addDarkMode(final WebView mWebView) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.BLACK);
            getWindow().setStatusBarColor(Color.BLACK);
            try {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            } catch (NullPointerException ignored) {
            }
        }

        mWebView.loadUrl("javascript:(" +
                "function(){ " +
                "try {  document.body.classList.add('dark') } catch(err) { }" +
                "})()");
    }
}