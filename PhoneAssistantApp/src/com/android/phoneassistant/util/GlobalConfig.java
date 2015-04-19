package com.android.phoneassistant.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.android.phoneassistant.R;
import com.google.gson.Gson;

public class GlobalConfig {
    public boolean debug;
    public String fontpath;

    private static GlobalConfig sGlobalConfig;

    public static GlobalConfig get() {
        return sGlobalConfig;
    }

    public static void initGlobalConfig(Context context) {
        boolean result = (parseFromSd() || parseFromRaw(context));
        if (sGlobalConfig != null) {
            Log.d("debug", sGlobalConfig.toString());
        }
    }

    private static boolean parseFromSd() {
        Log.d("debug", "parseFromSd");
        String configPath = null;
        StringBuilder configString = new StringBuilder();
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            File external = Environment.getExternalStorageDirectory();
            if (external != null) {
                String sdPath = external.getAbsolutePath();
                Log.d("debug", "external = " + sdPath);
                configPath = sdPath + File.separator + "global.conf";
            }
        }
        if (!TextUtils.isEmpty(configPath)) {
            File configFile = new File(configPath);
            if (configFile.exists()) {
                try {
                    byte buf[] = new byte[512];
                    int readLen = 0;
                    FileInputStream fis = new FileInputStream(configFile);
                    while ((readLen = fis.read(buf)) > 0) {
                        configString.append(new String(buf, 0, readLen));
                    }
                    fis.close();
                } catch (FileNotFoundException e) {
                    Log.d("debug", "error : " + e);
                } catch (IOException e) {
                    Log.d("debug", "error : " + e);
                }
            }
        }
        try {
            Gson gson = new Gson();
            sGlobalConfig = gson.fromJson(configString.toString(),
                GlobalConfig.class);
            if (sGlobalConfig == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.d("debug", "error : " + e);
        }
        return false;
    }

    private static boolean parseFromRaw(Context context) {
        Log.d("debug", "parseFromRaw");
        StringBuilder configString = new StringBuilder();
        try {
            byte buf[] = new byte[512];
            int readLen = 0;
            InputStream fis = context.getResources().openRawResource(
                    R.raw.global);
            while ((readLen = fis.read(buf)) > 0) {
                configString.append(new String(buf, 0, readLen));
            }
            fis.close();
        } catch (FileNotFoundException e) {
            Log.d("debug", "error : " + e);
        } catch (IOException e) {
            Log.d("debug", "error : " + e);
        }
        try {
            Gson gson = new Gson();
            sGlobalConfig = gson.fromJson(configString.toString(),
                    GlobalConfig.class);
            if (sGlobalConfig == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.d("debug", "error : " + e);
        }
        return false;
    }
    public String toString() {
        String str = "=====================\n";
        str += "debug    : " + debug + "\n";
        str += "fontpath : " + fontpath + "\n";
        str += "========================\n";
        return str;
    }
}
