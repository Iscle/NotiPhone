package me.iscle.notiphone;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class App extends Application {

    public static final String SERVICE_CHANNEL_ID = "service_channel";
    public static final String NOTIFICATION_CHANNEL_ID = "notification_channel";

    public static final String SERVICE_PREFERENCES = "service_preferences";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        // Create the NotificationChannels if we are on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(SERVICE_CHANNEL_ID, "Services", NotificationManager.IMPORTANCE_LOW);
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notifications", NotificationManager.IMPORTANCE_LOW);

            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(serviceChannel);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
