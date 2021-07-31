package me.iscle.notiphone.model;

public class BatteryStatus {
    private int batteryPercentage;
    private int chargeStatus;

    public BatteryStatus(int batteryPercentage, int chargeStatus) {
        this.batteryPercentage = batteryPercentage;
        this.chargeStatus = chargeStatus;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public int getChargeStatus() {
        return chargeStatus;
    }
}
