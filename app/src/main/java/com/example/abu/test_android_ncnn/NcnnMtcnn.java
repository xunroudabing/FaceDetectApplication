package com.example.abu.test_android_ncnn;

import android.graphics.Bitmap;

public class NcnnMtcnn {

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    //face detect model init
    public native boolean Init(byte[] detparam1, byte[] detbin1,
                               byte[] detparam2, byte[] detbin2,
                               byte[] detparam3, byte[] detbin3);

    //face detect function
    public native FaceBox[] DetectFace(FaceBox facebox_class, Bitmap bitmap,
                                       int mins, double th1, double th2, double th3, double sf);

    //face feature compare
    public native float FaceCompare(float[] fea1, float[] fea2, int fealen, boolean bnorm);

    //face align
    public native FaceAlignInf[] FaceAlign(FaceAlignInf facealign_class, Bitmap bitmap, FaceBox[] face_boxes);

    //face feature extract, do not extract feature in this vision
    public native boolean FeaInit(byte[] Regparam, byte[] Regbin);
    public native float[] FeaExtract(Bitmap bitmap);

    static { System.loadLibrary("native-lib"); }

}
