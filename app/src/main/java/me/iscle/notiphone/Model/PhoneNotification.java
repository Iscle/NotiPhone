package me.iscle.notiphone.Model;

public class PhoneNotification {
    String key;
    String title;
    String text;

    public PhoneNotification(String key, String title, String text) {
        this.key = key;
        this.title = title;
        this.text = text;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }
}
