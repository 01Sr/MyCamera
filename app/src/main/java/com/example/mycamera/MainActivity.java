package com.example.mycamera;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    android.hardware.Camera camera=null;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    int rotation=0;
    static int count=0;
    Camera.CameraInfo cameraInfo;
    int preTime=0;
    int curTime;
    final String URL="http://01face.tunnel.qydev.com/upload.php/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        surfaceView= (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder=surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(MainActivity.this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
         cameraInfo=new Camera.CameraInfo();
        for(int i=0;i<Camera.getNumberOfCameras();i++){
            Camera.getCameraInfo(i, cameraInfo);
            if(cameraInfo.facing== Camera.CameraInfo.CAMERA_FACING_FRONT)
                camera=Camera.open(i);
        }
        try {

            camera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
                Toast toast = null;

                @Override
                public void onFaceDetection(Camera.Face[] faces, Camera camera) {


                    if (faces.length != 0) {
                        final Camera.Face face = faces[0];
                        Camera.Size size = camera.getParameters().getPreviewSize();
                        if (face.rect.width() * 3 < size.width) {

                            if (toast != null)
                                toast.setText("请靠近点");
                            else
                                toast = Toast.makeText(MainActivity.this, "请靠近点", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            if (toast != null)
                                toast.cancel();
                            if (count!=5) {
                                camera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
                                    @Override
                                    public void onPreviewFrame(byte[] data, Camera camera) {
                                        Camera.Size size = camera.getParameters().getPreviewSize();
                                        int w = size.width;
                                        int h = size.height;
                                        YuvImage image = new YuvImage(data, ImageFormat.NV21, w, h, null);
                                        ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
                                        if (!image.compressToJpeg(new Rect(0, 0, w, h), 100, os)) {
                                            return;
                                        }
                                        final byte[] tmp = os.toByteArray();
                                        Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
                                        RectF rect = new RectF();
                                        rect.set(face.rect);
                                        Log.e("+++++++++rect x  width", face.rect.left + " " + face.rect.width());
                                        Matrix matrix = new Matrix();
                                        matrix.postScale(bmp.getWidth() / 2000f, bmp.getHeight() / 2000f);
                                        Log.e("+++++++++bmp width", String.valueOf(bmp.getWidth()));
                                        matrix.postTranslate(bmp.getWidth() / 2f, bmp.getHeight() / 2f);
                                        matrix.mapRect(rect);
//                                        Bitmap bmp1=Bitmap.createBitmap(bmp,(int)rect.left,(int)rect.top,(int)rect.width(),(int)rect.height(),null,false);
                                        matrix = new Matrix();
                                        matrix.postRotate(rotation);
                                        Bitmap bmp1 = Bitmap.createBitmap(bmp, (int) rect.left, (int) rect.top, (int) rect.width(), (int) rect.height(), matrix, false);
                                        Log.e("+++++++++bmp1 width", String.valueOf(bmp1.getWidth()));
                                        Log.e("+++++++++rect1 x  width", rect.left + " " + rect.width());
                                        bmp = null;
                                        ByteArrayOutputStream os1 = new ByteArrayOutputStream();
                                        bmp1.compress(Bitmap.CompressFormat.JPEG, 100, os1);
                                        final byte[] tmp1 = os1.toByteArray();
                                         curTime= (int) new Date().getTime();
                                       if(preTime==0||curTime-preTime>1500){
                                           preTime=curTime;
                                           new Thread(new Runnable() {
                                               @Override
                                               public void run() {

                                                   String end = "\r\n";
                                                   String twoHyphens = "--";
                                                   String boundary = "*********";
                                                   int TIME_OUT = 10 * 10000000;
                                                   String filename = UUID.randomUUID().toString();
                                                   try {
                                                       URL url = new URL(URL);
                                                       HttpURLConnection httpURLConnection = (HttpURLConnection) url
                                                               .openConnection();
                                                       httpURLConnection.setReadTimeout(TIME_OUT);
                                                       httpURLConnection.setConnectTimeout(TIME_OUT);
                                                       // 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
                                                       // 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
                                                       httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
                                                       // 允许输入输出流
                                                       httpURLConnection.setDoInput(true);
                                                       httpURLConnection.setDoOutput(true);
                                                       httpURLConnection.setUseCaches(false);
                                                       // 使用POST方法
                                                       httpURLConnection.setRequestMethod("POST");
                                                       httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                                                       httpURLConnection.setRequestProperty("Charset", "UTF-8");
                                                       httpURLConnection.setRequestProperty("Content-Type",
                                                               "multipart/form-data;boundary=" + boundary);

                                                       DataOutputStream dos = new DataOutputStream(
                                                               httpURLConnection.getOutputStream());
                                                       dos.writeBytes(twoHyphens + boundary + end);
                                                       dos.writeBytes("Content-Disposition: form-data; name=\"img\"; filename=\"tmp"+UUID.randomUUID().toString()+".jpeg\""
                                                               + end);
                                                       dos.writeBytes(end);
                                                       dos.write(tmp1);


                                                       dos.writeBytes(end);
                                                       dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
                                                       dos.flush();

                                                       InputStream is = httpURLConnection.getInputStream();
                                                       InputStreamReader isr = new InputStreamReader(is, "utf-8");
                                                       BufferedReader br = new BufferedReader(isr);
                                                       String line;
                                                       String result = new String();
                                                       while ((line = br.readLine()) != null)
                                                           result += line;
                                                       final String finalResult = result;
                                                       runOnUiThread(new Runnable() {
                                                           @Override
                                                           public void run() {
                                                          if(finalResult.equals("0")){
                                                              Toast.makeText(MainActivity.this,"图片接收失败，请稍后重试",Toast.LENGTH_SHORT);
                                                          }else{
                                                              if(finalResult.equals("-1")){
                                                                  startActivity(new Intent(MainActivity.this,RegeistActivity.class));
                                                              }else{
                                                                  Intent intent=new Intent(MainActivity.this,ShowNameActivity.class);
                                                                  intent.putExtra("name",finalResult);
                                                                  startActivity(intent);
                                                              }
                                                          }
                                                           }
                                                       });

                                                       Log.e("*************result", result);
                                                       dos.close();
                                                       is.close();

                                                   } catch (Exception e) {
                                                       e.printStackTrace();
                                                       runOnUiThread(new Runnable() {
                                                           @Override
                                                           public void run() {
                                                               Toast.makeText(MainActivity.this, "服务器访问错误！", Toast.LENGTH_SHORT).show();
                                                           }
                                                       });
                                                   }
                                               }
                                           }

                                           ).start();
                                           count++;
                                       }

                                    }
                                });
                            }
                        }
                    }
                }
            });
            camera.setPreviewDisplay(holder);
            final android.hardware.Camera.Parameters parameters=camera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            int rotation =MainActivity.this.getWindowManager().getDefaultDisplay().getRotation();
            int degree=0;
            switch(rotation){
                case Surface.ROTATION_0: degree=0;break;
                case Surface.ROTATION_90:degree=90;break;
                case Surface.ROTATION_180:degree=180;break;
                case Surface.ROTATION_270:degree=270;break;
            }
            int result;
            result=(cameraInfo.orientation+degree)%360;
           result=(360-result)%360;
            this.rotation=(degree+cameraInfo.orientation)%360;
            camera.setDisplayOrientation(result);
            camera.setParameters(parameters);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            camera.setFaceDetectionListener(null);
            camera.setOneShotPreviewCallback(null);
            camera.release();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(camera!=null){
            camera.setFaceDetectionListener(null);
            camera.setOneShotPreviewCallback(null);
            camera.stopPreview();
            camera.release();
        }
    }
}
