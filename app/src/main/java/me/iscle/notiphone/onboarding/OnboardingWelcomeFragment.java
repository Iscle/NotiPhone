package me.iscle.notiphone.onboarding;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.iscle.notiphone.R;

public class OnboardingWelcomeFragment extends OnboardingPageFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        image.setImageResource(R.drawable.undraw_setup);
        title.setText("Welcome to NotiPhone");
        description.setText("NotiPhone is part of NotiSuite, a pair of apps that will allow your watch to get in sync with your phone in a simple and easy way.\nBefore we can start, there are a few permissions that need to be granted for the app to work as intended.");
    }
}
