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

import com.example.tonyjia.camerademo.camera.SurfaceCameraView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SurfaceCameraViewActivity extends AppCompatActivity {

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SurfaceCameraViewActivity.class);
        context.startActivity(intent);
    }

    @BindView(R.id.surface_camera_view)
    SurfaceCameraView mSurfaceCameraView;
    private Handler mHandler;

    private SurfaceCameraView.CallbackResult mCallbackResult = new SurfaceCameraView.CallbackResult() {
        @Override
        public void callbackResult(String result) {
            Toast.makeText(SurfaceCameraViewActivity.this, result, Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(mCameraRunnable, 1200);
        }
    };

    private Runnable mCameraRunnable = new Runnable() {
        @Override
        public void run() {
            mSurfaceCameraView.startPreView();
        }
    };

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
        setContentView(R.layout.activity_surface_camera_view);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initSurfaceCameraView();
    }

    private void initSurfaceCameraView() {
        mHandler = new Handler();
        mSurfaceCameraView.setResultListener(mCallbackResult);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceCameraView.restartPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceCameraView.pausePreView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSurfaceCameraView.release();
        mHandler.removeCallbacks(mCameraRunnable);
    }
}
