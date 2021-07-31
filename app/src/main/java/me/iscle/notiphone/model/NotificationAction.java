package me.iscle.notiphone.model;

public class NotificationAction {
    private final String title;
    private final String icon;
    private final int hashCode;

    public NotificationAction(String title, String icon, int hashCode) {
        this.title = title;
        this.icon = icon;
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
