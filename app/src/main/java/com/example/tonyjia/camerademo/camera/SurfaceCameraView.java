package com.example.tonyjia.camerademo.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.example.tonyjia.camerademo.R;
import com.example.tonyjia.camerademo.util.DisplayUtils;
import com.example.tonyjia.camerademo.util.Utils;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by tonyjia on 2018/3/14.
 */
public class SurfaceCameraView extends FrameLayout implements
        SurfaceHolder.Callback {

    static {
        System.loadLibrary("iconv");
    }


    public SurfaceCameraView(@NonNull Context context) {
        super(context);
        initCameraView(context);
    }

    public SurfaceCameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initCameraView(context);
    }

    public SurfaceCameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCameraView(context);
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            try {
                //设置预览数据
                if (camera != null) {
                    long startTime = System.currentTimeMillis();

                    Camera.Parameters parameters = camera.getParameters();
                    Camera.Size size = parameters.getPreviewSize();
                    //根据ViewFinderView和preview的尺寸之比，缩放扫码区域
                    Rect rect = getScaledRect(size.width, size.height);
                    rect = getRotatedRect(size.width, size.height, rect);

                    //从preView的图像中截取扫码区域
                    Image barcode = new Image(size.width, size.height, "Y800");
                    barcode.setData(data);
                    barcode.setCrop(rect.left, rect.top, rect.width(), rect.height());
                    String symData = "";
                    int result = mScanner.scanImage(barcode);
                    if (result != 0) {
                        SymbolSet syms = mScanner.getResults();
                        for (Symbol sym : syms) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                symData = new String(sym.getDataBytes(), StandardCharsets.UTF_8);
                            } else {
                                symData = sym.getData();
                            }
                            if (!TextUtils.isEmpty(symData)) {
                                break;
                            }
                        }
                        if (null != mListener) {
                            stopPreView();
                            mListener.callbackResult(symData);
                            long nowTime = System.currentTimeMillis() - startTime;
                            Log.e("111---->", "" + nowTime);
                        }
                    } else {
                        mCamera.setOneShotPreviewCallback(this);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    private ViewFinderView mViewFinderView;
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private ImageScanner mScanner;
    private Integer mBarCodeArrays[] = {
            Symbol.CODABAR,     // 条形码            38
            Symbol.CODE39,      // 39编码格式条形码   39
            Symbol.QRCODE,      // 64编码二维码      64
            Symbol.CODE93,      // 93编码格式条形码   93
            Symbol.CODE128      // 128编码格式条形码  128
    };
    private Rect scaledRect, rotatedRect;
    private boolean mIsReleased = false;

    private void initCameraView(Context context) {
        inflate(context, R.layout.view_surface_camera, this);
        mSurfaceView = findViewById(R.id.surface_view);
        mViewFinderView = findViewById(R.id.view_finder_view);
        mSurfaceView.getHolder().addCallback(this);

        mScanner = new ImageScanner();
        mScanner.setConfig(0, Config.X_DENSITY, 3);
        mScanner.setConfig(0, Config.Y_DENSITY, 3);
        mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
        for (Integer barCode : mBarCodeArrays) {//设置支持的码格式
            mScanner.setConfig(barCode, Config.ENABLE, 1);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            mCamera.setPreviewDisplay(holder);
            initCameraParameters();
            mCamera.startPreview();
            mCamera.setOneShotPreviewCallback(mPreviewCallback);
        } catch (Exception e) {
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (null != mCamera) {
            mCamera.setOneShotPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 配置前置摄像头参数
     */
    private void initCameraParameters() {
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
        mSurfaceView.setVisibility(VISIBLE);
        startPreView();
    }

    public void pausePreView() {
        mSurfaceView.setVisibility(INVISIBLE);
        stopPreView();
    }

    public void startPreView() {
        if (mIsReleased) {
            return;
        }
        if (null != mCamera) {
            mCamera.startPreview();
            mCamera.setOneShotPreviewCallback(mPreviewCallback);
        }
    }


    public void stopPreView() {
        if (null != mCamera) {
            mCamera.setOneShotPreviewCallback(null);
            mCamera.stopPreview();
        }
    }

    public void release() {
        mIsReleased = true;
        mSurfaceView.getHolder().removeCallback(this);
    }

    private CallbackResult mListener;

    public void setResultListener(CallbackResult listener) {
        this.mListener = listener;
    }

    public interface CallbackResult {

        void callbackResult(String result);
    }

    /**
     * 根据ViewFinderView和preview的尺寸之比，缩放扫码区域
     */
    public Rect getScaledRect(int previewWidth, int previewHeight) {
        if (scaledRect == null) {
            Rect framingRect = mViewFinderView.getFramingRect();//获得扫码框区域
            int viewFinderViewWidth = mViewFinderView.getWidth();
            int viewFinderViewHeight = mViewFinderView.getHeight();

            int width, height;
            if (DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT//竖屏使用
                    && previewHeight < previewWidth) {
                width = previewHeight;
                height = previewWidth;
            } else if (DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_LANDSCAPE//横屏使用
                    && previewHeight > previewWidth) {
                width = previewHeight;
                height = previewWidth;
            } else {
                width = previewWidth;
                height = previewHeight;
            }

            scaledRect = new Rect(framingRect);
            scaledRect.left = scaledRect.left * width / viewFinderViewWidth;
            scaledRect.right = scaledRect.right * width / viewFinderViewWidth;
            scaledRect.top = scaledRect.top * height / viewFinderViewHeight;
            scaledRect.bottom = scaledRect.bottom * height / viewFinderViewHeight;
        }

        return scaledRect;
    }

    public Rect getRotatedRect(int previewWidth, int previewHeight, Rect rect) {
        if (rotatedRect == null) {
            int rotationCount = getRotationCount();
            rotatedRect = new Rect(rect);

            if (rotationCount == 1) {//若相机图像需要顺时针旋转90度，则将扫码框逆时针旋转90度
                rotatedRect.left = rect.top;
                rotatedRect.top = previewHeight - rect.right;
                rotatedRect.right = rect.bottom;
                rotatedRect.bottom = previewHeight - rect.left;
            } else if (rotationCount == 2) {//若相机图像需要顺时针旋转180度,则将扫码框逆时针旋转180度
                rotatedRect.left = previewWidth - rect.right;
                rotatedRect.top = previewHeight - rect.bottom;
                rotatedRect.right = previewWidth - rect.left;
                rotatedRect.bottom = previewHeight - rect.top;
            } else if (rotationCount == 3) {//若相机图像需要顺时针旋转270度，则将扫码框逆时针旋转270度
                rotatedRect.left = previewWidth - rect.bottom;
                rotatedRect.top = rect.left;
                rotatedRect.right = previewWidth - rect.top;
                rotatedRect.bottom = rect.right;
            }
        }

        return rotatedRect;
    }

    /**
     * 旋转data
     */
    public byte[] rotateData(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        int rotationCount = getRotationCount();
        for (int i = 0; i < rotationCount; i++) {
            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++)
                    rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
            data = rotatedData;
            int tmp = width;
            width = height;
            height = tmp;
        }

        return data;
    }

    /**
     * 获取（旋转角度/90）
     */
    public int getRotationCount() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        int displayOrientation = result;
        return displayOrientation / 90;
    }
}
