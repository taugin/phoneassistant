package com.android.phoneassistant.call;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.phoneassistant.util.Log;

public class CallNotifier extends Handler {

    private static final int PHONE_BASE = 300;
    private static final int PHONE_STATE_CHANGED = PHONE_BASE + 1;
    private static final int PHONE_NEW_RINGING_CONNECTION = PHONE_BASE + 2;
    private static final int PHONE_DISCONNECT = PHONE_BASE + 3;
    protected static final int PHONE_INCOMING_RING = PHONE_BASE + 5;
    private static final int PHONE_STATE_DISPLAYINFO = PHONE_BASE + 6;
    private static final int PHONE_STATE_SIGNALINFO = PHONE_BASE + 7;
    private static final int PHONE_CDMA_CALL_WAITING = PHONE_BASE + 8;

    private static final int SUPP_SERVICE_NOTIFY = PHONE_BASE + 34;
    
    protected CallManager mCM;
    
    
    private static CallNotifier sCallNotifier = null;
    public static CallNotifier getInstance() {
        if (sCallNotifier == null) {
            sCallNotifier = new CallNotifier();
        }
        return sCallNotifier;
    }
    private CallNotifier() {
        mCM = CallManager.getInstance();
        updateCallNotifierRegistrationsAfterRadioTechnologyChange();
    }
    protected void registerForNotifications() {
        mCM.registerForNewRingingConnection(this, PHONE_NEW_RINGING_CONNECTION, null);
        mCM.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED, null);
        mCM.registerForDisconnect(this, PHONE_DISCONNECT, null);
        mCM.registerForIncomingRing(this, PHONE_INCOMING_RING, null);
        mCM.registerForCallWaiting(this, PHONE_CDMA_CALL_WAITING, null);
        mCM.registerForDisplayInfo(this, PHONE_STATE_DISPLAYINFO, null);
        mCM.registerForSignalInfo(this, PHONE_STATE_SIGNALINFO, null);
        mCM.registerForSuppServiceNotification(this, SUPP_SERVICE_NOTIFY, null);
    }
    
    void updateCallNotifierRegistrationsAfterRadioTechnologyChange() {
        // Unregister all events from the old obsolete phone
        mCM.unregisterForNewRingingConnection(this);
        mCM.unregisterForPreciseCallStateChanged(this);
        mCM.unregisterForDisconnect(this);
        mCM.unregisterForIncomingRing(this);
        mCM.unregisterForCallWaiting(this);
        mCM.unregisterForDisplayInfo(this);
        mCM.unregisterForSignalInfo(this);
        mCM.unregisterForSuppServiceNotification(this);

        // Register all events new to the new active phone
        registerForNotifications();
    }
    
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case PHONE_NEW_RINGING_CONNECTION:
                Log.d(Log.TAG, "PHONE_NEW_RINGING_CONNECTION");
                break;

            case PHONE_INCOMING_RING:
                Log.d(Log.TAG, "PHONE_INCOMING_RING");
                // repeat the ring when requested by the RIL, and when the user has NOT
                // specifically requested silence.
                break;

            case PHONE_STATE_CHANGED:
                Log.d(Log.TAG, "PHONE_STATE_CHANGED");
                break;

            case PHONE_DISCONNECT:
                Log.d(Log.TAG, "PHONE_DISCONNECT");
                break;

            case PHONE_CDMA_CALL_WAITING:
                Log.d(Log.TAG, "PHONE_CDMA_CALL_WAITING");
                break;

            case PHONE_STATE_DISPLAYINFO:
                Log.d(Log.TAG, "PHONE_STATE_DISPLAYINFO");
                break;

            case PHONE_STATE_SIGNALINFO:
                Log.d(Log.TAG, "PHONE_STATE_SIGNALINFO");
                break;

            case SUPP_SERVICE_NOTIFY:
                Log.d(Log.TAG, "SUPP_SERVICE_NOTIFY");

                if (msg.obj != null && ((AsyncResult) msg.obj).result != null) {
                    SuppServiceNotification suppSvcNotification = (SuppServiceNotification)((AsyncResult) msg.obj).result;
                    Log.d(Log.TAG, "suppSvcNotification.notificationType = " + suppSvcNotification.notificationType + ", suppSvcNotification.code = " + suppSvcNotification.code);
                }
                break;

            default:
                // super.handleMessage(msg);
        }
    }
}
