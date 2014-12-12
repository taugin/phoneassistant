package com.android.phoneassistant.webserver.recevier;

import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.android.phoneassistant.webserver.listener.OnWsListener;
import com.chukong.sdk.Constants.Config;
import com.chukong.sdk.common.Log;

/**
 * 应用广播接收者
 * @author join
 */
public class WSReceiver extends BroadcastReceiver {

    static final String TAG = "WSReceiver";
    static final boolean DEBUG = false || Config.DEV_MODE;

    public static final String ACTION_SERV_AVAILABLE = "org.join.action.SERV_AVAILABLE";
    public static final String ACTION_SERV_UNAVAILABLE = "org.join.action.SERV_UNAVAILABLE";
    public static final String ACTION_WEBSERVER_START = "org.join.action.WEBSERVER_START";
    public static final String ACTION_WEBSERVER_STOP = "org.join.action.WEBSERVER_STOP";
    public static final String ACTION_WEBSERVER_ERROR = "org.join.action.WEBSERVER_ERROR";
    public static final String ACTION_WEBSERVER_RUNNING = "org.join.action.WEBSERVER_RUNNING";

    public static final String PERMIT_WS_RECEIVER = "org.join.ws.permission.WS_RECEIVER";

    private static Map<Context, WSReceiver> mReceiverMap = new HashMap<Context, WSReceiver>();

    private OnWsListener mListener;

    public WSReceiver(OnWsListener listener) {
        mListener = listener;
    }

    /**
     * 注册
     */
    public static void register(Context context, OnWsListener listener) {
        if (mReceiverMap.containsKey(context)) {
            if (DEBUG)
                Log.d(TAG, "This context already registered.");
            return;
        }

        WSReceiver receiver = new WSReceiver(listener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SERV_AVAILABLE);
        filter.addAction(ACTION_SERV_UNAVAILABLE);
        filter.addAction(ACTION_WEBSERVER_START);
        filter.addAction(ACTION_WEBSERVER_STOP);
        filter.addAction(ACTION_WEBSERVER_ERROR);
        filter.addAction(ACTION_WEBSERVER_RUNNING);
        context.registerReceiver(receiver, filter);

        mReceiverMap.put(context, receiver);

        if (DEBUG)
            Log.d(TAG, "WSReceiver registered.");
    }

    /**
     * 注销
     */
    public static void unregister(Context context) {
        WSReceiver receiver = mReceiverMap.remove(context);
        if (receiver != null) {
            context.unregisterReceiver(receiver);
            receiver = null;

            if (DEBUG)
                Log.d(TAG, "WSReceiver unregistered.");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DEBUG)
            Log.d(TAG, action);
        if (mListener == null) {
            return;
        }
        Log.d(Log.TAG, "action = " + action);
        if (ACTION_SERV_AVAILABLE.equals(action)) {
            mListener.onServAvailable();
        } else if (ACTION_SERV_UNAVAILABLE.equals(action)){ // ACTION_SERV_UNAVAILABLE
            mListener.onServUnavailable();
        }/* else if (ACTION_WEBSERVER_START.equals(action)) {
            mListener.onWebServerStart();
        } else if (ACTION_WEBSERVER_STOP.equals(action)) {
            mListener.onWebServerStop();
        } else if (ACTION_WEBSERVER_ERROR.equals(action)) {
            mListener.onWebServerError(intent.getIntExtra("error_code", 0));
        } else if (ACTION_WEBSERVER_RUNNING.equals(action)) {
            mListener.onWebServerRunning(intent.getBooleanExtra("server_running", false));
        }*/
    }

}
