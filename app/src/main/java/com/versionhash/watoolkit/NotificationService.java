package com.versionhash.watoolkit;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.preference.PreferenceManager;

import com.android.volley.VolleyError;
import com.versionhash.watoolkit.model.helpers.RuleHelper;
import com.versionhash.watoolkit.model.logs.whatsapp.WhatsappAutoReplyLogs;
import com.versionhash.watoolkit.model.logs.whatsapp.WhatsappAutoReplyLogsDB;
import com.versionhash.watoolkit.model.preferences.PreferencesManager;
import com.versionhash.watoolkit.model.rules.Rule;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;

import static java.lang.Math.max;

public class NotificationService extends NotificationListenerService {
    private final String TAG = NotificationService.class.getSimpleName();
    private final int DELAY_BETWEEN_REPLY_IN_MILLISEC = 1 * 1000;
    private final int DELAY_BETWEEN_NOTIFICATION_RECEIVED_IN_MILLISEC = 10;
    Realm realm;
    private WhatsappAutoReplyLogsDB whatsappAutoReplyLogsDB;
    private static SharedPreferences _sharedPrefs;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (canReply(sbn)) {
            //sendReply(sbn);
            deduceAndSend(sbn);
        }
    }

    private boolean canReply(StatusBarNotification sbn) {
        return isServiceEnabled() &&
                isSupportedPackage(sbn) &&
                //isNewNotification(sbn) &&
                isGroupMessageAndReplyAllowed(sbn) &&
                canSendReplyNow(sbn) &&
                !hasEmptyRemoteInput(sbn);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        realm = Realm.getDefaultInstance();
        //START_STICKY  to order the system to restart your service as soon as possible when it was killed.
        return START_STICKY;
    }

    private void deduceAndSend(StatusBarNotification sbn)
    {
        NotificationWear notificationWear = extractWearNotification(sbn);
        String incomingMessage = sbn.getNotification().extras.getString("android.text");
        identifyRuleAndSendMsg(sbn,notificationWear,incomingMessage);
    }

    private void onlySend(StatusBarNotification sbn,NotificationWear notificationWear,String replyMessage) {
        RemoteInput[] remoteInputs = new RemoteInput[notificationWear.getRemoteInputs().size()];
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle localBundle = new Bundle();//notificationWear.bundle;
        int i = 0;
        for (RemoteInput remoteIn : notificationWear.getRemoteInputs()) {
            remoteInputs[i] = remoteIn;
            // This works. Might need additional parameter to make it for Hangouts? (notification_tag?)
            localBundle.putCharSequence(remoteInputs[i].getResultKey(), replyMessage);
            i++;
        }

        RemoteInput.addResultsToIntent(remoteInputs, localIntent, localBundle);
        try {
            if (notificationWear.getPendingIntent() != null) {
                logReply(sbn.getNotification().extras.getString("android.title"));
                notificationWear.getPendingIntent().send(this, 0, localIntent);
            }
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG, "replyToLastNotification error: " + e.getLocalizedMessage());
        }
    }
    private void sendReply(StatusBarNotification sbn) {
        _sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        NotificationWear notificationWear = extractWearNotification(sbn);
        String incomingMessage = sbn.getNotification().extras.getString("android.text");
        String replyMessage = "";//getCustomMessage(sbn,notificationWear,incomingMessage);

        RemoteInput[] remoteInputs = new RemoteInput[notificationWear.getRemoteInputs().size()];

        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle localBundle = new Bundle();//notificationWear.bundle;
        int i = 0;
        for (RemoteInput remoteIn : notificationWear.getRemoteInputs()) {
            remoteInputs[i] = remoteIn;
            // This works. Might need additional parameter to make it for Hangouts? (notification_tag?)
            localBundle.putCharSequence(remoteInputs[i].getResultKey(), replyMessage);
            i++;
        }

        RemoteInput.addResultsToIntent(remoteInputs, localIntent, localBundle);
        try {
            if (notificationWear.getPendingIntent() != null) {
                logReply(sbn.getNotification().extras.getString("android.title"));
                notificationWear.getPendingIntent().send(this, 0, localIntent);
            }
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG, "replyToLastNotification error: " + e.getLocalizedMessage());
        }
    }

    //unused for now
    private void getDetailsOfNotification(RemoteInput remoteInput) {
        //Some more details of RemoteInput... no idea what for but maybe it will be useful at some point
        String resultKey = remoteInput.getResultKey();
        String label = remoteInput.getLabel().toString();
        Boolean canFreeForm = remoteInput.getAllowFreeFormInput();
        if (remoteInput.getChoices() != null && remoteInput.getChoices().length > 0) {
            String[] possibleChoices = new String[remoteInput.getChoices().length];
            for (int i = 0; i < remoteInput.getChoices().length; i++) {
                possibleChoices[i] = remoteInput.getChoices()[i].toString();
            }
        }
    }

    /**
     * Extract WearNotification with RemoteInputs that can be used to send a response
     *
     * @param statusBarNotification
     * @return
     */
    private NotificationWear extractWearNotification(StatusBarNotification statusBarNotification) {
        //Should work for communicators such:"com.whatsapp", "com.facebook.orca", "com.google.android.talk", "jp.naver.line.android", "org.telegram.messenger"

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender(statusBarNotification.getNotification());
        List<NotificationCompat.Action> actions = wearableExtender.getActions();
        List<RemoteInput> remoteInputs = new ArrayList<>(actions.size());
        PendingIntent pendingIntent = null;
        for (NotificationCompat.Action act : actions) {
            if (act != null && act.getRemoteInputs() != null) {
                for (int x = 0; x < act.getRemoteInputs().length; x++) {
                    RemoteInput remoteInput = act.getRemoteInputs()[x];
                    remoteInputs.add(remoteInput);
                    pendingIntent = act.actionIntent;
                }
            }
        }

        return new NotificationWear(
                statusBarNotification.getPackageName(),
                pendingIntent,
                remoteInputs,
                wearableExtender.getPages(),
                statusBarNotification.getNotification().extras,
                statusBarNotification.getTag(),
                UUID.randomUUID().toString()
        );
    }

    private boolean isSupportedPackage(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        switch (packageName) {
            case SupportedPackageNames.WHATSAPP_PACK_NAME:
                return true;
            default:
                return false;
        }
    }

    private boolean canSendReplyNow(StatusBarNotification sbn) {
        String userId = sbn.getNotification().extras.getString("android.title");
        whatsappAutoReplyLogsDB = WhatsappAutoReplyLogsDB.getInstance(getApplicationContext());
        long timeDelay = PreferencesManager.getPreferencesInstance(this).getAutoReplyDelay();
        return (System.currentTimeMillis() - whatsappAutoReplyLogsDB.logsDao().getLastReplyTimeStamp(userId) >= max(timeDelay, DELAY_BETWEEN_REPLY_IN_MILLISEC));
    }
    private boolean hasEmptyRemoteInput(StatusBarNotification sbn) {
        NotificationWear notificationWear = extractWearNotification(sbn);
        // Possibly transient or non-user notification from WhatsApp like
        // "Checking for new messages" or "WhatsApp web is Active"
        return notificationWear.getRemoteInputs().isEmpty();
    }

    private void logReply(String userId) {
        whatsappAutoReplyLogsDB = WhatsappAutoReplyLogsDB.getInstance(getApplicationContext());
        WhatsappAutoReplyLogs logs = new WhatsappAutoReplyLogs(userId, System.currentTimeMillis());
        whatsappAutoReplyLogsDB.logsDao().logReply(logs);
    }

    private boolean isGroupMessageAndReplyAllowed(StatusBarNotification sbn) {
        if (!sbn.getNotification().extras.getBoolean("android.isGroupConversation")) {
            return true;
        } else {
            return PreferencesManager.getPreferencesInstance(this).isGroupReplyEnabled();
        }
    }

    private boolean isServiceEnabled() {
        return PreferencesManager.getPreferencesInstance(this).isServiceEnabled();
    }

    /*
    This method is used to avoid replying to unreplied notifications
    which are posted again when next message is received
     */
    private boolean isNewNotification(StatusBarNotification sbn) {
        //For apps targeting {@link android.os.Build.VERSION_CODES#N} and above, this time is not shown
        //by default unless explicitly set by the apps hence checking not 0
        return sbn.getNotification().when == 0 ||
                (System.currentTimeMillis() - sbn.getNotification().when) > DELAY_BETWEEN_NOTIFICATION_RECEIVED_IN_MILLISEC;
    }

    /*
        These are the package names of the apps. for which we want to
        listen the notifications
     */
    private static final class SupportedPackageNames {
        public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
    }

    public void identifyRuleAndSendMsg(StatusBarNotification sbn, NotificationWear notificationWear, String message_from_friend) {
        _sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        RuleHelper ruleHelper = new RuleHelper();
        Rule rule = ruleHelper.identifyRule(message_from_friend);
        if (rule != null) {
            //all the case will be here to get the message from the server or generate  from the local database
            if (rule.getResponseMsgSourceCondition().equals(Rule.WEBSERVER)) {
                    //send this message to server
                    JSONObject postData = new JSONObject();
                    try {
                        postData.put("number", sbn.getNotification().extras.getString("android.title"));
                        postData.put("message", message_from_friend);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    APIHelper apiHelper = new APIHelper(this);
                    apiHelper.volleyPost(_sharedPrefs.getString(PreferencesManager.KEY_WEB_SERVER_URL, ""),
                        _sharedPrefs.getString(PreferencesManager.KEY_POST_HEADER_KEY, ""),
                        _sharedPrefs.getString(PreferencesManager.KEY_POST_HEADER_VALUE, ""),
                        postData,
                        new VolleyCallback() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                if (apiHelper.isValidResponse(response)) {
                                    try {
                                        onlySend(sbn,notificationWear,response.get("reply").toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                }
                            }
                            @Override
                            public void onError(VolleyError error) {
                            }
                        });

            }
            else {
                onlySend(sbn,notificationWear,rule.getReplyMsg());
            }
        }
    }
}
