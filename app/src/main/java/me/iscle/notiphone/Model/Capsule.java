package me.iscle.notiphone.Model;

import android.util.Log;

import com.google.gson.Gson;

public class Capsule {
    private static final String TAG = "Capsule";

    private int command;
    private String jsonData;

    public Capsule(int command, String jsonData) {
        Log.d(TAG, "Capsule: command = " + command + ", data = " + jsonData);
        this.command = command;
        this.jsonData = jsonData;
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
        return jsonData;
    }
}
