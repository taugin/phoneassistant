package com.android.phoneassistant.backup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Log;
import com.android.phoneassistant.util.Utils;

public class ExportHelper {

    private Context mContext;
    private OnImportExportListener mOnImportExportListener;

    public ExportHelper(Context context) {
        mContext = context;
    }

    public void setOnImportExportListener(OnImportExportListener l) {
        mOnImportExportListener = l;
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

    private void updateStatus(int index, String fileName) {
        if (mOnImportExportListener != null) {
            mOnImportExportListener.onProcessing(index, fileName);
        }
    }
    private void zippingFile(int index, FileInputStream fis, ZipOutputStream zos, String srcPath, String srcName) {
        try {
        File file = new File(srcPath);
        if (file.exists()) {
            // Log.d(Log.TAG, "Zipping : " + srcName);
            fis = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int len = 0;
            ZipEntry entry = new ZipEntry(srcName);
            zos.putNextEntry(entry);
            updateStatus(index, srcName);
            while ((len = fis.read(bytes)) > 0) {
                zos.write(bytes, 0, len);
            }
            fis.close();
            zos.closeEntry();
        }
        } catch (FileNotFoundException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
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
        int index = 1;
        try {
            FileOutputStream fos = new FileOutputStream(zipfile);
            zos = new ZipOutputStream(new BufferedOutputStream(fos));
            String backName = DBConstant.DB_NAME;
            String backFile = recordDir + File.separator + backName;
            zippingFile(index, fis, zos, backFile, backName);
            index++;
            deleteAfterZipping(backFile);
            c = mContext.getContentResolver().query(DBConstant.RECORD_URI,
                    null, null, null, DBConstant.RECORD_START + " DESC");
            if (c != null && c.moveToFirst()) {
                do {
                    recorderName = c.getString(c.getColumnIndex(DBConstant.RECORD_NAME));
                    recorderFile = c.getString(c.getColumnIndex(DBConstant.RECORD_FILE));
                    if (!TextUtils.isEmpty(recorderName) && !TextUtils.isEmpty(recorderFile)) {
                        zippingFile(index, fis, zos, recorderFile, recorderName);
                        index++;
                    }
                } while(c.moveToNext());
            }
        } catch(Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }finally {
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
        }
    }

    private void deleteAfterZipping(String filePath) {
        File deleteFile = new File(filePath);
        if (deleteFile.exists()) {
            deleteFile.delete();
        }
    }

    public void exportZipFile() {
        String recordDir = Utils.getRecorderFolder();
        File recorderPath = new File(recordDir);
        if (!recorderPath.exists()) {
            return;
        }
        File databaseFile = mContext.getDatabasePath(DBConstant.DB_NAME);
        if (databaseFile == null || !databaseFile.exists()) {
            return ;
        }
        String srcPath = databaseFile.getAbsolutePath();
        String dstPath = recordDir + File.separator + DBConstant.DB_NAME;
        Log.d(Log.TAG, "srcPath : " + srcPath);
        Log.d(Log.TAG, "dstPath : " + dstPath);
        Utils.copyFile(srcPath, dstPath);
        generateZipFile();
        if (mOnImportExportListener != null) {
            mOnImportExportListener.onEnd();
        }
    }
}
