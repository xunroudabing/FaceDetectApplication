package com.hanzheng.facedetectapplication.face;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 每一帧的人脸集合
 * Created by HanZheng(305058709@qq.com) on 2018-12-14.
 */

public class FaceBitmap implements Comparable<FaceBitmap> {
    public List<WeakReference<Bitmap>> bitmaps;
    //所属帧
    public long frame = 0L;
    public FaceBitmap(){
        bitmaps = new ArrayList<>();
    }
    public void addBitmap(Bitmap bitmap){
        bitmaps.add(new WeakReference<Bitmap>(bitmap));
    }
    @Override
    public int compareTo(@NonNull FaceBitmap o) {
        return Long.compare(frame,o.frame);
    }
}
