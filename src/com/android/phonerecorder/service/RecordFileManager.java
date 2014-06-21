package com.android.phonerecorder.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.android.phonerecorder.RecordInfo;
import com.android.phonerecorder.provider.DBConstant;
import com.android.phonerecorder.util.Constant;

public class RecordFileManager {

    private static RecordFileManager sRecordFileManager = null;
    private Context mContext;
    
    public static RecordFileManager getInstance(Context context) {
        if (sRecordFileManager == null) {
            sRecordFileManager = new RecordFileManager(context);
        }
        return sRecordFileManager;
    }
    private RecordFileManager(Context context) {
        mContext = context;
    }
    private ArrayList<RecordInfo> listRecordFiles(ArrayList<RecordInfo> list) {
        File recordDir = new File(Environment.getExternalStorageDirectory() + "/" + Constant.FILE_RECORD_FOLDER);
        if (!recordDir.exists()) {
            return null;
        }
        list.clear();

        File files[] = recordDir.listFiles();
        if (files != null) {
            RecordInfo info = null;
            for (File file : files) {
                info = new RecordInfo();
                info.recordFile = file.getName();
                info.recordName = getDisplayName(info.recordFile);
                info.recordSize = file.length();
                info.recordStart = getCreateTime(info.recordFile);
                info.incoming = incomingCall(info.recordFile);
                info.recordEnd = file.lastModified();
                list.add(info);
            }
            Collections.sort(list);
        }
        return list;
    }
    private String getDisplayName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        String []parts = fileName.split("_");
        if (parts == null || parts.length != 4) {
            return null;
        }
        return parts[0] + "_" + parts[3];
    }

    private long getCreateTime(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return 0;
        }
        String []parts = fileName.split("_");
        if (parts == null || parts.length != 4) {
            return 0;
        }
        if (TextUtils.isDigitsOnly(parts[1])) {
            return Long.parseLong(parts[1]);
        }
        return 0;
    }

    private boolean incomingCall(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        String []parts = fileName.split("_");
        if (parts == null || parts.length != 4) {
            return false;
        }
        return "in".equals(parts[2]);
    }

    public String getProperName(String phoneNumber, long time) {
        Calendar calendar = Calendar.getInstance();
        String fileName = "recorder_" + time + "_" + phoneNumber + ".amr";
        return Environment.getExternalStorageDirectory() + "/" + Constant.FILE_RECORD_FOLDER + "/" + fileName;
    }
    
    public void deleteRecordFiles(ArrayList<RecordInfo> list) {
        String recordFile = null;
        int count = list.size();
        RecordInfo info = null;
        for (int index = count - 1; index >=0; index --) {
            info = list.get(index);
            if (info == null) {
                continue;
            }
            Log.d("taugin", "info = " + info.recordFile);
            if (info.checked) {
                recordFile =  Environment.getExternalStorageDirectory() + "/" + Constant.FILE_RECORD_FOLDER + "/" + info.recordFile;
                deleteRecordFile(recordFile);
                list.remove(info);
            }
        }
    }

    public boolean deleteRecordFile(String file) {
        try {
            File recordFile = new File(file);
            return recordFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getRecordFolder() {
        return Environment.getExternalStorageDirectory() + "/" + Constant.FILE_RECORD_FOLDER;
    }

    public ArrayList<RecordInfo> getRecordsFromDB(ArrayList<RecordInfo> list) {
        File recordDir = new File(Environment.getExternalStorageDirectory() + "/" + Constant.FILE_RECORD_FOLDER);
        if (!recordDir.exists()) {
            return list;
        }
        if (list == null) {
            return null;
        }
        list.clear();
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(DBConstant.RECORD_URI, null, null, null, DBConstant.RECORD_START + " DESC");
            if (c != null) {
                if (c.moveToFirst()) {
                    RecordInfo info = null;
                    do {
                        info = new RecordInfo();
                        info.recordFile = c.getString(c.getColumnIndex(DBConstant.RECORD_FILE));
                        info.recordName = c.getString(c.getColumnIndex(DBConstant.RECORD_NAME));
                        info.recordSize = c.getLong(c.getColumnIndex(DBConstant.RECORD_SIZE));
                        info.recordStart = c.getLong(c.getColumnIndex(DBConstant.RECORD_START));
                        info.recordEnd = c.getLong(c.getColumnIndex(DBConstant.RECORD_END));
                        int flag = c.getInt(c.getColumnIndex(DBConstant.RECORD_FLAG));
                        info.incoming = flag == DBConstant.FLAG_INCOMING;
                        Log.d("taugin", "info.recordSize = " + info.recordSize + " , info.recordEnd = " + info.recordEnd);
                        if (recordExists(info.recordFile)) {
                            list.add(info);
                        }
                    } while(c.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        Collections.sort(list);
        return list;
    }

    private boolean recordExists(String recordFile) {
        File file = new File(recordFile);
        if (file.exists()) {
            return true;
        }
        return false;
    }
}
