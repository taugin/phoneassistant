package com.android.phoneassistant.manager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.android.phoneassistant.info.BlackInfo;
import com.android.phoneassistant.info.ContactInfo;
import com.android.phoneassistant.info.RecordInfo;
import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Constant;
import com.android.phoneassistant.util.Log;

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

    @SuppressLint("SimpleDateFormat")
    public String getProperName(String phoneNumber, long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String fileName = "recorder_" + phoneNumber + "_"
                + sdf.format(new Date(time)) + ".amr";
        return fileName;
    }
    
    public String getProperFile(String phoneNumber, long time) {
        String fileName = getProperName(phoneNumber, time);
        return Environment.getExternalStorageDirectory() + "/"
                + Constant.FILE_RECORD_FOLDER + "/" + fileName;
    }

    private int deleteRecordFromDB(ArrayList<RecordInfo> list) {
        if (list == null || list.size() == 0) {
            return 0;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (RecordInfo info : list) {
            builder.append(info.recordId);
            builder.append(",");
        }
        builder.append(")");
        builder.deleteCharAt(builder.length() - 2);
        String area = builder.toString();
        String where = DBConstant._ID + " IN " + area;
        Log.d(Log.TAG, "where = " + where);
        int ret = mContext.getContentResolver().delete(DBConstant.RECORD_URI, where, null);
        Log.d(Log.TAG, "ret = " + ret);
        return ret;
    }
    public int deleteRecordFiles(ArrayList<RecordInfo> list) {
        int count = list.size();
        RecordInfo info = null;
        int ret = deleteRecordFromDB(list);
        if (ret <= 0) {
            return 0;
        }
        for (int index = count - 1; index >=0; index --) {
            info = list.get(index);
            if (info == null) {
                continue;
            }
            Log.d(Log.TAG, "info.recordFile = " + info.recordFile);
            deleteRecordFile(info.recordFile);
            list.remove(info);
        }
        return ret;
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

    public void deleteContactFromDB(ArrayList<ContactInfo> list) {
        if (list == null || list.size() == 0) {
            return ;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (ContactInfo info : list) {
            if (info.checked) {
                builder.append(info._id);
                builder.append(",");
            }
        }
        builder.append(")");
        builder.deleteCharAt(builder.length() - 2);
        String area = builder.toString();
        String whereBaseInfo = DBConstant._ID + " IN " + area;
        Log.d(Log.TAG, "deleteBaseInfoFromDB where = " + whereBaseInfo);
        mContext.getContentResolver().delete(DBConstant.CONTACT_URI, whereBaseInfo, null);
        String whereRecord = DBConstant.RECORD_CONTACT_ID + " IN " + area;
        Log.d(Log.TAG, "deleteBaseInfoFromDB where = " + whereRecord);
        ArrayList<RecordInfo> list2 = queryRecordFiles(whereRecord);
        mContext.getContentResolver().delete(DBConstant.RECORD_URI, whereRecord, null);
        for (RecordInfo info : list2) {
            deleteRecordFile(info.recordFile);
        }
        for (int index = list.size() - 1; index >=0; index--) {
            ContactInfo info = list.get(index);
            if (info.checked) {
                Log.getLog(mContext).recordOperation("Remove record " + info.contactNumber);
                list.remove(index);
            }
        }
    }

    public ContactInfo getSingleContact(int id) {
        Cursor c = null;
        ContactInfo info = null;
        try {
            Uri uri = ContentUris.withAppendedId(DBConstant.CONTACT_URI, id);
            c = mContext.getContentResolver().query(uri, null, null, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    info = new ContactInfo();
                    info._id = c.getInt(c.getColumnIndex(DBConstant._ID));
                    info.contactName = c.getString(c.getColumnIndex(DBConstant.CONTACT_NAME));
                    info.contactNumber = c.getString(c.getColumnIndex(DBConstant.CONTACT_NUMBER));
                    info.contactLogCount = c.getInt(c.getColumnIndex(DBConstant.CONTACT_CALLLOG_COUNT));
                    info.contactModifyName = c.getInt(c.getColumnIndex(DBConstant.CONTACT_MODIFY_NAME)) == DBConstant.MODIFY_NAME_FORBID;
                } 
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return info;
    }
    public ArrayList<ContactInfo> getContactFromDB(ArrayList<ContactInfo> list) {
        if (list == null) {
            return null;
        }
        list.clear();
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(DBConstant.CONTACT_URI, null, null, null, DBConstant.CONTACT_UPDATE + " DESC");
            if (c != null) {
                if (c.moveToFirst()) {
                    ContactInfo info = null;
                    do {
                        info = new ContactInfo();
                        info._id = c.getInt(c.getColumnIndex(DBConstant._ID));
                        info.contactName = c.getString(c.getColumnIndex(DBConstant.CONTACT_NAME));
                        info.contactNumber = c.getString(c.getColumnIndex(DBConstant.CONTACT_NUMBER));
                        info.contactLogCount = c.getInt(c.getColumnIndex(DBConstant.CONTACT_CALLLOG_COUNT));
                        info.contactUpdate = c.getLong(c.getColumnIndex(DBConstant.CONTACT_UPDATE));
                        info.blocked = BlackNameManager.getInstance(mContext).isBlock(info.contactNumber, true);
                        list.add(info);
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
        //Collections.sort(list);
        return list;
    }

    public ArrayList<RecordInfo> getRecordsFromDB(ArrayList<RecordInfo> list, int id) {
        File recordDir = new File(Environment.getExternalStorageDirectory() + "/" + Constant.FILE_RECORD_FOLDER);
        if (!recordDir.exists()) {
            return list;
        }
        if (list == null) {
            return null;
        }
        list.clear();
        Cursor c = null;
        String selection = null;
        if (id != -1) {
            selection = DBConstant.RECORD_CONTACT_ID + "=" + id;
        }
        Log.d(Log.TAG, "selection = " + selection);
        try {
            c = mContext.getContentResolver().query(DBConstant.RECORD_URI, null, selection, null, DBConstant.RECORD_START + " DESC");
            if (c != null) {
                if (c.moveToFirst()) {
                    RecordInfo info = null;
                    do {
                        info = new RecordInfo();
                        info.recordId = c.getInt(c.getColumnIndex(DBConstant._ID));
                        info.recordFile = c.getString(c.getColumnIndex(DBConstant.RECORD_FILE));
                        info.recordName = c.getString(c.getColumnIndex(DBConstant.RECORD_NAME));
                        info.recordSize = c.getLong(c.getColumnIndex(DBConstant.RECORD_SIZE));
                        info.recordRing = c.getLong(c.getColumnIndex(DBConstant.RECORD_RING));
                        info.recordStart = c.getLong(c.getColumnIndex(DBConstant.RECORD_START));
                        info.recordEnd = c.getLong(c.getColumnIndex(DBConstant.RECORD_END));
                        info.callFlag  = c.getInt(c.getColumnIndex(DBConstant.RECORD_FLAG));
                        if (!recordExists(info.recordFile)) {
                            info.recordFile = null;
                        }
                        list.add(info);
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
        // Collections.sort(list);
        return list;
    }

    private ArrayList<RecordInfo> queryRecordFiles(String selection) {
        Cursor c = null;
        ArrayList<RecordInfo> list = new ArrayList<RecordInfo>();
        try {
            c = mContext.getContentResolver().query(DBConstant.RECORD_URI, null, selection, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    RecordInfo info = null;
                    do {
                        info = new RecordInfo();
                        info.recordFile = c.getString(c.getColumnIndex(DBConstant.RECORD_FILE));
                        list.add(info);
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
        return list;
    }
    
    public ArrayList<BlackInfo> getBlackListFromDB(ArrayList<BlackInfo> list) {
        if (list == null) {
            return null;
        }
        list.clear();
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(DBConstant.BLOCK_URI, null, null, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    BlackInfo info = null;
                    do {
                        info = new BlackInfo();
                        info._id = c.getInt(c.getColumnIndex(DBConstant._ID));
                        info.blackName = c.getString(c.getColumnIndex(DBConstant.BLOCK_NAME));
                        info.blackNumber = c.getString(c.getColumnIndex(DBConstant.BLOCK_NUMBER));
                        info.blockCallCount = c.getInt(c.getColumnIndex(DBConstant.BLOCK_CALL_COUNT));
                        info.blockSmsCount = c.getInt(c.getColumnIndex(DBConstant.BLOCK_SMS_COUNT));
                        info.blockCall = c.getInt(c.getColumnIndex(DBConstant.BLOCK_CALL)) == DBConstant.BLOCK;
                        info.blockSms = c.getInt(c.getColumnIndex(DBConstant.BLOCK_SMS)) == DBConstant.BLOCK;
                        list.add(info);
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
        //Collections.sort(list);
        return list;
    }

    public void deleteBlackInfoFromDB(ArrayList<BlackInfo> list) {
        if (list == null || list.size() == 0) {
            return ;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (BlackInfo info : list) {
            if (info.checked) {
                builder.append(info._id);
                builder.append(",");
            }
        }
        builder.append(")");
        builder.deleteCharAt(builder.length() - 2);
        String area = builder.toString();
        String where = DBConstant._ID + " IN " + area;
        Log.d(Log.TAG, "where = " + where);
        mContext.getContentResolver().delete(DBConstant.BLOCK_URI, where, null);

        for (int index = list.size() - 1; index >=0; index--) {
            BlackInfo info = list.get(index);
            if (info.checked) {
                Log.getLog(mContext).recordOperation("Remove record " + info.blackNumber);
                list.remove(index);
            }
        }
    }

    private boolean recordExists(String recordFile) {
        Log.d(Log.TAG, "recordFile = " + recordFile);
        if (recordFile == null) {
            return false;
        }
        File file = new File(recordFile);
        if (file.exists()) {
            return true;
        }
        return false;
    }
}
