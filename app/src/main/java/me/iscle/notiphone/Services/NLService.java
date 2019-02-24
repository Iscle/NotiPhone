package me.iscle.notiphone.Services;

import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_POSTED;

public class NLService extends NotificationListenerService {
    private static final String TAG = "NLService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationPosted(sbn, rankingMap);

        Log.d(TAG, "onNotificationPosted");

        Intent newNotification = new Intent(BROADCAST_NOTIFICATION_POSTED);

        Bundle sbnExtras = sbn.getNotification().extras;

        newNotification.putExtra("notificationId", sbn.getId());
        newNotification.putExtra("notificationPackage", sbn.getPackageName());
        newNotification.putExtra("notificationIsOngoing", sbn.isOngoing());
        newNotification.putExtra("notificationIsClearable", sbn.isClearable());
        newNotification.putExtra("notificationExtras", sbnExtras);

        // Send the broadcast
        LocalBroadcastManager.getInstance(this).sendBroadcast(newNotification);

        Toast.makeText(getApplicationContext(), "onNotificationPosted: " + sbn.getPackageName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap, int reason) {
        super.onNotificationRemoved(sbn, rankingMap, reason);

        Bundle sbnExtras = sbn.getNotification().extras;
        Log.d(TAG, "onNotificationRemoved: " + sbnExtras.getString("android.title"));
        Toast.makeText(getApplicationContext(), "onNotificationRemoved: " + sbn.getPackageName(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        super.onNotificationRankingUpdate(rankingMap);
    }
}
