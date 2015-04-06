package com.android.phoneassistant.webserver.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.android.phoneassistant.R;
import com.chukong.sdk.Constants;
import com.chukong.sdk.common.Log;
import com.chukong.sdk.receiver.OnWifiApStateChangeListener;
import com.chukong.sdk.receiver.WifiApStateReceiver;
import com.chukong.sdk.wifiap.WifiApManager;

public class HotpotHelper implements OnWifiApStateChangeListener, OnDismissListener{

    private static final int DLG_PROGRESS_TIMEOUT = 30 * 1000;

    private ProgressDialog mProgressDialog = null;
    private Context mContext;
    private Handler mHandler;
    private OnHotpotStateListener mOnHotpotStateListener;
    public HotpotHelper(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        WifiApStateReceiver.register(context, this);
    }

    @Override
    public void onWifiApStateChanged(int state) {
        if (state == WifiApStateReceiver.WIFI_AP_STATE_ENABLED) {
            WifiConfiguration config = WifiApManager.getInstance(mContext).getWifiApConfiguration();
            if (config != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDlg();
                    }
                }, 1000);
            }
        } else if (state == WifiApStateReceiver.WIFI_AP_STATE_DISABLED) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissProgressDlg();
                }
            }, 1000);
        }
    }

    public void showProgressDlg(boolean start) {
        String message = mContext.getResources().getString(start ? R.string.starting_server : R.string.stoping_server);
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(message);
            mProgressDialog.setOnDismissListener(this);
        }
        mProgressDialog.show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissProgressDlg();
            }
        }, DLG_PROGRESS_TIMEOUT);
    }

    public void dismissProgressDlg() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @SuppressLint("NewApi")
    public void setWifiApEnabled(boolean enabled) {
        showProgressDlg(enabled);
        boolean apEnable = WifiApManager.getInstance(mContext).isWifiApEnabled();
        Log.d(Log.TAG, "apEnable = " + apEnable);
        if (apEnable == enabled) {
            dismissProgressDlg();
            return ;
        }
        if (enabled) {
            WifiConfiguration oldConfig = WifiApManager.getInstance(mContext).getWifiApConfiguration();
            Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
            Log.d(Log.TAG, "+++++++++++++++++++++++++++++++++++oldConfig.SSID = " + oldConfig.SSID + " , oldConfig.preShareKey = " + oldConfig.preSharedKey);
            editor.putString(Constants.KEY_SAVED_SSID, oldConfig.SSID);
            editor.putString(Constants.KEY_SAVED_PASS, oldConfig.preSharedKey);
            editor.putInt(Constants.KEY_SECURITY_TYPE, WifiApManager.getSecurityTypeIndex(oldConfig));
            editor.apply();
            String SSID = "Chukong-Share";
            WifiConfiguration config = WifiApManager.getInstance(mContext).getConfig(SSID, null, WifiApManager.OPEN_INDEX);
            Log.d(Log.TAG, "config =  " + config.SSID);
            WifiApManager.getInstance(mContext).setWifiApConfiguration(config);
            WifiApManager.getInstance(mContext).setSoftApEnabled(null, enabled);
        } else {
            String SSID = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.KEY_SAVED_SSID, null);
            String pass = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.KEY_SAVED_PASS, null);
            int securityType = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(Constants.KEY_SECURITY_TYPE, WifiApManager.OPEN_INDEX);
            WifiConfiguration config = WifiApManager.getInstance(mContext).getConfig(SSID, pass, securityType);
            Log.d(Log.TAG, "+++++++++++++++++++++++++++++++++++ssid = " + SSID + " , preSharedKey = " + pass + ", securityType = " + securityType);
            // 还原原来的SSID会导致重启
            WifiApManager.getInstance(mContext).setSoftApEnabled(null, enabled);
            WifiApManager.getInstance(mContext).setWifiApConfiguration(config);
        }
    }
    
    public boolean hotpotEnabled() {
        return WifiApManager.getInstance(mContext).isWifiApEnabled();
    }

    public void setOnHotpotStateListener(OnHotpotStateListener l) {
        mOnHotpotStateListener = l;
    }

    public interface OnHotpotStateListener {
        public void onHotpotState(boolean enable);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        boolean apEnable = WifiApManager.getInstance(mContext).isWifiApEnabled();
        if (mOnHotpotStateListener != null) {
            mOnHotpotStateListener.onHotpotState(apEnable);
        }
    }
}
