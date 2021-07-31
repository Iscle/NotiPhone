package me.iscle.notiphone.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.iscle.notiphone.R;
import me.iscle.notiphone.activity.MainActivity;

public class OnboardingDoneFragment extends OnboardingPageFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //pages.add(new OnboardingPage(R.drawable.undraw_sync, "We're done!",
        //                "",
        //                "Get Started!", v -> {
        //            startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
        //            finish();
        //        }));

        image.setImageResource(R.drawable.undraw_sync);
        title.setText("We're done!");
        description.setText("Great! You've done it! You can now sync a new device.");
        button.setVisibility(View.VISIBLE);
        button.setText("Get Started!");
        button.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), MainActivity.class));
            getActivity().finish();
        });
    }
}
