package me.iscle.notiphone.Adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.iscle.notiphone.R;

public class BluetoothListAdapter extends ArrayAdapter<BluetoothDevice> {
    private static final String TAG = "BluetoothListAdapter";
    
    public BluetoothListAdapter(Context context, ArrayList<BluetoothDevice> devices) {
        super(context, 0, devices);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d(TAG, "getView: method called!");
        if (convertView == null) {
            Log.d(TAG, "getView: convertView is null!");
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bluetooth_device_row, null);
        }

        BluetoothDevice device = getItem(position);

        String bluetoothName = device.getName();
        String bluetoothAddress = device.getAddress();

        TextView deviceName = convertView.findViewById(R.id.device_name);
        deviceName.setText(bluetoothName);

        return convertView;
    }


}
