package com.android.phoneassistant.webserver.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.android.phoneassistant.webserver.listener.OnNetworkListener;
import com.android.phoneassistant.webserver.listener.OnStorageListener;
import com.android.phoneassistant.webserver.recevier.NetworkReceiver;
import com.android.phoneassistant.webserver.recevier.StorageReceiver;
import com.android.phoneassistant.webserver.recevier.WSReceiver;
import com.chukong.sdk.Constants.Config;
import com.chukong.sdk.common.Log;
import com.chukong.sdk.util.CommonUtil;

/**
 * @brief 应用后台服务
 * @author join
 */
public class WSService extends Service implements OnNetworkListener, OnStorageListener {

    static final String TAG = "WSService";
    static final boolean DEBUG = true || Config.DEV_MODE;

    public static final String ACTION = "org.join.service.WS";

    public boolean isWebServAvailable = false;

    private boolean isNetworkAvailable;
    private boolean isStorageMounted;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Log.TAG, "");
        NetworkReceiver.register(this, this);
        StorageReceiver.register(this, this);

        CommonUtil mCommonUtil = CommonUtil.getSingleton();
        isNetworkAvailable = mCommonUtil.isNetworkAvailable();
        isStorageMounted = mCommonUtil.isExternalStorageMounted();

        isWebServAvailable = isNetworkAvailable && isStorageMounted;
        notifyWebServAvailable(isWebServAvailable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NetworkReceiver.unregister(this);
        StorageReceiver.unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(boolean isWifi) {
        isNetworkAvailable = true;
        notifyWebServAvailableChanged();
    }

    @Override
    public void onDisconnected() {
        isNetworkAvailable = false;
        notifyWebServAvailableChanged();
    }

    @Override
    public void onMounted() {
        isStorageMounted = true;
        notifyWebServAvailableChanged();
    }

    @Override
    public void onUnmounted() {
        isStorageMounted = false;
        notifyWebServAvailableChanged();
    }

    private void notifyWebServAvailable(boolean isAvailable) {
        if (DEBUG)
            Log.d(Log.TAG, "isAvailable:" + isAvailable);
        // Notify if web service is available.
        String action = isAvailable ? WSReceiver.ACTION_SERV_AVAILABLE
                : WSReceiver.ACTION_SERV_UNAVAILABLE;
        Intent intent = new Intent(action);
        sendBroadcast(intent, WSReceiver.PERMIT_WS_RECEIVER);
    }

    private void notifyWebServAvailableChanged() {
        boolean isAvailable = isNetworkAvailable && isStorageMounted;
        Log.d(Log.TAG, "isAvailable = " + isAvailable + " , isWebServAvailable = " + isWebServAvailable);
        if (isAvailable != isWebServAvailable) {
            notifyWebServAvailable(isAvailable);
            isWebServAvailable = isAvailable;
        }
    }

    public static void startWsService(Context context) {
        context.startService(new Intent(WSService.ACTION));
    }

    /**
     * @brief 停止全局服务
     */
    public static void stopWsService(Context context) {
        context.stopService(new Intent(WSService.ACTION));
    }
}
