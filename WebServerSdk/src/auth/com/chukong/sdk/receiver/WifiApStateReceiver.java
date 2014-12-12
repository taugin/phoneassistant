package com.chukong.sdk.receiver;

import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.chukong.sdk.common.Log;

public class WifiApStateReceiver extends BroadcastReceiver {

    public static final int WIFI_AP_STATE_UNKNOWN = -1;
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";

    private static Map<Context, WifiApStateReceiver> mReceiverMap = new HashMap<Context, WifiApStateReceiver>();
    private OnWifiApStateChangeListener mListener;
    
    public WifiApStateReceiver(OnWifiApStateChangeListener listener) {
        mListener = listener;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(Log.TAG, action);
        
        if (mListener == null) {
            return;
        }
        if (WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(EXTRA_WIFI_AP_STATE, WIFI_AP_STATE_UNKNOWN);
            mListener.onWifiApStateChanged(state);
        }
    }

    public static void register(Context context, OnWifiApStateChangeListener listener) {
        if (mReceiverMap.containsKey(context)) {
            return;
        }

        WifiApStateReceiver receiver = new WifiApStateReceiver(listener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
        context.registerReceiver(receiver, filter);

        mReceiverMap.put(context, receiver);

        Log.d(Log.TAG, "WifiApStateReceiver registered.");
    }
    
    public static void unregister(Context context) {
        WifiApStateReceiver receiver = mReceiverMap.remove(context);
        if (receiver != null) {
            context.unregisterReceiver(receiver);
            receiver = null;

            Log.d(Log.TAG, "WifiApStateReceiver unregistered.");
        }
    }
}
