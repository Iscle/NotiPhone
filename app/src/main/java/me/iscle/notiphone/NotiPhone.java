package me.iscle.notiphone;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import me.iscle.notiphone.service.NotiPhoneService;

public class NotiPhone extends Application {
    private static final String TAG = "NotiPhone";

    public static final String SERVICE_CHANNEL_ID = "service_channel";
    public static final String NOTIFICATION_CHANNEL_ID = "notification_channel";

    public static final String SERVICE_PREFERENCES = "service_preferences";

    private NotiPhoneService notiPhoneService;

    @Override
    public void onCreate() {
        super.onCreate();

        interceptUncaughtExceptions();
        createNotificationChannels();
        startAndBindNotiPhoneService();
    }

    private void interceptUncaughtExceptions() {
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler
                = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            // TODO: Log uncaught exceptions
            if (defaultUncaughtExceptionHandler != null) {
                defaultUncaughtExceptionHandler.uncaughtException(t, e);
            }
        });
    }

    private void createNotificationChannels() {
        // Create the NotificationChannels if we are on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(SERVICE_CHANNEL_ID, getString(R.string.notification_channel_services), NotificationManager.IMPORTANCE_LOW);
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.notification_channel_notifications), NotificationManager.IMPORTANCE_LOW);

            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(serviceChannel);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void startAndBindNotiPhoneService() {
        Intent serviceIntent = new Intent(this, NotiPhoneService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        bindService(serviceIntent, notiPhoneServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection notiPhoneServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notiPhoneService = ((NotiPhoneService.NotiPhoneServiceBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            notiPhoneService = null;
        }
    };

    public NotiPhoneService getNotiPhoneService() {
        return notiPhoneService;
    }
}
