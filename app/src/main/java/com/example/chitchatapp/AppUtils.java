package com.example.chitchatapp;

import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AppUtils {

    public String formatE164Number(String countryCode, String phNum) {

      //  PhoneNumberUtils phoneUtil = PhoneNumberUtils.getInstance();
       // String formattedNumber = phoneUtil.format(inputNumber, PhoneNumberFormat.E164);
       // return e164Number;
        return countryCode;
    }
}
