package com.hanzheng.facedetectapplication.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hanzheng.facedetectapplication.R;
import com.hanzheng.facedetectapplication.config.AppConfig;
import com.hanzheng.facedetectapplication.log.HmctLog;
import com.hanzheng.facedetectapplication.utils.StringUtils;

/**
 * 设置
 * Created by HanZheng(305058709@qq.com) on 2018-12-15.
 */

public class SettingActivity extends AppCompatActivity {
    EditText editServerIp;
    EditText editServerPort;
    EditText editCamreaWidth;
    EditText editCamreaHeight;
    EditText editScreenWidth;
    EditText editScreenHeight;
    EditText editDetectInterval;
    EditText editShootCount;
    Button btnSave;
    static final String TAG = SettingActivity.class.getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        bindData();
    }
    protected void initViews(){
        setContentView(R.layout.activity_setting);
        editServerIp = (EditText) findViewById(R.id.setting_editServerIP);
        editServerPort = (EditText) findViewById(R.id.setting_editServerPort);
        editCamreaWidth = (EditText) findViewById(R.id.setting_editCamreaWidth);
        editCamreaHeight = (EditText) findViewById(R.id.setting_editCamreaHeight);
        editScreenWidth = (EditText) findViewById(R.id.setting_editScreenWidth);
        editScreenHeight = (EditText) findViewById(R.id.setting_editScreenHeight);
        editDetectInterval = (EditText) findViewById(R.id.setting_editDetectInterval);
        editShootCount = (EditText) findViewById(R.id.setting_editShootCount);
        btnSave = (Button) findViewById(R.id.setting_btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }
    protected void bindData(){
        try{
            String serverip = AppConfig.getServerIP();
            String serverport = String.valueOf(AppConfig.getServerPort());
            String detect_interval = String.valueOf(AppConfig.getDetectInterval());
            String shoot_count = String.valueOf(AppConfig.getShootCount());
            String camrea_width = String.valueOf(AppConfig.getCamreaWidth());
            String camrea_height = String.valueOf(AppConfig.getCamreaHeight());
            String screen_width = String.valueOf(AppConfig.getScreenWidth());
            String screen_height = String.valueOf(AppConfig.getScreenHeight());

            editServerIp.setText(serverip);
            editServerPort.setText(serverport);
            editDetectInterval.setText(detect_interval);
            editShootCount.setText(shoot_count);
            editCamreaWidth.setText(camrea_width);
            editCamreaHeight.setText(camrea_height);
            editScreenWidth.setText(screen_width);
            editScreenHeight.setText(screen_height);
        }catch (Exception ex){
            HmctLog.e(TAG,ex.toString());
        }

    }
    protected void save(){
        String serverip = editServerIp.getText().toString();
        String serverport = editServerPort.getText().toString();
        String detect_interval = editDetectInterval.getText().toString();
        String shoot_count = editShootCount.getText().toString();
        String camrea_width = editCamreaWidth.getText().toString();
        String camrea_height = editCamreaHeight.getText().toString();
        String screen_width = editScreenWidth.getText().toString();
        String screen_height = editScreenHeight.getText().toString();

        try{
            if(!TextUtils.isEmpty(serverip)){
                AppConfig.setServerIP(serverip);
            }
            if(!TextUtils.isEmpty(serverport)){
                AppConfig.setServerPort(Integer.valueOf(serverport));
            }
            if(!TextUtils.isEmpty(detect_interval)){
                AppConfig.setDetectInterval(Long.valueOf(detect_interval));
            }
            if(!TextUtils.isEmpty(shoot_count)){
                AppConfig.setShootCount(Integer.valueOf(shoot_count));
            }
            if(!TextUtils.isEmpty(camrea_width)){
                AppConfig.setCamreaWidth(Integer.valueOf(camrea_width));
            }
            if(!TextUtils.isEmpty(camrea_height)){
                AppConfig.setCamreaHeight(Integer.valueOf(camrea_height));
            }
            if(!TextUtils.isEmpty(screen_width)){
                AppConfig.setScreenWidth(Integer.valueOf(screen_width));
            }
            if(!TextUtils.isEmpty(screen_height)){
                AppConfig.setScreenHeight(Integer.valueOf(screen_height));
            }
            Toast.makeText(SettingActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
        }catch (Exception ex){
            HmctLog.e(TAG,"setting error：" + ex.toString());
        }
    }
}
