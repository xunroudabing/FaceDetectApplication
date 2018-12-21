package com.hanzheng.facedetectapplication.log;



import com.hanzheng.facedetectapplication.config.AppConfig;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Created by HanZheng(305058709@qq.com) on 2017-11-28.
 */

public class AppLogger {
    private static final Logger log = Logger.getRootLogger();
    private static final String FILENAME = "app.log";
    private static boolean isInitConfig = false;

    private AppLogger() {
    }

    /**
     * 私有构造器
     */
    private static void initConfig() {
        if (!isInitConfig) {

            AppLogConfigurator logConfiger = new AppLogConfigurator();
            // 设置Log4j的输出路径文件夹
            String rootPath = AppConfig.getLog();
            File dirFile = new File(rootPath);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            } // 文件夹准备

            // 其实这下面可以做成一个生成器的设计模式
            logConfiger.setFileName(rootPath + File.separator + FILENAME);  // 设置文件名字
            logConfiger.setRootLevel(Level.DEBUG); // 设置调试等级

            logConfiger.setUseFileAppender(true); // 设置用户文件输出器
            logConfiger.setFilePattern("%d{MM-dd HH:mm:ss} - %m%n"); // 设置文件输出模式
            logConfiger.setImmediateFlush(true); // 设置是否立即刷新
            logConfiger.setInternalDebugging(false);
            logConfiger.setMaxBackupSize(20); // 设置最大备份数量
            logConfiger.setMaxFileSize(10 * 1024 * 1024); // 设置最大文件数
            logConfiger.setUseLogCatAppender(true);
            logConfiger.setLogCatPattern("%m%n");
            logConfiger.configure();
            isInitConfig = true;
        }
    }

    public static void v(String msg) {
        initConfig();
        log.debug(buildMessage(msg));

    }

    public static void v(String msg, Throwable thr) {
        initConfig();
        log.debug(buildMessage(msg), thr);
    }

    public static void d(String msg) {
        initConfig();
        log.debug(buildMessage(msg));
    }

    public static void d(String msg, Throwable thr) {
        initConfig();
        log.debug(buildMessage(msg), thr);
    }

    public static void i(String msg) {
        initConfig();
        log.info(buildMessage(msg));
    }

    public static void i(String msg, Throwable thr) {
        initConfig();
        log.info(buildMessage(msg), thr);
    }

    public static void w(String msg) {
        initConfig();
        log.warn(buildMessage(msg));
    }

    public static void w(String msg, Throwable thr) {
        initConfig();
        log.warn(buildMessage(msg), thr);
    }

    public static void w(Throwable thr) {
        initConfig();
        log.warn(buildMessage(""), thr);
    }

    public static void e(String msg) {
        initConfig();
        log.error(buildMessage(msg));
    }

    public static void e(String msg, Throwable thr) {
        initConfig();
        log.error(buildMessage(msg), thr);
    }

    /**
     * 生成消息
     *
     * @param msg
     * @return
     */
    protected static String buildMessage(String msg) {
        initConfig();
        StackTraceElement caller = new Throwable().fillInStackTrace()
                .getStackTrace()[2];

        return new StringBuilder().append(caller.getClassName()).append(".")
                .append(caller.getMethodName()).append("(): ").append(msg)
                .toString();
        //return  msg;
    }
}
