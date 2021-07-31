package me.iscle.notiphone.service;

import android.app.Notification;
import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import me.iscle.notiphone.LocalNotificationManager;
import me.iscle.notiphone.model.PhoneNotification;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";

    public static final String EXTRA_CONTAINS_CUSTOM_VIEW = "android.contains.customView";
    public static final String EXTRA_RANKING_MAP = "rankingMap";

    private LocalNotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = LocalNotificationManager.getInstance();
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

        StatusBarNotification[] sbns = getActiveNotifications();
        Log.d(TAG, "onListenerConnected: got " + sbns.length + " notifications...");
        for (StatusBarNotification sbn : sbns) {
            if (shouldDiscardNotification(sbn)) continue;
            try {
                PhoneNotification pn = new PhoneNotification(getApplicationContext(), sbn);
                notificationManager.addActiveNotification(pn);
            } catch (Exception e) {
                Log.e(TAG, "onListenerConnected: Failed to parse notification", e);
            }
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        if (shouldDiscardNotification(sbn)) return;
        try {
            PhoneNotification n = new PhoneNotification(getApplicationContext(), sbn);
            notificationManager.addActiveNotification(n);
        } catch (Exception e) {
            Log.e(TAG, "onNotificationPosted: Failed to parse notification", e);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        if (shouldDiscardNotification(sbn)) return;
        notificationManager.removeActiveNotification(PhoneNotification.getId(sbn));
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        super.onNotificationRankingUpdate(rankingMap);
    }

    private boolean shouldDiscardNotification(final StatusBarNotification sbn) {
        return shouldDiscardNotification(this, sbn);
    }

    public static boolean shouldDiscardNotification(Context context, StatusBarNotification sbn) {
        final Notification n = sbn.getNotification();

        if ("android".equals(sbn.getPackageName()))
            return true;

        if (context.getPackageName().equals(sbn.getPackageName()))
            return true;

        if (n.extras.getBoolean(EXTRA_CONTAINS_CUSTOM_VIEW, false))
            return true;

        //if (!TextUtils.isEmpty(n.extras.getString(EXTRA_TEMPLATE)))
        //return true;

        return false;
    }

}
