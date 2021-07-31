package me.iscle.notiphone.model;

import android.bluetooth.BluetoothDevice;

public class Watch {
    private final BluetoothDevice device;
    private boolean supportsBle;
    private long lastSeen;
    private transient BatteryStatus batteryStatus;

    public Watch(BluetoothDevice device) {
        this.device = device;
    }

    public String getName() {
        return device.getName();
    }

    public String getAddress() {
        return device.getAddress();
    }

    public boolean isSupportsBle() {
        return supportsBle;
    }

    public void setSupportsBle(boolean supportsBle) {
        this.supportsBle = supportsBle;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public BatteryStatus getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(BatteryStatus batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public int getBatteryPercentage() {
        return batteryStatus.getBatteryPercentage();
    }
}
