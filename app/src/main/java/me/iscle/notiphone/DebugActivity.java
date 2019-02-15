package me.iscle.notiphone;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import me.iscle.notiphone.Interfaces.WatchServiceCallbacks;
import me.iscle.notiphone.Services.WatchService;
import me.iscle.notiphone.Services.WatchService.WatchBinder;

public class DebugActivity extends AppCompatActivity {
    private static final String TAG = "DebugActivity";

    private SharedPreferences sharedPreferences;
    private WatchServiceCallbacks watchServiceCallbacks;

    public static final int REQUEST_ENABLE_BT = 0;
    public static final int REQUEST_PICK_FILE = 1;
    public static final int SERVICE_NOTIFICATION_ID = 1;

    private WatchService watchService;
    private boolean watchServiceBound = false;

    private ServiceConnection mConnection;

    @Override
    protected void onStart() {
        super.onStart();

        Intent serviceIntent = new Intent(this, WatchService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // Bind to WatchService
        Intent intent = new Intent(this, WatchService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        watchService.setWatchServiceCallbacks(null);
        unbindService(mConnection);
        watchServiceBound = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        watchServiceCallbacks = new WatchServiceCallbacks() {
            @Override
            public void updateBluetoothDevices(ArrayList<BluetoothDevice> bluetoothDevices) {
                Toast.makeText(getApplicationContext(), "Toast from updateBluetoothDevices!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void updateWatchStatus(Watch watch) {
                Toast.makeText(getApplicationContext(), "Toast from updateWatchStatus!", Toast.LENGTH_LONG).show();
            }
        };

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                WatchBinder binder = (WatchBinder) service;
                watchService = binder.getService();
                watchServiceBound = true;

                watchService.setWatchServiceCallbacks(watchServiceCallbacks);

                TextView bluetoothStatus = findViewById(R.id.isBluetoothConnected);

                switch (watchService.getBluetoothStatus()) {
                    case 0:
                        bluetoothStatus.setText("Bluetooth connected!");
                        break;
                    case 1:
                        bluetoothStatus.setText("Bluetooth adapter not found!");
                        break;
                    case 2:
                        bluetoothStatus.setText("Bluetooth not connected!");
                        break;
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                watchServiceBound = false;
            }
        };

        if (!watchServiceBound) {
            TextView bluetoothStatus = findViewById(R.id.isBluetoothConnected);
            bluetoothStatus.setText("WatchService not bound!");
        }
    }

    public void showBluetoothError() {
        Toast.makeText(this, getText(R.string.bluetooth_required), Toast.LENGTH_LONG).show();
        finishAffinity();
    }

    private void startApp() {

    }

    public void startService(View v) {
        Intent serviceIntent = new Intent(this, WatchService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(serviceIntent);
        } else {
            this.startService(serviceIntent);
        }
    }

    public void stopService(View v) {
        Intent serviceIntent = new Intent(this, WatchService.class);
        stopService(serviceIntent);
    }

    public void updateServiceNotification(View v) {
        if (watchServiceBound) {
            String title = ((EditText) findViewById(R.id.title)).getText().toString();
            String text = ((EditText) findViewById(R.id.text)).getText().toString();
            watchService.updateNotification(title, text);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    // Bluetooth enabled
                    Toast.makeText(this, getText(R.string.bluetooth_enabled), Toast.LENGTH_LONG).show();
                    startApp();
                } else {
                    // Bluetooth not enabled
                    showBluetoothError();
                }
                break;
            case REQUEST_PICK_FILE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, data.getData().getPath(), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
