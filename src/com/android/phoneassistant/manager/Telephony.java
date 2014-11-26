package com.android.phoneassistant.manager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.internal.telephony.ITelephony;
import com.android.phoneassistant.util.Log;

public class Telephony {

    public static Telephony sCallManager = null;
    private Context mContext;
    private ITelephony telephony;
    
    public static Telephony getInstance(Context context) {
        if (sCallManager == null) {
            sCallManager = new Telephony(context);
        }
        return sCallManager;
    }
    private Telephony(Context context) {
        mContext = context;
        try {
            Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder)method.invoke(null, new Object[]{Context.TELEPHONY_SERVICE});
            telephony = ITelephony.Stub.asInterface(binder);
        } catch (NoSuchMethodException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (ClassNotFoundException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IllegalAccessException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IllegalArgumentException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (InvocationTargetException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }

    public void endCall() {
        Log.d(Log.TAG, "endCall");
        try {
            telephony.endCall();
        } catch (RemoteException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IllegalArgumentException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }
    
    public void muteCall() {
        Log.d(Log.TAG, "CallManager muteCall");
        try {
            telephony.silenceRinger();
        } catch (RemoteException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }
}
