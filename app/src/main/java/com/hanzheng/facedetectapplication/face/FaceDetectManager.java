package com.hanzheng.facedetectapplication.face;

import com.example.abu.test_android_ncnn.NcnnMtcnn;
import com.hanzheng.facedetectapplication.FaceDetectApplication;
import com.hanzheng.facedetectapplication.log.HmctLog;
import com.hanzheng.facedetectapplication.utils.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-12-14.
 */

public class FaceDetectManager {
    static final String TAG = FaceDetectManager.class.getSimpleName();
    static FaceDetectManager instance;
    private NcnnMtcnn mFaceMtcnn;
    private List<FaceLink> mFaceLinkArray;
    private PriorityBlockingQueue<FaceBitmap> mFaceBitmapQueue;

    private FaceDetectManager() {
        mFaceMtcnn = FaceDetectApplication.getInstance().getmFaceMtcnn();
        mFaceBitmapQueue = new PriorityBlockingQueue<>();
        mFaceLinkArray = new CopyOnWriteArrayList<>();
    }

    public static FaceDetectManager getInstance() {
        if (instance == null) {
            synchronized (FaceDetectManager.class) {
                instance = new FaceDetectManager();
            }
        }
        return instance;
    }

    public void addQueue(FaceBitmap faceBitmap) {
        mFaceBitmapQueue.add(faceBitmap);
    }

    public FaceBitmap take() throws InterruptedException {
        return mFaceBitmapQueue.take();
    }

    public int getQueueSize() {
        return mFaceBitmapQueue.size();
    }

    /**
     * 人脸特征比对
     *
     * @param features
     */
    public synchronized void compareFaces(List<float[]> features) {
        if (features == null) {
            return;
        }
        if(features.size() <= 0){
            return;
        }
        HmctLog.d(TAG, "******compareFace start*******");
        HmctLog.d(TAG,"features.length=" + features.size());
        if(mFaceLinkArray.size() <= 0){
            for (float[] f1 : features) {
                FaceLink faceLink = new FaceLink(f1);
                mFaceLinkArray.add(faceLink);
            }
            HmctLog.d(TAG,"create new face size=" + features.size());
            return;
        }
        for (FaceLink facelink : mFaceLinkArray) {
            facelink.alive = false;
        }
        List<FaceLink> toAdd = new ArrayList<>();
        for (float[] f1 : features) {
            boolean exist = false;
            for (FaceLink facelink : mFaceLinkArray) {
                float[] f2 = facelink.getfeature();
                if (f2 != null) {
                    //特征比较
                    float ret = mFaceMtcnn.FaceCompare(f1, f2, 256, false);
                    HmctLog.d(TAG, "FaceCompare,ret=" + ret);
                    if (ret > 0.8F) {
                        facelink.add(f1);
                        facelink.countPlus();//命中加1
                        facelink.alive = true;
                        exist = true;
                    }
                }
            }
            //不存在，加入
            if (!exist) {
                FaceLink faceLink = new FaceLink(f1);
                toAdd.add(faceLink);
            }
        }
        //清理上一帧数据
        Iterator<FaceLink> iterator = mFaceLinkArray.iterator();
        while(iterator.hasNext()){
            FaceLink item = iterator.next();
            if (!item.alive) {
                mFaceLinkArray.remove(item);
            }
        }
        //加入新样本
        mFaceLinkArray.addAll(toAdd);
        HmctLog.d(TAG,"create new face size=" + toAdd.size());
        HmctLog.d(TAG, "******compareFace complete*******");
    }

    /**
     * 获取计数大于count的特征数据
     *
     * @param count
     * @return
     */
    public synchronized List<FaceLink> getFaceLinkOverCount(int count) {
        List<FaceLink> ret = new ArrayList<>();
        for (FaceLink item : mFaceLinkArray) {
            if (item.getCount() >= count) {
                ret.add(item);
            }
        }
        return ret;
    }

    /**
     * 清空人脸库
     */
    public synchronized void clearFaceLinkArray() {
        mFaceLinkArray.clear();
    }

    /**
     * 打印人脸库明细
     */
    public synchronized void printLog() {
        StringBuilder sb = new StringBuilder();
        HmctLog.d("facelog","print facelink detail log");
        sb.append("*********************************\r\n");
        int i = 1;
        for (FaceLink faceLink : mFaceLinkArray) {
            sb.append("**feature:" + i + ",link.size=" +
                    faceLink.features.size() + ",count=" + faceLink.getCount() + "**\r\n");
            i++;
        }
        sb.append("*********************************\r\n");
        HmctLog.d("facelog", sb.toString());
    }
}
