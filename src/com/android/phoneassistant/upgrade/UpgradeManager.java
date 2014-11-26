package com.android.phoneassistant.upgrade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.phoneassistant.R;
import com.android.phoneassistant.util.Log;
import com.google.gson.Gson;

public class UpgradeManager implements Runnable, OnClickListener {

    private static final String CONFIG_PATH = "https://raw.githubusercontent.com/taugin/phoneassistant/master/release_version/config.json";

    private static final int ACTION_FETCH_CONFIG = 0;
    private static final int ACTION_DOWNLOAD = 1;

    private static final int MSG_SHOW_PROGRESS_DIALOG = 0;
    private static final int MSG_DISMISS_PROGRESS_DIALOG = 1;
    private static final int MSG_SHOW_TOAST = 2;
    private static final int MSG_SHOW_NEWVERSION_DIALOG = 3;
    private static final int MSG_UPDATE_PROGRESS_BAR = 4;
    private static final int MSG_SET_PROGRESS_BAR_MAX = 5;

    private static UpgradeManager sUpgradeManager = null;
    private Context mContext;
    private int mAction = -1;
    private ProgressDialog mProgressDialog = null;
    private Handler mHandler = null;

    private ProgressBar mProgressBar;
    private Button mDownload;
    private Button mCancel;
    private UpgradeInfo mUpgradeInfo;

    private UpgradeManager(Context context) {
        mContext = context;
        init();
    }

    public static UpgradeManager get(Context context) {
        if (sUpgradeManager == null) {
            sUpgradeManager = new UpgradeManager(context);
        }
        return sUpgradeManager;
    }

    public String getUpgradeConfig() {
        try {
            URL url = new URL(CONFIG_PATH);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.connect();
            InputStream inStream = conn.getInputStream();
            byte buf[] = new byte[1024];
            int read = 0;
            StringBuilder builder = new StringBuilder();
            while ((read = inStream.read(buf)) > 0) {
                builder.append(new String(buf, 0, read));
            }
            inStream.close();
            Log.d(Log.TAG, "config = \n" + builder.toString());
            return builder.toString();
        } catch (MalformedURLException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return null;
    }

    private void download() {
        Message msg = null;
        try {
            msg = mHandler.obtainMessage(MSG_SET_PROGRESS_BAR_MAX);
            msg.obj = mUpgradeInfo.file_size;
            mHandler.sendMessage(msg);
            URL url = new URL(mUpgradeInfo.app_url);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            Log.d(Log.TAG, "setTimeout");
            conn.setConnectTimeout(10000);
            conn.connect();
            Log.d(Log.TAG, "conn.getResponseCode() = " + conn.getResponseCode());
            if (conn.getResponseCode() != 200) {
                return;
            }
            InputStream inStream = conn.getInputStream();
            byte buf[] = new byte[1024];
            int read = 0;
            File tmpFile = File.createTempFile("tmp", ".apk");
            Log.d(Log.TAG, "tmpFile = " + tmpFile);
            FileOutputStream fos = new FileOutputStream(tmpFile);
            long totalRead = 0;
            while ((read = inStream.read(buf)) > 0) {
                totalRead += read;
                msg = mHandler.obtainMessage(MSG_UPDATE_PROGRESS_BAR);
                msg.obj = totalRead;
                mHandler.sendMessage(msg);
                fos.write(buf, 0, read);
            }
            Log.d(Log.TAG, "totalRead = " + totalRead);
            fos.close();
            inStream.close();
        } catch (MalformedURLException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }
    private void upgradeCheck() {
        String config = getUpgradeConfig();
        if (TextUtils.isEmpty(config)) {
            return;
        }
        Gson gson = new Gson();
        UpgradeInfo info = gson.fromJson(config, UpgradeInfo.class);
        mUpgradeInfo = info;
        Log.d(Log.TAG, info.toString());
        int versionCode = getAppVer();
        mHandler.sendEmptyMessage(MSG_DISMISS_PROGRESS_DIALOG);
        if (versionCode >= info.version_code && false) {
            mHandler.sendEmptyMessage(MSG_SHOW_TOAST);
            return;
        }
        mHandler.sendEmptyMessage(MSG_SHOW_NEWVERSION_DIALOG);
    }

    private void newVersionDialog() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.upgrade_layout, null);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mDownload = (Button) view.findViewById(R.id.download);
        mDownload.setOnClickListener(this);
        mCancel = (Button) view.findViewById(R.id.cancel);
        mCancel.setOnClickListener(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.new_version_tiptitle);
        builder.setView(view);
        builder.create().show();
    }

    public void checkUpgrade() {
        mAction = ACTION_FETCH_CONFIG;
        mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS_DIALOG);
        new Thread(this).start();
    }

    private void startDownload() {
        mAction = ACTION_DOWNLOAD;
        new Thread(this).start();
    }
    @Override
    public void run() {
        if (ACTION_FETCH_CONFIG == mAction) {
            upgradeCheck();
        } else {
            download();
        }
    }

    private int getAppVer() {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            return pi.versionCode;
        } catch (NameNotFoundException e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return -1;
    }

    @SuppressLint("HandlerLeak")
    private void init() {
        mHandler = new Handler(mContext.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_SHOW_PROGRESS_DIALOG:
                    if (mProgressDialog == null) {
                        String content = mContext.getResources().getString(
                                R.string.loading);
                        mProgressDialog = ProgressDialog.show(mContext, null,
                                content, true, false);
                        mProgressDialog.show();
                        Log.d(Log.TAG, "show progress dialog");
                    }
                    break;
                case MSG_DISMISS_PROGRESS_DIALOG:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    break;
                case MSG_SHOW_NEWVERSION_DIALOG:
                    newVersionDialog();
                    break;
                case MSG_SHOW_TOAST:
                    Toast.makeText(mContext, R.string.no_newversion_tip,
                            Toast.LENGTH_LONG).show();
                    break;
                case MSG_SET_PROGRESS_BAR_MAX: {
                    Long integer = (Long) msg.obj;
                    mProgressBar.setMax(integer.intValue());
                }
                    break;
                case MSG_UPDATE_PROGRESS_BAR: {
                    Long integer = (Long) msg.obj;
                    mProgressBar.setProgress(integer.intValue());
                }
                    break;
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.download:
            startDownload();
            break;
        case R.id.cancel:
            break;
        }
    }
}
