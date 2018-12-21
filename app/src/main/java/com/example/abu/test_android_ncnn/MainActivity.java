package com.example.abu.test_android_ncnn;

import android.os.Bundle;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import android.text.method.ScrollingMovementMethod;

import android.support.annotation.Nullable;

import android.app.Activity;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.net.Uri;
import android.util.Log;

import com.hanzheng.facedetectapplication.R;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final int USE_PHOTO = 1001;
    private ImageView show_image;
    private TextView result_text;
    private NcnnMtcnn face_mtcnn = new NcnnMtcnn();
    private boolean load_result = false;


    //private boolean load_result = false;
    //private List<String> resultLabel = new ArrayList<>();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        try {
            initMtcnnNcnn();
        } catch (IOException e) {
            Log.e("MainActivity", "initSqueezeNcnn error");
        }
        init_view();

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.result_text);
        tv.setText("Please select image!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initMtcnnNcnn() throws IOException {
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

        //load reg---do not use in this vision
        /*byte[] reg_param = null;
        byte[] reg_bin = null;

        {
            InputStream assetsInputStream = getAssets().open("zreg_new.param.bin");
            int available = assetsInputStream.available();
            reg_param = new byte[available];
            int byteCode = assetsInputStream.read(reg_param);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = getAssets().open("zreg_new_q1.bin");
            int available = assetsInputStream.available();
            reg_bin = new byte[available];
            int byteCode = assetsInputStream.read(reg_bin);
            assetsInputStream.close();
        }
        face_mtcnn.FeaInit(reg_param, reg_bin);*/

        load_result = face_mtcnn.Init(det1_param, det1_bin, det2_param, det2_bin, det3_param, det3_bin);

        Log.d("load model", "result:" + load_result);
    }

    // initialize view
    private void init_view() {
        show_image = (ImageView) findViewById(R.id.show_image);
        result_text = (TextView) findViewById(R.id.result_text);
        result_text.setMovementMethod(ScrollingMovementMethod.getInstance());
        Button use_photo = (Button) findViewById(R.id.use_photo); // use photo click

        use_photo.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {

                if (!load_result) {
                    Toast.makeText(MainActivity.this, "never load model", Toast.LENGTH_SHORT).show();
                    return;
                }
                PhotoUtil.use_photo(MainActivity.this, USE_PHOTO);
            }
        });
    }

    //draw face boxes
    private void draw_face_boxes(Bitmap rgba, FaceBox[] face_boxes)
    {
        int min_img_size = rgba.getHeight() < rgba.getWidth() ? rgba.getHeight() : rgba.getWidth();
        float line_width = 6*min_img_size /1000 > 2 ? 6*min_img_size /1000 : 2;
        float point_radius = 5*min_img_size /1000 > 2 ? 5*min_img_size /1000 : 2;

        Canvas canvas = new Canvas(rgba);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);//不填充
        paint.setStrokeWidth(line_width);  //线的宽度

        int face_num = face_boxes.length;

        //draw face boxes
        for(int i = 0; i < face_num; i++)
        {
            String key = String.valueOf(i);
            if(face_boxes[i].is_upright == 0)
                paint.setColor(Color.RED);
            else
                paint.setColor(Color.GREEN);

            //图像上画矩形
            canvas.drawRect(face_boxes[i].rect[0], face_boxes[i].rect[1],
                    face_boxes[i].rect[2], face_boxes[i].rect[3], paint);
        }

        //draw landmarks
        paint.setColor(Color.rgb(240,250, 15));
        paint.setStyle(Paint.Style.FILL);//不填充
        for(int i = 0; i < face_num; i++)
        {
            String key = String.valueOf(i);

            for(int lmi = 0; lmi < 5; lmi++)
            {
                canvas.drawCircle(face_boxes[i].align_points[lmi], face_boxes[i].align_points[lmi+5], point_radius, paint);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        String image_path;
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case USE_PHOTO:
                    if (data == null) {
                        Log.w(TAG, "user photo data is null"); return;
                    }
                    String result_inf = "";

                    //Get image form storge
                    Uri image_uri = data.getData();
                    image_path = PhotoUtil.get_path_from_URI(MainActivity.this, image_uri);// get image path from uri
                    Bitmap bmp = PhotoUtil.getScaleBitmap(image_path);//get bitmap image

                    if(bmp == null)
                    {
                        //print results
                        TextView tv = (TextView) findViewById(R.id.result_text);
                        tv.setText("read image failed! this maybe cased by permission of storage.");
                        break;
                    }

                    int img_width = bmp.getWidth();
                    int img_height = bmp.getHeight();
                    int max_size = img_width > img_height ? img_width : img_height;
                    int new_width = img_width, new_height = img_height;

                    if(max_size > 960) {
                        float scale = 960 / (float)max_size;

                        new_width = (int) (img_width * scale);
                        new_height = (int) (img_height * scale);
                    }


                    Bitmap rgba = bmp.copy(Bitmap.Config.ARGB_8888, true);//copy as process image
                    Bitmap input_bmp = Bitmap.createScaledBitmap(rgba, new_width, new_height, false);
                    Bitmap rgba_show = input_bmp.copy(Bitmap.Config.ARGB_8888, true);//copy as show image

                    result_inf += "image:  " + image_path + "\n\n";
                    result_inf += "size:  " + String.valueOf(rgba_show.getWidth()) + " X " +
                            String.valueOf(rgba_show.getHeight()) + "\n\n";

                    //show_image.setImageURI(data.getData());
                    show_image.setImageBitmap(input_bmp);


                    long startTime=System.currentTimeMillis();//记录开始时间
                    //do face detection, return the face inf sequence
                    FaceBox facebox_class = new FaceBox();
                    FaceBox[] face_boxes = face_mtcnn.DetectFace(facebox_class, input_bmp, 50, 0.65, 0.7, 0.6, 0.559);

                    long endTime=System.currentTimeMillis();//记录结束时间
                    float detTime=(float)(endTime-startTime);

                    startTime=System.currentTimeMillis();//记录开始时间
                    //float[] fea = face_mtcnn.FeaExtract(input_bmp);//do not extract feature in this vision
                    endTime=System.currentTimeMillis();//记录结束时间
                    float excTime=(float)(endTime-startTime);

                    //face crop and align
                    FaceAlignInf fa_class = new FaceAlignInf();
                    FaceAlignInf[] facealigns = face_mtcnn.FaceAlign(fa_class, input_bmp, face_boxes);

                    //show align face
                    /*for(int i = 0; i < facealigns.length; i++)
                    {
                        Bitmap resultface = Bitmap.createBitmap(facealigns[i].img_data,facealigns[i].width,facealigns[i].height, Bitmap.Config.ARGB_8888);

                        int kk = 0;
                    }*/

                    result_inf += "face front： 红框为侧面|绿色为正面\n\n";
                    result_inf += "face num:  " + String.valueOf(face_boxes.length) + "\n\n";
                    result_inf += "time:  " + "det-"+String.valueOf(detTime) + ", fea-"+String.valueOf(excTime) + "ms\n\n";

                    //draw face detection results
                    draw_face_boxes(rgba_show, face_boxes);
                    show_image.setImageBitmap(rgba_show);

                    //print results
                    TextView tv = (TextView) findViewById(R.id.result_text);
                    tv.setText(result_inf);

                    break;
            }
        }
    }

}
