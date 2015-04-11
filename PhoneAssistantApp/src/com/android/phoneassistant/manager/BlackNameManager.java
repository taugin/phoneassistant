package com.android.phoneassistant.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Constant;
import com.android.phoneassistant.util.Log;

public class BlackNameManager {

    private static BlackNameManager sBlackNameManager = null;
    private Context mContext;
    public static BlackNameManager getInstance(Context context) {
        if (sBlackNameManager == null) {
            sBlackNameManager = new BlackNameManager(context);
        }
        return sBlackNameManager;
    }

    private BlackNameManager(Context context) {
        mContext = context;
    }

    public boolean isBlack(String phoneNumber) {
        /*
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("key_block_all", false)) {
            return true;
        }*/
        return isBlockCall(phoneNumber);
    }

    public boolean isBlockCall(String number) {
        String where = DBConstant.BLOCK_NUMBER + " LIKE '%" + number + "' AND " + DBConstant.BLOCK_CALL + "=" + DBConstant.BLOCK;
        Cursor c = null;
        int count = 0;
        try {
            c = mContext.getContentResolver().query(DBConstant.BLOCK_URI, null, where, null, null);
            if (c != null) {
                count = c.getCount();
            }
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return count > 0;
    }
    public boolean isMMINunber(String phoneNumber) {
        String mmiNumber = phoneNumber.replaceAll("#", "%23");
        Log.d(Log.TAG, "mmiNumber = " + mmiNumber);
        if (Constant.ENABLE_SERVICE.equals("tel:" + mmiNumber)) {
            return true;
        }
        if (Constant.ENABLE_POWEROFF_SERVICE.equals("tel:" + mmiNumber)) {
            return true;
        }
        if (Constant.ENABLE_STOP_SERVICE.equals("tel:" + mmiNumber)) {
            return true;
        }
        if (Constant.DISABLE_SERVICE.equals("tel:" + mmiNumber)) {
            return true;
        }
        return false;
    }
    public boolean interceptPhoneNumber(String phoneNumber) {
        if (isBlack(phoneNumber)) {
            Telephony.getInstance(mContext).endCall();
            updateBlock(phoneNumber);
            return true;
        }
        return false;
    }
    
    public void updateBlock(String phoneNumber) {
        String where = DBConstant.BLOCK_NUMBER + " LIKE '%" + phoneNumber + "'";
        int count = getBlockCount(phoneNumber);
        long time = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(DBConstant.BLOCK_COUNT, count + 1);
        values.put(DBConstant.BLOCK_TIME, time);
        mContext.getContentResolver().update(DBConstant.BLOCK_URI, values, where, null);
    }
    public int getBlockCount(String phoneNumber) {
        String where = DBConstant.BLOCK_NUMBER + " LIKE '%" + phoneNumber + "'";
        Cursor c = null;
        int count = 0;
        try {
            c = mContext.getContentResolver().query(DBConstant.BLOCK_URI, null, where, null, null);
            if (c != null && c.moveToFirst()) {
                count = c.getInt(c.getColumnIndex(DBConstant.BLOCK_COUNT));
            }
        } catch (Exception e) {
            
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return count;
    }
    public boolean deleteBlackName(String phoneNumber) {
        String where = DBConstant.BLOCK_NUMBER + " LIKE '%" + phoneNumber + "'";
        return mContext.getContentResolver().delete(DBConstant.BLOCK_URI, where, null) > 0;
    }

    public long getBlockTime(String phoneNumber) {
        String where = DBConstant.BLOCK_NUMBER + " LIKE '%" + phoneNumber + "'";
        Cursor c = null;
        long time = 0;
        try {
            c = mContext.getContentResolver().query(DBConstant.BLOCK_URI, null, where, null, null);
            if (c != null && c.moveToFirst()) {
                time = c.getLong(c.getColumnIndex(DBConstant.BLOCK_TIME));
            }
        } catch (Exception e) {
            
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return time;
    }
}
