package me.iscle.notiphone.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import me.iscle.notiphone.Fragments.FilesFragment;
import me.iscle.notiphone.Fragments.HomeFragment;
import me.iscle.notiphone.Fragments.SettingsFragment;
import me.iscle.notiphone.R;
import me.iscle.notiphone.Services.WatchService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int CONNECT_DEVICE = 1;

    private WatchService watchService;
    private ServiceConnection mConnection;
    private boolean watchServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("NotiPhone");

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
            if (watchServiceBound) {
                watchService.write("Test".getBytes());
            }
        });

        Button btActivityButton = findViewById(R.id.bt_activity_button);
        btActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ConnectDeviceActivity.class);
            startActivityForResult(intent, CONNECT_DEVICE);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationListener);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new HomeFragment());
            transaction.commit();
        }
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
                newFragment = new HomeFragment();
                break;
            case R.id.navigation_files:
                newFragment = new FilesFragment();
                break;
            case R.id.navigation_settings:
                newFragment = new SettingsFragment();
                break;
            default:
                newFragment = new HomeFragment();
                break;
        }

        transaction.replace(R.id.fragment_container, newFragment);
        transaction.commit();

        return true;
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                new LibsBuilder()
                        .withActivityTitle("About")
                        .withAboutAppName("NotiPhone")
                        .withAboutIconShown(true)
                        // TODO: CHANGE DESCRIPTION
                        .withAboutDescription("NotiPhone is an app created to...")
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withLicenseShown(true)
                        .start(this);
                break;
        }

        return true;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

            }
        }
    };
}
