package com.hanzheng.facedetectapplication;

import android.app.Application;
import android.util.Log;

import com.example.abu.test_android_ncnn.NcnnMtcnn;
import com.hanzheng.facedetectapplication.config.AppConfig;
import com.hanzheng.facedetectapplication.log.HmctLog;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-12-13.
 */

public class FaceDetectApplication extends Application {
    private NcnnMtcnn mFaceMtcnn = new NcnnMtcnn();
    static final String TAG = FaceDetectApplication.class.getSimpleName();
    private static final int CORE_POOL_SIZE = 20;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "MyThread #" + mCount.getAndIncrement());
        }
    };
    static FaceDetectApplication mInstance;
    private ExecutorService mExecutorService;

    public static FaceDetectApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        AppConfig.init(getApplicationContext());
        initFilePath();
        try{
            initMtcnnNcnn();
        }catch (Exception ex){
            HmctLog.e(TAG,ex.toString());
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    protected void initFilePath() {
        File picFile = new File(AppConfig.getPic());
        if (!picFile.exists()) {
            picFile.mkdirs();
        }
    }

    public ExecutorService getExecutor() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newFixedThreadPool(CORE_POOL_SIZE,
                    sThreadFactory);
        }
        return mExecutorService;
    }
    public NcnnMtcnn getmFaceMtcnn(){
        return mFaceMtcnn;
    }
    private void initMtcnnNcnn() throws Exception {
        byte[] det1_param = null;
        byte[] det1_bin = null;

        byte[] det2_param = null;
        byte[] det2_bin = null;

        byte[] det3_param = null;
        byte[] det3_bin = null;
        //load pnet
        {
            InputStream assetsInputStream = getAssets().open("zdet1_new.param.bin");
            int available = assetsInputStream.available();
            det1_param = new byte[available];
            int byteCode = assetsInputStream.read(det1_param);
            assetsInputStream.close();
        }

        {
            InputStream assetsInputStream = getAssets().open("zdet1_new.bin");
            int available = assetsInputStream.available();
            det1_bin = new byte[available];
            int byteCode = assetsInputStream.read(det1_bin);
            assetsInputStream.close();
        }

        //load rnet
        {
            InputStream assetsInputStream = getAssets().open("zdet2_new.param.bin");
            int available = assetsInputStream.available();
            det2_param = new byte[available];
            int byteCode = assetsInputStream.read(det2_param);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = getAssets().open("zdet2_new_q1.bin");
            int available = assetsInputStream.available();
            det2_bin = new byte[available];
            int byteCode = assetsInputStream.read(det2_bin);
            assetsInputStream.close();
        }
        //load onet
        {
            InputStream assetsInputStream = getAssets().open("zdet3_new.param.bin");
            int available = assetsInputStream.available();
            det3_param = new byte[available];
            int byteCode = assetsInputStream.read(det3_param);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = getAssets().open("zdet3_new_q1.bin");
            int available = assetsInputStream.available();
            det3_bin = new byte[available];
            int byteCode = assetsInputStream.read(det3_bin);
            assetsInputStream.close();
        }

        boolean load_result = mFaceMtcnn.Init(det1_param, det1_bin, det2_param, det2_bin, det3_param,
                det3_bin);

        Log.d(TAG, "load model,result=" + load_result);
    }
}
