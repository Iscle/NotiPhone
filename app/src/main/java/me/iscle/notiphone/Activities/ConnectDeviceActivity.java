package me.iscle.notiphone.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import me.iscle.notiphone.R;

public class ConnectDeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        getSupportActionBar().setTitle("Select a device - NotiPhone");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
