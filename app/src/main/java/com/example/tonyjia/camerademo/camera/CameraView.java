package com.example.tonyjia.camerademo.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.widget.FrameLayout;

import com.example.tonyjia.camerademo.R;
import com.example.tonyjia.camerademo.util.Utils;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.Arrays;
import java.util.List;

/**
 * Created by tonyjia on 2018/3/14.
 */
public class CameraView extends FrameLayout implements
        TextureView.SurfaceTextureListener {

    static {
        System.loadLibrary("iconv");
    }

    private static final int START_PREVIEW = 1000;
    private static final int START_FOCUS = 1001;

    public CameraView(@NonNull Context context) {
        super(context);
        initCameraView(context);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initCameraView(context);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCameraView(context);
    }

    private long mTimes;

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            try {
                //设置预览数据
                if (camera != null) {
                    mTimes = System.currentTimeMillis();

                    mCamera.setPreviewCallback(null);
                    Camera.Parameters parameters = camera.getParameters();
                    Camera.Size size = parameters.getPreviewSize();
                    // 图片是被旋转了90度的
                    Image source = new Image(size.width, size.height, "Y800");
                    //Rect scanImageRect = mScanBoxView.getScanImageRect(size.height, size.width);
                    //startX, startY, width, height 因为旋转过90度，所以设置位置不同
                    //source.setCrop(scanImageRect.top, 0, scanImageRect.width(), size.height);
                    source.setData(data);
                    AsyncDecode asyncDecode = new AsyncDecode();
                    asyncDecode.execute(source);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_PREVIEW:
                    mCamera.startPreview();
                    break;
                case START_FOCUS:
                    if (null != mCamera && !mIsReleased) {
                        mCamera.setPreviewCallback(mPreviewCallback);
                    }
                    break;

            }
        }
    };
    private TextureView mTextureView;
    private Camera mCamera;
    private ImageScanner mScanner;
    private int mBarCodeArrays[] = {
            Symbol.CODABAR,     // 条形码            38
            Symbol.CODE39,      // 39编码格式条形码   39
            Symbol.QRCODE,      // 64编码二维码      64
            Symbol.CODE93,      // 93编码格式条形码   93
            Symbol.CODE128      // 128编码格式条形码  128
    };

    private boolean mIsStop = false;
    private boolean mIsDecode = false;
    private boolean mIsReleased = false;

    private void initCameraView(Context context) {
        inflate(context, R.layout.view_camera, this);
        mTextureView = findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(this);

        mScanner = new ImageScanner();
        mScanner.setConfig(0, Config.X_DENSITY, 3);
        mScanner.setConfig(0, Config.Y_DENSITY, 3);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            mCamera.setPreviewTexture(surface);
            initFaceCameraParameters();

            mHandler.sendEmptyMessage(START_PREVIEW);
        } catch (Exception e) {
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        mIsReleased = true;
        if (null != mHandler) {
            mHandler.removeMessages(START_FOCUS);
            mHandler.removeMessages(START_PREVIEW);
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (!mIsDecode) {
            mIsDecode = true;
            mHandler.sendEmptyMessageDelayed(START_FOCUS, 1200);
        }

    }


    /**
     * 配置前置摄像头参数
     */
    private void initFaceCameraParameters() {
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        Camera.Size previewSize = getLargePreviewSize(mCamera);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        Camera.Size pictureSize = getLargePictureSize(mCamera);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);

        parameters.setRotation(90);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
    }

    public Camera.Size getLargePictureSize(Camera camera) {
        if (camera != null) {
            List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();
            Camera.Size temp = sizes.get(0);
            for (int i = 1; i < sizes.size(); i++) {
                float scale = (float) (sizes.get(i).height) / sizes.get(i).width;
                if (temp.width < sizes.get(i).width && scale < 0.6f && scale > 0.5f) {
                    temp = sizes.get(i);
                }
            }
            return temp;
        }
        return null;
    }


    public Camera.Size getLargePreviewSize(Camera camera) {
        if (camera != null) {
            int screenWidth = Utils.getScreenWidth(getContext());
            List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
            Camera.Size temp = sizes.get(0);
            for (int i = 1; i < sizes.size(); i++) {
                if (sizes.get(i).height <= screenWidth && temp.width < sizes.get(i).width) {
                    temp = sizes.get(i);
                }

            }
            return temp;
        }
        return null;
    }


    public void restartPreview() {
        mTextureView.setVisibility(VISIBLE);
        startPreView();
    }

    public void pausePreView() {
        mTextureView.setVisibility(INVISIBLE);
        stopPreView();
    }

    public void startPreView() {
        if (mIsReleased) {
            return;
        }
        if (null != mCamera) {
            mHandler.sendEmptyMessage(START_PREVIEW);
            mIsDecode = false;
            mIsStop = false;
        }
    }


    public void stopPreView() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
    }


    private class AsyncDecode extends AsyncTask<Image, Void, String> {
        /**
         * 解析zbar过程
         *
         * @param params
         * @return
         */
        @Override
        protected String doInBackground(Image... params) {
            StringBuilder sb = new StringBuilder();
            Image barcode = params[0];
            int result = mScanner.scanImage(barcode);
            if (result != 0) {
                SymbolSet syms = mScanner.getResults();
                for (Symbol sym : syms) {
                    sb.delete(0, sb.length());
                    //使用Arrays.binarySearch 数组排序必须正确（自然顺序对数组进行升序排序）
                    int typeResult = Arrays.binarySearch(mBarCodeArrays, sym.getType());
                    if (typeResult >= 0) {
                        sb.append(sym.getData().trim());
                    } else {
                        sb.append("");
                    }
                }
            }
            return sb.toString();
        }

        /**
         * zbar 解析完成回调
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mIsDecode = false;
            if (null == result || result.equals("")) {
                return;
            }
            if (mIsStop) {
                return;
            }
            if (null != mListener) {
                mIsStop = true;
                stopPreView();
                mListener.callbackResult(result);
                long nowTime = System.currentTimeMillis() - mTimes;
                Log.e("222---->", "" + nowTime);
            }
        }

    }

    private CallbackResult mListener;

    public void setResultListener(CallbackResult listener) {
        this.mListener = listener;
    }

    public interface CallbackResult {

        void callbackResult(String result);
    }

}
