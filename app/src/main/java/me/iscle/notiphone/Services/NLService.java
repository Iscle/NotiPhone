package me.iscle.notiphone.Services;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

public class NLService extends NotificationListenerService {
    private static final String TAG = "NLService";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationPosted(sbn, rankingMap);

        Log.d(TAG, "onNotificationPosted: Received!");

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
