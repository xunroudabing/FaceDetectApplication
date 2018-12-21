package com.hanzheng.facedetectapplication.config;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-12-13.
 */

public class Constant {
    public static final boolean DEBUG = true;
    /**
     * 人脸特征检测开启
     */
    public static final String ACTION_START_FACE_DETECT = "com.boyun.broadcast.action.start_face_detect";
    /**
     * 人脸特征检测关闭
     */
    public static final String ACTION_STOP_FACE_DETECT = "com.boyun.broadcast.action.stop_face_detect";
    /**
     * 开始人脸抓拍
     */
    public static final String ACTION_CATCH_FACE = "com.boyun.broadcast.action.catch_face";

    /**
     * 流程1识别出结果通知
     */
    public static final String ACTION_SEND_FACE_DETECT_COMPLETE = "com.boyun.broadcast.action.face_detect_complete";
    /**
     * 流程2执行结束通知
     */
    public static final String ACTION_SEND_CATCH_FACE_COMPLETE = "com.boyun.broadcast.action.catch_face_complete";
    /**
     * 根目录
     */
    public static final String PATH_ROOT = "FaceDetect";
    public static final String PATH_LOG = "logs";
    public static final String PATH_PIC = "pics";
    /**
     * API服务地址
     * 192.168.1.147 3006
     */
    public static final String SERVER_IP = "192.168.1.147";
    /**
     * API服务端口
     */
    public static final int SERVER_PORT = 3006;

    public static final int CAMREA_WIDTH = 480;
    public static final int CAMREA_HEIGHT = 864;
    /**
     * 屏幕宽高若设为0则自动获取设备屏幕宽高
     */
    public static final int SCREEN_WIDTH = 0;
    public static final int SCREEN_HEIGHT = 0;
    /**
     * 扫描人脸间隔 单位：ms
     */
    public static final long DETECT_INTERVAL = 50;
    /**
     * 命中次数，大于此数值则判定为2秒
     */
    public static final int SHOOT_COUNT = 15;
}
