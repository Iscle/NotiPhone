package me.iscle.notiphone.Activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.iscle.notiphone.Adapters.BluetoothRecyclerViewAdapter;
import me.iscle.notiphone.R;

public class NewDeviceActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "NewDeviceActivity";

    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private BluetoothRecyclerViewAdapter bluetoothRecyclerViewAdapter;
    // Create a BroadcastReceiver for ACTION_FOUND and ACTION_DISCOVERY_FINISHED.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    bluetoothRecyclerViewAdapter.addItem(device);
                    Log.d(TAG, "onReceive: New device found. " + device.getName());
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d(TAG, "onReceive: Discovery finished.");
                    unregisterReceiver(receiver);

                    ProgressBar scanningBar = findViewById(R.id.scanning_bar);
                    scanningBar.setVisibility(View.INVISIBLE);
                    LinearLayout buttonPanel = findViewById(R.id.button_panel);
                    buttonPanel.setVisibility(View.VISIBLE);

                    break;
            }
        }
    };
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_device);

        // Configure the Action Bar
        getSupportActionBar().setTitle("Select a device - NotiPhone");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set result as canceled in case user doesn't choose a device
        setResult(RESULT_CANCELED);

        // Get the default bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Create a new adapter for the RecyclerView and attach an onClickListener to it
        bluetoothRecyclerViewAdapter = new BluetoothRecyclerViewAdapter(new ArrayList<>(bluetoothAdapter.getBondedDevices()), this);

        RecyclerView btDevicesView = findViewById(R.id.bluetooth_device_list);
        btDevicesView.setLayoutManager(new LinearLayoutManager(btDevicesView.getContext()));
        btDevicesView.addItemDecoration(new DividerItemDecoration(btDevicesView.getContext(), DividerItemDecoration.VERTICAL));
        btDevicesView.setAdapter(bluetoothRecyclerViewAdapter);

        Button startDiscovery = findViewById(R.id.start_discovery);
        startDiscovery.setOnClickListener(v -> {
            Log.d(TAG, "startDiscovery");

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_PERMISSION_REQUEST);
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_PERMISSION_REQUEST);
                }

                return;
            }

            LinearLayout buttonPanel = findViewById(R.id.button_panel);
            buttonPanel.setVisibility(View.INVISIBLE);
            ProgressBar scanningBar = findViewById(R.id.scanning_bar);
            scanningBar.setVisibility(View.VISIBLE);

            bluetoothRecyclerViewAdapter.setItems(new ArrayList<>(bluetoothAdapter.getBondedDevices()));

            // Register for broadcasts when a device is discovered.
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, filter);

            // TODO: CHANGE THE FOLLOWING LINES
            if (!bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.startDiscovery();
            }
        });
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(receiver);

            // TODO: CHANGE THE FOLLOWING LINES
            bluetoothAdapter.cancelDiscovery();
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "onDestroy: Receiver already unregistered.");
        }
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View v) {
        // Get the FileViewHolder (item) that called the listener
        BluetoothRecyclerViewAdapter.ViewHolder vh = (BluetoothRecyclerViewAdapter.ViewHolder) v.getTag();

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            ProgressBar scanningBar = findViewById(R.id.scanning_bar);
            scanningBar.setVisibility(View.INVISIBLE);
            LinearLayout buttonPanel = findViewById(R.id.button_panel);
            buttonPanel.setVisibility(View.VISIBLE);
        }

        // Get the BluetoothDevice from that item
        BluetoothDevice bd = vh.getDevice();

        Intent intent = new Intent();
        intent.putExtra("BluetoothName", bd.getName());
        intent.putExtra("BluetoothAddress", bd.getAddress());
        setResult(RESULT_OK, intent);
        finish();
    }
}
