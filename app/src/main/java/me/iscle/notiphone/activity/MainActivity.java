package me.iscle.notiphone.activity;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;

import me.iscle.notiphone.fragment.HomeFragment;
import me.iscle.notiphone.fragment.SettingsFragment;
import me.iscle.notiphone.R;
import me.iscle.notiphone.service.WatchService;

import static android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS;
import static me.iscle.notiphone.Constants.BROADCAST_SERVICE_NOTIFICATION_UPDATED;
import static me.iscle.notiphone.Constants.BROADCAST_WATCH_CONNECTED;
import static me.iscle.notiphone.Constants.BROADCAST_WATCH_CONNECTING;
import static me.iscle.notiphone.Constants.BROADCAST_WATCH_CONNECTION_FAILED;
import static me.iscle.notiphone.Constants.BROADCAST_WATCH_DISCONNECTED;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int CONNECT_DEVICE = 1;

    private WatchService watchService;

    private LocalBroadcastManager localBroadcastManager;

    private HomeFragment homeFragment;
    private SettingsFragment settingsFragment;

    OnNavigationItemSelectedListener navigationListener = menuItem -> {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (menuItem.getItemId()) {
            case R.id.navigation_home:
                setTitle("Home - NotiPhone");
                transaction.show(homeFragment);
                transaction.hide(settingsFragment);
                break;
            case R.id.navigation_settings:
                setTitle("Settings - NotiPhone");
                transaction.hide(homeFragment);
                transaction.show(settingsFragment);
                break;
            default:
                setTitle("Home - NotiPhone");
                transaction.show(homeFragment);
                transaction.hide(settingsFragment);
                break;
        }

        transaction.commit();

        return true;
    };
    private ServiceConnection watchServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WatchService.WatchBinder binder = (WatchService.WatchBinder) service;
            watchService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            watchService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind to WatchService
        bindService(new Intent(this, WatchService.class), watchServiceConnection, Context.BIND_AUTO_CREATE);

        // Create the fragment instances
        homeFragment = new HomeFragment();
        settingsFragment = new SettingsFragment();

        if (savedInstanceState == null) {
            setTitle("Home - NotiPhone");

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, homeFragment, "HOME");
            transaction.add(R.id.fragment_container, settingsFragment, "SETTINGS").hide(settingsFragment);
            transaction.commit();
        }

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationListener);

        IntentFilter watchServiceReceiverFilter = new IntentFilter();
        watchServiceReceiverFilter.addAction(BROADCAST_WATCH_CONNECTED);
        watchServiceReceiverFilter.addAction(BROADCAST_WATCH_CONNECTING);
        watchServiceReceiverFilter.addAction(BROADCAST_WATCH_CONNECTION_FAILED);
        watchServiceReceiverFilter.addAction(BROADCAST_WATCH_DISCONNECTED);
        watchServiceReceiverFilter.addAction(BROADCAST_SERVICE_NOTIFICATION_UPDATED);
        localBroadcastManager.registerReceiver(watchServiceReceiver, watchServiceReceiverFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(watchServiceReceiver);
        // Unbind from WatchService
        unbindService(watchServiceConnection);
    }

    public void ignoreBatteryOptimisations() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        if (powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            Log.d(TAG, "Already ignoring battery optimizations!");
        } else {
            Log.d(TAG, "Not ignoring battery optimizations... Requesting permissions");
            try {
                Intent intent = new Intent(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Could not open the battery optimization settings!
                Log.d(TAG, "ignoreBatteryOptimisations: Couldn't open battery optimisation activity!");
            }
        }
    }

    public void ignoreMiuiBatteryOptimisations() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"));
            intent.putExtra("package_name", getPackageName());
            intent.putExtra("package_label", getText(R.string.app_name));
            startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CONNECT_DEVICE) {
            if (resultCode == RESULT_OK) {
                if (watchService != null) {
                    String bluetoothAddress = data.getStringExtra("BluetoothAddress");
                    watchService.connect(bluetoothAddress);
                } else {
                    Log.e(TAG, "onActivityResult: WatchService not bound!");
                    Toast.makeText(MainActivity.this, "There was an error connecting to the device!", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private BroadcastReceiver watchServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BROADCAST_WATCH_CONNECTED:
                    String name = intent.getStringExtra("device_name");
                    String description = intent.getStringExtra("device_description");
                    homeFragment.setStatus(name, description);
                    break;
                case BROADCAST_WATCH_DISCONNECTED:
                    homeFragment.setStatus("Watch not connected", "Click to connect a new watch");
                    break;
                case BROADCAST_WATCH_CONNECTION_FAILED:

                    break;
                case BROADCAST_WATCH_CONNECTING:

                    break;
                case BROADCAST_SERVICE_NOTIFICATION_UPDATED:
                    String title = intent.getStringExtra("notificationTitle");
                    String text = intent.getStringExtra("notificationText");
                    homeFragment.setStatus(title, text);
                    break;
            }
        }
    };
}
