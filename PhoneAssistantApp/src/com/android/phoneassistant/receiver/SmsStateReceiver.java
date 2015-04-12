package com.android.phoneassistant.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.android.phoneassistant.manager.BlackNameManager;
import com.android.phoneassistant.service.PhoneAssistantService;
import com.android.phoneassistant.util.Constant;
import com.android.phoneassistant.util.Log;

public class SmsStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return ;
        }
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            SmsMessage[] messages = BlackNameManager.getMessagesFromIntent(intent);
            if (messages == null || messages.length <= 0) {
                return;
            }
            String address = messages[0].getOriginatingAddress();
            if (address.startsWith("+86")) {
                address = address.substring("+86".length());
            }
            address = address.replaceAll("-", "");
            address = address.replaceAll("\\s+", "");
            boolean blockSms = BlackNameManager.getInstance(context).isBlockSms(address);
            Log.d(Log.TAG, "address : " + address + " , blockSms : " + blockSms);
            if (blockSms) {
                abortBroadcast();
                intent.setAction(Constant.ACTION_PHONE_BLOCKSMS);
                intent.setClass(context, PhoneAssistantService.class);
                context.startService(intent);
            }
        }
    }
}
