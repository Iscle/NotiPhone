package me.iscle.notiphone.model;

public class RawCapsule {
    public boolean isNew;
    public int id;
    public byte[] data;

    public RawCapsule(int id, byte[] data) {
        this.id = id;
        this.data = data;
        this.isNew = true;
    }
}
