package me.iscle.notiphone.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.iscle.notiphone.Adapters.BluetoothRecyclerViewAdapter;
import me.iscle.notiphone.R;

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

        // Get the default bluetooth adapter
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();

        // Get the bonded bluetooth devices
        ArrayList<BluetoothDevice> bondedDevices = new ArrayList<>(ba.getBondedDevices());

        // Create a new adapter for the RecyclerView and attach an onClickListener to it
        bluetoothRecyclerViewAdapter = new BluetoothRecyclerViewAdapter(bondedDevices, this);

        RecyclerView btDevicesView = findViewById(R.id.bluetooth_device_list);
        btDevicesView.setLayoutManager(new LinearLayoutManager(btDevicesView.getContext()));
        btDevicesView.addItemDecoration(new DividerItemDecoration(btDevicesView.getContext(), DividerItemDecoration.VERTICAL));
        btDevicesView.setAdapter(bluetoothRecyclerViewAdapter);

        Button button = findViewById(R.id.addItem);
        button.setOnClickListener((v -> {
            bluetoothRecyclerViewAdapter.addItem(bondedDevices.get(0));
        }));
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

        // Get the BluetoothDevice from that item
        BluetoothDevice bd = vh.getDevice();

        // Do whatever
        Toast.makeText(v.getContext(), "Selected: " + bd.getName(), Toast.LENGTH_SHORT).show();
    }
}
