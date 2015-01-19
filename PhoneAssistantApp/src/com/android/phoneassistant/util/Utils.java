package com.android.phoneassistant.util;

import java.io.File;

import android.os.Environment;

public class Utils {

    public static String getRecorderFolder() {
        File recordDir = new File(Environment.getExternalStorageDirectory()
                + "/" + Constant.FILE_RECORD_FOLDER);
        return recordDir.getAbsolutePath();
    }
}
