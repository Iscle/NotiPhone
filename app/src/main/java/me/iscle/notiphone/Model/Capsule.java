package me.iscle.notiphone.Model;

import com.google.gson.Gson;

public class Capsule {
    private static final String TAG = "Capsule";

    private int command;
    private String data;

    public Capsule(int command, String data) {
        this.command = command;
        this.data = data;
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }

    public int getCommand() {
        return command;
    }

    public String getData() {
        return data;
    }
}
