package com.android.phoneassistant.backup;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.android.phoneassistant.R;
import com.android.phoneassistant.util.Constant;
import com.android.phoneassistant.util.Log;

public class ImportExportManager {
    enum WorkingState{
        NOWORKING, EXPORTING, IMPORTING
    }
    private static ImportExportManager sImportExportManager;
    private Context mContext = null;
    private WorkingState mWorkingState = WorkingState.NOWORKING;
    private String mImportFilePath = null;
    private ProgressDialog mProgressDialog = null;
    private Handler mHandler;
    private ImportExportManager() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static ImportExportManager get(Context context) {
        if (sImportExportManager == null) {
            sImportExportManager = new ImportExportManager();
        }
        sImportExportManager.init(context);
        return sImportExportManager;
    }

    private void init(Context context) {
        mContext = context;
    }

    public boolean isWorking() {
        return mWorkingState != WorkingState.NOWORKING;
    }

    public boolean isExport() {
        return mWorkingState == WorkingState.EXPORTING;
    }

    public boolean isImport() {
        return mWorkingState == WorkingState.IMPORTING;
    }

    public void startExport() {
        if (isWorking()) {
            return ;
        }
        mWorkingState = WorkingState.EXPORTING;
        WorkThread workThread = new WorkThread();
        workThread.start();
    }

    public void startImport(String filePath) {
        if (isWorking()) {
            return ;
        }
        mImportFilePath = filePath;
        mWorkingState = WorkingState.IMPORTING;
        WorkThread workThread = new WorkThread();
        workThread.start();
    }

    class WorkThread extends Thread {
        @Override
        public void run() {
            if (isExport()) {
                ExportHelper helper = new ExportHelper(mContext, ImportExportManager.this);
                helper.exportZipFile();
            } else if (isImport()) {
                ImportHelper helper = new ImportHelper(mContext, ImportExportManager.this);
                helper.importZipFile(mImportFilePath);
            }
            mWorkingState = WorkingState.NOWORKING;
        }
    }

    public void working(final String fileName) {
        // Log.d(Log.TAG, "workState : " + workState + " , fileName : " + fileName);
        Intent intent = new Intent(Constant.ACTION_IMPORTING_EXPORING);
        WorkingState workState = isExport() ? WorkingState.EXPORTING : WorkingState.IMPORTING;
        intent.putExtra("workingstate", workState.ordinal());
        intent.putExtra("workingname", fileName);
        mContext.sendBroadcast(intent);
        // showNotification(fileName);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showProgressDlg(fileName);
            }
        });
    }

    public void workDone() {
        Log.d(Log.TAG, "");
        Intent intent = new Intent(Constant.ACTION_IMPORTING_EXPORING);
        intent.putExtra("workingstate", WorkingState.NOWORKING.ordinal());
        mContext.sendBroadcast(intent);
        // showNotificationOver();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showProgressDlgOver();
            }
        });
    }

    private void showProgressDlg(String fileName) {
        String []splits = fileName.split("_");
        String tmp = fileName;
        if (splits != null && splits.length > 1) {
            tmp = splits[1];
        }
        String title = mContext.getResources().getString(isExport() ? R.string.exporting : R.string.importing, "");
        String message = mContext.getResources().getString(isExport() ? R.string.exporting : R.string.importing, tmp);
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(mContext, title, message, true, false);
        }
        mProgressDialog.setMessage(message);
    }

    private void showProgressDlgOver() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @SuppressLint("NewApi")
    private void showNotification(String fileName) {
        String []splits = fileName.split("_");
        String tmp = fileName;
        if (splits != null && splits.length > 1) {
            tmp = splits[1];
        }
        String title = mContext.getResources().getString(isExport() ? R.string.exporting : R.string.importing, tmp);
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher));
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setAutoCancel(false);
        String ticker = mContext.getResources().getString(isExport() ? R.string.exporting : R.string.importing, "");
        builder.setTicker(ticker);
        builder.setWhen(System.currentTimeMillis());
        builder.setProgress(100, 0, true);
        builder.setContentTitle(title);
        builder.setContentText(mContext.getString(R.string.app_name));
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(), 0);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(123456, notification);
    }

    @SuppressLint("NewApi")
    private void showNotificationOver() {
        String title = mContext.getResources().getString(isExport() ? R.string.exportover : R.string.importover);
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher));
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setAutoCancel(true);
        builder.setTicker(title);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(title);
        builder.setContentText(mContext.getString(R.string.app_name));
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(), 0);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(123456, notification);
    }
}
