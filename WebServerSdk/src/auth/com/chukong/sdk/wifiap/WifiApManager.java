package com.chukong.sdk.wifiap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.preference.PreferenceManager;
import android.provider.Settings;


import com.chukong.sdk.common.Log;
import com.chukong.sdk.receiver.WifiApStateReceiver;

public class WifiApManager {
    public static final int OPEN_INDEX = 0;
    public static final int WPA_INDEX = 1;
    public static final int WPA2_INDEX = 2;

    private Context mContext;
    
    private static WifiApManager sWifiApAdmin = null;
    public static WifiApManager getInstance(Context context) {
        if (sWifiApAdmin == null) {
            sWifiApAdmin = new WifiApManager(context);
        }
        return sWifiApAdmin;
    }
    private WifiApManager(Context context) {
        mContext = context;
    }

    private boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Class<? extends WifiManager> wifiManagerClass = wifiManager.getClass();
            if (wifiManagerClass == null) {
                return false;
            }
            Method method = wifiManagerClass.getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            if (method == null) {
                return false;
            }
            Boolean result = (Boolean) method.invoke(wifiManager, wifiConfig, enabled);
            return result.booleanValue();
        } catch (NoSuchMethodException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalAccessException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalArgumentException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (InvocationTargetException e) {
            Log.d(Log.TAG, "e = " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public void setSoftApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiManager.getWifiState();
        if (enabled && ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||  
                (wifiState == WifiManager.WIFI_STATE_ENABLED))) {  
            wifiManager.setWifiEnabled(false);  
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("wifi_saved_state", 1).commit();
        } 
        setWifiApEnabled(wifiConfig, enabled);
        if (!enabled) {  
            int wifiSavedState = 0;  
            wifiSavedState = PreferenceManager.getDefaultSharedPreferences(mContext).getInt("wifi_saved_state", 0);
            if (wifiSavedState == 1) {  
                wifiManager.setWifiEnabled(true);  
                PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("wifi_saved_state", 0).commit();
            }  
        }  
    }
    public int getWifiApState() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Class<? extends WifiManager> wifiManagerClass = wifiManager.getClass();
            if (wifiManagerClass == null) {
                return WifiApStateReceiver.WIFI_AP_STATE_FAILED;
            }

            Method method = wifiManagerClass.getMethod("getWifiApState");
            if (method == null) {
                return WifiApStateReceiver.WIFI_AP_STATE_FAILED;
            }
            Integer result = (Integer) method.invoke(wifiManager);
            return result.intValue();
        } catch (NoSuchMethodException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalAccessException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalArgumentException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (InvocationTargetException e) {
            Log.d(Log.TAG, "e = " + e);
        }
        return WifiApStateReceiver.WIFI_AP_STATE_FAILED;
    }
    
    public boolean isWifiApEnabled() {
        boolean enabled = false;
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Class<? extends WifiManager> wifiManagerClass = wifiManager.getClass();
            if (wifiManagerClass == null) {
                return false;
            }
            Method method = wifiManagerClass.getDeclaredMethod("isWifiApEnabled");
            if (method == null) {
                return false;
            }
            enabled = (Boolean) method.invoke(wifiManager);
        } catch (NoSuchMethodException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalAccessException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalArgumentException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (InvocationTargetException e) {
            Log.d(Log.TAG, "e = " + e);
            e.printStackTrace();
        }
        return enabled;
    }
    
    public WifiConfiguration getWifiApConfiguration() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Class<? extends WifiManager> wifiManagerClass = wifiManager.getClass();
            if (wifiManagerClass == null) {
                return null;
            }
            Method method = wifiManagerClass.getDeclaredMethod("getWifiApConfiguration");
            if (method == null) {
                return null;
            }
            WifiConfiguration wifiConfiguration = (WifiConfiguration) method.invoke(wifiManager);
            return wifiConfiguration;
        } catch (NoSuchMethodException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalAccessException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalArgumentException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (InvocationTargetException e) {
            Log.d(Log.TAG, "e = " + e);
            e.printStackTrace();
        }
        return null;
    }

    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Class<? extends WifiManager> wifiManagerClass = wifiManager.getClass();
            if (wifiManagerClass == null) {
                return false;
            }
            Method method = wifiManagerClass.getDeclaredMethod("setWifiApConfiguration", WifiConfiguration.class);
            if (method == null) {
                return false;
            }
            Boolean result = (Boolean) method.invoke(wifiManager, wifiConfig);
            return result.booleanValue();
        } catch (NoSuchMethodException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalAccessException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalArgumentException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (InvocationTargetException e) {
            Log.d(Log.TAG, "e = " + e);
            e.printStackTrace();
        }
        return false;
    }

    public int getAuth(WifiConfiguration config) {
        for (int a = 0; a < config.allowedAuthAlgorithms.size(); a++) {
            if (config.allowedAuthAlgorithms.get(a)) {
                return a;
            }
        }
        return 0;
    }

    public WifiConfiguration createWifiInfo(String SSID, String password, int type) {
        Log.v(Log.TAG, "SSID = " + SSID + "## Password = " + password + "## Type = " + type);

        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = SSID;
/*
        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
*/
        // 分为三种情况：1没有密码2用wep加密3用wpa加密
        if (type == -1) {// WIFICIPHER_NOPASS
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == 1) {  //  WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = password;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == 0) {   // WIFICIPHER_WPA
            config.preSharedKey = password;
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }
    public WifiConfiguration getConfig(String ssid, String password, int type) {

        WifiConfiguration config = new WifiConfiguration();

        config.SSID = ssid;

        switch (type) {
            case OPEN_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                return config;

            case WPA_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.preSharedKey = password;
                return config;

            case WPA2_INDEX:
                config.allowedKeyManagement.set(/*KeyMgmt.WPA2_PSK*/4);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.preSharedKey = password;
                return config;
        }
        return null;
    }
    public static int getSecurityTypeIndex(WifiConfiguration wifiConfig) {
        if (wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return WPA_INDEX;
        } else if (wifiConfig.allowedKeyManagement.get(/*KeyMgmt.WPA2_PSK*/4)) {
            return WPA2_INDEX;
        }
        return OPEN_INDEX;
    }
}
