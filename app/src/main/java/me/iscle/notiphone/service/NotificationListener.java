package me.iscle.notiphone.service;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import me.iscle.notiphone.model.PhoneNotification;

import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_POSTED;
import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_RANKING_UPDATE;
import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_REMOVED;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        if (sbn.getPackageName().equals("android") || sbn.getPackageName().equals("me.iscle.notiphone")) return;

        PhoneNotification n = new PhoneNotification(getApplicationContext(), sbn);

        Intent postedNotification = new Intent(BROADCAST_NOTIFICATION_POSTED);
        postedNotification.putExtra("phoneNotification", n);

        // Send the broadcast
        LocalBroadcastManager.getInstance(this).sendBroadcast(postedNotification);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        if (sbn.getPackageName().equals("android") || sbn.getPackageName().equals("me.iscle.notiphone")) return;

        Intent removedNotification = new Intent(BROADCAST_NOTIFICATION_REMOVED);
        removedNotification.putExtra("notificationId", sbn.getId());

        LocalBroadcastManager.getInstance(this).sendBroadcast(removedNotification);
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        super.onNotificationRankingUpdate(rankingMap);

        Intent updatedNotificationRanking = new Intent(BROADCAST_NOTIFICATION_RANKING_UPDATE);
        updatedNotificationRanking.putExtra("rankingMap", rankingMap);

        LocalBroadcastManager.getInstance(this).sendBroadcast(updatedNotificationRanking);
    }

}
