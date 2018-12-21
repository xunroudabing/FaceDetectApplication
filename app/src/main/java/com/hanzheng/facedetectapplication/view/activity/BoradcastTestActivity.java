package com.hanzheng.facedetectapplication.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.hanzheng.facedetectapplication.R;
import com.hanzheng.facedetectapplication.config.Constant;
import com.hanzheng.facedetectapplication.log.HmctLog;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-12-15.
 */

public class BoradcastTestActivity extends AppCompatActivity implements View.OnClickListener{
    Button btnStart,btnStop,btnCatch;
    static final String TAG = BoradcastTestActivity.class.getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }
    protected void initViews(){
        setContentView(R.layout.activity_broadcast_test);
        btnStart = (Button) findViewById(R.id.broad_btnStartFaceDetect);
        btnStop = (Button) findViewById(R.id.broad_btnStopFaceDetect);
        btnCatch = (Button) findViewById(R.id.broad_btnCatchFace);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnCatch.setOnClickListener(this);
    }
    protected void sendBoYunBroadCast(String action){
        Intent intent = new Intent(action);
        sendBroadcast(intent);
        HmctLog.d(TAG,"sendBoYunBroadCast,action=" + action);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.broad_btnStartFaceDetect:
                sendBoYunBroadCast(Constant.ACTION_START_FACE_DETECT);
                break;
            case R.id.broad_btnStopFaceDetect:
                sendBoYunBroadCast(Constant.ACTION_STOP_FACE_DETECT);
                break;
            case R.id.broad_btnCatchFace:
                sendBoYunBroadCast(Constant.ACTION_CATCH_FACE);
                break;
        }
    }
}
