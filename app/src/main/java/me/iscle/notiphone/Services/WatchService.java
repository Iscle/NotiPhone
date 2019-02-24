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

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import me.iscle.notiphone.Activities.MainActivity;
import me.iscle.notiphone.App;
import me.iscle.notiphone.Model.Capsule;
import me.iscle.notiphone.Model.PhoneNotification;
import me.iscle.notiphone.R;

import static android.app.Notification.EXTRA_TITLE;
import static android.app.Notification.EXTRA_TEXT;
import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_POSTED;
import static me.iscle.notiphone.Constants.HANDLER_WATCH_CONNECTED;

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

    private BroadcastReceiver notificationListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                PhoneNotification phoneNotification = new PhoneNotification(intent.getIntExtra("notificationId", 1), intent.getBundleExtra("notificationExtras").getString(EXTRA_TITLE), intent.getBundleExtra("notificationExtras").getString(EXTRA_TEXT));
                Capsule capsule = new Capsule(1, new Gson().toJson(phoneNotification));
                write(capsule.toJSON());
            Log.d(TAG, "onReceive");
        }
    };

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

        LocalBroadcastManager.getInstance(this).registerReceiver(notificationListener, new IntentFilter(BROADCAST_NOTIFICATION_POSTED));

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

    public void setTestMessage() {
        if (mHandler != null) {
            mHandler.obtainMessage(HANDLER_WATCH_CONNECTED, 10, -1, "hola").sendToTarget();
        }
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

    private void handleMessage(String data) {
        Log.d(TAG, "handleMessage: " + data);
        Capsule capsule = new Gson().fromJson(data, Capsule.class);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationListener);
        stop();
        stopForeground(true);
        super.onDestroy();
    }

    public Notification newNotification(String title, String text) {
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
    public synchronized void connected(BluetoothSocket socket) {
        BluetoothDevice device = socket.getRemoteDevice();
        Log.d(TAG, "Connected to: " + device.getName() + " (" + device.getAddress() + ")");

        updateNotification("Connecting to: " + device.getName(),
                "Address: " + device.getAddress());

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
     * @param data The bytes to write
     * @see ConnectedThread#write(String)
     */
    public void write(String data) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(data);
    }

    /*
     * Indicate that the connection was lost
     */
    public synchronized void disconnected() {
        mState = STATE_NONE;
        updateNotification("No watch connected...", "Click to open the app");
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
                Log.e(TAG, "Error while creating the BluetoothSocket!", e);
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
            updateNotification("Connecting to " + device.getName() + "...",
                    "Tap to open the app");
        }

        public void run() {
            Log.i(TAG, "Starting ConnectThread");
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Connection failed; close the socket and return.
                updateNotification("No watch connected...", "Click to open the app");
                Log.d(TAG, "Error while creating the BluetoothSocket!");
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the socket!", closeException);
                }
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket);
            Log.i(TAG, "Finishing ConnectThread");
        }

        // Closes the client socket and causes the thread to finish.
        void cancel() {
            Log.i(TAG, "Closing sockets");
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the socket!", e);
            }
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
            Log.d(TAG, "Created ConnectedThread");
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
            mState = STATE_CONNECTED;
            updateNotification("Connected to: " + socket.getRemoteDevice().getName(),
                    "Address: " + socket.getRemoteDevice().getAddress());
        }

        public void run() {
            Log.i(TAG, "Started ConnectedThread");

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    handleMessage(mmInStream.readLine());
                } catch (IOException e) {
                    Log.e(TAG, "Disconnected from remote device!", e);
                    disconnected();
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
                // Write a carriage return after the data to indicate we've
                // finished sending
                mmOutStream.write("\r");
                // Flush the stream to send the data
                mmOutStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error while writing the data!", e);
            }
        }

        void cancel() {
            try {
                mmInStream.close();
                mmOutStream.close();
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error while closing the streams and socket!", e);
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
