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
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.phoneassistant.R;
import com.android.phoneassistant.util.Log;
import com.google.gson.Gson;

public class UpgradeManager implements Runnable, OnClickListener {

    private static final String CONFIG_PATH = "https://raw.githubusercontent.com/taugin/versionrelease/master/phoneassistant/config.json";

    private static final int ACTION_FETCH_CONFIG = 0;
    private static final int ACTION_DOWNLOAD = 1;

    private static final int MSG_SHOW_PROGRESS_DIALOG = 0;
    private static final int MSG_DISMISS_PROGRESS_DIALOG = 1;
    private static final int MSG_SHOW_TOAST = 2;
    private static final int MSG_SHOW_NEWVERSION_DIALOG = 3;
    private static final int MSG_UPDATE_PROGRESS_BAR = 4;
    private static final int MSG_SET_PROGRESS_BAR_MAX = 5;
    private static final int MSG_DISMISS_ALERTDIALOG = 6;

    private Context mContext;
    private int mAction = -1;
    private ProgressDialog mProgressDialog = null;
    private Handler mHandler = null;
    private boolean mCancelDownload = false;

    private ProgressBar mProgressBar;
    private TextView mDownloadSize;
    private Button mDownload;
    private Button mCancel;
    private View mProgressLayout;
    private View mBtnLayout;
    private UpgradeInfo mUpgradeInfo;
    private AlertDialog mAlertDialog;

    public UpgradeManager(Context context) {
        mContext = context;
        init();
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

    private String download() {
        Message msg = null;
        try {
            URL url = new URL(mUpgradeInfo.app_url);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            Log.d(Log.TAG, "setTimeout");
            conn.setConnectTimeout(10000);
            conn.connect();
            Log.d(Log.TAG, "conn.getResponseCode() = " + conn.getResponseCode());
            if (conn.getResponseCode() != 200) {
                return null;
            }
            long fileLen = 0;
            String length = conn.getHeaderField("Content-Length");
            if (TextUtils.isDigitsOnly(length)) {
                try {
                    fileLen = Long.parseLong(length);
                } catch(NumberFormatException e) {
                    Log.d(Log.TAG, "error : " + e);
                }
            }
            if (fileLen == 0) {
                return null;
            }
            msg = mHandler.obtainMessage(MSG_SET_PROGRESS_BAR_MAX);
            msg.obj = fileLen;
            mHandler.sendMessage(msg);
            Log.d(Log.TAG, "fileLen = " + fileLen);
            InputStream inStream = conn.getInputStream();
            byte buf[] = new byte[1024];
            int read = 0;
            String apkPath = generateDetFile(mUpgradeInfo.app_name);
            if (TextUtils.isEmpty(apkPath)) {
                return null;
            }
            Log.d(Log.TAG, "apkPath = " + apkPath);
            FileOutputStream fos = new FileOutputStream(apkPath);
            long totalRead = 0;
            while ((read = inStream.read(buf)) > 0 && !mCancelDownload) {
                totalRead += read;
                msg = mHandler.obtainMessage(MSG_UPDATE_PROGRESS_BAR);
                msg.obj = totalRead;
                mHandler.sendMessage(msg);
                fos.write(buf, 0, read);
            }
            Log.d(Log.TAG, "totalRead = " + totalRead);
            fos.close();
            inStream.close();
            if (fileLen == totalRead) {
                return apkPath;
            } else {
                File f = new File(apkPath);
                if (f.exists()) {
                    f.delete();
                }
            }
        } catch (MalformedURLException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return null;
    }

    private String generateDetFile(String apkName) {
        File externalFile = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (externalFile != null) {
            String apkPath = externalFile.getAbsolutePath() + File.separator
                    + apkName;
            return apkPath;
        }
        File packagePath = mContext.getCacheDir();
        if (packagePath != null) {
            String apkPath = packagePath.getAbsolutePath() + File.separator
                    + apkName;
            return apkPath;
        }
        return null;
    }

    private void upgradeCheck() {
        String config = getUpgradeConfig();
        mHandler.sendEmptyMessage(MSG_DISMISS_PROGRESS_DIALOG);
        if (TextUtils.isEmpty(config)) {
            return;
        }
        Gson gson = new Gson();
        UpgradeInfo info = gson.fromJson(config, UpgradeInfo.class);
        mUpgradeInfo = info;
        Log.d(Log.TAG, info.toString());
        int versionCode = getAppVer();
        if (versionCode >= info.version_code) {
            mHandler.sendEmptyMessage(MSG_SHOW_TOAST);
            return;
        }
        mHandler.sendEmptyMessage(MSG_SHOW_NEWVERSION_DIALOG);
    }

    private void openFile(File file) {
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    private void newVersionDialog() {
        if (mAlertDialog == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.upgrade_layout, null);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            mDownloadSize = (TextView) view.findViewById(R.id.download_size);
            mProgressLayout = view.findViewById(R.id.progress_layout);
            mBtnLayout = view.findViewById(R.id.btn_layout);
            mDownload = (Button) view.findViewById(R.id.download);
            mDownload.setOnClickListener(this);
            mCancel = (Button) view.findViewById(R.id.cancel);
            mCancel.setOnClickListener(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            String title = mContext.getResources().getString(R.string.new_version_tiptitle);
            title += mUpgradeInfo.version_name;
            builder.setTitle(title);
            builder.setView(view);

            mAlertDialog = builder.create();
        }
        mAlertDialog.setCancelable(false);
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.show();
    }

    public void checkUpgrade() {
        mAction = ACTION_FETCH_CONFIG;
        mCancelDownload = false;
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
            String apkPath = download();
            mHandler.sendEmptyMessage(MSG_DISMISS_ALERTDIALOG);
            if (!TextUtils.isEmpty(apkPath)) {
                openFile(new File(apkPath));
            }
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
                    mProgressLayout.setVisibility(View.VISIBLE);
                }
                    break;
                case MSG_UPDATE_PROGRESS_BAR: {
                    Long integer = (Long) msg.obj;
                    mProgressBar.setProgress(integer.intValue());
                    int max = mProgressBar.getMax();
                    int cur = mProgressBar.getProgress();
                    mDownloadSize.setText(cur + "/" + max);
                    break;
                }
                case MSG_DISMISS_ALERTDIALOG:
                    if (mAlertDialog != null && mAlertDialog.isShowing()) {
                        mAlertDialog.dismiss();
                        mAlertDialog = null;
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
            v.setEnabled(false);
            break;
        case R.id.cancel:
            mCancelDownload = true;
            if (mAlertDialog != null && mAlertDialog.isShowing()) {
                mAlertDialog.dismiss();
                mAlertDialog = null;
            }
            break;
        }
    }
}
