package com.example.abu.test_android_ncnn;

public class FaceBox {
    public float score = 0;
    public int is_upright = 0;
    public int[] rect = new int[4];
    public float[] align_points = new float[10];

}
