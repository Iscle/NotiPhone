package me.iscle.notiphone.service;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import me.iscle.notiphone.model.PhoneNotification;

import static android.app.Notification.EXTRA_TEMPLATE;
import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_POSTED;
import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_RANKING_UPDATE;
import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_REMOVED;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        if (!filterNotification(this, sbn)) return;

        PhoneNotification n = new PhoneNotification(getApplicationContext(), sbn);

        Intent postedNotification = new Intent(BROADCAST_NOTIFICATION_POSTED);
        postedNotification.putExtra("phoneNotification", n.toJson());

        // Send the broadcast
        LocalBroadcastManager.getInstance(this).sendBroadcast(postedNotification);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        if (sbn.getPackageName().equals("android") || sbn.getPackageName().equals(getPackageName())) return;

        Intent removedNotification = new Intent(BROADCAST_NOTIFICATION_REMOVED);
        removedNotification.putExtra("notificationId", PhoneNotification.getId(sbn));

        LocalBroadcastManager.getInstance(this).sendBroadcast(removedNotification);
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        super.onNotificationRankingUpdate(rankingMap);

        Intent updatedNotificationRanking = new Intent(BROADCAST_NOTIFICATION_RANKING_UPDATE);
        updatedNotificationRanking.putExtra("rankingMap", rankingMap);

        LocalBroadcastManager.getInstance(this).sendBroadcast(updatedNotificationRanking);
    }

    public static boolean filterNotification(Context c, StatusBarNotification sbn) {
        Notification n = sbn.getNotification();

        if (sbn.getPackageName().equals("android"))
            return false;

        if (sbn.getPackageName().equals(c.getPackageName()))
            return false;

        if (n.extras.getBoolean("android.contains.customView", false))
            return false;

        //if (!TextUtils.isEmpty(n.extras.getString(EXTRA_TEMPLATE)))
            //return false;

        return true;
    }

}
