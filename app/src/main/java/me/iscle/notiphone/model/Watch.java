package me.iscle.notiphone.model;

import android.bluetooth.BluetoothDevice;

public class Watch {
    private final String name;
    private final String address;
    private transient Status status;
    private long lastConnection;
    private boolean isBLE;

    public Watch(BluetoothDevice device) {
        this.name = device.getName();
        this.address = device.getAddress();
        this.status = null;
        this.lastConnection = -1;
        this.isBLE = false;
    }

    public void setLastConnection(long time) {
        this.lastConnection = time;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
