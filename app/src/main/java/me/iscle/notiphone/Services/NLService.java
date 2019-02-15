package me.iscle.notiphone.Services;

import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

public class NLService extends NotificationListenerService {
    private static final String TAG = "NLService";

    public static final String BCAST_NOTIFICATION_POSTED = "me.iscle.notiphone.NL.notification_posted";
    public static final String BCAST_NOTIFICATION_REMOVED = "me.iscle.notiphone.NL.notification_removed";
    public static final String BCAST_MUSIC_CHANGED = "me.iscle.notiphone.NL.music_changed";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationPosted(sbn, rankingMap);

        Log.d(TAG, "onNotificationPosted: Received!");

        Intent intent = new Intent();
        intent.setAction(BCAST_NOTIFICATION_POSTED);
        sendBroadcast(intent);

        Bundle sbnExtras = sbn.getNotification().extras;

        Toast.makeText(getApplicationContext(), "onNotificationPosted: " + sbn.getPackageName(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap, int reason) {
        super.onNotificationRemoved(sbn, rankingMap, reason);

        Bundle sbnExtras = sbn.getNotification().extras;
        Log.d(TAG, "onNotificationRemoved: " + sbnExtras.getString("android.title"));
        Toast.makeText(getApplicationContext(), "onNotificationRemoved: " + sbn.getPackageName(), Toast.LENGTH_LONG).show();
    }

}
