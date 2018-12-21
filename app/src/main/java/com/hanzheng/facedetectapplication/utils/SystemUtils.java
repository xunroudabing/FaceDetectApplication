package com.hanzheng.facedetectapplication.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-12-15.
 */

public class SystemUtils {
    public static String getDeviceID(Context context) {
        //板子上deviceid返回为null，这里暂时注释
//        TelephonyManager TelephonyMgr = (TelephonyManager) context.getApplicationContext().getSystemService(TELEPHONY_SERVICE);
//        String szImei = TelephonyMgr.getDeviceId();
//        return szImei;
        return "111111";
    }
}
