package com.android.internal.telephony;

import android.os.Handler;

import com.android.phoneassistant.util.Log;

public class CallManager {
    
    public static CallManager getInstance() {
        Log.d(Log.TAG, "getInstance");
        return null;
    }
    public void registerForNewRingingConnection(Handler h, int what, Object obj){
        Log.d(Log.TAG, "");
    }
    
    public void registerForPreciseCallStateChanged(Handler h, int what, Object obj){
        Log.d(Log.TAG, "");
    }
    
    public void registerForDisconnect(Handler h, int what, Object obj) {
        Log.d(Log.TAG, "");
    }
    
    public void registerForDisplayInfo(Handler h, int what, Object obj){
        Log.d(Log.TAG, "");
    }
    
    public void registerForSignalInfo(Handler h, int what, Object obj){
        Log.d(Log.TAG, "");
    }
    
    public void registerForSuppServiceNotification(Handler h, int what, Object obj){
        Log.d(Log.TAG, "");
    }
    
    public void registerForIncomingRing(Handler h, int what, Object obj){
        Log.d(Log.TAG, "");
    }
    
    public void registerForCallWaiting(Handler h, int what, Object obj){
        Log.d(Log.TAG, "");
    }
    
    public void unregisterForNewRingingConnection(Handler h){
        Log.d(Log.TAG, "");
    }
    
    public void unregisterForPreciseCallStateChanged(Handler h){
        Log.d(Log.TAG, "");
    }
    public void unregisterForDisconnect(Handler h){
        Log.d(Log.TAG, "");
    }
    
    public void unregisterForIncomingRing(Handler h){
        Log.d(Log.TAG, "");
    }
    public void unregisterForCallWaiting(Handler h){
        Log.d(Log.TAG, "");
    }
    public void unregisterForDisplayInfo(Handler h) {
        Log.d(Log.TAG, "");
    }
    
    public void unregisterForSignalInfo(Handler h){
        Log.d(Log.TAG, "");
    }
    
    public void unregisterForSuppServiceNotification(Handler h){
        Log.d(Log.TAG, "");
    }
}
