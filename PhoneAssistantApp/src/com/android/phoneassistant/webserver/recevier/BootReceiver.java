package com.android.phoneassistant.webserver.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.chukong.sdk.Constants;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove(Constants.REDIRECT_STATUS).apply();
        }
    }

}
