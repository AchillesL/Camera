package com.example.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.bifan.detectlib.FaceDetectTextureView;
import com.bifan.detectlib.FaceDetectView;

public class MainActivity extends AppCompatActivity {

    private FaceDetectView faceDetectView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        faceDetectView = findViewById(R.id.faceDetectView);

        faceDetectView.setFramePreViewListener(new FaceDetectTextureView.IFramePreViewListener() {
            @Override
            public boolean onFrame(Bitmap eachFrame) {

                return false;
            }

            @Override
            public boolean onFaceFrame(Bitmap faceFrame, FaceDetector.Face[] faces) {
                return false;
            }
        });

        faceDetectView.postDelayed(new Runnable() {
            @Override
            public void run() {
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
        },1000);
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
            faceDetectView.getDetectConfig().Simple = 1f;//图片检测时的压缩取样率，0~1，越小检测越流畅
            faceDetectView.getDetectConfig().MaxDetectTime = 2000;//进入智能休眠检测，以2秒一次的这个速度检测
            faceDetectView.getDetectConfig().EnableIdleSleepOption = true;//启用智能休眠检测机制
            faceDetectView.getDetectConfig().IdleSleepOptionJudgeTime = 1000 * 10;//1分钟内没有检测到人脸，进入智能休眠检测
            faceDetectView.getDetectConfig().DETECT_FACE_NUM = 1;
        }
        faceDetectView.startCameraPreview();
    }

    public void endDetect(View view) {
        faceDetectView.stopCameraPreview();
        faceDetectView.getFaceRectView().clearBorder();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endDetect(null);
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