package net.gps.tracker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {

    private final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(BOOT_COMPLETED_ACTION)) {
            Intent myIntent = new Intent(context, net.gps.tracker.service.Main.class);
            context.startService(myIntent);
        }

    }
}
