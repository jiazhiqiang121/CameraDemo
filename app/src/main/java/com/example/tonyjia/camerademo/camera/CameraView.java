package com.example.tonyjia.camerademo.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.FrameLayout;

import com.example.tonyjia.camerademo.R;

import java.util.List;

/**
 * Created by tonyjia on 2018/3/14.
 */
public class CameraView extends FrameLayout implements
        TextureView.SurfaceTextureListener {

    private Camera.FaceDetectionListener mFaceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                String msg = ("face detected: " + faces.length +
                        " Face 1 Location X: " + faces[0].rect.centerX() +
                        "Y: " + faces[0].rect.centerY());
                //ToastUtils.showSingleToast(msg);

                /**
                 List<Rect> faceRects;
                 faceRects = new ArrayList<Rect>();
                 for (Camera.Face face : faces) {
                 int left = face.rect.left;
                 int right = face.rect.right;
                 int top = face.rect.top;
                 int bottom = face.rect.bottom;
                 Rect uRect = new Rect(left, top, right, bottom);
                 faceRects.add(uRect);
                 */
            }
        }
    };

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

    private Camera mCamera;

    private void initCameraView(Context context) {
        inflate(context, R.layout.view_camera, this);
        TextureView textureView = findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            mCamera.setPreviewTexture(surface);
            initFaceCameraParameters();
            mCamera.startPreview();
            startFaceDetection();

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
            stopFaceDetection();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

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
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            parameters.setRotation(270);
        }
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
    }

    public Camera.Size getLargePictureSize(Camera camera) {
        if (camera != null) {
            List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();
            Camera.Size temp = sizes.get(0);
            for (int i = 1; i < sizes.size(); i++) {
                float scale = (float) (sizes.get(i).height) / sizes.get(i).width;
                if (temp.width < sizes.get(i).width && scale < 0.6f && scale > 0.5f)
                    temp = sizes.get(i);
            }
            return temp;
        }
        return null;
    }


    public Camera.Size getLargePreviewSize(Camera camera) {
        if (camera != null) {
            List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
            Camera.Size temp = sizes.get(0);
            for (int i = 1; i < sizes.size(); i++) {
                if (temp.width < sizes.get(i).width)
                    temp = sizes.get(i);
            }
            return temp;
        }
        return null;
    }


    private void statPreView() {
        if (null != mCamera) {
            mCamera.startPreview();
        }
    }

    private void stopPreView() {
        if (null != mCamera) {
            mCamera.stopPreview();
        }
    }

    private void startFaceDetection() {
        Camera.Parameters params = mCamera.getParameters();
        if (params.getMaxNumDetectedFaces() > 0) {
            mCamera.startFaceDetection();
        }
        mCamera.setFaceDetectionListener(mFaceDetectionListener);
    }

    private void stopFaceDetection() {
        Camera.Parameters params = mCamera.getParameters();
        if (params.getMaxNumDetectedFaces() > 0) {
            mCamera.stopFaceDetection();
        }
    }
}
