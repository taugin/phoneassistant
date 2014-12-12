package com.android.phoneassistant.webserver.activity;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.chukong.sdk.common.Log;
import com.chukong.sdk.serv.WebServer.OnWebServListener;
import com.chukong.sdk.service.WebService;

/**
 * @brief 绑定Web Service的抽象Activity
 * @author join
 */
public abstract class WebServActivity extends Activity implements OnWebServListener {

    static final String TAG = "WebServActivity";

    protected Intent webServIntent;
    protected WebService webService;
    private boolean isBound = false;

    private ServiceConnection servConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Log.TAG, "");
            webService = ((WebService.LocalBinder) service).getService();
            webService.setOnWebServListener(WebServActivity.this);
            webService.openServer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(Log.TAG, "");
            webService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webServIntent = new Intent(this, WebService.class);
    }

    protected boolean isBound() {
        return this.isBound;
    }

    protected void doBindService() {
        // Restore configs of port and root here.
        Log.d(Log.TAG, "");
        PreferActivity.restore(this, PreferActivity.KEY_SERV_PORT,
                PreferActivity.KEY_SERV_ROOT);
        bindService(webServIntent, servConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    protected void doUnbindService() {
        Log.d(Log.TAG, "isBound = " + isBound);
        if (isBound) {
            unbindService(servConnection);
            isBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }
}
