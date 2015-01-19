package com.android.phoneassistant.util;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

public class Log {

    public static final String TAG = "phoneassistant";
    private static final boolean GLOBAL_TAG = true;
    public static boolean DEBUGABLE = false;
    private static final int VERBOSE = android.util.Log.VERBOSE;
    private static final int DEBUG = android.util.Log.DEBUG;
    private static final int INFO = android.util.Log.INFO;
    private static final int ERROR = android.util.Log.ERROR;
    private static final int WARN = android.util.Log.WARN;

    private static Log sLog;
    private Context mContext;
    public static Log getLog(Context context) {
        if (sLog == null) {
            sLog = new Log(context);
        }
        return sLog;
    }
    private Log(Context context) {
        mContext = context;
    }

    private static boolean isLoggable(String tag, int level) {
        if (Log.TAG.equals(tag)) {
            return android.util.Log.isLoggable(tag, level) || true;
        }
        return true;
    }
    public static void d(String tag, String message) {
        if (DEBUGABLE && isLoggable(tag, DEBUG)) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.d(tag, extraString + message);
        }
    }

    public static void v(String tag, String message) {
        if (DEBUGABLE && isLoggable(tag, VERBOSE)) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.v(tag, extraString + message);
        }
    }

    public static void i(String tag, String message) {
        if (DEBUGABLE && isLoggable(tag, INFO)) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.i(tag, extraString + message);
        }
    }

    public static void w(String tag, String message) {
        if (DEBUGABLE && isLoggable(tag, WARN)) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.w(tag, extraString + message);
        }
    }

    public static void e(String tag, String message) {
        if (DEBUGABLE && isLoggable(tag, ERROR)) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.e(tag, extraString + message);
        }
    }

    private static boolean privateTag() {
        return GLOBAL_TAG;
    }
    private static String getMethodNameAndLineNumber() {
        StackTraceElement element[] = Thread.currentThread().getStackTrace();
        if (element != null && element.length >= 4) {
            String methodName = element[4].getMethodName();
            int lineNumber = element[4].getLineNumber();
            long threadId = Thread.currentThread().getId();
            return String.format("%s.%s : %d ---> ", getClassName(), methodName, lineNumber);
        }
        return null;
    }
    
    private static String getTag() {
        StackTraceElement element[] = Thread.currentThread().getStackTrace();
        if (element != null && element.length >= 4) {
            String className = element[4].getClassName();
            if (className == null) {
                return null;
            }
            int index = className.lastIndexOf(".");
            if (index != -1) {
                className = className.substring(index + 1);
            }
            index = className.indexOf('$');
            if (index != -1) {
                className = className.substring(0, index);
            }
            // android.util.Log.d("taugin", "className = " + className);
            return className;
        }
        return null;
    }

    private static String getClassName() {
        StackTraceElement element[] = Thread.currentThread().getStackTrace();
        if (element != null && element.length >= 4) {
            String className = element[5].getClassName();
            if (className == null) {
                return null;
            }
            int index = className.lastIndexOf(".");
            if (index != -1) {
                className = className.substring(index + 1);
            }
            index = className.indexOf('$');
            if (index != -1) {
                className = className.substring(0, index);
            }
            // android.util.Log.d("taugin", "className = " + className);
            return className;
        }
        return null;
    }

    public void recordOperation(String operation) {
        if (!DEBUGABLE) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(new Date(System.currentTimeMillis())) + " : ";
        try {
            File external = Environment.getExternalStorageDirectory();
            String dir = external.getAbsoluteFile() + File.separator
                    + "anzhuoshangdian";
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            if (external != null) {
                FileWriter fp = new FileWriter(
                        dir + File.separator + "log.txt", true);
                fp.write(time + operation + "\n");
                fp.close();
            }
        } catch (Exception e) {
            android.util.Log.d(Log.TAG, "error : " + e);
        }
    }

    public void initDebug() {
        GlobalConfig config = GlobalConfig.get();
        if (config != null) {
            DEBUGABLE = config.debug;
        }
    }
}