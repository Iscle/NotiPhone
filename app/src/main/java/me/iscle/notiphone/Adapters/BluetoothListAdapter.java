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

    // Layout to use for each row
    private static final int ROW_LAYOUT = R.layout.bluetooth_device_row;
    
    public BluetoothListAdapter(Context context, ArrayList<BluetoothDevice> devices) {
        super(context, ROW_LAYOUT, devices);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // If the view is not already created, create it with the required row layout
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(ROW_LAYOUT, null);
        }

        // Get the bluetooth device from the list for this position
        BluetoothDevice device = getItem(position);

        // Set the device name
        TextView deviceNameView = convertView.findViewById(R.id.device_name);
        deviceNameView.setText(device.getName());

        // Set the device address
        TextView deviceAddressView = convertView.findViewById(R.id.device_address);
        deviceAddressView.setText(device.getAddress());

        // Return the populated view
        return convertView;
    }


}
