package me.iscle.notiphone.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.iscle.notiphone.R;

public class OnboardingNotificationFragment extends OnboardingPageFragment {

    private final static int REQUEST_NOTIFICATION_ACCESS = 1;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        image.setImageResource(R.drawable.undraw_social_notifications);
        title.setText("Notification access");
        description.setText("NotiPhone requires access to this device's notifications in order to send them to the watch.");
        button.setVisibility(View.VISIBLE);
        button.setText("Grant notification access");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), REQUEST_NOTIFICATION_ACCESS);
            }
        });
    }

    private boolean hasNotificationAccess() {
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_NOTIFICATION_ACCESS) {
            boolean hasNotificationAccess = hasNotificationAccess();
            if (hasNotificationAccess) getOnboardingActivity().nextPage();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
