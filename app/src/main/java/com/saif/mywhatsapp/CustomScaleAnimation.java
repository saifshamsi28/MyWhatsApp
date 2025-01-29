//package com.saif.mywhatsapp;
//
//import static androidx.core.view.ViewCompat.setPivotX;
//import static androidx.core.view.ViewCompat.setPivotY;
//
//import android.view.animation.ScaleAnimation;
//
//public class CustomScaleAnimation extends ScaleAnimation {
//
//    private float mPivotX;
//    private float mPivotY;
//
//    public CustomScaleAnimation(float fromX, float toX, float fromY, float toY, float pivotX, float pivotY) {
//        super(fromX, toX, fromY, toY, pivotX, pivotY);
//        mPivotX = pivotX;
//        mPivotY = pivotY;
//    }
//
//    @Override
//    public void initialize(int width, int height, int parentWidth, int parentHeight) {
//        super.initialize(width, height, parentWidth, parentHeight);
//        setPivotX(mPivotX);
//        setPivotY(mPivotY);
//    }
//}
