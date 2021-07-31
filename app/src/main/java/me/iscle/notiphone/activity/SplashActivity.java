package me.iscle.notiphone.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import me.iscle.notiphone.Constants;
import me.iscle.notiphone.R;
import me.iscle.notiphone.onboarding.OnboardingActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (BluetoothAdapter.getDefaultAdapter() == null) { // Check if device has bluetooth
            new AlertDialog.Builder(this)
                    .setTitle("Bluetooth is required!")
                    .setMessage("Bluetooth adapter not found, the app will now close.")
                    .setPositiveButton("Exit", (dialog, which) -> finish())
                    .show();
            return;
        }

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(Constants.PREFERENCE_FIRST_RUN, true)) {
            startActivity(new Intent(this, OnboardingActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}
