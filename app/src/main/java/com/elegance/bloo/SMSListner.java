package com.elegance.bloo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSListner extends BroadcastReceiver
{
        private static final String LOG_TAG = "Bloo";
        /* package */
        static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    @Override
        public void onReceive(Context context, Intent intent){
            if (intent.getAction().equals(ACTION)){
                Bundle bundle = intent.getExtras();
                if (bundle != null){
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++){
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    String strFrom = "";
                    String strMsg = "";
                    for (SmsMessage message : messages){
                        strFrom = message.getDisplayOriginatingAddress();
                        strMsg = message.getDisplayMessageBody();

                        if(strFrom == "MTN MoMo")
                        {
                            if(strMsg.contains("Your payment of") && (strMsg.contains("to Elegance Websites MMPay has been completed")))
                            {
                               PersistentData.paymentConfirmed = true;
                            }
                        }
                    }


                }
            }
        }



}
