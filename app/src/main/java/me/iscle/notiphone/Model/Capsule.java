package me.iscle.notiphone.Model;

import android.util.Log;

import com.google.gson.Gson;

public class Capsule {
    private static final String TAG = "Capsule";

    private int command;
    private String data;

    public Capsule(int command, String data) {
        Log.d(TAG, "Capsule: command = " + command + ", data = " + data);
        this.command = command;
        this.data = data;
    }

    public byte[] toJSONByteArray() {
        String json = new Gson().toJson(this);
        Log.d(TAG, "toJSONByteArray: " + json);
        return json.getBytes();
    }

    public int getCommand() {
        return command;
    }

    public String getData() {
        return data;
    }
}
