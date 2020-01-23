package me.iscle.notiphone;

import java.util.HashMap;
import java.util.Map;

import me.iscle.notiphone.model.PhoneNotification;

public class LocalNotificationManager {
    private static final String TAG = "LocalNotificationManager";

    private static volatile LocalNotificationManager instance;

    private Map<String, PhoneNotification> activeNotifications;

    private LocalNotificationManager() {
        if (instance != null)
            throw new RuntimeException("Use getInstance() to get an instance of this class!");

        activeNotifications = new HashMap<>();
    }

    public static LocalNotificationManager getInstance() {
        if (instance == null)
            synchronized (LocalNotificationManager.class) {
                if (instance == null)
                    instance = new LocalNotificationManager();
            }

        return instance;
    }

    public Map<String, PhoneNotification> getActiveNotifications() {
        return activeNotifications;
    }

    public void putActiveNotification(PhoneNotification pn) {
        activeNotifications.put(pn.getId(), pn);
    }

    public PhoneNotification getActiveNotification(String id) {
        return activeNotifications.get(id);
    }

    public PhoneNotification removeActiveNotification(String id) {
        return activeNotifications.remove(id);
    }
}
