package com.hanzheng.facedetectapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.hanzheng.facedetectapplication.config.Constant;
import com.hanzheng.facedetectapplication.log.HmctLog;
import com.hanzheng.facedetectapplication.view.activity.TranslucentCamreaActivity;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-12-15.
 */

public class BoYunBroadcastReceiver extends BroadcastReceiver {
    static final String TAG = BoYunBroadcastReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            String action = intent.getAction();
            HmctLog.d(TAG,"receive broadcast,action=" + action);
            if(action.equals(Constant.ACTION_START_FACE_DETECT)){
                Intent intent1 = new Intent(context.getApplicationContext(), TranslucentCamreaActivity.class);
                intent1.setAction(Constant.ACTION_START_FACE_DETECT);
                context.startActivity(intent1);
            }else if(action.equals(Constant.ACTION_STOP_FACE_DETECT)){

            }else if(action.equals(Constant.ACTION_CATCH_FACE)){
                Intent intent1 = new Intent(context.getApplicationContext(), TranslucentCamreaActivity.class);
                intent1.setAction(Constant.ACTION_CATCH_FACE);
                context.startActivity(intent1);
            }
        }catch (Exception ex){
            HmctLog.e(TAG,ex.toString());
        }
    }
}
