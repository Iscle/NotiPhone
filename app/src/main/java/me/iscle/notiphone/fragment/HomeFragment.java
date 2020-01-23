package me.iscle.notiphone.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import me.iscle.notiphone.activity.NewDeviceActivity;
import me.iscle.notiphone.R;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private ScrollView scrollView;
    private TextView watchName;
    private TextView watchDescription;
    private ImageView csPreview;

    private FrameLayout notificationFrame;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollView = view.findViewById(R.id.home_scroll);
        watchName = view.findViewById(R.id.watch_name);
        watchDescription = view.findViewById(R.id.watch_description);
        csPreview = view.findViewById(R.id.watch_image);

        notificationFrame = view.findViewById(R.id.notificationFrame);

        CardView deviceStatus = view.findViewById(R.id.device_status);
        deviceStatus.setOnClickListener(view1 -> getActivity().startActivityForResult(new Intent(getContext(), NewDeviceActivity.class), 1));
    }

    public FrameLayout getNotificationFrame() {
        return notificationFrame;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void setStatus(String name, String description) {
        if (watchName != null)
            watchName.setText(name);
        if (watchDescription != null)
            watchDescription.setText(description);
    }
}
