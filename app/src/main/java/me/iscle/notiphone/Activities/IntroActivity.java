package me.iscle.notiphone.Activities;

import android.Manifest;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import me.iscle.notiphone.Fragments.FilesFragment;

public class IntroActivity extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(new FilesFragment());
        addSlide(new FilesFragment());
        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        addSlide(new FilesFragment());

        showSkipButton(false);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
