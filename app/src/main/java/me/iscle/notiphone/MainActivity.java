package me.iscle.notiphone;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import me.iscle.notiphone.Activities.ConnectDeviceActivity;
import me.iscle.notiphone.Activities.IntroActivity;
import me.iscle.notiphone.Fragments.FilesFragment;
import me.iscle.notiphone.Fragments.HomeFragment;
import me.iscle.notiphone.Fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("NotiPhone");

        Button debugButton = findViewById(R.id.debug_button);
        debugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });

        Button btActivityButton = findViewById(R.id.bt_activity_button);
        btActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ConnectDeviceActivity.class);
                startActivity(intent);
            }
        });

        Button introActivityButton = findViewById(R.id.intro_activity_button);
        introActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), IntroActivity.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationListener);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new HomeFragment());
        transaction.commit();
    }

    OnNavigationItemSelectedListener navigationListener = new OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment newFragment = null;

            switch (menuItem.getItemId()) {
                case R.id.navigation_home:
                    newFragment = new HomeFragment();
                    break;
                case R.id.navigation_files:
                    newFragment = new FilesFragment();
                    break;
                case R.id.navigation_settings:
                    newFragment = new SettingsFragment();
                    break;
            }

            transaction.replace(R.id.fragment_container, newFragment);
            transaction.commit();

            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                new LibsBuilder()
                        .withActivityTitle("About")
                        .withAboutAppName("NotiPhone")
                        .withAboutIconShown(true)
                        .withAboutDescription("NotiPhone is an app created to...")
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withLicenseShown(true)
                        .start(this);
                break;
        }

        return true;
    }
}
