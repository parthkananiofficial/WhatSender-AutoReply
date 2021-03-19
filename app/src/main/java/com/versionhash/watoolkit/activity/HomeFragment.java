package com.versionhash.watoolkit.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.versionhash.watoolkit.NotificationService;
import com.versionhash.watoolkit.R;
import com.versionhash.watoolkit.activity.integration.WebIntegrationActivity;
import com.versionhash.watoolkit.activity.whatsdirect.WhatsDirectActivity;
import com.versionhash.watoolkit.activity.whatsweb.WhatsWebActivity;
import com.versionhash.watoolkit.model.preferences.PreferencesManager;
import com.versionhash.watoolkit.model.utils.Constants;
import com.versionhash.watoolkit.model.utils.CustomDialog;

import static android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private static final int REQ_NOTIFICATION_LISTENER = 100;
    private final int MINUTE_FACTOR = 60;
    CardView whatsDirectCard, whatsWebCard, serverConfigCardView, card_viewRateUs, shareCardView, writeUsCardView;

    String autoReplyTextPlaceholder;
    TextView mainAutoReplySwitch;
    ImageView powerSwitch;
    private PreferencesManager preferencesManager;
    private InterstitialAd mInterstitialAd;

    private View view;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        preferencesManager = PreferencesManager.getPreferencesInstance(getActivity());

        // Assign Views
        mainAutoReplySwitch = view.findViewById(R.id.mainAutoReplySwitch);
        shareCardView = view.findViewById(R.id.shareCardView);
        writeUsCardView = view.findViewById(R.id.writeUsCardView);

        writeUsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"versionhash@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "User Review");
                intent.putExtra(Intent.EXTRA_TEXT, "Write your Opinion here");
                startActivity(Intent.createChooser(intent, ""));
            }
        });
        autoReplyTextPlaceholder = getResources().getString(R.string.mainAutoReplyTextPlaceholder);
        card_viewRateUs = view.findViewById(R.id.card_viewRateUs);
        card_viewRateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = "com.versionhash.watoolkit"; // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });

        serverConfigCardView = view.findViewById(R.id.serverConfigCardView);
        serverConfigCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WebIntegrationActivity.class);
                startActivity(intent);
            }
        });
        whatsDirectCard = view.findViewById(R.id.whatsDirectCardView);
        whatsWebCard = view.findViewById(R.id.whatsWebCardView);
        whatsDirectCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WhatsDirectActivity.class);
                startActivity(intent);
            }
        });
        whatsWebCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WhatsWebActivity.class);
                startActivity(intent);
            }
        });
        powerSwitch = view.findViewById(R.id.powerSwitch);
        powerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isListenerEnabled(getActivity(), NotificationService.class)) {
                    showPermissionsDialog();
                } else if (!preferencesManager.isServiceEnabled()) {
                    enableService(true);
                    mainAutoReplySwitch.setText(R.string.mainAutoReplySwitchOnLabel);
                    preferencesManager.setServicePref(true);
                    setSwitchState();
                    //preferencesManager.setGroupReplyPref(true);
                    showInterstitial();
                } else {
                    enableService(false);
                    mainAutoReplySwitch.setText(R.string.mainAutoReplySwitchOffLabel);
                    preferencesManager.setServicePref(false);
                    setSwitchState();
                    showInterstitial();
                }
            }
        });
        shareCardView.setOnClickListener(v -> launchShareIntent());
        loadInterstitial();
        return view;
    }

    private void showInterstitial()
    {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(getActivity());
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
            loadInterstitial();
        }
    }
    private void loadInterstitial()
    {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(getActivity(),"ca-app-pub-3940256099942544/1033173712", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                Log.i("TAG", "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i("TAG", loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        //If user directly goes to Settings and removes notifications permission
        //when app is launched check for permission and set appropriate app state
        if (!isListenerEnabled(getActivity(), NotificationService.class)) {
            preferencesManager.setServicePref(false);
        }

        if (!preferencesManager.isServiceEnabled()) {
            enableService(false);
        }
        setSwitchState();
    }

//    private void setSwitchState(){
//        mainAutoReplySwitch.setChecked(preferencesManager.isServiceEnabled());
//    }

    private void setSwitchState() {
        if (preferencesManager.isServiceEnabled()) {
            powerSwitch.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.notification_turned_on));
        } else {
            powerSwitch.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.notification_not_on));
        }
    }

    //https://stackoverflow.com/questions/20141727/check-if-user-has-granted-notificationlistener-access-to-my-app/28160115
    //TODO: Use in UI to verify if it needs enabling or restarting
    public boolean isListenerEnabled(Context context, Class notificationListenerCls) {
        ComponentName cn = new ComponentName(context, notificationListenerCls);
        String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    private void showPermissionsDialog() {
        CustomDialog customDialog = new CustomDialog(getActivity());
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PERMISSION_DIALOG_TITLE, getString(R.string.permission_dialog_title));
        bundle.putString(Constants.PERMISSION_DIALOG_MSG, getString(R.string.permission_dialog_msg));
        customDialog.showDialog(bundle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == -2) {
                    //Decline
                    showPermissionDeniedDialog();
                } else {
                    //Accept
                    launchNotificationAccessSettings();
                }
            }
        });
    }

    private void showPermissionDeniedDialog() {
        CustomDialog customDialog = new CustomDialog(getActivity());
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PERMISSION_DIALOG_DENIED_TITLE, getString(R.string.permission_dialog_denied_title));
        bundle.putString(Constants.PERMISSION_DIALOG_DENIED_MSG, getString(R.string.permission_dialog_denied_msg));
        bundle.putBoolean(Constants.PERMISSION_DIALOG_DENIED, true);
        customDialog.showDialog(bundle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == -2) {
                    //Decline
                    setSwitchState();
                } else {
                    //Accept
                    launchNotificationAccessSettings();
                }
            }
        });
    }

    public void launchNotificationAccessSettings() {
        enableService(true);//we need to enable the service for it so show in settings

        final String NOTIFICATION_LISTENER_SETTINGS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            NOTIFICATION_LISTENER_SETTINGS = ACTION_NOTIFICATION_LISTENER_SETTINGS;
        } else {
            NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
        }
        Intent i = new Intent(NOTIFICATION_LISTENER_SETTINGS);
        startActivityForResult(i, REQ_NOTIFICATION_LISTENER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_NOTIFICATION_LISTENER) {
            if (isListenerEnabled(getActivity(), NotificationService.class)) {
                Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_LONG).show();
                preferencesManager.setServicePref(true);
                setSwitchState();
            } else {
                Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_LONG).show();
                preferencesManager.setServicePref(false);
                setSwitchState();
            }
        }
    }

    private void enableService(boolean enable) {
        PackageManager packageManager = getActivity().getPackageManager();
        ComponentName componentName = new ComponentName(getActivity(), NotificationService.class);
        int settingCode = enable
                ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        // enable dummyActivity (as it is disabled in the manifest.xml)
        packageManager.setComponentEnabledSetting(componentName, settingCode, PackageManager.DONT_KILL_APP);

    }

    private void launchShareIntent() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_app_text));
        startActivity(Intent.createChooser(sharingIntent, "Share app via"));
    }


}