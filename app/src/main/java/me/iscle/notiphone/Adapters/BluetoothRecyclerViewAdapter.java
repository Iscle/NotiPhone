package me.iscle.notiphone.Adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import me.iscle.notiphone.R;

public class BluetoothRecyclerViewAdapter extends RecyclerView.Adapter<BluetoothRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "BluetoothRecyclerViewAd";

    private ArrayList<BluetoothDevice> devices;
    private View.OnClickListener clickListener;

    public BluetoothRecyclerViewAdapter(ArrayList<BluetoothDevice> devices, View.OnClickListener clickListener) {
        this.devices = devices;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_row,
                parent, false);

        return new ViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindDevice(devices.get(position));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addItem(BluetoothDevice device) {
        devices.add(device);
        notifyItemInserted(devices.size() - 1);
    }

    public void removeItem(BluetoothDevice device) {
        int position = devices.indexOf(device);
        devices.remove(position);
        notifyItemRemoved(position);
    }

    public BluetoothDevice getItem(int position) {
        return devices.get(position);
    }

    public void setItems(Set<BluetoothDevice> devices) {
        this.devices = new ArrayList<>(devices);
    }

    public ArrayList<BluetoothDevice> getItems() {
        return devices;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceNameTv;
        private TextView deviceAddressTv;
        private BluetoothDevice device = null;

        private ViewHolder(@NonNull View itemView, View.OnClickListener clickListener) {
            super(itemView);
            itemView.setTag(this);
            deviceNameTv = itemView.findViewById(R.id.device_name);
            deviceAddressTv = itemView.findViewById(R.id.device_address);

            itemView.setOnClickListener(clickListener);
        }

        private void bindDevice(BluetoothDevice device) {
            deviceNameTv.setText(device.getName());
            deviceAddressTv.setText(device.getAddress());
            this.device = device;
        }

        public BluetoothDevice getDevice() {
            return device;
        }
    }
}