package me.iscle.notiphone;

public class Constants {
    // Capsule constants
    public static final int CAPSULE_NEW_NOTIFICATION = 1;
    public static final int CAPSULE_MUSIC_CHANGED = 2;
    public static final int CAPSULE_MUSIC_NEXT = 3;
    public static final int CAPSULE_MUSIC_PREV = 4;
    public static final int CAPSULE_MUSIC_PAUSE = 5;
    public static final int CAPSULE_GET_APP_VERSION = 6;

    // Broadcast constants
    public static final String BROADCAST_WATCH_CONNECTED = "me.iscle.notiphone.WATCH_CONNECTED";
    public static final String BROADCAST_WATCH_DISCONNECTED = "me.iscle.notiphone.WATCH_DISCONNECTED";
    public static final String BROADCAST_WATCH_CONNECTING = "me.iscle.notiphone.WATCH_CONNECTING";
    public static final String BROADCAST_WATCH_CONNECTION_FAILED = "me.iscle.notiphone.WATCH_CONNECTION_FAILED";

    public static final String BROADCAST_SERVICE_NOTIFICATION_UPDATED = "me.iscle.notiphone.SERVICE_NOITIFICATION_UPDATED";

    public static final String BROADCAST_NOTIFICATION_POSTED = "me.iscle.notiphone.NOTIFICATION_POSTED";
    public static final String BROADCAST_NOTIFICATION_REMOVED = "me.iscle.notiphone.NOTIFICATION_REMOVED";
    public static final String BROADCAST_NOTIFICATION_RANKING_UPDATE = "me.iscle.notiphone.NOTIFICATION_RAKING_UPDATE";
}
