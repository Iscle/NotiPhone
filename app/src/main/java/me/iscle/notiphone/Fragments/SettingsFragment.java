package me.iscle.notiphone.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import me.iscle.notiphone.Activities.NewDeviceActivity;
import me.iscle.notiphone.BuildConfig;
import me.iscle.notiphone.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int CONNECT_DEVICE = 1;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);

        Preference managePermissionsButton = findPreference("manage_permissions");
        managePermissionsButton.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getContext(), NewDeviceActivity.class));

            return true;
        });

        Preference feedbackButton = findPreference("feedback");
        feedbackButton.setOnPreferenceClickListener(preference -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:albertiscle9@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "NotiPhone Feedback");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "What do you think about the app?");

            try {
                startActivity(Intent.createChooser(emailIntent, "Send email using..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getContext(), "No email clients installed.", Toast.LENGTH_SHORT).show();
            }

            return true;
        });

        Preference aboutButton = findPreference("about");
        aboutButton.setOnPreferenceClickListener(preference -> {
            new LibsBuilder()
                    .withActivityTitle("About")
                    .withAboutAppName("NotiPhone")
                    .withAboutIconShown(true)
                    // TODO: CHANGE DESCRIPTION
                    .withAboutDescription("NotiPhone is an app created to...")
                    .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                    .withLicenseShown(true)
                    .withFields(R.string.class.getFields())
                    .start(getContext());
            return true;
        });

        Preference versionText = findPreference("version");
        versionText.setSummary(BuildConfig.VERSION_NAME);

        Preference pairDeviceButton = findPreference("pairDevice");
        pairDeviceButton.setSummary("Connected to LEM7");
        pairDeviceButton.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getContext(), NewDeviceActivity.class);
            startActivityForResult(intent, CONNECT_DEVICE);
            return true;
        });
    }

}