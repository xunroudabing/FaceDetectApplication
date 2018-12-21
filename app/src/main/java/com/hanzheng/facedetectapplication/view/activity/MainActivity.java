package com.hanzheng.facedetectapplication.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hanzheng.facedetectapplication.R;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-12-13.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnOpen;
    Button btnTranslucent;
    Button btnDemo;
    Button btnSetting;
    Button btnTest;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnOpen = (Button) findViewById(R.id.main_btnOpenCamrea);
        btnOpen.setOnClickListener(this);
        btnDemo = (Button) findViewById(R.id.main_btnGoDemo);
        btnDemo.setOnClickListener(this);
        btnSetting = (Button) findViewById(R.id.main_btnSetting);
        btnSetting.setOnClickListener(this);
        btnTranslucent = (Button) findViewById(R.id.main_btnOpenTranslucentCamrea);
        btnTranslucent.setOnClickListener(this);
        btnTest = (Button) findViewById(R.id.main_btnTest);
        btnTest.setOnClickListener(this);
        startCheckPermission();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_btnOpenCamrea:
                gotoCamrea();
                break;
            case R.id.main_btnGoDemo:
                gotoDemo();
                break;
            case R.id.main_btnSetting:
                gotoSetting();
                break;
            case R.id.main_btnOpenTranslucentCamrea:
                gotoTranslucentCamrea();
                break;
            case R.id.main_btnTest:
                gotoBroadTest();
                break;
        }
    }
    protected void gotoCamrea(){
        Intent intent = new Intent(getApplicationContext(),CamreaActivity.class);
        startActivity(intent);
    }
    protected void gotoDemo(){
        Intent intent = new Intent(getApplicationContext(), com.example.abu.test_android_ncnn
                .MainActivity.class);
        startActivity(intent);
    }
    protected void gotoSetting(){
        Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
        startActivity(intent);
    }
    protected void gotoTranslucentCamrea(){
        Intent intent = new Intent(getApplicationContext(),TranslucentCamreaActivity.class);
        startActivity(intent);
    }
    protected void gotoBroadTest(){
        Intent intent = new Intent(getApplicationContext(),BoradcastTestActivity.class);
        startActivity(intent);
    }
    public void startCheckPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "当前无悬浮窗权限，请授权", Toast.LENGTH_SHORT);
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
        } else {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
