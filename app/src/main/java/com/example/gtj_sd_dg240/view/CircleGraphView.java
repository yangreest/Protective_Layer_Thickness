package com.example.gtj_sd_dg240.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.blankj.utilcode.util.SizeUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.model.ArcBean;
import com.example.gtj_sd_dg240.model.CPointBean;
import com.example.gtj_sd_dg240.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;

public class CircleGraphView extends View {
    private Context mContext;
    private int colorD8D8D8, color8E8E8E, color2D2D2D, colorFFF, color006F5F;
    private Paint mPaint1, mPaint2;
    private CPointBean centerPoint;  //圆心
    private List<ArcBean> angleList = new ArrayList<>();
    private float circleRadius, smallRadius;  //半径
    private float unitAngle;  //单位角度

    public CircleGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    private void initView() {
        colorD8D8D8 = ContextCompat.getColor(mContext, R.color.colorD8D8D8);
        colorFFF = ContextCompat.getColor(mContext, R.color.colorFFF);
        color8E8E8E = ContextCompat.getColor(mContext, R.color.color8E8E8E);
        color2D2D2D = ContextCompat.getColor(mContext, R.color.color2D2D2D);
        color006F5F = ContextCompat.getColor(mContext, R.color.color006F5F);
        //
        smallRadius = SizeUtils.dp2px(7);
        //圆形
        mPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint1.setAntiAlias(true);
        mPaint1.setStyle(Paint.Style.STROKE);
        //圆点
        mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint2.setAntiAlias(true);
        mPaint2.setStyle(Paint.Style.FILL);
        mPaint2.setStrokeWidth(2);
        mPaint2.setTextSize(SizeUtils.dp2px(16));
    }

    public void setData(int size) {
        angleList.clear();
        unitAngle = 360.0f / size;
        for (int i = 1; i <= size; i++) {
            angleList.add(new ArcBean(i, unitAngle * i));
        }
        invalidate();
    }

    public void selectedIndex(int index) {
        if (index == -1) {
            for (int i = 0; i < angleList.size(); i++) {
                angleList.get(i).isSelected = false;
            }
        } else {
            angleList.get(index).isSelected = true;
            invalidate();
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        float mWidth = getWidth();
        float mHeight = getHeight();
        centerPoint = new CPointBean(mWidth / 2, mHeight / 2);
        circleRadius = mWidth / 2 - 2;
    }

    public void onDraw(Canvas canvas) {
        if (angleList.size() > 0) {
            drawCircle(canvas);
        }
    }

    //300 270 200 250
    private void drawCircle(Canvas canvas) {
        //灰色圆环
        mPaint2.setColor(colorD8D8D8);
        canvas.drawCircle(centerPoint.x, centerPoint.y, circleRadius, mPaint2);
        mPaint2.setColor(colorFFF);
        canvas.drawCircle(centerPoint.x, centerPoint.y, circleRadius * 200 / 300, mPaint2);
        //外壁圈
        mPaint1.setColor(color8E8E8E);
        mPaint1.setStrokeWidth(2);
        canvas.drawCircle(centerPoint.x, centerPoint.y, circleRadius, mPaint1);
        //箍筋圈
        mPaint1.setColor(color2D2D2D);
        mPaint1.setStrokeWidth(2 * 2);
        canvas.drawCircle(centerPoint.x, centerPoint.y, circleRadius * 266 / 300, mPaint1);
        //内壁圈
        mPaint1.setColor(color8E8E8E);
        mPaint1.setStrokeWidth(2);
        canvas.drawCircle(centerPoint.x, centerPoint.y, circleRadius * 200 / 300, mPaint1);
        //主筋点
        int initialAngle = 270;
        for (int i = 0; i < angleList.size(); i++) {
            CPointBean point = ViewUtils.getCircleArcPoint(centerPoint, circleRadius * 244 / 300, -unitAngle + initialAngle + angleList.get(i).arcAngle);
            mPaint2.setColor(angleList.get(i).isSelected ? color006F5F : color2D2D2D);
            canvas.drawCircle(point.x, point.y, smallRadius, mPaint2);
        }
        //主筋编号
        for (int i = 0; i < angleList.size(); i++) {
            CPointBean point = ViewUtils.getCircleArcPoint(centerPoint, circleRadius * 168 / 300, -unitAngle + initialAngle + angleList.get(i).arcAngle);
            mPaint2.setColor(angleList.get(i).isSelected ? color006F5F : color2D2D2D);
            int number = angleList.get(i).number;
            float x = number >= 10 ? point.x - 10 : point.x - 7;
            canvas.drawText(angleList.get(i).number + "", x, point.y + 8, mPaint2);
        }
    }
}
