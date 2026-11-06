package com.example.gtj_sd_dg240.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.blankj.utilcode.util.SizeUtils;
import com.example.gtj_sd_dg240.R;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class DiagonalLineView extends View {
    private float mWidth, mHeight;
    private Paint mPaint;

    public DiagonalLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(ContextCompat.getColor(context, R.color.colorFFF));
        mPaint.setStrokeWidth(1);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getWidth();
        mHeight = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, 0, mWidth, mHeight, mPaint);
    }
}
