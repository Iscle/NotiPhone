package me.iscle.notiphone.Model;

import com.google.gson.Gson;

public class Capsule {
    private static final transient Gson gson = new Gson();

    private int command;
    private String data;

    public Capsule(int command, Object data) {
        this.command = command;
        this.data = gson.toJson(data);
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public int getCommand() {
        return command;
    }

    public <T> T getData(Class<T> type) {
        return gson.fromJson(data, type);
    }
}
