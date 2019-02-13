package me.iscle.notiphone.Services;

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
import android.os.Binder;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import java.util.Set;

import me.iscle.notiphone.App;
import me.iscle.notiphone.DebugActivity;
import me.iscle.notiphone.R;

public class WatchService extends Service {
    private static final String TAG = "WatchService";

    private final IBinder mBinder = new WatchBinder();
    private BluetoothAdapter mBluetoothAdapter;

    private boolean watchConnected = false;

    public static final int SERVICE_NOTIFICATION_ID = 1;
    public static final int REQUEST_ENABLE_BT = 2;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        // Get the phone's bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Notification notification = newNotification("No watch connected...", "Click to open the app");
        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    public void updateNotification(String title, String text) {
        Log.d(TAG, "updateNotification");

        Notification notification = newNotification(title, text);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);
    }

    public int getBluetoothStatus() {
        if (mBluetoothAdapter == null) {
            return 1;
        } else if (!mBluetoothAdapter.isEnabled()) {
            return 2;
        } else {
            return 0;
        }
    }

    public Set<BluetoothDevice> getBondedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    public void searchBluetoothDevice() {
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        mBluetoothAdapter.startDiscovery();
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    unregisterReceiver(mReceiver);
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopForeground(true);
        super.onDestroy();
    }

    public Notification newNotification(String title, String text) {
        Intent notificationIntent = new Intent(this, DebugActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new NotificationCompat.Builder(this, App.SERVICE_CHANNEL_ID)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setSmallIcon(R.drawable.watch_icon)
                        .setContentIntent(pendingIntent)
                        .build();

        return notification;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    public class WatchBinder extends Binder {
        public WatchService getService() {
            return WatchService.this;
        }
    }
}
