package me.iscle.notiphone;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;

import me.iscle.notiphone.model.Capsule;
import me.iscle.notiphone.model.NotificationAction;
import me.iscle.notiphone.model.PhoneNotification;
import me.iscle.notiphone.model.Status;
import me.iscle.notiphone.service.WatchService;

public class CommandHandler {
    private static final String TAG = "CommandHandler";

    private final WatchService watchService;
    private final LocalNotificationManager localNotificationManager;

    public CommandHandler(WatchService watchService) {
        this.watchService = watchService;
        this.localNotificationManager = LocalNotificationManager.getInstance();
    }

    public void handleCommand(String data) {
        Capsule capsule = new Gson().fromJson(data, Capsule.class);
        Log.d(TAG, "handleCommand: Got a new command: " + capsule.getCommand());

        switch (capsule.getCommand()) {
            case SET_BATTERY_STATUS:
                watchService.getCurrentWatch().setStatus(capsule.getData(Status.class));
                watchService.updateNotification();
                break;
            case NOTIFICATION_ACTION_CALLBACK:
                NotificationAction.Callback callback = capsule.getData(NotificationAction.Callback.class);
                PhoneNotification pn = localNotificationManager.getActiveNotification(callback.getNotificationId());
                if (pn != null) {
                    for (Notification.Action a : pn.getSbn().getNotification().actions) {
                        if (a.hashCode() == callback.getHashCode()) {
                            try {
                                a.actionIntent.send(watchService, 0, new Intent());
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
                break;
            default:
                Log.d(TAG, "handleMessage: Unknown command: " + capsule.getCommand());
        }
    }
}
