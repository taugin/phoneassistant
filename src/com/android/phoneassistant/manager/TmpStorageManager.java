package com.android.phoneassistant.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Log;

public class TmpStorageManager {

    private static final String SHARED_NAME = "phone_record";
    public static final String CALL_STATE = "call_state";
    public static final String CALL_BLOCK = "call_block";
    public void put(Context context) {
    }
    
    public static void callState(Context context, int callState) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putInt(CALL_STATE, callState);
        editor.commit();
    }

    public static void recordName(Context context, String name, String file) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(DBConstant.RECORD_NAME, name);
        editor.putString(DBConstant.RECORD_FILE, file);
        editor.commit();
    }

    public static void recordSize(Context context, long fileSize) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putLong(DBConstant.RECORD_SIZE, fileSize);
        editor.commit();
    }

    public static void inCallRing(Context context, String phoneNumber, int flag, long ringTime) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(DBConstant.RECORD_NUMBER, phoneNumber);
        editor.putInt(DBConstant.RECORD_FLAG, flag);
        editor.putLong(DBConstant.RECORD_RING, ringTime);
        editor.commit();
    }

    public static void inCallOffHook(Context context, long offHookTime) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putLong(DBConstant.RECORD_START, offHookTime);
        editor.commit();
    }

    public static void callIdle(Context context, long idleTime) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putLong(DBConstant.RECORD_END, idleTime);
        editor.commit();
    }

    public static void inCallBlock(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(CALL_BLOCK, true);
        editor.commit();
    }

    public static void outCallOffHook(Context context, String phoneNumber, int flag, long offHookTime) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(DBConstant.RECORD_NUMBER, phoneNumber);
        editor.putInt(DBConstant.RECORD_FLAG, flag);
        editor.putLong(DBConstant.RECORD_START, offHookTime);
        editor.commit();
    }
    
    public static void clear(Context context) {
        Log.getLog(context).recordOperation("clear");
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }
    
    public static String getRecordName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            return sharedPreferences.getString(DBConstant.RECORD_NAME, null);
        }
        return null;
    }
    public static String getRecordFile(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            return sharedPreferences.getString(DBConstant.RECORD_FILE, null);
        }
        return null;
    }
    
    public static long getRecordSize(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(DBConstant.RECORD_SIZE, 0);
        }
        return 0;
    }

    public static String getPhoneNumber(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            return sharedPreferences.getString(DBConstant.RECORD_NUMBER, null);
        }
        return null;
    }
    
    public static int getCallFlag(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            return sharedPreferences.getInt(DBConstant.RECORD_FLAG, DBConstant.FLAG_NONE);
        }
        return 0;
    }
    
    public static int getCallState(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            return sharedPreferences.getInt(CALL_STATE, 0);
        }
        return 0;
    }
    
    public static long getRingTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(DBConstant.RECORD_RING, 0);
        }
        return 0;
    }
    
    public static long getStartTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(DBConstant.RECORD_START, 0);
        }
        return 0;
    }
    
    public static long getEndTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(DBConstant.RECORD_END, 0);
        }
        return 0;
    }
    
    public static boolean callBlock(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(CALL_BLOCK, false);
        }
        return false;
    }
    
    public static void toString(Context context) {
        String phoneNumber = getPhoneNumber(context);
        int callFlag = getCallFlag(context);
        long ringTime = getRingTime(context);
        long startTime = getStartTime(context);
        long endTime = getEndTime(context);
        String recordName = getRecordName(context);
        String recordFile = getRecordFile(context);
        long fileSize = getRecordSize(context);
        String out = "\n";
        out += "phoneNumber = " + phoneNumber + "\n";
        out += "callFlag = " + callFlag + "\n";
        out += "ringTime = " + ringTime + "\n";
        out += "startTime = " + startTime + "\n";
        out += "endTime = " + endTime + "\n";
        out += "recordName = " + recordName + "\n";
        out += "recordFile = " + recordFile + "\n";
        out += "fileSize = " + fileSize + "\n";
        Log.getLog(context).recordOperation(out);
    }
}
