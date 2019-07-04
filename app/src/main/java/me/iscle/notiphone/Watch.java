package me.iscle.notiphone;

public class Watch {
    private String name;
    private String address;
    private transient String battery;
    private long lastConnection;

    public Watch(String name, String address) {
        this.name = name;
        this.address = address;
        this.battery = "--%";
        this.lastConnection = -1;
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

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }
}
