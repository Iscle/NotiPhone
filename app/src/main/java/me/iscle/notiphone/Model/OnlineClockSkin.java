package me.iscle.notiphone.Model;

public class OnlineClockSkin {
    public static String CLOCKSKIN_BASE_URL = "http://www.weitetech.cn/clockskin/";

    private String name;
    private String skinid;
    private String file;
    private String customer;
    private String type;

    public OnlineClockSkin() {
        name = "";
        skinid = "";
        file = "";
        customer = "";
        type = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSkinid() {
        return skinid;
    }

    public void setSkinid(String skinid) {
        this.skinid = skinid;
    }

    public String getFileURL() {
        return CLOCKSKIN_BASE_URL + file;
    }

    public String getPreviewURL() {
        return getFileURL().replace(".zip", ".png");
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
