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
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.util.HashMap;

import me.iscle.notiphone.CommandHandler;
import me.iscle.notiphone.LocalNotificationManager;
import me.iscle.notiphone.activity.MainActivity;
import me.iscle.notiphone.App;
import me.iscle.notiphone.Command;
import me.iscle.notiphone.model.Capsule;
import me.iscle.notiphone.model.NotificationAction;
import me.iscle.notiphone.model.PhoneNotification;
import me.iscle.notiphone.model.Status;
import me.iscle.notiphone.model.Watch;
import me.iscle.notiphone.R;

import static android.os.BatteryManager.EXTRA_LEVEL;
import static android.os.BatteryManager.EXTRA_SCALE;
import static android.os.BatteryManager.EXTRA_STATUS;
import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_POSTED;
import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_REMOVED;
import static me.iscle.notiphone.Constants.BROADCAST_SERVICE_NOTIFICATION_UPDATED;

public class WatchService extends Service {
    private static final String TAG = "WatchService";

    private static final int SERVICE_NOTIFICATION_ID = 1;

    private final Object bluetoothLock = new Object();
    private final Object writeLock = new Object();

    private CommandHandler commandHandler;

    private final IBinder mBinder = new WatchBinder();

    private BluetoothDevice currentDevice;
    private Watch currentWatch;

    private BluetoothAdapter bluetoothAdapter;
    private ConnectionThread connectionThread;
    private ConnectionState state;

    private LocalBroadcastManager localBroadcastManager;
    private NotificationManager notificationManager;
    private LocalNotificationManager localNotificationManager;

    public Object getBluetoothLock() {
        return bluetoothLock;
    }

    public Object getWriteLock() {
        return writeLock;
    }

    private final BroadcastReceiver batteryListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendBattery(intent);
        }
    };

    private final BroadcastReceiver notificationListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            switch (intent.getAction()) {
                case BROADCAST_NOTIFICATION_POSTED:
                    PhoneNotification pn = new Gson().fromJson(intent.getStringExtra("phoneNotification"), PhoneNotification.class);
                    localNotificationManager.putActiveNotification(pn);
                    sendCommand(Command.NOTIFICATION_POSTED, pn);
                    break;
                case BROADCAST_NOTIFICATION_REMOVED:
                    String notificationId = intent.getStringExtra("notificationId");
                    localNotificationManager.removeActiveNotification(notificationId);
                    sendCommand(Command.NOTIFICATION_REMOVED, notificationId);
                    break;
            }
        }
    };

    private void handleFirstConnection() {
        sendBattery();
        sendCurrentNotifications();
    }

    private void sendCurrentNotifications() {
        for (StatusBarNotification sbn : notificationManager.getActiveNotifications()) {
            PhoneNotification pn = new PhoneNotification(this, sbn);
            sendCommand(Command.NOTIFICATION_POSTED, pn);
            Log.d(TAG, "sendCurrentNotifications: " + pn.getId());
        }
    }

    private void sendBattery() {
        Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        sendBattery(intent);
    }

    private void sendBattery(Intent batteryStatus) {
        int level = batteryStatus.getIntExtra(EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(EXTRA_SCALE, -1);
        byte batteryLevel = (byte) (level * 100 / (float) scale);

        int chargeStatus = batteryStatus.getIntExtra(EXTRA_STATUS, -1);

        Status status = new Status(batteryLevel, chargeStatus);
        sendCommand(Command.SET_BATTERY_STATUS, status);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Get the phone's bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "onCreate: BluetoothAdapter is null!");
            return;
        }

        startForeground(SERVICE_NOTIFICATION_ID,
                getServiceNotification(getString(R.string.no_watch_connected), getString(R.string.click_to_open_the_app)));

        commandHandler = new CommandHandler(this);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        setupNotifications();
        registerReceiver(batteryListener, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        // Set the initial state
        setState(ConnectionState.DISCONNECTED);
    }

    private void setupNotifications() {
        localNotificationManager = LocalNotificationManager.getInstance();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        IntentFilter notificationFilter = new IntentFilter();
        notificationFilter.addAction(BROADCAST_NOTIFICATION_POSTED);
        notificationFilter.addAction(BROADCAST_NOTIFICATION_REMOVED);
        localBroadcastManager.registerReceiver(notificationListener, notificationFilter);

        StatusBarNotification[] sbns = notificationManager.getActiveNotifications();
        for (StatusBarNotification sbn : sbns) {
            PhoneNotification pn = new PhoneNotification(this, sbn);
            if (!NotificationListener.filterNotification(this, pn.getSbn())) {
                continue;
            }
            if (pn.getTemplate() != null
                    && (pn.getTemplate().equals("android.app.Notification$DecoratedCustomViewStyle")
                    || pn.getTemplate().equals("android.app.Notification$DecoratedMediaCustomViewStyle"))) {
                continue;
            }
            localNotificationManager.putActiveNotification(pn);
        }
    }

    private void updateNotification(String title, String text) {
        Notification notification = getServiceNotification(title, text);

        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);

        Intent i = new Intent(BROADCAST_SERVICE_NOTIFICATION_UPDATED);
        i.putExtra("notificationTitle", title);
        i.putExtra("notificationText", text);
        localBroadcastManager.sendBroadcast(i);
    }

    public void updateNotification() {
        String text;
        int chargeStatus = currentWatch.getStatus().getChargeStatus();
        if (chargeStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
            text = "Charging: ";
        } else if (chargeStatus == BatteryManager.BATTERY_STATUS_FULL){
            text = "Charged: ";
        } else {
            text = "Discharging: ";
        }
        text = text + currentWatch.getStatus().getBatteryLevel() + "%";

        updateNotification("Connected to: " + currentWatch.getName(), text);
    }

    public void connect(String deviceAddress) {
        stop();

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        currentDevice = device;
        currentWatch = new Watch(device);

        connectionThread = new ConnectionThread(WatchService.this, device);
        connectionThread.start();
    }

    public Watch getCurrentWatch() {
        return currentWatch;
    }

    public void handleMessage(String data) {
        commandHandler.handleCommand(data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationListener);
        unregisterReceiver(batteryListener);
        stop();
        stopForeground(true);
    }

    private Notification getServiceNotification(String title, String text) { // TODO: Fix notifications
        Intent notificationIntent = new Intent(this, MainActivity.class); // Activity to open when tapping the notification
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        return new NotificationCompat.Builder(this, App.SERVICE_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.watch_icon)
                .setContentIntent(pendingIntent)
                .build();
    }

    /**
     * Stop all threads
     */
    private void stop() {
        if (connectionThread != null) {
            connectionThread.cancel();
            connectionThread = null;
        }
    }

    /*
     * Indicate that the connection was lost
     */
    private void disconnected() {
        updateNotification("No watch connected...", "Click to open the app");
        // TODO: do something (tell the activity, etc)
    }

    private void connecting() {
        updateNotification("Connecting to " + (currentDevice.getName() == null ? "No name" : currentDevice.getName()) + "...",
                "Tap to open the app"); // TODO: Improve notifications
    }

    private void connected() {
        handleFirstConnection();
    }

    public void sendCommand(Command command, Object object) {
        if (connectionThread == null) return;
        Log.d(TAG, "sendCommand: " + command + ", state: " + getState());
        connectionThread.write(new Capsule(command, object).toJson());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void setState(ConnectionState newState) {
        if (this.state == newState) return;
        this.state = newState;

        Log.d(TAG, "setState: " + newState);

        switch (newState) {
            case DISCONNECTED:
                disconnected();
                break;
            case CONNECTING:
                connecting();
                break;
            case CONNECTED:
                connected();
                break;
        }
    }

    public ConnectionState getState() {
        return state;
    }

    public class WatchBinder extends Binder {
        public WatchService getService() {
            return WatchService.this;
        }
    }
}
