package me.iscle.notiphone.adapter;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import me.iscle.notiphone.R;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private ArrayList<BluetoothDevice> devices;
    private Set<BluetoothDevice> devicesSet;
    private View.OnClickListener clickListener;

    public BluetoothDeviceAdapter(ArrayList<BluetoothDevice> devices, View.OnClickListener clickListener) {
        this.devices = devices;
        this.devicesSet = new HashSet<>(devices);
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
        if (!devicesSet.contains(device)) {
            devicesSet.add(device);
            devices.add(device);
            notifyItemInserted(devices.size() - 1);
        }
    }

    public void setItems(ArrayList<BluetoothDevice> devices) {
        this.devices = devices;
        this.devicesSet = new HashSet<>(devices);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceNameTv;
        private TextView deviceAddressTv;
        private BluetoothDevice device;

        public ViewHolder(@NonNull View itemView, View.OnClickListener clickListener) {
            super(itemView);
            itemView.setTag(this);
            deviceNameTv = itemView.findViewById(R.id.device_name);
            deviceAddressTv = itemView.findViewById(R.id.device_address);
            device = null;

            itemView.setOnClickListener(clickListener);
        }

        public void bindDevice(BluetoothDevice device) {
            this.deviceNameTv.setText(device.getName());
            this.deviceAddressTv.setText(device.getAddress());
            this.device = device;
        }

        public BluetoothDevice getDevice() {
            return device;
        }
    }
}