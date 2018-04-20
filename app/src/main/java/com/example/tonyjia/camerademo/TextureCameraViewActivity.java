package com.example.tonyjia.camerademo;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.tonyjia.camerademo.camera.CameraView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TextureCameraViewActivity extends AppCompatActivity {


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, TextureCameraViewActivity.class);
        context.startActivity(intent);
    }

    private CameraView.CallbackResult mCallbackResult = new CameraView.CallbackResult() {
        @Override
        public void callbackResult(String result) {
            Toast.makeText(TextureCameraViewActivity.this, result, Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(mCameraRunnable, 1200);
        }
    };

    private Runnable mCameraRunnable = new Runnable() {
        @Override
        public void run() {
            mCameraView.startPreView();
        }
    };
    @BindView(R.id.camera_view)
    CameraView mCameraView;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        }
        setContentView(R.layout.activity_texture_camera_view);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initCameraView();
    }

    private void initCameraView() {
        mHandler = new Handler();
        mCameraView.setResultListener(mCallbackResult);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.restartPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.pausePreView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraView.removeCallbacks(mCameraRunnable);
    }
}
