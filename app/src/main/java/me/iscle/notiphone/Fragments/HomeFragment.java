package me.iscle.notiphone.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.squareup.picasso.Picasso;

import me.iscle.notiphone.Activities.NewDeviceActivity;
import me.iscle.notiphone.R;
import me.iscle.notiphone.Watch;

import static me.iscle.notiphone.Constants.BROADCAST_NOTIFICATION_POSTED;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private ScrollView scrollView;
    private TextView watchName;
    private TextView watchDescription;
    private ImageView csPreview;

    private BroadcastReceiver newNotificationListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            StatusBarNotification sbn = (StatusBarNotification) intent.getExtras().get("statusBarNotification");
            if (sbn.getNotification().getSmallIcon() == null) {
                Log.d(TAG, "onReceive: Small icon is null!");
            }

            if (sbn.getNotification().getLargeIcon() == null) {
                Log.d(TAG, "onReceive: Large icon is null!");
            }

            csPreview.setImageIcon(sbn.getNotification().getSmallIcon());
            csPreview.setColorFilter(sbn.getNotification().color);
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollView = view.findViewById(R.id.home_scroll);
        watchName = view.findViewById(R.id.watch_name);
        watchDescription = view.findViewById(R.id.watch_description);
        csPreview = view.findViewById(R.id.watch_image);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(newNotificationListener, new IntentFilter(BROADCAST_NOTIFICATION_POSTED));

        CardView deviceStatus = view.findViewById(R.id.device_status);
        deviceStatus.setOnClickListener(view1 -> getActivity().startActivityForResult(new Intent(getContext(), NewDeviceActivity.class), 1));

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void updateFullWatchStatus(Watch watch) {
        watchName.setText(watch.getName() + " (" + watch.getAddress() + ")");
        watchDescription.setText("Battery: " + watch.getBattery() + "%");
    }

    public void updateWatchBattery(Watch watch) {
        watchDescription.setText("Battery: " + watch.getBattery() + "%");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray("HOME_SCROLL_POSITION", new int[]{scrollView.getScrollX(), scrollView.getScrollY()});
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void updateWatchInfo(String name, String description) {
        watchName.setText(name);
        watchDescription.setText(description);
    }

    public void updateCSPreview(String previewUrl) {
        Picasso.get().load(previewUrl).into(csPreview);
    }
}
