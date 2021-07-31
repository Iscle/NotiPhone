package me.iscle.notiphone;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import me.iscle.notiphone.model.BatteryStatus;
import me.iscle.notiphone.model.Capsule;
import me.iscle.notiphone.model.NotificationAction;
import me.iscle.notiphone.model.PhoneNotification;
import me.iscle.notiphone.service.NotiPhoneService;

public class CommandHandler {
    private static final String TAG = "CommandHandler";

    private final NotiPhoneService notiPhoneService;
    private final LocalNotificationManager localNotificationManager;

    public CommandHandler(NotiPhoneService notiPhoneService) {
        this.notiPhoneService = notiPhoneService;
        this.localNotificationManager = LocalNotificationManager.getInstance();
    }

    public void handleCommand(Capsule capsule) {
        Log.d(TAG, "handleCommand: Got a new command: " + capsule.getCommand());

        switch (capsule.getCommand()) {
            case SET_BATTERY_STATUS:
                notiPhoneService.getWatch().setBatteryStatus(capsule.getData(BatteryStatus.class));
                notiPhoneService.updateNotification();
                break;
            case NOTIFICATION_ACTION_CALLBACK:
                NotificationAction.Callback callback = capsule.getData(NotificationAction.Callback.class);
                PhoneNotification pn = localNotificationManager.getActiveNotification(callback.getNotificationId());
                if (pn != null) {
                    for (Notification.Action action : pn.getSbn().getNotification().actions) {
                        if (action.hashCode() == callback.getHashCode()) {
                            try {
                                action.actionIntent.send(notiPhoneService, 0, new Intent());
                            } catch (PendingIntent.CanceledException e) {
                                Log.e(TAG, "handleCommand: Failed to send notification action", e);
                            }
                            break;
                        }
                    }
                }
                break;
            default:
                Log.w(TAG, "handleMessage: Unknown command: " + capsule.getCommand());
                break;
        }
    }
}
