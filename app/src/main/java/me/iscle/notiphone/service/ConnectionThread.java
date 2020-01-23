package me.iscle.notiphone.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static me.iscle.notiphone.Utils.readLength;
import static me.iscle.notiphone.Utils.readString;
import static me.iscle.notiphone.Utils.writeLength;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public class ConnectionThread extends Thread {
    private static final String TAG = "ConnectionThread";

    private static final UUID NOTI_UUID = UUID.fromString("c4547ff6-e6e4-4ccd-9a30-4cdce6249d19");

    private final WatchService watchService;
    private final BluetoothDevice bluetoothDevice;

    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private boolean isCanceled;

    public ConnectionThread(WatchService watchService, BluetoothDevice bluetoothDevice) {
        this.watchService = watchService;
        this.bluetoothDevice = bluetoothDevice;
        this.isCanceled = false;
    }

    public void run() {
        synchronized (watchService.getBluetoothLock()) {
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // NOTI_UUID is the app's UUID string, also used in the server code.
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(NOTI_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Error while creating bluetoothSocket!", e);
                cancel();
                return;
            }

            watchService.setState(ConnectionState.CONNECTING);

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                bluetoothSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, "Error while connecting to bluetoothSocket!", e);
                cancel();
                return;
            }

            // Get the BluetoothSocket input and output streams
            try {
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error while creating bluetoothSocket streams!", e);
                cancel();
                return;
            }

            watchService.setState(ConnectionState.CONNECTED);
        }

        // Keep listening to the InputStream while connected
        while (watchService.getState() == ConnectionState.CONNECTED) {
            try {
                int length = readLength(inputStream);
                String data = readString(inputStream, length);

                watchService.handleMessage(data);
            } catch (IOException e) {
                Log.e(TAG, "Disconnected from remote device!", e);
                cancel();
                return;
            }
        }

        cancel();
    }

    /**
     * Write to the connected OutStream.
     *
     * @param data The string to write
     */
    public void write(String data) {
        if (isCanceled) return;

        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        synchronized (watchService.getWriteLock()) {
            if (watchService.getState() == ConnectionState.CONNECTED) {
                try {
                    writeLength(outputStream, bytes.length);
                    outputStream.write(bytes);
                    outputStream.flush();
                } catch (IOException e) {
                    Log.e(TAG, "Error while writing data to outputStream!", e);
                    cancel();
                    return;
                }
            }
        }
    }

    void cancel() {
        if (isCanceled) return;
        isCanceled = true;

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error while closing inputStream!", e);
            }
            inputStream = null;
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error while closing outputStream!", e);
            }
            outputStream = null;
        }

        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error while closing bluetoothSocket!", e);
            }
            bluetoothSocket = null;
        }

        watchService.setState(ConnectionState.DISCONNECTED);
    }
}
