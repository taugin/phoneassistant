package com.android.phoneassistant.backup;

import java.io.File;
import java.io.FilenameFilter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import com.android.phoneassistant.util.Constant;

public class ImportHelper {

    private Context mContext;

    public ImportHelper(Context context) {
        mContext = context;
    }

    @SuppressLint("SimpleDateFormat")
    public String[] queryImportFiles() {
        File recordDir = new File(Environment.getExternalStorageDirectory()
                + "/" + Constant.FILE_RECORD_FOLDER);
        if (!recordDir.exists()) {
            return null;
        }
        String backupFiles[] = recordDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        });
        return backupFiles;
    }

    public void importCallInfo(String file) {

    }

    class BaseInfo {
        String name;
        int sex;
        int age;
        String address;
        String number;
        int calllogcount;
        int allowrecord;
        String callstate;
        long update;
        int allowmodify;
    }

    class RecordInfo {
        int baseid;
        String recordname;
        String recordfile;
        String recordnumber;
        int recordflag;
        long recordsize;
        long recordring;
        long recordstart;
        long recordend;
    }
}
