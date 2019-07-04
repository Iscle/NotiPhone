package me.iscle.notiphone.Services;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_POSTED;
import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_REMOVED;

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
        newNotification.putExtra("statusBarNotification", sbn);

        // Send the broadcast
        LocalBroadcastManager.getInstance(this).sendBroadcast(newNotification);

        //Toast.makeText(getApplicationContext(), "onNotificationPosted: " + sbn.getPackageName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationRemoved(sbn, rankingMap);

        Log.d(TAG, "onNotificationRemoved");

        Intent removedNotification = new Intent(BROADCAST_NOTIFICATION_REMOVED);
        removedNotification.putExtra("notificationKey", sbn.getKey());

        LocalBroadcastManager.getInstance(this).sendBroadcast(removedNotification);

        //Toast.makeText(getApplicationContext(), "onNotificationRemoved: " + sbn.getPackageName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        super.onNotificationRankingUpdate(rankingMap);
    }
}
