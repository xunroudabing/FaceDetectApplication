package com.hanzheng.facedetectapplication.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.abu.test_android_ncnn.FaceBox;
import com.example.abu.test_android_ncnn.NcnnMtcnn;
import com.hanzheng.facedetectapplication.FaceDetectApplication;
import com.hanzheng.facedetectapplication.R;
import com.hanzheng.facedetectapplication.api.Api;
import com.hanzheng.facedetectapplication.camrea.CameraMatrix;
import com.hanzheng.facedetectapplication.camrea.ICamera;
import com.hanzheng.facedetectapplication.camrea.NV21ToBitmap;
import com.hanzheng.facedetectapplication.camrea.OpenGLUtil;
import com.hanzheng.facedetectapplication.camrea.PointsMatrix;
import com.hanzheng.facedetectapplication.camrea.Screen;
import com.hanzheng.facedetectapplication.config.AppConfig;
import com.hanzheng.facedetectapplication.config.Constant;
import com.hanzheng.facedetectapplication.face.FaceBitmap;
import com.hanzheng.facedetectapplication.face.FaceDetectManager;
import com.hanzheng.facedetectapplication.face.FaceLink;
import com.hanzheng.facedetectapplication.log.HmctLog;
import com.hanzheng.facedetectapplication.utils.BitmapUtils;
import com.hanzheng.facedetectapplication.utils.StringUtils;
import com.hanzheng.facedetectapplication.utils.SystemUtils;

import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-12-13.
 */

public class CamreaActivity extends AppCompatActivity implements Camera.PreviewCallback,
        GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    static final String TAG = CamreaActivity.class.getSimpleName();
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    ICamera mICamera;
    Camera mCamera;
    GLSurfaceView mSurfaceView;
    NV21ToBitmap mNV21Converter;
    int width, height;
    long currentTime = 0;
    long lastTime = 0;
    private NcnnMtcnn mFaceMtcnn;
    private Disposable mDisposable, mAutoFocusDisposable;
    private int mTextureID = -1;
    private SurfaceTexture mSurface;
    private CameraMatrix mCameraMatrix;
    private PointsMatrix mPointsMatrix;
    private boolean load_result = false;
    private boolean uploadFaceFeatureRunning = true;
    private boolean faceDetectRunning = true;
    private boolean catchFaceRunning = false;
    private long catchFaceStartTime = 0L;
    private Consumer<Long> mAutoFocus = new Consumer<Long>() {
        @Override
        public void accept(Long aLong) throws Exception {
            try {
                autoFocus();
            } catch (Exception ex) {
                HmctLog.e(TAG, ex.toString());
            }
        }
    };
    private Consumer<Long> mConsumer = new Consumer<Long>() {
        @Override
        public void accept(Long aLong) throws Exception {
            try {
                FaceDetectManager.getInstance().printLog();
                //获取计数大于n次的数据
                List<FaceLink> list = FaceDetectManager.getInstance().getFaceLinkOverCount(AppConfig.getShootCount());
                Api api = new Api(AppConfig.getServerIP(), AppConfig.getServerPort());
                if (list != null && list.size() > 0) {
                    for (FaceLink item : list) {
                        try {
                            String device_id = SystemUtils.getDeviceID(getApplicationContext());
                            String ret = api.personIdRecognize(item.getfeature(), device_id);
                            HmctLog.d(TAG, "Seq1 personIdRecognize success,ret=" + ret);
                        } catch (Exception ex) {
                            HmctLog.e(TAG, ex.toString());
                        }
                    }
                    FaceDetectManager.getInstance().clearFaceLinkArray();
                    sendBoYunBroadCast(Constant.ACTION_SEND_FACE_DETECT_COMPLETE);
                }
            } catch (Exception ex) {
                HmctLog.e(TAG, ex.toString());
            }
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action.equals(Constant.ACTION_START_FACE_DETECT)) {
                    faceDetectRunning = true;
                    Toast.makeText(CamreaActivity.this,"人脸检测开始...",Toast.LENGTH_SHORT).show();
                } else if (action.equals(Constant.ACTION_STOP_FACE_DETECT)) {
                    faceDetectRunning = false;
                    Toast.makeText(CamreaActivity.this,"人脸检测停止...",Toast.LENGTH_SHORT).show();
                } else if (action.equals(Constant.ACTION_CATCH_FACE)) {
                    catchFaceRunning = true;
                    catchFaceStartTime = System.currentTimeMillis();
                    Toast.makeText(CamreaActivity.this,"开始抓拍人脸",Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                HmctLog.e(TAG, ex.toString());
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        try {
            mFaceMtcnn = FaceDetectApplication.getInstance().getmFaceMtcnn();
            //initMtcnnNcnn();
            startInterval();
            startGetFaceFeature();
            registerBoYunReceiver();
        } catch (Exception ex) {
            HmctLog.e(TAG, ex.toString());
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //Log.d(TAG, "onPreviewFrame,data.length=" + data.length);
        if (faceDetectRunning) {
            onDetect(data, camera);
        }
        if (catchFaceRunning) {
            onDetectFaceAndUpload(data, camera);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        openCamrea();
        startAutoFocus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamrea();
        stopAutoFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopInterval();
        uploadFaceFeatureRunning = false;
        unregisterBoYunReceiver();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 黑色背景
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        surfaceInit();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        ratio = 1; // 这样OpenGL就可以按照屏幕框来画了，不是一个正方形了
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);// 清除屏幕和深度缓存
        float[] mtx = new float[16];
        mSurface.getTransformMatrix(mtx);
        mCameraMatrix.draw(mtx);
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1f, 0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
        mPointsMatrix.draw(mMVPMatrix);
        mSurface.updateTexImage();// 更新image，会调用onFrameAvailable方法
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mSurfaceView.requestRender();
    }

    protected void initViews() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Screen.initialize(CamreaActivity.this);
        setContentView(R.layout.activity_camrea_demo);
        mSurfaceView = (GLSurfaceView) findViewById(R.id.camrea_surfaceview);
        mSurfaceView.setEGLContextClientVersion(2);// 创建一个OpenGL ES 2.0
        mSurfaceView.setRenderer(this);// 设置渲染器进入gl
        mSurfaceView.setRenderMode(mSurfaceView.RENDERMODE_WHEN_DIRTY);// 设置渲染器模式
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoFocus();
            }
        });
        mICamera = new ICamera();
        mNV21Converter = new NV21ToBitmap(getApplicationContext());
    }

    protected void startGetFaceFeature() {
        FaceDetectApplication.getInstance().getExecutor().execute(new GetFaceFeature());
    }

    protected void startInterval() {
        mDisposable = Flowable.interval(1, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe(mConsumer);
    }

    protected void startAutoFocus() {
        mAutoFocusDisposable = Flowable.interval(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers
                .mainThread())
                .subscribe(mAutoFocus);
    }

    protected void stopAutoFocus() {
        if (mAutoFocusDisposable != null) {
            mAutoFocusDisposable.dispose();
        }
    }

    protected void stopInterval() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    protected void openCamrea() {
        HashMap<String, Integer> resolutionMap = new HashMap<>();
        resolutionMap.put("width", Constant.CAMREA_WIDTH);
        resolutionMap.put("height", Constant.CAMREA_HEIGHT);
        mCamera = mICamera.openCamera(true, this, resolutionMap);
        if (mCamera != null) {
            RelativeLayout.LayoutParams layout_params = mICamera.getLayoutParam();
            mSurfaceView.setLayoutParams(layout_params);

            width = mICamera.cameraWidth;
            height = mICamera.cameraHeight;

            HmctLog.d(TAG, "wight = " + width + ", height = " + height);
        } else {
            Toast.makeText(this, "打开相机失败", Toast.LENGTH_SHORT).show();
        }
    }

    protected void releaseCamrea() {
        mICamera.closeCamera();
        mCamera = null;
    }

    private void surfaceInit() {
        mTextureID = OpenGLUtil.createTextureID();
        mSurface = new SurfaceTexture(mTextureID);
        mSurface.setOnFrameAvailableListener(this);// 设置照相机有数据时进入
        mCameraMatrix = new CameraMatrix(mTextureID);
        mPointsMatrix = new PointsMatrix(false);
        mPointsMatrix.isShowFaceRect = true;
        mICamera.startPreview(mSurface);// 设置预览容器
        mICamera.actionDetect(this);
    }

    private void autoFocus() {
        try {
            if (mCamera != null) {
                mCamera.cancelAutoFocus();
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                mCamera.setParameters(parameters);
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        HmctLog.d(TAG, "******AutoFocus = " + success);
                    }
                });
            }
        } catch (Exception ex) {
            HmctLog.e(TAG, ex.toString());
        }
    }

    /**
     * 立即采一张当前图片获取人脸，进行查询。
     *
     * @param data
     * @param camrea
     */
    protected void onDetectFaceAndUpload(byte[] data, Camera camrea) {
        try {
            currentTime = System.currentTimeMillis();
            boolean start = false;
            if (currentTime - lastTime > Constant.DETECT_INTERVAL) {
                lastTime = currentTime;
                start = true;
            }
            if (start) {
                HmctLog.i(TAG, "DetectFaceAndUpload...");
                Bitmap bitmap = mNV21Converter.nv21ToBitmap(data, width, height);
                //bitmap = BitmapUtils.rotaingImageView(90F,bitmap);
//                String path = AppConfig.getPic() + System.currentTimeMillis() + ".png";
//                BitmapUtils.save(bitmap,path);
//                HmctLog.d(TAG, "bitmap.width=" + bitmap.getWidth() + ",height=" + bitmap
//                        .getHeight());
                FaceBox facebox_class = new FaceBox();
                FaceBox[] face_boxes = mFaceMtcnn.DetectFace(facebox_class, bitmap, 50, 0.65,
                        0.7, 0.6, 0.559);
                if (face_boxes != null && face_boxes.length > 0) {
                    HmctLog.i(TAG, "detect face success,face.count=" + face_boxes.length);
                    //找出像素最大的人脸
                    Rect targetRect = new Rect();
                    for (FaceBox item : face_boxes) {
                        HmctLog.i(TAG, "FaceBox is_upright=" + item.is_upright + ",score=" + item
                                .score +
                                ",rect=" + item.rect[0] + "," + item.rect[1] + "," + item.rect[2]
                                + "," + item.rect[3]);
                        int is_upright = item.is_upright;
                        //正脸
                        if (is_upright == 1) {
                            Rect rect = new Rect(item.rect[0], item.rect[1], item.rect[2], item
                                    .rect[3]);
                            int rect_px = rect.width() * rect.height();
                            int target_px = targetRect.width() * targetRect.height();
                            if (rect_px > target_px) {
                                targetRect = rect;
                            }
                        }
                    }
                    if (targetRect.width() > 0 && targetRect.height() > 0) {
                        //目标人脸
                        Bitmap b = Bitmap.createBitmap(bitmap, targetRect.left, targetRect.top,
                                targetRect.width(), targetRect.height());
                        FaceDetectApplication.getInstance().getExecutor().execute(new CatchFace(b));
                        //流程2结束
                        catchFaceRunning = false;
                    } else {
                        //失败，无正脸
                        long currentMills = System.currentTimeMillis();
                        //失败超出5秒则停止
                        if (currentMills - catchFaceStartTime > 5000) {
                            catchFaceRunning = false;
                        }
                    }
                } else {
                    //失败,无人脸
                    long currentMills = System.currentTimeMillis();
                    //失败超出5秒则停止
                    if (currentMills - catchFaceStartTime > 5000) {
                        catchFaceRunning = false;
                    }
                }
            }
        } catch (Exception ex) {
            HmctLog.e(TAG, ex.toString());
        }
    }

    /**
     * 检测人脸
     *
     * @param data
     * @param camera
     */
    protected void onDetect(byte[] data, Camera camera) {
        try {
            currentTime = System.currentTimeMillis();
            boolean start = false;
            if (currentTime - lastTime > Constant.DETECT_INTERVAL) {
                lastTime = currentTime;
                start = true;
            }
            if (start) {
                HmctLog.i(TAG, "detect...");
//                YuvImage image = new YuvImage(data, ImageFormat.NV21, width,
//                        height, null);
                Bitmap bitmap = mNV21Converter.nv21ToBitmap(data, width, height);
                //bitmap = BitmapUtils.rotaingImageView(90F,bitmap);
//                String path = AppConfig.getPic() + System.currentTimeMillis() + ".png";
//                BitmapUtils.save(bitmap,path);
//                HmctLog.d(TAG, "bitmap.width=" + bitmap.getWidth() + ",height=" + bitmap
//                        .getHeight());
                FaceBox facebox_class = new FaceBox();
                FaceBox[] face_boxes = mFaceMtcnn.DetectFace(facebox_class, bitmap, 50, 0.65,
                        0.7, 0.6, 0.559);
                if (face_boxes != null && face_boxes.length > 0) {
                    HmctLog.i(TAG, "detect face success,face.count=" + face_boxes.length);
                    FaceBitmap faceBitmap = new FaceBitmap();
                    faceBitmap.frame = currentTime;
                    for (FaceBox item : face_boxes) {
                        HmctLog.i(TAG, "FaceBox is_upright=" + item.is_upright + ",score=" + item
                                .score +
                                ",rect=" + item.rect[0] + "," + item.rect[1] + "," + item.rect[2]
                                + "," + item.rect[3]);
                        int is_upright = item.is_upright;
                        //正脸
                        if (is_upright == 1) {
                            Bitmap b = Bitmap.createBitmap(bitmap, item.rect[0], item.rect[1], item
                                    .rect[2] - item.rect[0], item.rect[3] - item.rect[1]);
                            faceBitmap.addBitmap(b);
                        }
                    }
                    FaceDetectManager.getInstance().addQueue(faceBitmap);
                    HmctLog.i(TAG, "detect complete");
                    bitmap.recycle();
                }
                ArrayList<ArrayList> pointsOpengl = new ArrayList<>();
                ArrayList<FloatBuffer> rectsOpengl = new ArrayList<>();//征脸
                ArrayList<FloatBuffer> rectsOpengl_red = new ArrayList<>();//侧脸
                if (face_boxes != null) {
                    for (int i = 0; i < face_boxes.length; i++) {
                        if (mPointsMatrix.isShowFaceRect) {
                            FaceBox item = face_boxes[i];
                            Rect rect = new Rect(item.rect[0], item.rect[1],
                                    item.rect[2], item.rect[3]);
                            FloatBuffer buffer = calRectPostion(rect, mICamera.cameraWidth,
                                    mICamera.cameraHeight);
                            if (item.is_upright == 1) {
                                rectsOpengl.add(buffer);
                            } else {
                                rectsOpengl_red.add(buffer);
                            }
                        }
                    }
                }
                synchronized (mPointsMatrix) {
                    mPointsMatrix.points = pointsOpengl;
                    mPointsMatrix.faceRects = rectsOpengl;
                    mPointsMatrix.faceSideRects = rectsOpengl_red;
                }
            }
        } catch (Exception e) {
            HmctLog.e(TAG, e.toString());
        }

    }

    private FloatBuffer calRectPostion(Rect rect, float width, float height) {
        float top = 1 - (rect.top * 1.0f / height) * 2;
        float left = (rect.left * 1.0f / width) * 2 - 1;
        float right = (rect.right * 1.0f / width) * 2 - 1;
        float bottom = 1 - (rect.bottom * 1.0f / height) * 2;

        // 左上角
        float x1 = -top;
        float y1 = left;

        // 右下角
        float x2 = -bottom;
        float y2 = right;
        //isBackCamera
        if (true) {
            y1 = -y1;
            y2 = -y2;
        }

        float[] tempFace = {
                x1, y2, 0.0f,
                x1, y1, 0.0f,
                x2, y1, 0.0f,
                x2, y2, 0.0f,
        };

        FloatBuffer buffer = mCameraMatrix.floatBufferUtil(tempFace);
        return buffer;
    }
    protected void sendBoYunBroadCast(String action){
        Intent intent = new Intent(action);
        sendBroadcast(intent);
        HmctLog.d(TAG,"sendBoYunBroadCast,action=" + action);
    }
    //注册广播监听
    protected void registerBoYunReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ACTION_START_FACE_DETECT);
        filter.addAction(Constant.ACTION_STOP_FACE_DETECT);
        filter.addAction(Constant.ACTION_CATCH_FACE);

        registerReceiver(mReceiver, filter);
    }

    protected void unregisterBoYunReceiver() {
        unregisterReceiver(mReceiver);
    }

    //流程2
    class CatchFace implements Runnable {
        Bitmap mBitmap;
        Api mApi;

        public CatchFace(Bitmap bitmap) {
            mBitmap = bitmap;
            mApi = new Api(AppConfig.getServerIP(), AppConfig.getServerPort());
        }

        @Override
        public void run() {
            try {
                byte[] bytes = BitmapUtils.bitmapPngToBytes(mBitmap);
                float[] floats = mApi.faceFeatureExtract(bytes);
                if (floats != null) {
                    HmctLog.i(TAG, "Seq2 faceFeatureExtract success,ret=" + StringUtils
                            .convert(floats));
                    String device_id = SystemUtils.getDeviceID(getApplicationContext());
                    HmctLog.d(TAG, "Seq2 start personIdRecognize deviceid=" + device_id);
                    String ret = mApi.personIdRecognize(floats, device_id);
                    HmctLog.d(TAG, "Seq2 personIdRecognize success,ret=" + ret);

                    sendBoYunBroadCast(Constant.ACTION_SEND_CATCH_FACE_COMPLETE);
                }
            } catch (Exception ex) {
                HmctLog.e(TAG, ex.toString());
            }finally {
                mBitmap.recycle();
            }
        }
    }

    class GetFaceFeature implements Runnable {
        Api mApi;
        public GetFaceFeature() {
            mApi = new Api(AppConfig.getServerIP(), AppConfig.getServerPort());
        }

        @Override
        public void run() {
            while (uploadFaceFeatureRunning) {
                try {
                    FaceBitmap faceBitmap = FaceDetectManager.getInstance().take();
                    HmctLog.i(TAG, "get a bitmap,queue.size=" + FaceDetectManager.getInstance()
                            .getQueueSize());
                    List<WeakReference<Bitmap>> bitmaps = faceBitmap.bitmaps;
                    List<float[]> features = new ArrayList<>();
                    for (WeakReference<Bitmap> item : bitmaps) {
                        Bitmap bitmap = item.get();
                        if (bitmap != null) {
//                            String filepath = AppConfig.getPic() + System.currentTimeMillis() +
// "" +
//                                    ".png";
//                            BitmapUtils.save(bitmap, filepath);
                            //特征检测
                            try {
                                //byte[] bytes = BitmapUtils.bitmapToBytes(bitmap);
                                byte[] bytes = BitmapUtils.bitmapPngToBytes(bitmap);
                                float[] floats = mApi.faceFeatureExtract(bytes);
                                if (floats != null) {
                                    features.add(floats);
                                    HmctLog.i(TAG, "Seq1 faceFeatureExtract success,ret=" + StringUtils
                                            .convert(floats));
                                }
                            } catch (Exception ex) {
                                HmctLog.e(TAG, ex.toString());
                            } finally {
                                bitmap.recycle();
                            }
                        }
                    }
                    //FaceDetectManager.getInstance().printLog();
                    //开始人脸特征比对
                    FaceDetectManager.getInstance().compareFaces(features);
                } catch (Exception ex) {
                    HmctLog.e(TAG, ex.toString());
                }
            }
        }
    }
}
