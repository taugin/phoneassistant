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

import com.android.phoneassistant.backup.ImportExportManager.WorkingState;
import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Log;
import com.android.phoneassistant.util.Utils;

public class ImportHelper {

    private Context mContext;
    private ImportExportManager mImportExportManager;

    public ImportHelper(Context context, ImportExportManager manager) {
        mContext = context;
        mImportExportManager = manager;
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
        }
        mImportExportManager.workDone();
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
                mImportExportManager.working(filename);
                byte[] bytes = baos.toByteArray();
                outFile = new File(recordDir + File.separator + filename);
                if (!outFile.exists()) {
                    outFile.createNewFile();
                }
                fos = new FileOutputStream(outFile);
                fos.write(bytes);
                zis.closeEntry();
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
}
