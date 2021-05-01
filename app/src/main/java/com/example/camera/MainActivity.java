package com.example.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.bifan.detectlib.FaceDetectTextureView;
import com.bifan.detectlib.FaceDetectView;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    private FaceDetectView faceDetectView;
    private Button buttonTakePhoto;
    private ImageView ivShow;
    private String bitmapStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        faceDetectView = findViewById(R.id.faceDetectView);
        buttonTakePhoto = findViewById(R.id.btnTakePhoto);
        ivShow = findViewById(R.id.ivShow);

        faceDetectView.setFramePreViewListener(new FaceDetectTextureView.IFramePreViewListener() {
            @Override
            public boolean onFrame(Bitmap eachFrame) {

                return false;
            }

            @Override
            public boolean onFaceFrame(Bitmap faceFrame, FaceDetector.Face[] faces) {
                bitmapStr = convertIconToString(faceFrame);
                ivShow.post(new Runnable() {
                    @Override
                    public void run() {
                        if (bitmapStr != null) {
                            ivShow.setImageBitmap(convertStringToIcon(bitmapStr));
                        }
                    }
                });
                return false;
            }
        });

        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    startDetect(null);
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                    ConfirmationDialogFragment.newInstance(R.string.camera_permission_confirmation,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                            .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }
            }
        });
    }

    public static String convertIconToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(appicon, Base64.DEFAULT);

    }

    /**
     * string转成bitmap
     *
     * @param st
     */
    public static Bitmap convertStringToIcon(String st) {
        // OutputStream out;
        Bitmap bitmap = null;
        try {
            // out = new FileOutputStream("/sdcard/aa.jpg");
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap =
                    BitmapFactory.decodeByteArray(bitmapArray, 0,
                            bitmapArray.length);
            // bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }


    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";


    public void startDetect(View view) {
        if (!faceDetectView.isHasInit()) {
            //必须是在view可见后进行初始化
            faceDetectView.initView();
            faceDetectView.initCamera();
            faceDetectView.getDetectConfig().CameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
            faceDetectView.getDetectConfig().EnableFaceDetect = true;
            faceDetectView.getDetectConfig().MinDetectTime = 100;
            faceDetectView.getDetectConfig().Simple = 0.8f;//图片检测时的压缩取样率，0~1，越小检测越流畅
            faceDetectView.getDetectConfig().MaxDetectTime = 2000;//进入智能休眠检测，以2秒一次的这个速度检测
            faceDetectView.getDetectConfig().EnableIdleSleepOption = false;//启用智能休眠检测机制
            faceDetectView.getDetectConfig().IdleSleepOptionJudgeTime = 1000 * 10;//1分钟内没有检测到人脸，进入智能休眠检测
        }
        faceDetectView.startCameraPreview();
    }

    public void endDetect(View view) {
        faceDetectView.stopCameraPreview();
        faceDetectView.getFaceRectView().clearBorder();
    }

    public static class ConfirmationDialogFragment extends DialogFragment {
        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                                                             String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }
    }
}