package me.iscle.notiphone.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import me.iscle.notiphone.Command;
import me.iscle.notiphone.CommandHandler;
import me.iscle.notiphone.LocalNotificationManager;
import me.iscle.notiphone.NotiPhone;
import me.iscle.notiphone.R;
import me.iscle.notiphone.activity.MainActivity;
import me.iscle.notiphone.model.BatteryStatus;
import me.iscle.notiphone.model.Capsule;
import me.iscle.notiphone.model.PhoneNotification;
import me.iscle.notiphone.model.Watch;

import static android.os.BatteryManager.EXTRA_LEVEL;
import static android.os.BatteryManager.EXTRA_SCALE;
import static android.os.BatteryManager.EXTRA_STATUS;

public class NotiPhoneService extends Service {
    private static final String TAG = "NotiPhoneService";
    private static final int SERVICE_NOTIFICATION_ID = 1;

    private ConnectionState state;
    private CommandHandler commandHandler;
    private ConnectionThread connectionThread;
    private Watch watch;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // We set the state first as getNotification() makes use of it
        state = ConnectionState.DISCONNECTED;

        startForeground(SERVICE_NOTIFICATION_ID, getNotification());

        commandHandler = new CommandHandler(this);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        LocalNotificationManager.getInstance().addNotificationObserver(notificationListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LocalNotificationManager.getInstance().removeNotificationObserver(notificationListener);
        unregisterReceiver(batteryReceiver);
        stop();
        stopForeground(true);
        super.onDestroy();
    }

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendBattery(intent);
        }
    };

    private final LocalNotificationManager.NotificationListener notificationListener = new LocalNotificationManager.NotificationListener() {
        @Override
        public void onNotificationPosted(PhoneNotification notification) {
            sendCommand(Command.NOTIFICATION_POSTED, notification);
        }

        @Override
        public void onNotificationRemoved(PhoneNotification notification) {
            sendCommand(Command.NOTIFICATION_REMOVED, notification.getId());
        }
    };

    private void sendBattery() {
        Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        sendBattery(intent);
    }

    private void sendBattery(Intent batteryStatus) {
        int level = batteryStatus.getIntExtra(EXTRA_LEVEL, 50);
        int scale = batteryStatus.getIntExtra(EXTRA_SCALE, 100);
        int chargeStatus = batteryStatus.getIntExtra(EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_DISCHARGING);
        int batteryLevel = Math.round((float) level / (float) scale * 100.0f);

        sendCommand(Command.SET_BATTERY_STATUS, new BatteryStatus(batteryLevel, chargeStatus));
    }

    public void updateNotification() {
        notificationManager.notify(SERVICE_NOTIFICATION_ID, getNotification());
    }

    public void connect(String deviceAddress) {
        connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress));
    }

    public void connect(BluetoothDevice device) {
        stop();

        watch = new Watch(device);

        setState(ConnectionState.CONNECTING);

        connectionThread = new ConnectionThread(device, connectionThreadListener);
        connectionThread.start();
    }

    private final ConnectionThread.ConnectionListener connectionThreadListener = new ConnectionThread.ConnectionListener() {
        @Override
        public void onConnect() {
            Log.d(TAG, "ConnectionThread connected!");
            setState(ConnectionState.CONNECTED);
            sendBattery();
            for (PhoneNotification pn : LocalNotificationManager.getInstance().getActiveNotifications()) {
                sendCommand(Command.NOTIFICATION_POSTED, pn);
            }
        }

        @Override
        public void onMessage(Capsule capsule) {
            Log.d(TAG, "New message from " + watch.getName());
            commandHandler.handleCommand(capsule);
        }

        @Override
        public void onError(Throwable t) {
            Log.e(TAG, "Error on ConnectionThread: ", t);
        }

        @Override
        public void onDisconnect() {
            Log.d(TAG, "ConnectionThread disconnected!");
            setState(ConnectionState.DISCONNECTED);
        }
    };

    public Watch getWatch() {
        return watch;
    }

    private Notification getNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        String title;
        String text;
        switch (state) {
            case DISCONNECTED:
                title = "Disconnected";
                text = "Waiting for device";
                break;
            case CONNECTING:
                title = "Connecting to " + watch.getName();
                text = "Please wait...";
                break;
            case CONNECTED:
                title = "Connected to " + watch.getName();
                text = "Battery: " + watch.getBatteryPercentage();
                break;
            default:
                throw new RuntimeException("Wrong service state");
        }

        return new NotificationCompat.Builder(this, NotiPhone.SERVICE_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.watch_icon)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void stop() {
        if (connectionThread != null) {
            connectionThread.close();
            connectionThread = null;
        }
    }

    private void setState(ConnectionState state) {
        this.state = state;
        updateNotification();
    }

    public void sendCommand(Command command, Object object) {
        if (state != ConnectionState.CONNECTED) return;
        connectionThread.send(new Capsule(command, object));
    }

    public ConnectionState getState() {
        return state;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new NotiPhoneServiceBinder();
    }

    public class NotiPhoneServiceBinder extends Binder {
        public NotiPhoneService getService() {
            return NotiPhoneService.this;
        }
    }
}
