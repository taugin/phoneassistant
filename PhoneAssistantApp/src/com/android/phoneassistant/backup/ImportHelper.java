package com.android.phoneassistant.backup;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;

import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Log;
import com.android.phoneassistant.util.Utils;

public class ImportHelper {

    private Context mContext;
    private OnImportExportListener mOnImportExportListener;

    public ImportHelper(Context context) {
        mContext = context;
    }

    public void setOnImportExportListener(OnImportExportListener l) {
        mOnImportExportListener = l;
    }

    public String[] queryImportFiles() {
        String recordDir = Utils.getRecorderFolder();
        File recorderFile = new File(recordDir);
        if (!recorderFile.exists()) {
            return null;
        }
        String backupFiles[] = recorderFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".zip")) {
                    return true;
                }
                return false;
            }
        });
        return backupFiles;
    }

    public void importZipFile(String zipFile) {
        Log.d(Log.TAG, "zipFile : " + zipFile);
        if (zipFile == null) {
            return;
        }
        File file = new File(zipFile);
        if (file.exists()) {
            unzipFile(zipFile);
            copyDatabase();
            if (mOnImportExportListener != null) {
                mOnImportExportListener.onEnd();
            }
        }
    }
    private void unzipFile(String file) {
        FileInputStream fis = null;
        ZipInputStream zis = null;
        ZipEntry ze = null;
        FileOutputStream fos = null;
        File outFile = null;
        String recordDir = Utils.getRecorderFolder();
        File recorderFile = new File(recordDir);
        if (!recorderFile.exists()) {
            recorderFile.mkdirs();
        }
        int index = 1;
        try {
            Log.d(Log.TAG, "file : " + file);
            fis = new FileInputStream(file);
            zis = new ZipInputStream(new BufferedInputStream(fis));
            while ((ze = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                String filename = ze.getName();
                // Log.d(Log.TAG, "filename : " + filename);
                updateStatus(index, filename);
                byte[] bytes = baos.toByteArray();
                outFile = new File(recordDir + File.separator + filename);
                if (!outFile.exists()) {
                    outFile.createNewFile();
                }
                fos = new FileOutputStream(outFile);
                fos.write(bytes);
                zis.closeEntry();
                index++;
            }
            zis.close();
        } catch (FileNotFoundException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }

    private void copyDatabase() {
        String recordDir = Utils.getRecorderFolder();
        File recorderPath = new File(recordDir);
        if (!recorderPath.exists()) {
            return;
        }
        File databaseFile = mContext.getDatabasePath(DBConstant.DB_NAME);

        String srcPath = recordDir + File.separator + DBConstant.DB_NAME;
        String dstPath = databaseFile.getAbsolutePath();
        Log.d(Log.TAG, "srcPath : " + srcPath);
        Log.d(Log.TAG, "dstPath : " + dstPath);
        Utils.copyFile(srcPath, dstPath);
        File dbFile = new File(srcPath);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    private void updateStatus(int index, String statusText) {
        if (mOnImportExportListener != null) {
            mOnImportExportListener.onProcessing(index, statusText);
        }
    }
}
