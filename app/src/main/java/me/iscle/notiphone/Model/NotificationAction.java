package me.iscle.notiphone.Model;

public class NotificationAction {
    private final String title;
    private final int hashCode;

    public NotificationAction(String title, int hashCode) {
        this.title = title;
        this.hashCode = hashCode;
    }
}
