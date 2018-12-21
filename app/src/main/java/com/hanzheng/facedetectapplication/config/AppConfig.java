package com.hanzheng.facedetectapplication.config;

import android.appwidget.AppWidgetHost;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-12-13.
 */

public class AppConfig {
    private static Context mContext;
    private static SharedPreferences mPreference;
    private static SharedPreferences.Editor mEditor;

    public static void init(Context context) {
        mContext = context.getApplicationContext();
        mPreference = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mPreference.edit();
    }

    /**
     * 根目录
     *
     * @return
     */
    public static String getRoot() {
        return Environment.getExternalStorageDirectory() + File.separator
                + Constant.PATH_ROOT + File.separator;
    }

    /**
     * 日志目录
     *
     * @return
     */
    public static String getLog() {
        return getRoot() + Constant.PATH_LOG + File.separator;
    }

    public static String getPic() {
        return getRoot() + Constant.PATH_PIC + File.separator;
    }

    public static String getServerIP() {
        return mPreference.getString("server_ip", Constant.SERVER_IP);
    }

    public static void setServerIP(String ip) {
        mEditor.putString("server_ip", ip);
        mEditor.commit();
    }

    public static int getServerPort() {
        return mPreference.getInt("server_port", Constant.SERVER_PORT);
    }

    public static void setServerPort(int port) {
        mEditor.putInt("server_port", port);
        mEditor.commit();
    }

    /**
     * 设置扫描人脸间隔
     * @param millseconds
     */
    public static void setDetectInterval(long millseconds){
        mEditor.putLong("detect_interval",millseconds);
        mEditor.commit();
    }

    /**
     * 获取扫描人脸间隔
     * @return
     */
    public static long getDetectInterval(){
        return  mPreference.getLong("detect_interval",Constant.DETECT_INTERVAL);
    }

    public static  void setShootCount(int count){
        mEditor.putInt("shoot_count",count);
        mEditor.commit();
    }

    /**
     * 获取命中次数阈值配置
     * @return
     */
    public static int getShootCount(){
        return  mPreference.getInt("shoot_count",Constant.SHOOT_COUNT);
    }

    public static void setCamreaWidth(int width){
        mEditor.putInt("camrea_width", width);
        mEditor.commit();
    }
    public static int getCamreaWidth(){
        return mPreference.getInt("camrea_width",Constant.CAMREA_WIDTH);
    }
    public static void setCamreaHeight(int height){
        mEditor.putInt("camrea_height",height);
        mEditor.commit();
    }
    public static int getCamreaHeight(){
        return mPreference.getInt("camrea_height",Constant.CAMREA_HEIGHT);
    }
    public static void setScreenWidth(int width){
        mEditor.putInt("screen_width",width);
        mEditor.commit();
    }
    public static int getScreenWidth(){
        return mPreference.getInt("screen_width",Constant.SCREEN_WIDTH);
    }
    public static void setScreenHeight(int height){
        mEditor.putInt("screen_height",height);
        mEditor.commit();
    }
    public static int getScreenHeight(){
        return mPreference.getInt("screen_height",Constant.SCREEN_HEIGHT);
    }
}
