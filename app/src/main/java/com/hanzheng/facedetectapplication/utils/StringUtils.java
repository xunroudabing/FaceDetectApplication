package com.hanzheng.facedetectapplication.utils;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-12-14.
 */

public class StringUtils {
    public static String convert(float[] floats) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < floats.length; i++) {
            sb.append(floats[i]);
        }
        return sb.toString();
    }
}
