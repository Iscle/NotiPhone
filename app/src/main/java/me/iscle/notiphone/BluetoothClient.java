package me.iscle.notiphone;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.bluetooth.BluetoothClass.Device.WEARABLE_WRIST_WATCH;

public final class BluetoothClient {
    private static final String TAG = "BluetoothClient";
    private static final long MAX_RETRY_DELAY_MS = TimeUnit.MINUTES.toMillis(30);

    public boolean connected;
    private final Context context;
    public long retryDelayMs = 1000;
    public boolean retryScheduled = false;
    private final UUID serviceUuid;

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", BluetoothAdapter.STATE_OFF);
                BluetoothClient bluetoothClient = BluetoothClient.this;
                Log.i(TAG, "Bluetooth adapter state changed: " + intExtra);
                if (intExtra == BluetoothAdapter.STATE_ON) {
                    bluetoothClient.attemptConnection();
                } else {
                    bluetoothClient.connected = false;
                    //bluetoothClient.listener.onConnectionLost();
                }
            }
        }
    };

    public BluetoothClient(Context context, UUID uuid) {
        this.context = context;
        this.serviceUuid = uuid;
    }

    public final void initialize() {
        this.context.registerReceiver(this.broadcastReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
        scheduleConnectionAttempt();
    }

    final void scheduleConnectionAttempt() {
        //ThreadUtils.checkOnMainThread();
        if (!this.retryScheduled && !this.connected) {
            this.retryScheduled = true;
            //this.handler.postDelayed(this.retryRunnable, this.retryDelayMs);
            long j = this.retryDelayMs;
            this.retryDelayMs = Math.min(j + j, MAX_RETRY_DELAY_MS);
        }
    }

    final int attemptConnection() {
        //ThreadUtils.checkOnMainThread();
        if (connected) {
            Log.i(TAG, "Already connected, not attempting connection");
            return 2;
        }

        int state = bluetoothAdapter.getState();
        if (state != BluetoothAdapter.STATE_ON) {
            Log.i(TAG, "Adapter state is " + state + "; not attempting connection");
            return 2;
        }

        for (BluetoothDevice bluetoothDevice : this.bluetoothAdapter.getBondedDevices()) {
            boolean obj = true;

            String name = bluetoothDevice.getName();

            if (name == null) {
                name = "";
            } else {
                name = name.toLowerCase(Locale.US);
            }

            Log.d(TAG, "Watch detected: " + bluetoothDevice.getName());
            try {
                Log.i(TAG, "Connecting to watch + " + bluetoothDevice.getName());
                BluetoothSocket createRfcommSocketToServiceRecord = bluetoothDevice.createRfcommSocketToServiceRecord(serviceUuid);
                createRfcommSocketToServiceRecord.connect();
                this.connected = true;
                //this.listener.onConnectionEstablished(createRfcommSocketToServiceRecord.getInputStream(), createRfcommSocketToServiceRecord.getOutputStream(), new DefaultBluetoothClient$$Lambda$1(this, createRfcommSocketToServiceRecord));
                return 2;
            } catch (Throwable e) {
                Log.w(TAG, "Failed to connect to rfcomm", e);
                return 1;
            }
        }

        Log.i(TAG, "No watch detected.");
        return 2;
    }
}
