package me.iscle.notiphone.Activities;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;


import java.lang.ref.WeakReference;
import java.util.TimerTask;

import me.iscle.notiphone.Fragments.FilesFragment;
import me.iscle.notiphone.Fragments.HomeFragment;
import me.iscle.notiphone.Fragments.SettingsFragment;
import me.iscle.notiphone.Model.Capsule;
import me.iscle.notiphone.Model.PhoneNotification;
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
    private ServiceConnection mConnection;
    private boolean watchServiceBound = false;

    private HomeFragment homeFragment;
    private FilesFragment filesFragment;
    private SettingsFragment settingsFragment;

    private Thread infoThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the ActionBar title
        getSupportActionBar().setTitle("NotiPhone");

        // Create the fragment instances
        homeFragment = new HomeFragment();
        filesFragment = new FilesFragment();
        settingsFragment = new SettingsFragment();

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, homeFragment, "HOME");
            transaction.add(R.id.fragment_container, filesFragment, "FILES");
            transaction.add(R.id.fragment_container, settingsFragment, "SETTINGS");
            transaction.hide(filesFragment);
            transaction.hide(settingsFragment);
            transaction.commit();
        }

        mConnection = new ServiceConnection() {
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

        Intent serviceIntent = new Intent(this, WatchService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // Bind to WatchService
        bindService(new Intent(this, WatchService.class), mConnection, Context.BIND_AUTO_CREATE);

        Button debugButton = findViewById(R.id.debug_button);
        debugButton.setOnClickListener(v -> {
            watchService.setTestMessage();
            PhoneNotification notification = new PhoneNotification(659, "Test from phone", "Text from phone notification");
            watchService.write(new Capsule(1, new Gson().toJson(notification)).toJSON());
            //ignoreBatteryOptimisations();
        });

        Button btActivityButton = findViewById(R.id.bt_activity_button);
        btActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ConnectDeviceActivity.class);
            startActivityForResult(intent, CONNECT_DEVICE);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationListener);

        infoThread = new Thread(() -> {

        });
    }

    public void ignoreBatteryOptimisations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        watchServiceBound = false;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case CONNECT_DEVICE:
                if (resultCode == RESULT_OK) {
                    String bluetoothName = data.getStringExtra("BluetoothName");
                    String bluetoothAddress = data.getStringExtra("BluetoothAddress");

                    if (watchServiceBound) {
                        watchService.connect(bluetoothAddress);
                    } else {
                        Log.e(TAG, "onActivityResult: WatchService not bound!");
                    }
                    Log.d(TAG, "onActivityResult (CONNECT_DEVICE): Selected " + bluetoothName + ":" + bluetoothAddress);
                } else {
                    // Not connected
                    Log.d(TAG, "onActivityResult (CONNECT_DEVICE): Canceled");
                }
                break;
        }
    }

    OnNavigationItemSelectedListener navigationListener = menuItem -> {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment newFragment;

        switch (menuItem.getItemId()) {
            case R.id.navigation_home:
                transaction.show(homeFragment);
                transaction.hide(filesFragment);
                transaction.hide(settingsFragment);
                break;
            case R.id.navigation_files:
                transaction.hide(homeFragment);
                transaction.show(filesFragment);
                transaction.hide(settingsFragment);
                break;
            case R.id.navigation_settings:
                transaction.hide(homeFragment);
                transaction.hide(filesFragment);
                transaction.show(settingsFragment);
                break;
            default:
                transaction.show(homeFragment);
                transaction.hide(filesFragment);
                transaction.hide(settingsFragment);
                break;
        }

        transaction.commit();

        return true;
    };

    private final MainHandler mHandler = new MainHandler(this);

    public static class MainHandler extends Handler {
        private final WeakReference<MainActivity> mainActivity;

        public MainHandler(MainActivity mainActivity) {
            this.mainActivity = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mainActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case HANDLER_WATCH_CONNECTED:
                        HomeFragment homeFragment = activity.homeFragment;
                        homeFragment.updateWatchInfo("Connected to: LEM7", "Remaining battery: 69%");
                        break;
                    case HANDLER_WATCH_DISCONNECTED:

                        break;
                    case HANDLER_WATCH_CONNECTION_FAILED:

                        break;
                    case HANDLER_WATCH_CONNECTING:

                        break;

                    case 69:
                        activity.homeFragment.updateWatchInfo("LEM7", (String) msg.obj);
                        break;
                    default:

                        break;
                }
            }
        }
    }

    private static class InfoThread extends TimerTask {
        @Override
        public void run() {

        }
    }
}
