package com.android.phoneassistant.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;
import android.text.TextUtils;

public class Utils {

    public static String getRecorderFolder() {
        File recordDir = new File(Environment.getExternalStorageDirectory()
                + "/" + Constant.FILE_RECORD_FOLDER);
        return recordDir.getAbsolutePath();
    }

    public static void copyFile(String srcPath, String dstPath) {
        if (TextUtils.isEmpty(srcPath)) {
            Log.d(Log.TAG, "srcPath is Empty");
            return ;
        }
        if (TextUtils.isEmpty(dstPath)) {
            Log.d(Log.TAG, "dstPath is Empty");
            return ;
        }
        File srcFile = new File(srcPath);
        File dstFile = new File(dstPath);
        if (!srcFile.exists()) {
            Log.d(Log.TAG, "Not Found : " + srcPath);
            return ;
        }
        if (!dstFile.exists()) {
            try {
                dstFile.createNewFile();
            } catch (IOException e) {
                Log.d(Log.TAG, "error : " + e);
            }
        }
        try {
            FileInputStream fis = new FileInputStream(srcFile);
            FileOutputStream fos = new FileOutputStream(dstFile);
            byte buf[] = new byte[2048];
            int read = 0;
            while((read = fis.read(buf)) > 0) {
                fos.write(buf, 0, read);
            }
            fis.close();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }
}
