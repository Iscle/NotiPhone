package me.iscle.notiphone.onboarding;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import me.iscle.notiphone.R;
import me.iscle.notiphone.activity.MainActivity;
import me.iscle.notiphone.adapter.OnboardingAdapter;
import me.iscle.notiphone.databinding.ActivityOnboardingBinding;
import me.iscle.notiphone.model.OnboardingPage;

public class OnboardingActivity extends FragmentActivity {
    private static final String TAG = "OnboardingActivity";

    private ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayList<OnboardingPageFragment> onboardingPageFragments = new ArrayList<>();
        onboardingPageFragments.add(new OnboardingWelcomeFragment());
        onboardingPageFragments.add(new OnboardingNotificationFragment());
        onboardingPageFragments.add(new OnboardingLocationFragment());
        onboardingPageFragments.add(new OnboardingDoneFragment());

        binding.pager.setAdapter(new OnboardingAdapter(this, onboardingPageFragments));
        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                binding.backBtn.animate()
                        .alpha(position == 0 ? 0f : 1f)
                        .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));

                binding.nextBtn.animate()
                        .alpha(position == binding.pager.getAdapter().getItemCount() - 1 ? 0f : 1f)
                        .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            }
        });
        new TabLayoutMediator(binding.tabIndicator, binding.pager, (tab, position) -> {}).attach();
        binding.backBtn.setAlpha(0f); // Start with the button hidden
        binding.backBtn.setOnClickListener(v -> previousPage());
        binding.nextBtn.setOnClickListener(v -> nextPage());
    }

    public void previousPage() {
        binding.pager.setCurrentItem(binding.pager.getCurrentItem() - 1);
    }

    public void nextPage() {
        binding.pager.setCurrentItem(binding.pager.getCurrentItem() + 1);
    }

    private void requestLocationAccess() {
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    /*binding.notificationAccess.setChecked(isGranted);
                    binding.notificationAccess.setEnabled(!isGranted);*/
                });
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

}