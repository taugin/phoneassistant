package com.android.phoneassistant.manager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.SmsMessage;
import android.widget.Toast;

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

    public boolean isBlockSms(String number) {
        String where = DBConstant.BLOCK_NUMBER + " LIKE '%" + number + "' AND " + DBConstant.BLOCK_SMS + "=" + DBConstant.BLOCK;
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
            return true;
        }
        return false;
    }

    public boolean deleteBlackName(String phoneNumber) {
        String where = DBConstant.BLOCK_NUMBER + " LIKE '%" + phoneNumber + "'";
        return mContext.getContentResolver().delete(DBConstant.BLOCK_URI, where, null) > 0;
    }

    public final static SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];
        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }

    public void updateBlockSms(Intent intent) {
        Log.d(Log.TAG, "intent : " + intent);
        SmsMessage[] messages = getMessagesFromIntent(intent);
        if (messages == null || messages.length <= 0) {
            return;
        }
        String address = messages[0].getOriginatingAddress();
        if (address.startsWith("+86")) {
            address = address.substring("+86".length());
        }
        address = address.replaceAll("-", "");
        address = address.replaceAll("\\s+", "");
        String content = "";
        for (SmsMessage message : messages) {
            content += message.getDisplayMessageBody();
        }
        ContentValues values = new ContentValues();
        values.put(DBConstant.BLOCK_DETAIL_NUMBER, address);
        values.put(DBConstant.BLOCK_DETAIL_TIME, System.currentTimeMillis());
        values.put(DBConstant.BLOCK_DETAIL_TYPE, DBConstant.BLOCK_TYPE_MMS);
        values.put(DBConstant.BLOCK_DETAIL_SMS, content);
        mContext.getContentResolver().insert(DBConstant.BLOCK_DETAIL_URI, values);
        Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
    }
}
