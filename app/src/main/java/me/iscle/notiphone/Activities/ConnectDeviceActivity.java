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

public class ConnectDeviceActivity extends AppCompatActivity {
    private static final String TAG = "ConnectDeviceActivity";

    BluetoothRecyclerViewAdapter bluetoothRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        getSupportActionBar().setTitle("Select a device - NotiPhone");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();

        View.OnClickListener bluetoothClickListener = v -> {
            BluetoothRecyclerViewAdapter.ViewHolder vh = (BluetoothRecyclerViewAdapter.ViewHolder) v.getTag();
            BluetoothDevice bd = vh.getDevice();
            Toast.makeText(v.getContext(), bd.getName(), Toast.LENGTH_SHORT).show();
        };

        RecyclerView lv = findViewById(R.id.bluetooth_device_list);
        bluetoothRecyclerViewAdapter = new BluetoothRecyclerViewAdapter(this, new ArrayList<>(ba.getBondedDevices()), bluetoothClickListener);
        lv.setAdapter(bluetoothRecyclerViewAdapter);
        lv.setLayoutManager(new LinearLayoutManager(this));
        lv.addItemDecoration(new DividerItemDecoration(lv.getContext(), DividerItemDecoration.VERTICAL));

        Button button = findViewById(R.id.addItem);
        button.setOnClickListener((v -> {
            bluetoothRecyclerViewAdapter.addItem((new ArrayList<>(ba.getBondedDevices())).get(0));
        }));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
