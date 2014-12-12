package com.android.phoneassistant;

import java.io.File;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Environment;

import com.chukong.sdk.GlobalInit;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GlobalInit init = new GlobalInit(App.this);
        CrashHandler crashHandler = CrashHandler.getInstance(this);
        Thread.setDefaultUncaughtExceptionHandler(crashHandler);
    }

    public String getLogFile() {
        if (Environment.getExternalStorageDirectory() != null) {
            String logdir = Environment.getExternalStorageDirectory()
                    .getAbsolutePath()
                    + File.separator
                    + ".phoneassistant"
                    + File.separator + "log";
            File dir = new File(logdir);
            dir.mkdirs();
            String file = logdir + File.separator + "error.log";
            return file;
        }
        return null;
    }
}
