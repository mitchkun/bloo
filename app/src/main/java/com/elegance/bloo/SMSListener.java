package com.elegance.bloo;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.CharMatcher;

public class SMSListener extends BroadcastReceiver {
    private static final String TAG = SMSListener.class.getSimpleName();
    public static final String pdu_type = "pdus";
    public final String strMoMo = "MTN MoMo";
    public String order_code;
    public Boolean ordered = false;

    private static Common.OTPListener mListener; // this listener will do the magic of throwing the extracted OTP to all the bound views.
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {

        // this function is trigged when each time a new SMS is received on device.
        Log.d(TAG, "onReceive: SMS Listener Class Started");
        Bundle bundle = intent.getExtras();
        //msgs = new SmsMessage[pdus.length];
        SmsMessage[] msgs;
        String strMessage = "";
        String strSender = "";
        String format = bundle.getString("format");
        // Retrieve the SMS message received.
        Object[] pdus = (Object[]) bundle.get(pdu_type);
        if (pdus != null) {
            Log.d(TAG, "onReceive: pdus not null");
            // Check the Android version.
            boolean isVersionM = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
            // Fill the msgs array.
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                // Check Android version and use appropriate createFromPdu.
                if (isVersionM) {
                    // If Android version M or newer:
                    Log.d(TAG, "onReceive: isVersionM");
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    // If Android version L or older:
                    Log.d(TAG, "onReceive: isnt isVersionM");
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                // Build the message to show.
                strSender = msgs[i].getOriginatingAddress();
                Log.d(TAG, "onReceive: "+ strSender);
                strMessage = msgs[i].getMessageBody();
                if (strSender.equals(strMoMo)) {
                    Log.d(TAG, "onReceive: sms from MTN MoMo");
                    order_code = CharMatcher.inRange('0', '9').retainFrom(strMessage);
                    ordered = true;
                    PersistentData.paymentConfirmed = true;

                    //CheckOutActivity.RegisterOrder.execute(""); //RO = new CheckOutActivity.RegisterOrder();
                    //RO.execute("");
                    // Log and display the SMS message.
                    Log.d(TAG, "onReceive: " + order_code);
                    Toast.makeText(context, order_code + " Successful & Payment " + PersistentData.paymentConfirmed, Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    public  String order(){
        return order_code;
    }

    public static void bindListener(Common.OTPListener listener) {
        mListener = listener;
    }

    public static void unbindListener() {
        mListener = null;
    }

    public interface Common {
        interface OTPListener {
            void onOTPReceived(String otp);
        }
    }
}



