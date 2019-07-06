package me.iscle.notiphone.Activities;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;

import java.lang.ref.WeakReference;

import me.iscle.notiphone.Fragments.HomeFragment;
import me.iscle.notiphone.Fragments.SettingsFragment;
import me.iscle.notiphone.R;
import me.iscle.notiphone.Services.WatchService;

import static android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS;
import static me.iscle.notiphone.Constants.HANDLER_WATCH_CONNECTED;
import static me.iscle.notiphone.Constants.HANDLER_WATCH_CONNECTING;
import static me.iscle.notiphone.Constants.HANDLER_WATCH_CONNECTION_FAILED;
import static me.iscle.notiphone.Constants.HANDLER_WATCH_DISCONNECTED;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int CONNECT_DEVICE = 1;

    private WatchService watchService;
    private boolean watchServiceBound = false;
    private MainHandler mHandler = new MainHandler(this);

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
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WatchService.WatchBinder binder = (WatchService.WatchBinder) service;
            watchService = binder.getService();
            watchServiceBound = true;

            // Set the handler on the service
            watchService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            watchServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind to WatchService
        bindService(new Intent(this, WatchService.class), mConnection, Context.BIND_AUTO_CREATE);

        // Create the fragment instances
        homeFragment = new HomeFragment();
        settingsFragment = new SettingsFragment();

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, homeFragment, "HOME");
            setTitle("Home - NotiPhone");
            transaction.add(R.id.fragment_container, settingsFragment, "SETTINGS").hide(settingsFragment);
            transaction.commit();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from WatchService
        unbindService(mConnection);
    }

    public void ignoreBatteryOptimisations() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            Toast.makeText(this, "Ignoring battery optimizations!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Not ignoring battery optimizations!", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Could not open the battery optimization settings!
                // Fallback to something else.
                Log.d(TAG, "ignoreBatteryOptimisations: Couldn't open battery optimisation activity!");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case CONNECT_DEVICE:
                if (resultCode == RESULT_OK) {
                    if (watchServiceBound) {
                        String bluetoothAddress = data.getStringExtra("BluetoothAddress");
                        watchService.connect(bluetoothAddress);
                    } else {
                        Log.e(TAG, "onActivityResult: WatchService not bound!");
                        Toast.makeText(MainActivity.this, "There was an error connecting to the device!", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public static class MainHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MainHandler(MainActivity mainActivity) {
            this.mActivity = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mActivity.get();
            if (mainActivity != null) {
                switch (msg.what) {
                    case HANDLER_WATCH_CONNECTED:
                        String[] data = (String[]) msg.obj;
                        HomeFragment homeFragment = mainActivity.homeFragment;
                        homeFragment.setStatus(data[0], data[1]);
                        break;
                    case HANDLER_WATCH_DISCONNECTED:
                        mainActivity.homeFragment.setStatus("Watch not connected", "Click to connect a new watch");
                        break;
                    case HANDLER_WATCH_CONNECTION_FAILED:

                        break;
                    case HANDLER_WATCH_CONNECTING:

                        break;

                    case 69:
                        mainActivity.homeFragment.setStatus("LEM7", (String) msg.obj);
                        break;
                    default:

                        break;
                }
            }
        }
    }
}
