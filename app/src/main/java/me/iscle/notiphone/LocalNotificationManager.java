package me.iscle.notiphone;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import me.iscle.notiphone.model.PhoneNotification;

public class LocalNotificationManager {
    private static final String TAG = "LocalNotificationManager";

    private static LocalNotificationManager instance;

    private final HashMap<String, PhoneNotification> activeNotifications;
    private final ArrayList<NotificationListener> notificationListeners;

    private LocalNotificationManager() {
        this.activeNotifications = new HashMap<>();
        this.notificationListeners = new ArrayList<>();
    }

    public static LocalNotificationManager getInstance() {
        if (instance == null) {
            synchronized (LocalNotificationManager.class) {
                if (instance == null) {
                    instance = new LocalNotificationManager();
                }
            }
        }

        return instance;
    }

    public Collection<PhoneNotification> getActiveNotifications() {
        return activeNotifications.values();
    }

    public void addActiveNotification(PhoneNotification pn) {
        activeNotifications.put(pn.getId(), pn);
        for (NotificationListener notificationListener : notificationListeners) {
            notificationListener.onNotificationPosted(pn);
        }
    }

    public PhoneNotification getActiveNotification(String id) {
        return activeNotifications.get(id);
    }

    public PhoneNotification removeActiveNotification(String id) {
        PhoneNotification pn = activeNotifications.remove(id);
        if (pn != null) {
            for (NotificationListener notificationListener : notificationListeners) {
                notificationListener.onNotificationRemoved(pn);
            }
        } else {
            Log.w(TAG, "removeActiveNotification: tried to remove an inexisting notification: " + id);
        }
        return pn;
    }

    public void addNotificationObserver(NotificationListener observer) {
        notificationListeners.add(observer);
    }

    public void removeNotificationObserver(NotificationListener observer) {
        notificationListeners.remove(observer);
    }

    public interface NotificationListener {
        void onNotificationPosted(PhoneNotification notification);
        void onNotificationRemoved(PhoneNotification notification);
    }
}
