package com.android.phoneassistant.service;

import java.io.File;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.phoneassistant.R;
import com.android.phoneassistant.manager.BlackNameManager;
import com.android.phoneassistant.manager.RecordFileManager;
import com.android.phoneassistant.manager.RecordManager;
import com.android.phoneassistant.manager.TmpStorageManager;
import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.sersor.FlipManager;
import com.android.phoneassistant.util.Constant;
import com.android.phoneassistant.util.Log;
import com.android.phoneassistant.util.ServiceUtil;

public class PhoneAssistantService extends Service {

    private static final boolean DEBUG = true;
    private static final int DELAY_TIME = 10 * 1000;
    private RecordManager mRecordManager;
    //private CallFlag callFlag = CallFlag.UNDEFIED;
    //private String phoneNumber = null;
    private Handler mHandler;
    //private CallLogObserver mCallLogObserver;
    //private TelephonyManager mTelephonyManager;
    //private int lastState = TelephonyManager.CALL_STATE_IDLE;
    //private boolean mNumberBlocked = false;
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logv("onCreate");
        // CallNotifier.getInstance();
        mRecordManager = RecordManager.getInstance(this);
        mHandler = new Handler();
        //mTelephonyManager = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        //mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onDestroy() {
        logv("onDestroy");
        //mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_STICKY;
        }
        if (Constant.ACTION_INCOMING_PHONE.equals(intent.getAction())) {
            String phoneNumber = intent.getStringExtra(Constant.EXTRA_PHONE_NUMBER);
            int state = intent.getIntExtra(Constant.EXTRA_PHONE_STATE, TelephonyManager.CALL_STATE_IDLE);
            TmpStorageManager.inCallRing(this, phoneNumber, DBConstant.FLAG_INCOMING, System.currentTimeMillis());
            if (BlackNameManager.getInstance(getBaseContext()).interceptPhoneNumber(phoneNumber)) {
                TmpStorageManager.inCallBlock(this);
                Log.getLog(getBaseContext()).recordOperation("Block a call : " + phoneNumber);
                return START_STICKY;
            }
            onCallStateChanged(state);
            if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("key_flip_mute", true)){
                FlipManager.getInstance(getBaseContext()).registerAccelerometerListener();
            }
            logv("onStartCommand Incoming PhoneNumber" + " : " + phoneNumber);
            Log.getLog(getBaseContext()).recordOperation("Incoming call : " + phoneNumber);
        } else if (Constant.ACTION_OUTGOING_PHONE.equals(intent.getAction())) {
            String phoneNumber = intent.getStringExtra(Constant.EXTRA_PHONE_NUMBER);
            //mHandler.postDelayed(mMonitorIncallScreen, DELAY_TIME);
            //mHandler.postDelayed(mEndCall, DELAY_TIME);
            if (BlackNameManager.getInstance(getBaseContext()).isMMINunber(phoneNumber)) {
                Log.getLog(getBaseContext()).recordOperation("a MMI Number : " + phoneNumber);
                return START_STICKY;
            }

            if (!TextUtils.isEmpty(phoneNumber)) {
                if (phoneNumber.startsWith("+86")) {
                    phoneNumber = phoneNumber.substring("+86".length());
                }
            }

            TmpStorageManager.outCallOffHook(this, phoneNumber, DBConstant.FLAG_OUTGOING, System.currentTimeMillis());
            logv("onStartCommand Outgoing PhoneNumber" + " : " + phoneNumber);
            startRecord();
            Log.getLog(getBaseContext()).recordOperation("Outgoing call : " + phoneNumber);
        } else if (Constant.ACTION_PHONE_STATE.equals(intent.getAction())) {
            int state = intent.getIntExtra(Constant.EXTRA_PHONE_STATE, TelephonyManager.CALL_STATE_IDLE);
            Log.d(Log.TAG, "state = " + stateToString(state));
            onCallStateChanged(state);
        }
        return START_STICKY;
    }

    private void onCallStateChanged(int state) {
        int lastState = TmpStorageManager.getCallState(this);
        String phoneNumber = TmpStorageManager.getPhoneNumber(this);
        int callFlag = TmpStorageManager.getCallFlag(this);
        boolean callBlock = TmpStorageManager.callBlock(this);
        switch(state) {
        case TelephonyManager.CALL_STATE_IDLE:
            logv("onCallStateChanged lastState = " + stateToString(lastState) + " , state = [CALL_STATE_IDLE]" + " , CallFlag = " + callFlag + " , callBlock = " + callBlock);
            if (callBlock) {
                TmpStorageManager.clear(this);
                return ;
            }
            TmpStorageManager.callIdle(this, System.currentTimeMillis());
            if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {
                stopRecord();
            }
            if (callFlag == DBConstant.FLAG_INCOMING) {
                if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("key_flip_mute", true)){
                    FlipManager.getInstance(getBaseContext()).unregisterAccelerometerListener();
                }
            }
            String operation = null;
            if (callFlag == DBConstant.FLAG_INCOMING) {
                operation = "Incoming phoneNumber : " + phoneNumber + " idle";
             } else if (callFlag == DBConstant.FLAG_OUTGOING) {
                 operation = "Outgoing phoneNumber : " + phoneNumber + " idle";
            }

            Log.getLog(getBaseContext()).recordOperation(operation);
            TmpStorageManager.toString(this);
            if (lastState == TelephonyManager.CALL_STATE_OFFHOOK || lastState == TelephonyManager.CALL_STATE_RINGING) {
                ServiceUtil.moveTmpInfoToDB(this);
                TmpStorageManager.clear(this);
            }
            TmpStorageManager.toString(this);
            break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
            logv("onCallStateChanged lastState = " + stateToString(lastState) + " , state = [CALL_STATE_OFFHOOK]" + " , CallFlag = " + callFlag);
            if (callFlag == DBConstant.FLAG_INCOMING) {
                TmpStorageManager.inCallOffHook(this, System.currentTimeMillis());
                startRecord();
            }

            operation = null;
            if (callFlag == DBConstant.FLAG_INCOMING) {
                operation = "Incoming phoneNumber : " + phoneNumber + " offhook";
             } else if (callFlag == DBConstant.FLAG_OUTGOING) {
                 operation = "Outgoing phoneNumber : " + phoneNumber + " offhook";
            }
            Log.getLog(getBaseContext()).recordOperation(operation);
            break;
        case TelephonyManager.CALL_STATE_RINGING:
            logv("onCallStateChanged lastState = " + stateToString(lastState) + " , state = [CALL_STATE_RINGING]" + " , CallFlag = " + callFlag);
            break;
        default:
            break;
        }
        TmpStorageManager.callState(this, state);
    }

    private Runnable mMonitorIncallScreen = new Runnable() {
        @Override
        public void run() {
            if (!topIncallScreen()) {
                stopRecord();
                stopSelf();
                return ;
            }
            startRecord();
            TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
            logv("call state = " + tm.getCallState());
            mHandler.postDelayed(mMonitorIncallScreen, DELAY_TIME);
        }
    };

    private void startRecord() {
        ensureRecordManager();
        if (!mRecordManager.recording()) {
            long time = TmpStorageManager.getStartTime(this);

            String phoneNumber = TmpStorageManager.getPhoneNumber(this);
            String fileName = RecordFileManager.getInstance(PhoneAssistantService.this).getProperName(phoneNumber, time);
            Log.d(Log.TAG, "fileName = " + fileName);
            String filePath = RecordFileManager.getInstance(
                    PhoneAssistantService.this).getProperFile(phoneNumber, time);
            Log.d(Log.TAG, "filePath = " + filePath);
            TmpStorageManager.recordName(this, fileName, filePath);
            if (needRecord()) {
                mRecordManager.initRecorder(filePath);
                mRecordManager.startRecorder();
                showNotification();
            }
        }
    }

    private boolean needRecord() {
        String recordContent = PreferenceManager.getDefaultSharedPreferences(this).getString("key_record_content", "all");
        int callFlag = TmpStorageManager.getCallFlag(this);
        if ("all".equals(recordContent)) {
            return true;
        } else if ("incoming".equals(recordContent) && callFlag == DBConstant.FLAG_INCOMING) {
            return true;
        } else if ("outgoing".equals(recordContent) && callFlag == DBConstant.FLAG_OUTGOING) {
            return true;
        }
        return false;
    }

    private void stopRecord() {
        ensureRecordManager();
        String fileName = TmpStorageManager.getRecordFile(this);
        Log.d(Log.TAG, "fileName = " + fileName);
        if (needRecord()) {
            if (mRecordManager.recording()) {
                mRecordManager.stopRecorder();
                Log.getLog(getBaseContext()).recordOperation("Saved record file " + fileName);
                cancel();
                long size = 0;
                if (fileName != null) {
                    File file = new File(fileName);
                    if (file.exists()) {
                        size = file.length();
                    }
                }
                TmpStorageManager.recordSize(this, size);
            }
        }
    }

    private void ensureRecordManager() {
        if (mRecordManager == null) {
            mRecordManager = RecordManager.getInstance(this);
        }
    }
    private boolean topIncallScreen() {
        ActivityManager am = (ActivityManager) getSystemService(Service.ACTIVITY_SERVICE);
        if (am == null) {
            return false;
        }
        List<RunningTaskInfo> taskList = am.getRunningTasks(5);
        if (taskList == null) {
            return false;
        }
        if (taskList.get(0) == null) {
            return false;
        }
        ComponentName topActivity = taskList.get(0).topActivity;
        if (topActivity == null) {
            return false;
        }
        logd(topActivity.getClassName());
        return "com.android.phone.InCallScreen".equals(topActivity.getClassName());
    }

    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setWhen(System.currentTimeMillis());
        builder.setOngoing(true);
        builder.setSmallIcon(R.drawable.ic_recording);
        builder.setTicker(getResources().getString(R.string.recording));
        builder.setContentText(getResources().getString(R.string.recording));
        builder.setContentTitle(getResources().getString(R.string.app_name));

        Notification notification = builder.getNotification();
        NotificationManager nm = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        startForeground(123456, notification);
    }
    
    private void cancel() {
        NotificationManager nm = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        stopForeground(true);
    }

    private class CallLogObserver extends ContentObserver{
        public CallLogObserver() {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(Log.TAG, "uri = " + uri);
        }
    }
    private String stateToString(int state) {
        switch(state) {
        case TelephonyManager.CALL_STATE_IDLE:
            return "[CALL_STATE_IDLE]";
        case TelephonyManager.CALL_STATE_OFFHOOK:
            return "[CALL_STATE_OFFHOOK]";
        case TelephonyManager.CALL_STATE_RINGING:
            return "[CALL_STATE_RINGING]";
        default:
            return "";
        }
    }
    private void logd(String msg) {
        if (DEBUG) {
            Log.d(Log.TAG, msg);
        }
    }
    private void logv(String msg) {
        Log.v(Log.TAG, msg);
    }
}
