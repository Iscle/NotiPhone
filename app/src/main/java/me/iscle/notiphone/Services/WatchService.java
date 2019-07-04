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
import me.iscle.notiphone.Model.Capsule;
import me.iscle.notiphone.R;

import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_POSTED;
import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_REMOVED;
import static me.iscle.notiphone.Constants.HANDLER_WATCH_CONNECTED;

public class WatchService extends Service {
    private static final String TAG = "WatchService";
    private static final UUID MY_UUID = UUID.fromString("c4547ff6-e6e4-4ccd-9a30-4cdce6249d19");
    private static final int SERVICE_NOTIFICATION_ID = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private final IBinder mBinder = new WatchBinder();
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler = null;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private boolean watchConnected = false;
    private ConnectionState mState;
    private BroadcastReceiver newNotificationListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //PhoneNotification phoneNotification = new PhoneNotification(intent.getStringExtra("notificationKey"), intent.getBundleExtra("notificationExtras").getString(EXTRA_TITLE), intent.getBundleExtra("notificationExtras").getString(EXTRA_TEXT));
            //Capsule capsule = new Capsule(1, new Gson().toJson(phoneNotification));
            //write(capsule.toJSON());
            //Log.d(TAG, "onReceive");
        }
    };
    private BroadcastReceiver notificationRemovedListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Capsule capsule = new Capsule(2, intent.getStringExtra("notificationKey"));
            //write(capsule.toJson());
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Notification notification = newNotification("No watch connected...", "Click to open the app"); // TODO: Improve this
        startForeground(SERVICE_NOTIFICATION_ID, notification);

        // Get the phone's bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "onCreate: BluetoothAdapter is null!");
        }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(newNotificationListener, new IntentFilter(BROADCAST_NOTIFICATION_POSTED));
        lbm.registerReceiver(notificationRemovedListener, new IntentFilter(BROADCAST_NOTIFICATION_REMOVED));

        // Set the initial state
        mState = ConnectionState.NONE;
    }

    private void updateNotification(String title, String text) {
        Notification notification = newNotification(title, text);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);

        if (mHandler != null) {
            mHandler.obtainMessage(HANDLER_WATCH_CONNECTED, "null").sendToTarget();
        }
    }

    public void connect(String deviceAddress) {
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(mBluetoothAdapter.getRemoteDevice(deviceAddress));
        mConnectThread.start();
    }

    private void handleMessage(String data) {
        Capsule capsule = new Gson().fromJson(data, Capsule.class);
        // TODO: Handle the message
        switch (capsule.getCommand()) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(newNotificationListener);
        stop();
        stopForeground(true);
    }

    private Notification newNotification(String title, String text) { // TODO: Fix notifications
        Intent notificationIntent = new Intent(this, MainActivity.class);
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
     *
     * @param data The bytes to write
     * @see ConnectedThread#write(String)
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
        private final BufferedReader mmInStream;
        private final BufferedWriter mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error while creating temporary streams!", e);
            }

            mmInStream = new BufferedReader(new InputStreamReader(tmpIn));
            mmOutStream = new BufferedWriter(new OutputStreamWriter(tmpOut));

            mState = ConnectionState.CONNECTED;
            updateNotification("Connected to: " + socket.getRemoteDevice().getName(),
                    "Address: " + socket.getRemoteDevice().getAddress());
        }

        public void run() {
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
