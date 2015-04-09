package com.android.phoneassistant.backup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Xml;

import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Log;
import com.android.phoneassistant.util.Utils;

public class ExportHelper {

    private static final String NAMESPACE = "";
    private Context mContext;
    private OnImportExportListener mOnImportExportListener;

    public ExportHelper(Context context) {
        mContext = context;
    }

    public void setOnImportExportListener(OnImportExportListener l) {
        mOnImportExportListener = l;
    }
    public void exportCallInfo() {
        int totalCount = getTotalCount();
        if (mOnImportExportListener != null) {
            mOnImportExportListener.onStart(totalCount + 1);
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
            if (mOnImportExportListener != null) {
                mOnImportExportListener.onProcessing("Zipping ... ");
            }
            generateZipFile();
            if (mOnImportExportListener != null) {
                mOnImportExportListener.onEnd();
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
                    String text = null;
                    for (int index = 0; index < columnCount; index++) {
                        columnName = c.getColumnName(index);
                        columnValue = c.getString(index);
                        serializer.startTag(NAMESPACE, columnName);
                        text = TextUtils.isEmpty(columnValue) ? ""
                                : columnValue;
                        serializer.text(text);
                        serializer.endTag(NAMESPACE, columnName);
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int id = c.getInt(c.getColumnIndex(DBConstant._ID));
                    String contactName = c.getString(c.getColumnIndex(DBConstant.CONTACT_NAME));
                    String contactNumber = c.getString(c.getColumnIndex(DBConstant.CONTACT_NUMBER));
                    String showInfo = contactName;
                    if (TextUtils.isEmpty(showInfo)) {
                        showInfo = contactNumber;
                    }
                    if (mOnImportExportListener != null) {
                        mOnImportExportListener.onProcessing(showInfo);
                    }
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
            c = mContext.getContentResolver().query(DBConstant.RECORD_URI,
                    null, selection, null, DBConstant.RECORD_START + " DESC");
            if (c != null && c.moveToFirst()) {
                /*
                count = c.getCount();
                serializer.startTag(NAMESPACE, "count");
                serializer.text(String.valueOf(count));
                serializer.endTag(NAMESPACE, "count");
                */
                do {
                    serializer.startTag(NAMESPACE, "record");
                    int columnCount = c.getColumnCount();
                    String columnName = null;
                    String columnValue = null;
                    String text = null;
                    for (int index = 0; index < columnCount; index++) {
                        columnName = c.getColumnName(index);
                        columnValue = c.getString(index);
                        serializer.startTag(NAMESPACE, columnName);
                        text = TextUtils.isEmpty(columnValue) ? ""
                                : columnValue;
                        serializer.text(text);
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
        String prefix = "datebase_backup";
        String suffix = ".xml";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String time = sdf.format(new Date());
        String recordDir = Utils.getRecorderFolder();
        File recorderFile = new File(recordDir);
        if (!recorderFile.exists()) {
            recorderFile.mkdirs();
        }
        return recorderFile + File.separator + prefix + suffix;
    }

    @SuppressLint("SimpleDateFormat")
    private String getBackupZipFileName() {
        String prefix = "phoneassistant_backup_";
        String suffix = ".zip";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String time = sdf.format(new Date());
        String recordDir = Utils.getRecorderFolder();
        File recorderFile = new File(recordDir);
        if (!recorderFile.exists()) {
            recorderFile.mkdirs();
        }
        return recorderFile + File.separator + prefix + time + suffix;
    }

    private String[] queryZippedFile() {
        String recordDir = Utils.getRecorderFolder();
        File recorderFile = new File(recordDir);
        if (!recorderFile.exists()) {
            return null;
        }
        String zippedFile[] = recorderFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".xml") || filename.endsWith("amr")) {
                    return true;
                }
                return false;
            }
        });
        return zippedFile;
    }
    private void generateZipFile() {
        String zipfile = getBackupZipFileName();
        String recordDir = Utils.getRecorderFolder();
        File recorderPath = new File(recordDir);
        if (!recorderPath.exists()) {
            return;
        }
        ZipOutputStream zos = null;
        FileInputStream fis = null;
        String recorderName = null;
        String recorderFile = null;

        Cursor c = null;
        try {
            FileOutputStream fos = new FileOutputStream(zipfile);
            zos = new ZipOutputStream(new BufferedOutputStream(fos));
            c = mContext.getContentResolver().query(DBConstant.RECORD_URI,
                    null, null, null, DBConstant.RECORD_START + " DESC");
            if (c != null && c.moveToFirst()) {
                do {
                    recorderName = c.getString(c.getColumnIndex(DBConstant.RECORD_NAME));
                    recorderFile = c.getString(c.getColumnIndex(DBConstant.RECORD_FILE));
                    if (!TextUtils.isEmpty(recorderName) && !TextUtils.isEmpty(recorderFile)) {
                        File file = new File(recorderFile);
                        if (file.exists()) {
                            Log.d(Log.TAG, "Zipping : " + recorderName);
                            fis = new FileInputStream(file);
                            byte[] bytes = new byte[1024];
                            int len = 0;
                            ZipEntry entry = new ZipEntry(recorderName);
                            zos.putNextEntry(entry);
                            while ((len = fis.read(bytes)) > 0) {
                                zos.write(bytes, 0, len);
                            }
                            fis.close();
                            zos.closeEntry();
                        }
                    }
                } while(c.moveToNext());
            }
        } catch (FileNotFoundException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        } finally {
            if (c != null) {
                c.close();
            }
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    Log.d(Log.TAG, "error : " + e);
                }
            }
            String databaseBack = recordDir + File.separator
                    + "datebase_backup.xml";
            File databaseBackFile = new File(databaseBack);
            if (databaseBackFile.exists()) {
                databaseBackFile.delete();
            }
        }
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
}
