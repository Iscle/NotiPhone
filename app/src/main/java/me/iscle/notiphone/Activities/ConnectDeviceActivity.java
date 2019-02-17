package me.iscle.notiphone.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.iscle.notiphone.Adapters.BluetoothRecyclerViewAdapter;
import me.iscle.notiphone.R;

import static android.view.Window.FEATURE_INDETERMINATE_PROGRESS;

public class ConnectDeviceActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "ConnectDeviceActivity";

    private BluetoothRecyclerViewAdapter bluetoothRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        // Configure the Action Bar
        getSupportActionBar().setTitle("Select a device - NotiPhone");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set result as canceled in case user doesn't choose a device
        setResult(RESULT_CANCELED);

        // Get the default bluetooth adapter
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();

        // Create a new adapter for the RecyclerView and attach an onClickListener to it
        bluetoothRecyclerViewAdapter = new BluetoothRecyclerViewAdapter(new ArrayList<>(ba.getBondedDevices()), this);

        RecyclerView btDevicesView = findViewById(R.id.bluetooth_device_list);
        btDevicesView.setLayoutManager(new LinearLayoutManager(btDevicesView.getContext()));
        btDevicesView.addItemDecoration(new DividerItemDecoration(btDevicesView.getContext(), DividerItemDecoration.VERTICAL));
        btDevicesView.setAdapter(bluetoothRecyclerViewAdapter);

        Button startDiscovery = findViewById(R.id.start_discovery);
        startDiscovery.setOnClickListener(v -> {
            Log.d(TAG, "startDiscovery");

            LinearLayout buttonPanel = findViewById(R.id.button_panel);
            buttonPanel.setVisibility(View.INVISIBLE);
            ProgressBar scanningBar = findViewById(R.id.scanning_bar);
            scanningBar.setVisibility(View.VISIBLE);

            bluetoothRecyclerViewAdapter.setItems(new ArrayList<>(ba.getBondedDevices()));

            // Register for broadcasts when a device is discovered.
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, filter);

            // TODO: CHANGE THE FOLLOWING LINES
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!btAdapter.isDiscovering()) {
                btAdapter.startDiscovery();
            }
        });
    }

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

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(receiver);

            // TODO: CHANGE THE FOLLOWING LINES
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btAdapter.isDiscovering()) {
                btAdapter.cancelDiscovery();
                ProgressBar scanningBar = findViewById(R.id.scanning_bar);
                scanningBar.setVisibility(View.INVISIBLE);
                LinearLayout buttonPanel = findViewById(R.id.button_panel);
                buttonPanel.setVisibility(View.VISIBLE);
            }
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
        // Get the ViewHolder (item) that called the listener
        BluetoothRecyclerViewAdapter.ViewHolder vh = (BluetoothRecyclerViewAdapter.ViewHolder) v.getTag();

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
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
