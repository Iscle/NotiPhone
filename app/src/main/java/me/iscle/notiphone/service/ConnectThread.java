package me.iscle.notiphone.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * This thread runs when connecting to a remote device.
 */
public class ConnectThread extends Thread {
    private static final String TAG = "ConnectThread";

    private static final UUID NOTI_UUID = java.util.UUID.fromString("c4547ff6-e6e4-4ccd-9a30-4cdce6249d19");

    private WatchService watchService;
    private final BluetoothSocket mmSocket;

    public ConnectThread(WatchService watchService, BluetoothDevice device) {
        this.watchService = watchService;

        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // NOTI_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(NOTI_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Error while creating the BluetoothSocket!", e);
        }

        mmSocket = tmp;
    }

    public void run() {
        watchService.setState(ConnectionState.CONNECTING);

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

        // Start the connected thread
        watchService.connected(mmSocket);
    }

    // Closes the client socket and causes the thread to finish.
    void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close mmSocket!", e);
        }

        watchService.setState(ConnectionState.DISCONNECTED);
    }
}
