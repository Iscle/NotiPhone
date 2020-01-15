package me.iscle.notiphone.service;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static me.iscle.notiphone.Utils.readLength;
import static me.iscle.notiphone.Utils.readString;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public class ConnectedThread extends Thread {
    private static final String TAG = "ConnectedThread";

    private final WatchService watchService;

    private final BluetoothSocket mmSocket;
    private final OutputStream outputStream;
    private final InputStream inputStream;

    public ConnectedThread(WatchService watchService, BluetoothSocket socket) {
        this.watchService = watchService;

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

        outputStream = tmpOut;
        inputStream = tmpIn;
    }

    public void run() {
        watchService.setState(ConnectionState.CONNECTED);

        // Keep listening to the InputStream while connected
        while (watchService.getState() == ConnectionState.CONNECTED) {
            try {
                int length = readLength(inputStream);
                String data = readString(inputStream, length);

                watchService.handleMessage(data);
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
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            int length = bytes.length;
            outputStream.write(length >> 24);
            outputStream.write(length >> 16);
            outputStream.write(length >> 8);
            outputStream.write(length);
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error while writing the data!", e);
            cancel();
        }
    }

    void cancel() {
        try {
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while closing inputStream!", e);
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while closing outputStream!", e);
        }

        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while closing mmSocket!", e);
        }

        watchService.setState(ConnectionState.DISCONNECTED);
    }
}
