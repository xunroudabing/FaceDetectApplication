package com.hanzheng.facedetectapplication.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.hanzheng.facedetectapplication.log.HmctLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-12-14.
 */

public class BitmapUtils {
    static final String TAG = BitmapUtils.class.getSimpleName();

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();

        ByteBuffer buf = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buf);

        byte[] byteArray = buf.array();
        return byteArray;
    }

    public static byte[] bitmapPngToBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    public static void save(Bitmap bitmap, String path) {
        File file = new File(path);
        try {
            FileOutputStream os = new FileOutputStream(file);
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            } catch (Exception ex) {
                HmctLog.e(TAG, ex.toString());
            } finally {
                os.close();
            }
        } catch (Exception ex) {
            HmctLog.e(TAG, ex.toString());
        }
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(float angle, Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int cx = w / 2;
        int cy = h / 2;

        // 旋转图片 动作
        Matrix matrix = new Matrix();
        // if (angle != 0) {
        // matrix.postTranslate(cx, cy);
        // }
        // matrix.postRotate(angle);
        if (angle != 0) {
            matrix.setRotate(angle, cx, cy);
        }
        System.out.println("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix,
                true);
        return resizedBitmap;
    }
}
