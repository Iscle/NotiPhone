package me.iscle.notiphone.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import me.iscle.notiphone.NetworkUtils;
import me.iscle.notiphone.Utils;
import me.iscle.notiphone.model.Capsule;
import me.iscle.notiphone.model.RawCapsule;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public class ConnectionThread extends Thread implements Closeable {
    private static final String TAG = "ConnectionThread";

    private static final int PART_MAX_SIZE = 1024 * 1;
    private static final int PART_TYPE_START = 1;
    private static final int PART_TYPE_DATA = 2;
    private static final int PART_TYPE_END = 3;
    private static final int PART_TYPE_SINGLE = 4;
    private static final int PART_TYPE_DISCARD = 5;

    private static final UUID NOTI_UUID = UUID.fromString("c4547ff6-e6e4-4ccd-9a30-4cdce6249d19");

    private final BluetoothDevice device;
    private final ConnectionListener listener;

    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private boolean connected;

    private final Map<Integer, byte[]> rawInCapsules;
    private final PriorityBlockingQueue<RawCapsule> rawOutCapsules;
    private final Set<Integer> takenOutIds;
    private final Thread writeThread;
    private final Thread readThread;

    public ConnectionThread(@NonNull BluetoothDevice device, @NonNull ConnectionListener listener) {
        this.device = device;
        this.listener = listener;
        this.connected = false;
        this.rawInCapsules = new HashMap<>();
        this.rawOutCapsules = new PriorityBlockingQueue<>(4, (o1, o2) -> Integer.compare(o1.data.length, o2.data.length));
        this.takenOutIds = ConcurrentHashMap.newKeySet();
        this.writeThread = new Thread(writeRunnable);
        this.readThread = new Thread(readRunnable);
    }

    public void run() {
        synchronized (this) {
            if (connected) return;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // NOTI_UUID is the app's UUID string, also used in the server code.
                bluetoothSocket = device.createRfcommSocketToServiceRecord(NOTI_UUID);

                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                bluetoothSocket.connect();

                // Get the BluetoothSocket input and output streams
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error while connecting to the Bluetooth device", e);
                listener.onError(e);
                return;
            }

            connected = true;
        }

        listener.onConnect();
        writeThread.start();
        readThread.start();
    }

    private final Runnable readRunnable = new Runnable() {
        @Override
        public void run() {
            while (connected) {
                try {
                    readCapsulePart();
                } catch (IOException e) {
                    if (connected) {
                        Log.d(TAG, "Disconnected from remote device!", e);
                        listener.onError(e);
                        close();
                    }
                }
            }
        }
    };

    private final Runnable writeRunnable = new Runnable() {
        @Override
        public void run() {
            while (connected) {
                try {
                    writeCapsulePart();
                } catch (IOException e) {
                    if (connected) {
                        Log.d(TAG, "Disconnected from remote device!", e);
                        listener.onError(e);
                        close();
                    }
                }
            }
        }
    };

    private void readCapsulePart() throws IOException {
        int type = NetworkUtils.readInt(inputStream);

        if (type == PART_TYPE_SINGLE) {
            handleSinglePartRead();
            return;
        }

        int id = NetworkUtils.readInt(inputStream);

        if (type == PART_TYPE_DISCARD) {
            rawInCapsules.remove(id);
            return;
        }

        int length = NetworkUtils.readInt(inputStream);
        byte[] data = new byte[length];
        NetworkUtils.read(inputStream, data);
        handleCapsulePart(type, id, data);
    }

    private void handleSinglePartRead() throws IOException {
        int length = NetworkUtils.readInt(inputStream);
        byte[] data = new byte[length];
        NetworkUtils.read(inputStream, data);
        listener.onMessage(Capsule.fromBytes(data));
    }

    private void handleCapsulePart(int type, int id, byte[] data) {
        if (type == PART_TYPE_START) {
            rawInCapsules.put(id, data);
        } else if (type == PART_TYPE_DATA || type == PART_TYPE_END) {
            byte[] oldData = rawInCapsules.get(id);
            byte[] newData = NetworkUtils.concatenateBytes(oldData, data);

            if (type == PART_TYPE_END) {
                rawInCapsules.remove(id);
                listener.onMessage(Capsule.fromBytes(newData));
            } else {
                rawInCapsules.put(id, newData);
            }
        } else {
            Log.w(TAG, "handleCapsulePart: Ignoring unknown capsule part type: " + type + ". Ignoring...");
        }
    }

    private void writeCapsulePart() throws IOException {
        RawCapsule rawCapsule;
        try {
            rawCapsule = rawOutCapsules.take();
        } catch (InterruptedException e) {
            return;
        }
        if (!connected) return;

        boolean isSingle = rawCapsule.isNew && rawCapsule.data.length <= PART_MAX_SIZE;
        boolean isEnd = !isSingle && rawCapsule.data.length <= PART_MAX_SIZE;
        if (isSingle) {
            NetworkUtils.writeInt(outputStream, PART_TYPE_SINGLE);
            Log.d(TAG, "writeCapsulePart: length:" + rawCapsule.data.length);
            NetworkUtils.writeInt(outputStream, rawCapsule.data.length);
            outputStream.write(rawCapsule.data);
            takenOutIds.remove(rawCapsule.id);
        } else if (isEnd) {
            NetworkUtils.writeInt(outputStream, PART_TYPE_END);
            NetworkUtils.writeInt(outputStream, rawCapsule.id);
            Log.d(TAG, "writeCapsulePart: length:" + rawCapsule.data.length);
            NetworkUtils.writeInt(outputStream, rawCapsule.data.length);
            outputStream.write(rawCapsule.data);
            takenOutIds.remove(rawCapsule.id);
        } else {
            if (rawCapsule.isNew) {
                NetworkUtils.writeInt(outputStream, PART_TYPE_START);
                rawCapsule.isNew = false;
            } else {
                NetworkUtils.writeInt(outputStream, PART_TYPE_DATA);
            }
            NetworkUtils.writeInt(outputStream, rawCapsule.id);
            Log.d(TAG, "writeCapsulePart: length:" + PART_MAX_SIZE);
            NetworkUtils.writeInt(outputStream, PART_MAX_SIZE);
            outputStream.write(rawCapsule.data, 0, PART_MAX_SIZE);
            byte[] newData = new byte[rawCapsule.data.length - PART_MAX_SIZE];
            System.arraycopy(rawCapsule.data, PART_MAX_SIZE, newData, 0, rawCapsule.data.length - PART_MAX_SIZE);
            rawCapsule.data = newData;
            rawOutCapsules.add(rawCapsule);
        }
    }

    public void send(Capsule capsule) {
        byte[] data = capsule.toJson().getBytes(StandardCharsets.UTF_8);
        int id;
        synchronized (takenOutIds) {
            id = getNewRawCapsuleId();
            takenOutIds.add(id);
        }
        rawOutCapsules.add(new RawCapsule(id, data));
    }

    private int getNewRawCapsuleId() {
        int id;
        Random rng = new Random();
        do {
            id = rng.nextInt();
        } while (takenOutIds.contains(id));
        return id;
    }

    @Override
    public void close() {
        if (!connected) return;

        connected = false;

        listener.onDisconnect();

        writeThread.interrupt();
        readThread.interrupt();

        rawInCapsules.clear();
        rawOutCapsules.clear();
        takenOutIds.clear();

        Utils.closeCloseable(inputStream);
        Utils.closeCloseable(outputStream);
        Utils.closeCloseable(bluetoothSocket);
    }

    public interface ConnectionListener {
        void onConnect();
        void onMessage(Capsule capsule);
        void onError(Throwable t);
        void onDisconnect();
    }
}
