package me.iscle.notiphone.onboarding;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import me.iscle.notiphone.R;

public class OnboardingLocationFragment extends OnboardingPageFragment {

    private final static int REQUEST_LOCATION_ACCESS = 1;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        image.setImageResource(R.drawable.undraw_signal);
        title.setText("Bluetooth permissions");
        description.setText("NotiPhone requires access to device location in order to use Bluetooth.");
        button.setVisibility(View.VISIBLE);
        button.setText("Grant location access");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_ACCESS);
            }
        });
    }

    private boolean hasLocationAccess() {
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_ACCESS) {
            boolean hasLocationAccess = hasLocationAccess();
            if (hasLocationAccess) getOnboardingActivity().nextPage();
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
