package com.android.phoneassistant.backup;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;

import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Log;
import com.android.phoneassistant.util.Utils;

public class ImportHelper {

    private Context mContext;
    private OnImportExportListener mOnImportExportListener;

    public ImportHelper(Context context) {
        mContext = context;
    }

    public void setOnImportExportListener(OnImportExportListener l) {
        mOnImportExportListener = l;
    }

    public String[] queryImportFiles() {
        String recordDir = Utils.getRecorderFolder();
        File recorderFile = new File(recordDir);
        if (!recorderFile.exists()) {
            return null;
        }
        String backupFiles[] = recorderFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".zip")) {
                    return true;
                }
                return false;
            }
        });
        return backupFiles;
    }

    public void unzipFile(String file) {
        FileInputStream fis = null;
        ZipInputStream zis = null;
        ZipEntry ze = null;
        FileOutputStream fos = null;
        File outFile = null;
        String recordDir = Utils.getRecorderFolder();
        File recorderFile = new File(recordDir);
        if (!recorderFile.exists()) {
            recorderFile.mkdirs();
        }
        try {
            Log.d(Log.TAG, "file : " + file);
            fis = new FileInputStream(file);
            zis = new ZipInputStream(new BufferedInputStream(fis));
            while ((ze = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                String filename = ze.getName();
                Log.d(Log.TAG, "filename : " + filename);
                byte[] bytes = baos.toByteArray();
                outFile = new File(recordDir + File.separator + filename);
                if (!outFile.exists()) {
                    outFile.createNewFile();
                }
                fos = new FileOutputStream(outFile);
                fos.write(bytes);
            }
        } catch (FileNotFoundException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }

    public void importCallInfo() {
        String recorderFolder = Utils.getRecorderFolder();
        File recorderFile = new File(recorderFolder);
        if (!recorderFile.exists()) {
            return;
        }
        try {
            FileInputStream fis = new FileInputStream(recorderFolder
                    + File.separator + "datebase_backup.xml");
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(fis, "utf-8");

            int eventType = parser.getEventType();
            ContactInfo info = null;
            String tagName = null;
            String text = null;
            RecordInfo recordInfo = null;
            ArrayList<RecordInfo> recordList = new ArrayList<RecordInfo>();
            while(eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d(Log.TAG, "Start Document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    parser.next();
                    text = parser.getText();
                    // Log.d(Log.TAG, tagName + " : " + text);

                    if ("count".equals(tagName)) {
                        int count = parseInt(text);
                        if (mOnImportExportListener != null) {
                            mOnImportExportListener.onStart(count);
                        }
                    } else if ("contact".equalsIgnoreCase(tagName)) {
                        info = new ContactInfo();
                        recordList.clear();
                    } else if ("contact_name".equalsIgnoreCase(tagName)) {
                        info.name = text;
                    } else if ("contact_sex".equalsIgnoreCase(tagName)) {
                        info.sex = parseInt(text);
                    } else if ("contact_age".equalsIgnoreCase(tagName)) {
                        info.age = parseInt(text);
                    } else if ("contact_address".equalsIgnoreCase(tagName)) {
                        info.address = text;
                    } else if ("contact_number".equalsIgnoreCase(tagName)) {
                        info.number = text;
                    } else if ("contact_call_log_count".equalsIgnoreCase(tagName)) {
                        info.calllogcount = parseInt(text);
                    } else if ("contact_allow_record".equalsIgnoreCase(tagName)) {
                        info.allowrecord = parseInt(text);
                    } else if ("contact_state".equalsIgnoreCase(tagName)) {
                        info.callstate = text;
                    } else if ("contact_update".equalsIgnoreCase(tagName)) {
                        info.update = parseLong(text);
                    } else if ("contact_allow_modify".equalsIgnoreCase(tagName)) {
                        info.allowmodify = parseInt(text);
                    } else if ("foo".equalsIgnoreCase(tagName)) {
                        info.foo = text;
                    } else if ("record".equalsIgnoreCase(tagName)) {
                        recordInfo = new RecordInfo();
                    } else if ("record_name".equalsIgnoreCase(tagName)) {
                        recordInfo.recordname = text;
                    } else if ("record_file".equalsIgnoreCase(tagName)) {
                        recordInfo.recordfile = text;
                    } else if ("record_number".equalsIgnoreCase(tagName)) {
                        recordInfo.recordnumber = text;
                    } else if ("record_flag".equalsIgnoreCase(tagName)) {
                        recordInfo.recordflag = parseInt(text);
                    } else if ("record_size".equalsIgnoreCase(tagName)) {
                        recordInfo.recordsize = parseLong(text);
                    } else if ("record_ring".equalsIgnoreCase(tagName)) {
                        recordInfo.recordring = parseLong(text);
                    } else if ("record_start".equalsIgnoreCase(tagName)) {
                        recordInfo.recordstart = parseLong(text);
                    } else if ("record_end".equalsIgnoreCase(tagName)) {
                        recordInfo.recordend = parseLong(text);
                    } else if ("foo".equalsIgnoreCase(tagName)) {
                        recordInfo.foo = text;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    tagName = parser.getName();
                    if ("record".equalsIgnoreCase(tagName)) {
                        recordList.add(recordInfo);
                    } else if ("contact".equalsIgnoreCase(tagName)) {
                        int _id = addOrUpdateContactInfo(info);
                        Log.d(Log.TAG, "_id : " + _id);
                        addOrThrowRecordInfo(_id, recordList);
                        if (mOnImportExportListener != null) {
                            String statusText = info.name;
                            if (TextUtils.isEmpty(statusText)) {
                                statusText = info.number;
                            }
                            mOnImportExportListener.onProcessing(statusText);
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                eventType = parser.next();
            }
            Log.d(Log.TAG, "End Document");
        } catch (XmlPullParserException e) {
        } catch (Exception e) {
        }
        String databaseBack = recorderFolder + File.separator
                + "datebase_backup.xml";
        File databaseBackFile = new File(databaseBack);
        if (databaseBackFile.exists()) {
            databaseBackFile.delete();
        }
        if (mOnImportExportListener != null) {
            mOnImportExportListener.onEnd();
        }
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
        }
        return 0;
    }

    private long parseLong(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
        }
        return 0;
    }

    private int addOrUpdateContactInfo(ContactInfo info) {
        int _id = queryContactId(info);
        if (_id != -1) {
            return _id;
        }
        ContentValues values = new ContentValues();
        values.put(DBConstant.CONTACT_NAME, info.name);
        values.put(DBConstant.CONTACT_SEX, info.sex);
        values.put(DBConstant.CONTACT_AGE, info.age);
        values.put(DBConstant.CONTACT_ADDRESS, info.address);
        values.put(DBConstant.CONTACT_NUMBER, info.number);
        values.put(DBConstant.CONTACT_CALLLOG_COUNT, info.calllogcount);
        values.put(DBConstant.CONTACT_ALLOW_RECORD, info.allowrecord);
        values.put(DBConstant.CONTACT_STATE, info.callstate);
        values.put(DBConstant.CONTACT_UPDATE, info.update);
        values.put(DBConstant.CONTACT_MODIFY_NAME, info.allowmodify);
        try {
            Uri uri = mContext.getContentResolver().insert(
                    DBConstant.CONTACT_URI, values);
            return (int) ContentUris.parseId(uri);
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return -1;
    }

    private int queryContactId(ContactInfo info) {
        Cursor c = null;
        String selection = DBConstant.CONTACT_NUMBER + "=" + info.number;
        try {
            c = mContext.getContentResolver().query(DBConstant.CONTACT_URI,
                    new String[] { "_id" }, selection, null, null);
            if (c != null && c.moveToFirst()) {
                return c.getInt(c.getColumnIndexOrThrow("_id"));
            }
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return -1;
    }

    private int addOrThrowRecordInfo(int _id, ArrayList<RecordInfo> list) {
        if (list == null || list.size() <= 0) {
            return -1;
        }
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        Builder builder = null;
        ContentProviderOperation cpo = null;
        ContentValues values = null;
        for (RecordInfo info : list) {
            values = new ContentValues();
            values.put(DBConstant.RECORD_CONTACT_ID, _id);
            values.put(DBConstant.RECORD_NAME, info.recordname);
            values.put(DBConstant.RECORD_FILE, info.recordfile);
            values.put(DBConstant.RECORD_NUMBER, info.recordnumber);
            values.put(DBConstant.RECORD_FLAG, info.recordflag);
            values.put(DBConstant.RECORD_SIZE, info.recordsize);
            values.put(DBConstant.RECORD_RING, info.recordring);
            values.put(DBConstant.RECORD_START, info.recordstart);
            values.put(DBConstant.RECORD_END, info.recordend);

            builder = ContentProviderOperation.newInsert(DBConstant.RECORD_URI);
            builder = builder.withValues(values);
            cpo = builder.build();
            ops.add(cpo);
        }

        try {
            ContentProviderResult[] result = mContext.getContentResolver()
                    .applyBatch(DBConstant.AUTHORITIES, ops);
            if (result != null) {
                return result.length;
            }
        } catch (RemoteException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (OperationApplicationException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return -1;
    }
    class ContactInfo {
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
        String foo;
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
        String foo;
    }
}
