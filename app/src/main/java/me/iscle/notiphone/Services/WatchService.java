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
import androidx.core.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import me.iscle.notiphone.Activities.MainActivity;
import me.iscle.notiphone.App;
import me.iscle.notiphone.Activities.DebugActivity;
import me.iscle.notiphone.Interfaces.WatchServiceCallbacks;
import me.iscle.notiphone.R;
import me.iscle.notiphone.Watch;

public class WatchService extends Service {
    private static final String TAG = "WatchService";

    private final IBinder mBinder = new WatchBinder();
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler = null;
    private static final UUID MY_UUID = UUID.fromString("c4547ff6-e6e4-4ccd-9a30-4cdce6249d19");
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private boolean watchConnected = false;
    private int mState;

    public static final int BLUETOOTH_ENABLED = 0;
    public static final int BLUETOOTH_DISABLED = 1;
    public static final int BLUETOOTH_NOT_FOUND = 2;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static final int SERVICE_NOTIFICATION_ID = 1;
    public static final int REQUEST_ENABLE_BT = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        Notification notification = newNotification("No watch connected...", "Click to open the app");
        startForeground(SERVICE_NOTIFICATION_ID, notification);

        // Get the phone's bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "onCreate: BluetoothAdapter is null!");
        }

        // Set the initial state
        mState = STATE_NONE;
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void updateNotification(String title, String text) {
        Log.d(TAG, "updateNotification");

        Notification notification = newNotification(title, text);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);
    }

    // Returns the Bluetooth status
    public int getBluetoothStatus() {
        if (mBluetoothAdapter == null) {
            return BLUETOOTH_NOT_FOUND;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            return BLUETOOTH_DISABLED;
        }

        return BLUETOOTH_ENABLED;
    }

    public void connect(String deviceAddress) {
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(mBluetoothAdapter.getRemoteDevice(deviceAddress));
        mConnectThread.start();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stop();
        stopForeground(true);
        super.onDestroy();
    }

    public Notification newNotification(String title, String text) {
        Intent notificationIntent = new Intent(this, WatchService.class);
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
    public synchronized void connected(BluetoothSocket socket) {
        Log.d(TAG, "connected");

        updateNotification("Connecting to: " + socket.getRemoteDevice().getName(),
                "Address: " + socket.getRemoteDevice().getAddress());

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
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        mState = STATE_NONE;
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /*
     * Indicate that the connection was lost
     */
    public synchronized void disconnected() {
        mState = STATE_NONE;
        // TODO: do something
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
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
            updateNotification("Connecting to " + device.getName() + "...",
                    "Tap to open the app");
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.d(TAG, "run: Unable to connect...");
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
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
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
            updateNotification("Connected to: " + socket.getRemoteDevice().getName(),
                    "Address: " + socket.getRemoteDevice().getAddress());
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    Log.d(TAG, "run: Received \"" + new String(buffer, 0, bytes) + "\"");

                    if (mHandler != null) {
                        mHandler.obtainMessage(1, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    disconnected();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                if (mHandler != null) {
                    mHandler.obtainMessage(2, -1, -1, buffer);
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
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

    public class WatchBinder extends Binder {
        public WatchService getService() {
            return WatchService.this;
        }
    }
}
