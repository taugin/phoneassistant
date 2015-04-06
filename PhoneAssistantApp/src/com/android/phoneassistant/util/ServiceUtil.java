package com.android.phoneassistant.util;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;

import com.android.phoneassistant.manager.TmpStorageManager;
import com.android.phoneassistant.provider.DBConstant;

public class ServiceUtil {

    public static int addOrThrowContact(Context context, String phoneNumber, long time) {
        Cursor c = null;
        int _id = -1;
        int count = 0;
        String selection = DBConstant.CONTACT_NUMBER + " LIKE '%" + phoneNumber + "'";
        try {
            c = context.getContentResolver().query(DBConstant.CONTACT_URI, new String[]{DBConstant._ID, DBConstant.CONTACT_CALL_LOG_COUNT}, selection, null, null);
            if (c != null && c.moveToFirst() && c.getCount() > 0) {
                _id = c.getInt(c.getColumnIndex(DBConstant._ID));
                count = c.getInt(c.getColumnIndex(DBConstant.CONTACT_CALL_LOG_COUNT));
            }
        } catch (Exception e) {
            
        } finally {
            if (c != null) {
                c.close();
            }
        }
        Log.d(Log.TAG, "id = " + _id);
        HashMap<String, String> hashMap = queryContact(context);
        String name = hashMap.get(phoneNumber);
        Log.d(Log.TAG, "name = " + name);
        if (_id != -1) {
            ContentValues values = new ContentValues();
            values.put(DBConstant.CONTACT_CALL_LOG_COUNT, (count + 1));
            values.put(DBConstant.CONTACT_UPDATE, time);
            if (!TextUtils.isEmpty(name)) {
                values.put(DBConstant.CONTACT_NAME, name);
                values.put(DBConstant.CONTACT_MODIFY_NAME, DBConstant.MODIFY_NAME_FORBID);
            } else {
                values.put(DBConstant.CONTACT_MODIFY_NAME, DBConstant.MODIFY_NAME_ALLOW);
            }
            context.getContentResolver().update(ContentUris.withAppendedId(DBConstant.CONTACT_URI, _id), values, null, null);
            return _id;
        }
        ContentValues values = new ContentValues();
        values.put(DBConstant.CONTACT_NUMBER, phoneNumber);
        values.put(DBConstant.CONTACT_CALL_LOG_COUNT, 1);
        values.put(DBConstant.CONTACT_UPDATE, time);
        if (!TextUtils.isEmpty(name)) {
            values.put(DBConstant.CONTACT_NAME, name);
            values.put(DBConstant.CONTACT_MODIFY_NAME, DBConstant.MODIFY_NAME_FORBID);
        }
        Uri contentUri = context.getContentResolver().insert(DBConstant.CONTACT_URI, values);

        return (int) ContentUris.parseId(contentUri);
    }
    
    public static void moveTmpInfoToDB(Context context) {
        Log.getLog(context).recordOperation("moveTmpInfoToDB");
        String phoneNumber = TmpStorageManager.getPhoneNumber(context);
        int callFlag = TmpStorageManager.getCallFlag(context);
        long ringTime = TmpStorageManager.getRingTime(context);
        long startTime = TmpStorageManager.getStartTime(context);
        long endTime = TmpStorageManager.getEndTime(context);
        String recordName = TmpStorageManager.getRecordName(context);
        String recordFile = TmpStorageManager.getRecordFile(context);
        long fileSize = TmpStorageManager.getRecordSize(context);
        boolean callBlock = TmpStorageManager.callBlock(context);

        long updateTime = 0;
        if (callFlag == DBConstant.FLAG_INCOMING) {
            updateTime = ringTime;
        } else if (callFlag == DBConstant.FLAG_OUTGOING) {
            updateTime = startTime;
        }
        int _id = addOrThrowContact(context, phoneNumber, updateTime);

        if (callBlock) {
            callFlag = DBConstant.FLAG_BLOCKCALL;
        }
        if (!callBlock && startTime == 0) {
            callFlag = DBConstant.FLAG_MISSCALL;
        }

        ContentValues value = new ContentValues();
        value.put(DBConstant.RECORD_CONTACT_ID, _id);
        value.put(DBConstant.RECORD_NUMBER, phoneNumber);
        value.put(DBConstant.RECORD_NAME, recordName);
        value.put(DBConstant.RECORD_FILE, recordFile);
        value.put(DBConstant.RECORD_FLAG, callFlag);
        value.put(DBConstant.RECORD_SIZE, fileSize);
        value.put(DBConstant.RECORD_RING, ringTime);
        value.put(DBConstant.RECORD_START, startTime);
        value.put(DBConstant.RECORD_END, endTime);
        Uri ret = context.getContentResolver().insert(DBConstant.RECORD_URI, value);
        Log.d(Log.TAG, "ret = " + ret);
    }
    
    private static String getNameFromContact(Context context, String phoneNumber) {
        Log.d(Log.TAG, "phoneNumber : " + phoneNumber);
        Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, phoneNumber);
        Cursor c = null;
        try {
            c = context.getContentResolver().query(uri, new String[]{Contacts.DISPLAY_NAME}, null, null, null);
            if (c != null && c.moveToFirst()) {
                return c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME));
            }
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }


    public static boolean isPhoneNumberValid(String phoneNumber) {
        boolean isValid = false;

        String expression = "((^(13|15|18)[0-9]{9}$)|(^0[1,2]{1}\\d{1}-?\\d{8}$)|(^0[3-9] {1}\\d{2}-?\\d{7,8}$)|(^0[1,2]{1}\\d{1}-?\\d{8}-(\\d{1,4})$)|(^0[3-9]{1}\\d{2}-? \\d{7,8}-(\\d{1,4})$))";
        CharSequence inputStr = phoneNumber;

        Pattern pattern = Pattern.compile(expression);

        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {
            isValid = true;
        }

        return isValid;

    }
    
    private static HashMap<String, String> queryContact(Context context) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        String[] PHONES_PROJECTION = new String[] {Phone.DISPLAY_NAME, Phone.NUMBER };
        Cursor c = null;
        try {
            c = context.getContentResolver().query(Phone.CONTENT_URI, PHONES_PROJECTION, 
                    null, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        String phoneNumber = c.getString(c.getColumnIndex(Phone.NUMBER));
                        String displayName = c.getString(c.getColumnIndex(Phone.DISPLAY_NAME));
                        if (phoneNumber.startsWith("+86")) {
                            phoneNumber = phoneNumber.substring("+86".length());
                        }
                        phoneNumber = phoneNumber.replaceAll("-", "");
                        phoneNumber = phoneNumber.replaceAll("\\s+", "");
                        //Log.d(Log.TAG, phoneNumber + " : " + displayName);
                        if (!TextUtils.isEmpty(displayName)) {
                            hashMap.put(phoneNumber, displayName);
                        }
                    } while (c.moveToNext());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return hashMap;
    }

}
