package com.khainv9.kubesolver.cubeview;

import android.content.Context;
import android.util.AttributeSet;
import org.opencv.android.JavaCameraView;
public class SquareCameraView extends JavaCameraView {

    public SquareCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
//        setMeasuredDimension(size, size);
//    }
//
//    @Override
//    protected void layoutChildren() {
//        super.layoutChildren();
//
//        int width = getWidth();
//        int height = getHeight();
//
//        if (mCameraFrameWidth > 0 && mCameraFrameHeight > 0) {
//            float previewRatio = (float) mCameraFrameWidth / mCameraFrameHeight;
//            float viewRatio = (float) width / height;
//
//            if (previewRatio > viewRatio) {
//                // Crop chiều ngang
//                int newWidth = (int) (height * previewRatio);
//                int offsetX = (newWidth - width) / 2;
//                layout(-offsetX, 0, newWidth - offsetX, height);
//            } else {
//                // Crop chiều dọc
//                int newHeight = (int) (width / previewRatio);
//                int offsetY = (newHeight - height) / 2;
//                layout(0, -offsetY, width, newHeight - offsetY);
//            }
//        }
//    }
}