package me.iscle.notiphone;

public class Constants {
    // Capsule constants
    public static final int CAPSULE_NEW_NOTIFICATION = 1;
    public static final int CAPSULE_MUSIC_CHANGED = 2;
    public static final int CAPSULE_MUSIC_NEXT = 3;
    public static final int CAPSULE_MUSIC_PREV = 4;
    public static final int CAPSULE_MUSIC_PAUSE = 5;
    public static final int CAPSULE_GET_APP_VERSION = 6;

    // Handler constants
    public static final int HANDLER_WATCH_CONNECTED = 1;
    public static final int HANDLER_WATCH_DISCONNECTED = 2;
    public static final int HANDLER_WATCH_CONNECTING = 3;
    public static final int HANDLER_WATCH_CONNECTION_FAILED = 4;

    // Broadcast constants
    public static final String BROADCAST_NOTIFICATION_POSTED = "me.iscle.notiphone.NOTIFICATION_POSTED";
    public static final String BROADCAST_NOTIFICATION_REMOVED = "me.iscle.notiphone.NOTIFICATION_REMOVED";

}
