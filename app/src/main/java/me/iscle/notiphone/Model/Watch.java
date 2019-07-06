package me.iscle.notiphone.Model;

public class Watch {
    private final String name;
    private final String address;
    private transient int battery;
    private long lastConnection;

    public Watch(String name, String address) {
        this.name = name;
        this.address = address;
        this.battery = -1;
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

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }
}
