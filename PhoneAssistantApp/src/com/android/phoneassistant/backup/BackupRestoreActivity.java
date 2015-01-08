package com.android.phoneassistant.backup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.phoneassistant.R;
import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Constant;
import com.android.phoneassistant.util.Log;

public class BackupRestoreActivity extends Activity implements OnClickListener,
        OnShowListener, Runnable {

    private static final String NAMESPACE = "";
    private BackupRestoreDialog mBackupRestoreDialog;
    private boolean mBackup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_restore);
        Button button = null;
        button = (Button) findViewById(R.id.backup);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.restore);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.backup: {
            mBackup = true;
            mBackupRestoreDialog = new BackupRestoreDialog(this, true);
            mBackupRestoreDialog.setOnShowListener(this);
            mBackupRestoreDialog.show();
        }
            break;
        case R.id.restore:
            restore();
            break;
        }
    }

    private int getTotalCount() {
        Cursor c1 = null;
        Cursor c2 = null;
        int count1 = 0;
        int count2 = 0;
        try {
            c1 = getContentResolver().query(DBConstant.CONTACT_URI, null, null,
                    null, null);
            if (c1 != null) {
                count1 = c1.getCount();
            }
            c2 = getContentResolver().query(DBConstant.RECORD_URI, null, null,
                    null, null);
            if (c2 != null) {
                count2 = c2.getCount();
            }
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        } finally {
            if (c1 != null) {
                c1.close();
            }
            if (c2 != null) {
                c2.close();
            }
        }
        return count1 + count2;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        new Thread(this).start();
    }

    @Override
    public void run() {
        if (mBackup) {
            backup();
        }
    }

    private void backup() {
        int totalCount = getTotalCount();
        mBackupRestoreDialog.setMax(totalCount);
        mBackupRestoreDialog.setProgress(0);
        String fileName = getBackupFileName();
        XmlSerializer serializer = Xml.newSerializer();
        try {
            Log.d(Log.TAG, "fileName : " + fileName);
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(fileName);
            serializer.setOutput(fos, "utf-8");
            serializer.setFeature(
                    "http://xmlpull.org/v1/doc/features.html#indent-output",
                    true);
            serializer.startDocument("utf-8", true);
            serializer.startTag(NAMESPACE, "phoneassistant");
            serializer.startTag(NAMESPACE, "total_count");
            serializer.text(String.valueOf(totalCount));
            serializer.endTag(NAMESPACE, "total_count");

            backupContacts(serializer);
            backupRecord(serializer);

            serializer.endTag(NAMESPACE, "phoneassistant");
            serializer.endDocument();
            serializer.flush();
            mBackupRestoreDialog.dismiss();
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e.getLocalizedMessage());
        }
    }

    private void backupContacts(XmlSerializer serializer) {
        Cursor c = null;
        int count = 0;
        try {
            serializer.startTag(NAMESPACE, "contacts");

            c = getContentResolver().query(DBConstant.CONTACT_URI, null, null,
                    null, DBConstant.CONTACT_UPDATE + " DESC");
            if (c != null && c.moveToFirst()) {
                count = c.getCount();
                serializer.startTag(NAMESPACE, "count");
                serializer.text(String.valueOf(count));
                serializer.endTag(NAMESPACE, "count");
                do {
                    serializer.startTag(NAMESPACE, "contact");
                    int columnCount = c.getColumnCount();
                    String columnName = null;
                    String columnValue = null;
                    for (int index = 0; index < columnCount; index++) {
                        columnName = c.getColumnName(index);
                        columnValue = c.getString(index);
                        serializer.startTag(NAMESPACE, columnName);
                        serializer.text(String.valueOf(columnValue));
                        serializer.endTag(NAMESPACE, columnName);
                    }
                    serializer.endTag(NAMESPACE, "contact");
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mBackupRestoreDialog.incrementProgress();
                } while (c.moveToNext());
            }
            serializer.endTag(NAMESPACE, "contacts");
        } catch(Exception e) {
            Log.d(Log.TAG, "error : " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
    
    private void backupRecord(XmlSerializer serializer) {
        Cursor c = null;
        int count = 0;
        try {
            serializer.startTag(NAMESPACE, "records");
            c = getContentResolver().query(DBConstant.RECORD_URI, null, null,
                    null, DBConstant.RECORD_START + " DESC");
            Log.d(Log.TAG, "c = " + c);
            if (c != null && c.moveToFirst()) {
                count = c.getCount();
                serializer.startTag(NAMESPACE, "count");
                serializer.text(String.valueOf(count));
                serializer.endTag(NAMESPACE, "count");
                do {
                    serializer.startTag(NAMESPACE, "record");
                    int columnCount = c.getColumnCount();
                    String columnName = null;
                    String columnValue = null;
                    for (int index = 0; index < columnCount; index++) {
                        columnName = c.getColumnName(index);
                        columnValue = c.getString(index);
                        serializer.startTag(NAMESPACE, columnName);
                        serializer.text(String.valueOf(columnValue));
                        serializer.endTag(NAMESPACE, columnName);
                    }
                    serializer.endTag(NAMESPACE, "record");
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mBackupRestoreDialog.incrementProgress();
                } while (c.moveToNext());
            }
            serializer.endTag(NAMESPACE, "records");
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String getBackupFileName() {
        String prefix = "phoneassistant_";
        String suffix = ".ptbk";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        String time = sdf.format(new Date());
        File recordDir = new File(Environment.getExternalStorageDirectory()
                + "/" + Constant.FILE_RECORD_FOLDER);
        if (!recordDir.exists()) {
            recordDir.mkdirs();
            File noMedia = new File(recordDir + "/.nomedia");
            try {
                noMedia.createNewFile();
            } catch (IOException e) {
                Log.d(Log.TAG, "create .nomedia file failure");
            }
        }
        return recordDir.getAbsolutePath() + File.separator + prefix + time
                + suffix;
    }
    private void restore() {

    }
    
    class BackupRestoreDialog extends Dialog {

        private TextView mIndexState;
        private TextView mStatusText;
        private ProgressBar mProgressBar;
        private boolean mBackup;

        public BackupRestoreDialog(Context context, boolean backup) {
            super(context);
            setCancelable(false);
            setCanceledOnTouchOutside(false);
            mBackup = backup;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.backup_restore_dlg);
            mIndexState = (TextView) findViewById(R.id.index_state);
            mStatusText = (TextView) findViewById(R.id.status_text);
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
            setTitle(mBackup ? R.string.backup : R.string.restore);
        }
        
        public void setMax(final int max) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setMax(max);
                }
            });
        }

        public void setProgress(final int progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setProgress(progress);
                }
            });
        }

        public void incrementProgress() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mProgressBar.incrementProgressBy(1);
                    int max = mProgressBar.getMax();
                    int cur = mProgressBar.getProgress();
                    String statusText = getResources().getString(
                            mBackup ? R.string.backuping : R.string.restoring);
                    mStatusText.setText(statusText);
                    mIndexState.setText(String.valueOf(cur + "/" + max));
                }
            });
        }
    }
}
