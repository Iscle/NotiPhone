package me.iscle.notiphone.model;

import android.graphics.drawable.Drawable;

import me.iscle.notiphone.Utils;

public class NotificationAction {
    private final String title;
    private final String icon;
    private final int hashCode;

    public NotificationAction(String title, Drawable icon, int hashCode) {
        this.title = title;
        if (icon != null) {
            this.icon = Utils.drawableToBase64(icon, 400, 400);
        } else {
            this.icon = null;
        }
        this.hashCode = hashCode;
    }

    public static class Callback {
        private String notificationId;
        private int hashCode;

        public Callback(String notificationId, int hashCode) {
            this.notificationId = notificationId;
            this.hashCode = hashCode;
        }

        public String getNotificationId() {
            return notificationId;
        }

        public int getHashCode() {
            return hashCode;
        }
    }
}
