package com.android.phoneassistant.backup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xmlpull.v1.XmlSerializer;

import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Constant;
import com.android.phoneassistant.util.Log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Xml;

public class BackupHelper {

    private static final String NAMESPACE = "";
    private Context mContext;
    private OnBackupListener mOnBackupListener;

    public BackupHelper(Context context) {
        mContext = context;
    }

    public void setOnBackupListener(OnBackupListener l) {
        mOnBackupListener = l;
    }
    public void backup() {
        int totalCount = getTotalCount();
        if (mOnBackupListener != null) {
            mOnBackupListener.onBackupStart(totalCount);
        }
        String fileName = getBackupFileName();
        XmlSerializer serializer = Xml.newSerializer();
        try {
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
            backupContacts(serializer);
            serializer.endTag(NAMESPACE, "phoneassistant");
            serializer.endDocument();
            serializer.flush();
            if (mOnBackupListener != null) {
                mOnBackupListener.onBackupEnd();
            }
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e.getLocalizedMessage());
        }
    }

    private void backupContacts(XmlSerializer serializer) {
        Cursor c = null;
        int count = 0;
        try {
            serializer.startTag(NAMESPACE, "contacts");

            c = mContext.getContentResolver().query(DBConstant.CONTACT_URI,
                    null, null,
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
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mOnBackupListener != null) {
                        mOnBackupListener.onBackupProcessing();
                    }
                    int id = c.getInt(c.getColumnIndex(DBConstant._ID));
                    backupRecord(serializer, id);
                    serializer.endTag(NAMESPACE, "contact");
                } while (c.moveToNext());
            }
            serializer.endTag(NAMESPACE, "contacts");
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private void backupRecord(XmlSerializer serializer, int id) {
        Cursor c = null;
        int count = 0;
        String selection = DBConstant.RECORD_CONTACT_ID + "=" + id;
        try {
            serializer.startTag(NAMESPACE, "records");
            c = mContext.getContentResolver().query(DBConstant.RECORD_URI,
                    null, selection, null, DBConstant.RECORD_START + " DESC");
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
        String prefix = "phoneassistant_backup";
        String suffix = ".xml";
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
        return recordDir.getAbsolutePath() + File.separator + prefix + suffix;
    }

    private int getTotalCount() {
        Cursor c1 = null;
        Cursor c2 = null;
        int count1 = 0;
        int count2 = 0;
        try {
            c1 = mContext.getContentResolver().query(DBConstant.CONTACT_URI,
                    null, null, null, null);
            if (c1 != null) {
                count1 = c1.getCount();
            }
            c2 = mContext.getContentResolver().query(DBConstant.RECORD_URI,
                    null, null, null, null);
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
        return count1;
    }

    public interface OnBackupListener {
        public void onBackupStart(int totalCount);
        public void onBackupProcessing();
        public void onBackupEnd();
    }
}
