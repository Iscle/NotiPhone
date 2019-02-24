package me.iscle.notiphone.Model;

public class PhoneNotification {
    int id;
    String title;
    String text;

    public PhoneNotification(int id, String title, String text) {
        this.id = id;
        this.title = title;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }
}
