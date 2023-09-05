package com.example.davidka;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearSmoothScroller;

public class CenterSmoothScroller extends LinearSmoothScroller {
    float MILLISECONDS_PER_CM = 100f;
    public CenterSmoothScroller(Context context) {
        super(context);
    }

    @Override
    public int calculateDtToFit(int viewStart,int viewEnd,int boxStart,int boxEnd,int snapPreference){
        return (boxStart+ (boxEnd-boxStart)/2) - (viewStart+ (viewEnd-viewStart)/2);
    }

    @Override
    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        super.calculateSpeedPerPixel(displayMetrics);
        return MILLISECONDS_PER_CM / displayMetrics.densityDpi;
    }
}
