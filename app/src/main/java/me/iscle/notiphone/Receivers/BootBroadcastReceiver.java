package me.iscle.notiphone.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import me.iscle.notiphone.App;
import me.iscle.notiphone.Services.WatchService;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(App.SERVICE_PREFERENCES, Context.MODE_PRIVATE);

            if (sharedPreferences.getBoolean("autoStartService", false)) {
                return;
            }

            Log.d(TAG, "onReceive: Starting WatchService!");

            Intent serviceIntent = new Intent(context, WatchService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
