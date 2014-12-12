package com.chukong.sdk.service;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.chukong.sdk.Constants.Config;
import com.chukong.sdk.common.Log;
import com.chukong.sdk.dns.UDPSocketMonitor;
import com.chukong.sdk.serv.WebServer;
import com.chukong.sdk.serv.WebServer.OnWebServListener;
import com.chukong.sdk.util.CommonUtil;
import com.chukong.sdk.wifiap.WifiApManager;

/**
 * @brief Web Service后台
 * @author join
 */
public class WebService extends Service implements OnWebServListener {

    static final String TAG = "WebService";
    static final boolean DEBUG = false || Config.DEV_MODE || true;

    /** 错误时自动恢复的次数。如果仍旧异常，则继续传递。 */
    private static final int RESUME_COUNT = 3;
    /** 错误时重置次数的时间间隔。 */
    private static final int RESET_INTERVAL = 3000;
    private int errCount = 0;
    private Timer mTimer = new Timer(true);
    private TimerTask resetTask;

    private OnWebServListener mListener;

    private boolean isRunning = false;

    private NotificationManager mNM;

    // private int NOTI_SERV_RUNNING = R.string.noti_serv_running;

    private WebServer mWebServer = null;
    private UDPSocketMonitor mUDPSocketMonitor = null;

    private LocalBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public WebService getService() {
            return WebService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG)
            Log.d(TAG,
                    String.format("create server: port=%d, root=%s", Config.PORT, Config.WEBROOT));
        String localAddress = CommonUtil.getSingleton().getLocalIpAddress();
        Log.d("taugin", "localAddress = " + localAddress);
        mWebServer = new WebServer(Config.PORT, Config.WEBROOT);
        mWebServer.setOnWebServListener(this);
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Log.TAG, "intent = " + intent);
        // openWebServer();
        return mBinder;
    }

    private void openWebServer() {
        if (mWebServer != null) {
            mWebServer.setDaemon(true);
            mWebServer.start();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(Log.TAG, "intent = " + intent);
        // closeWebServer();
        return super.onUnbind(intent);
    }

    private void closeWebServer() {
        if (mWebServer != null) {
            mWebServer.close();
            mWebServer = null;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(Log.TAG, "");
        closeServer();
        super.onDestroy();
    }

    @Override
    public void onStarted() {
        if (DEBUG)
            Log.d(TAG, "onStarted");
        // showNotification(NOTI_SERV_RUNNING, R.drawable.ic_noti_running);
        Log.d(Log.TAG, "mListener = " + mListener);
        if (mListener != null) {
            mListener.onStarted();
        }
        isRunning = true;
    }

    @Override
    public void onStopped() {
        if (DEBUG)
            Log.d(TAG, "onStopped");
        //mNM.cancel(NOTI_SERV_RUNNING);
        // stopForeground(true);
        Log.d(Log.TAG, "mListener = " + mListener);
        if (mListener != null) {
            mListener.onStopped();
        }
        isRunning = false;
    }

    @Override
    public void onError(int code) {
        if (DEBUG)
            Log.d(TAG, "onError");
        if (code != WebServer.ERR_UNEXPECT) {
            if (mListener != null) {
                mListener.onError(code);
            }
            return;
        }
        errCount++;
        restartResetTask(RESET_INTERVAL);
        if (errCount <= RESUME_COUNT) {
            if (DEBUG)
                Log.d(TAG, "Retry times: " + errCount);
            openWebServer();
        } else {
            if (mListener != null) {
                mListener.onError(code);
            }
            errCount = 0;
            cancelResetTask();
        }
    }

    private void cancelResetTask() {
        if (resetTask != null) {
            resetTask.cancel();
            resetTask = null;
        }
    }

    private void restartResetTask(long delay) {
        cancelResetTask();
        resetTask = new TimerTask() {
            @Override
            public void run() {
                errCount = 0;
                resetTask = null;
                if (DEBUG)
                    Log.d(TAG, "ResetTask executed.");
            }
        };
        mTimer.schedule(resetTask, delay);
    }

    /*
    @SuppressWarnings("deprecation")
    private void showNotification(int resId, int iconId) {
        CharSequence text = getText(resId);

        Notification notification = new Notification(iconId, text, System.currentTimeMillis());

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                WSActivity.class), 0);

        notification.setLatestEventInfo(this, getText(R.string.app_name), text, contentIntent);
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        //mNM.notify(resId, notification);
        startForeground(resId, notification);
    } */

    public boolean isRunning() {
        return isRunning;
    }

    public void setOnWebServListener(OnWebServListener mListener) {
        Log.d(Log.TAG, "mListener = " + mListener);
        this.mListener = mListener;
    }

    private void openDnsServer() {
        String localAddress = CommonUtil.getSingleton().getLocalIpAddress();
        mUDPSocketMonitor = new UDPSocketMonitor(localAddress, 7755);
        mUDPSocketMonitor.start();
    }
    private void closeDnsServer() {
        if (mUDPSocketMonitor != null) {
            mUDPSocketMonitor.close();
            mUDPSocketMonitor = null;
        }
    }
    
    public void openServer() {
        openWebServer();
        if (WifiApManager.getInstance(this).isWifiApEnabled()) {
            openDnsServer();
        }
    }
    public void closeServer() {
        closeWebServer();
        if (WifiApManager.getInstance(this).isWifiApEnabled()) {
            closeDnsServer();
        }
    }
}
