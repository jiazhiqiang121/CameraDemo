package com.example.tonyjia.camerademo.camera;

import android.graphics.Rect;

public interface IViewFinder {

    /**
     * 获得扫码区域(识别区域)
     * <p>
     * Provides {@link Rect} that identifies area where barcode scanner can detect visual codes
     * <p>Note: This rect is a area representation in absolute pixel values. <br/>
     * For example: <br/>
     * If View's size is 1024x800 so framing rect might be 500x400</p>
     *
     * @return {@link Rect} that identifies barcode scanner area
     */
    Rect getFramingRect();

    /**
     * 获得整个View的宽
     * <p>
     * Width of a {@link android.view.View} that implements this interface
     * <p>Note: this is already implemented in {@link android.view.View},
     * so you don't need to override method and provide your implementation</p>
     *
     * @return width of a view
     */
    int getWidth();

    /**
     * 获得整个View的高
     * <p>
     * Height of a {@link android.view.View} that implements this interface
     * <p>Note: this is already implemented in {@link android.view.View},
     * so you don't need to override method and provide your implementation</p>
     *
     * @return height of a view
     */
    int getHeight();
}