package me.iscle.notiphone.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import me.iscle.notiphone.onboarding.OnboardingPageFragment;

public class OnboardingAdapter extends FragmentStateAdapter {

    private List<OnboardingPageFragment> onboardingPageFragments;

    public OnboardingAdapter(FragmentActivity fragmentActivity, List<OnboardingPageFragment> onboardingPageFragments) {
        super(fragmentActivity);
        this.onboardingPageFragments = onboardingPageFragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return onboardingPageFragments.get(position);
    }

    @Override
    public int getItemCount() {
        return onboardingPageFragments.size();
    }
}
