package me.iscle.notiphone.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import me.iscle.notiphone.Adapters.BluetoothListAdapter;
import me.iscle.notiphone.R;

public class ConnectDeviceActivity extends AppCompatActivity {
    private static final String TAG = "ConnectDeviceActivity";

    BluetoothListAdapter bluetoothListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        getSupportActionBar().setTitle("Select a device - NotiPhone");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();

        ListView lv = findViewById(R.id.bluetooth_device_list);
        bluetoothListAdapter = new BluetoothListAdapter(this, new ArrayList<>(ba.getBondedDevices()));
        lv.setAdapter(bluetoothListAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: " + bluetoothListAdapter.getItem(position).getName());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
