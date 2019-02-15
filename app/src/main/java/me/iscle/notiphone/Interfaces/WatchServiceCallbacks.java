package me.iscle.notiphone.Interfaces;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

import me.iscle.notiphone.Watch;

public interface WatchServiceCallbacks {
    void updateBluetoothDevices(ArrayList<BluetoothDevice> bluetoothDevices);
    void updateWatchStatus(Watch watch);
}
