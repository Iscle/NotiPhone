package me.iscle.notiphone.model;

import android.bluetooth.BluetoothSocket;

public class Status {
    private byte batteryLevel;
    private int chargeStatus;

    public Status(byte batteryLevel, int chargeStatus) {
        this.batteryLevel = batteryLevel;
        this.chargeStatus = chargeStatus;
    }

    public byte getBatteryLevel() {
        return batteryLevel;
    }

    public int getChargeStatus() {
        return chargeStatus;
    }
}
