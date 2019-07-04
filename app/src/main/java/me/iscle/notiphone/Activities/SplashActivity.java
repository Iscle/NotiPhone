package me.iscle.notiphone.Activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import me.iscle.notiphone.R;
import me.iscle.notiphone.Services.WatchService;

public class SplashActivity extends AppCompatActivity {
    private static final int ENABLE_BLUETOOTH_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) { // Check if device has bluetooth
            new AlertDialog.Builder(this)
                    .setTitle("Bluetooth is required!")
                    .setMessage("We didn't find any bluetooth adapter, the app will now close.")
                    .setPositiveButton("Exit", (dialog, which) -> finishAffinity())
                    .show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) { // Check if bluetooth is enabled
            new AlertDialog.Builder(this)
                    .setTitle("Uh oh...")
                    .setMessage("Bluetooth needs to be enabled for this app to work.\nDo you want to enable it now?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST);
                    })
                    .setNegativeButton("No", (dialog, which) -> finishAffinity())
                    .show();
            return;
        }

        startApp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case ENABLE_BLUETOOTH_REQUEST:
                if (resultCode == RESULT_OK) {
                    startApp();
                } else {
                    finishAffinity();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startApp() {
        Intent serviceIntent = new Intent(this, WatchService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finishAffinity();
    }
}
