package com.hanzheng.facedetectapplication.face;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-12-14.
 */

public class FaceLink {
    public List<float[]> features;
    public int count = 0;
    public boolean alive = true;

    public FaceLink() {
        features = new ArrayList<>();
    }

    public FaceLink(float[] featrue) {
        features = new ArrayList<>();
        add(featrue);
    }

    public float[] getfeature() {
        if (features.size() > 0) {
            return features.get(0);
        }
        return null;
    }

    public void add(float[] feature) {
        features.add(feature);
    }

    public void countPlus() {
        count++;
    }

    public int getCount() {
        return count;
    }
}
