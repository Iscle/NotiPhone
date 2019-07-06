package me.iscle.notiphone.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import me.iscle.notiphone.Activities.MainActivity;
import me.iscle.notiphone.App;
import me.iscle.notiphone.Command;
import me.iscle.notiphone.Model.Capsule;
import me.iscle.notiphone.Model.PhoneNotification;
import me.iscle.notiphone.Model.Watch;
import me.iscle.notiphone.R;

import static android.os.BatteryManager.EXTRA_LEVEL;
import static android.os.BatteryManager.EXTRA_SCALE;
import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_POSTED;
import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_REMOVED;
import static me.iscle.notiphone.Constants.HANDLER_WATCH_CONNECTED;

public class WatchService extends Service {
    private static final String TAG = "WatchService";
    private static final UUID MY_UUID = UUID.fromString("c4547ff6-e6e4-4ccd-9a30-4cdce6249d19");
    private static final int SERVICE_NOTIFICATION_ID = 1;

    private final IBinder mBinder = new WatchBinder();
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler = null;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private ConnectionState mState;
    final BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendBattery(intent);
        }
    };
    private final BroadcastReceiver notificationListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            PhoneNotification pn = (PhoneNotification) extras.get("phoneNotification");

            switch (intent.getAction()) {
                case BROADCAST_NOTIFICATION_POSTED:
                    write(new Capsule(Command.NOTIFICATION_POSTED, pn).toJson());
                    break;
                case BROADCAST_NOTIFICATION_REMOVED:
                    write(new Capsule(Command.NOTIFICATION_REMOVED, pn).toJson());
                    break;
            }
        }
    };
    private Watch currentWatch;

    private void sendBattery(Intent i) {
        int batteryLevel = (int) (100 * (((float) i.getIntExtra(EXTRA_LEVEL, 0)) / ((float) i.getIntExtra(EXTRA_SCALE, 1))));
        write(new Capsule(Command.SET_BATTERY_STATUS, batteryLevel).toJson());
    }

    private void sendBattery() {
        Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        sendBattery(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Get the phone's bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "onCreate: BluetoothAdapter is null!");
            return;
        }

        Context context = getApplicationContext();
        startForeground(SERVICE_NOTIFICATION_ID,
                getServiceNotification(context.getString(R.string.no_watch_connected), context.getString(R.string.click_to_open_the_app)));

        IntentFilter notificationFilter = new IntentFilter();
        notificationFilter.addAction(BROADCAST_NOTIFICATION_POSTED);
        notificationFilter.addAction(BROADCAST_NOTIFICATION_REMOVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationListener, notificationFilter);
        registerReceiver(batteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        // Set the initial state
        mState = ConnectionState.NONE;
    }

    private void updateNotification(String title, String text) {
        Notification notification = getServiceNotification(title, text);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);

        if (mHandler != null)
            mHandler.obtainMessage(HANDLER_WATCH_CONNECTED, new String[]{title, text}).sendToTarget();
    }

    private void updateNotification() {
        updateNotification("Connected to: " + currentWatch.getName(), "Remaining battery: " + currentWatch.getBattery() + "%");
    }

    public void connect(String deviceAddress) {
        // Start the thread to connect with the given device
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        currentWatch = new Watch(device.getName(), device.getAddress());
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    private void handleMessage(String data) {
        Log.d(TAG, "handleMessage: " + data);
        Capsule capsule = new Gson().fromJson(data, Capsule.class);

        switch (capsule.getCommand()) {
            case SET_BATTERY_STATUS:
                int batteryPercentage = capsule.getData(int.class);
                currentWatch.setBattery(batteryPercentage);
                updateNotification();
                Log.d(TAG, "handleMessage: Watch battery: " + batteryPercentage);
                break;
            default:
                Log.d(TAG, "handleMessage: Unknown command: " + capsule.getCommand());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationListener);
        unregisterReceiver(batteryChangedReceiver);
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
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     */
    private synchronized void connected(BluetoothSocket socket) {
        BluetoothDevice device = socket.getRemoteDevice();
        Log.d(TAG, "Connecting to: " + device.getName() + " (" + device.getAddress() + ")");

        updateNotification("Connecting to: " + device.getName(),
                "Address: " + device.getAddress()); // TODO: Fix notifications

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    /**
     * Stop all threads
     */
    private synchronized void stop() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        disconnected();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     */
    public void write(String data) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != ConnectionState.CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(data);
    }

    /*
     * Indicate that the connection was lost
     */
    private synchronized void disconnected() {
        mState = ConnectionState.NONE;
        updateNotification("No watch connected...", "Click to open the app");
        // TODO: do something (tell the activity, etc)
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Set the Handler to null as we don't have any activity attached anymore
        mHandler = null;
        return super.onUnbind(intent);
    }

    private enum ConnectionState {
        NONE, // We're doing nothing
        CONNECTING, // Initiating an outgoing connection
        CONNECTED // Connected to a remote device
    }

    /**
     * This thread runs when connecting to a remote device.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Error while creating the BluetoothSocket!", e);
            }
            mmSocket = tmp;
            mState = ConnectionState.CONNECTING;
            updateNotification("Connecting to " + device.getName() + "...",
                    "Tap to open the app"); // TODO: Improve notifications
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Connection failed; close the socket and return.
                Log.d(TAG, "Error while creating the BluetoothSocket!");
                cancel();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close mmSocket!", e);
            }

            disconnected();
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BufferedWriter mmOutStream;
        private final BufferedReader mmInStream;

        ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            OutputStream tmpOut = null;
            InputStream tmpIn = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpOut = socket.getOutputStream();
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error while creating temporary streams!", e);
            }

            mmOutStream = new BufferedWriter(new OutputStreamWriter(tmpOut));
            mmInStream = new BufferedReader(new InputStreamReader(tmpIn));

            mState = ConnectionState.CONNECTED;
            //updateNotification("Connected to: " + socket.getRemoteDevice().getName(),
            //        "Remaining battery: -%");
        }

        public void run() {
            sendBattery();

            // Keep listening to the InputStream while connected
            while (mState == ConnectionState.CONNECTED) {
                try {
                    // Read from the InputStream
                    handleMessage(mmInStream.readLine());
                } catch (IOException e) {
                    Log.e(TAG, "Disconnected from remote device!", e);
                    cancel();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param data The string to write
         */
        public void write(String data) {
            try {
                mmOutStream.write(data);
                // Send a new line after the data
                mmOutStream.newLine();
                // Flush the stream to send the data
                mmOutStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error while writing the data!", e);
            }
        }

        void cancel() {
            try {
                mmInStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error while closing mmInStream!", e);
            }

            try {
                mmOutStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error while closing mmOutStream!", e);
            }

            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error while closing mmSocket!", e);
            }

            disconnected();
        }
    }

    public class WatchBinder extends Binder {
        public WatchService getService() {
            return WatchService.this;
        }
    }
}
