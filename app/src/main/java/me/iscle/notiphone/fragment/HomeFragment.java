package me.iscle.notiphone.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import me.iscle.notiphone.NotiPhone;
import me.iscle.notiphone.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.deviceStatus.setOnClickListener(view1 -> Navigation.findNavController(view).navigate(HomeFragmentDirections.actionHomeFragmentToNewDeviceActivity()));
    }

    public FrameLayout getNotificationFrame() {
        return binding.notificationFrame;
    }

    public void setStatus(String name, String description) {
        binding.watchName.setText(name);
        binding.watchDescription.setText(description);
    }
}
