package me.iscle.notiphone.model;

public class Watch {
    private final String name;
    private final String address;
    private transient Status status;
    private long lastConnection;
    private boolean isBLE;

    public Watch(String name, String address) {
        this.name = name;
        this.address = address;
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
