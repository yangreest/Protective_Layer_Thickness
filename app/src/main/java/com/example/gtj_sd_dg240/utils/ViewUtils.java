package com.example.gtj_sd_dg240.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.widget.ScrollView;

import com.example.gtj_sd_dg240.model.CPointBean;

import androidx.annotation.NonNull;

public class ViewUtils {
    /**
     * @param : tempPaint:圆点
     * @param : radius：半径
     * @param : tempAngle：角度
     * @return : 计算当前角度点位置
     */
    @NonNull
    public static CPointBean getCircleArcPoint(CPointBean tempPaint, float radius, float tempAngle) {
        if (tempAngle > 360) {
            tempAngle = tempAngle % 360;
        }
        if (tempAngle >= 270 && tempAngle < 360) {
            float arcWidth = (float) (radius * Math.sin((tempAngle % 90) * Math.PI / 180));
            float arcHeight = (float) (radius * Math.sin((90 - tempAngle % 90) * Math.PI / 180));
            return new CPointBean(tempPaint.x + arcWidth, tempPaint.y - arcHeight);
        } else if (tempAngle >= 180 && tempAngle < 270) {// 240
            float arcWidth = (float) (radius * Math.sin((90 - tempAngle % 90) * Math.PI / 180));
            float arcHeight = (float) (radius * Math.sin((tempAngle % 90) * Math.PI / 180));
            return new CPointBean(tempPaint.x - arcWidth, tempPaint.y - arcHeight);
        } else if (tempAngle >= 90 && tempAngle < 180) {
            float arcWidth = (float) (radius * Math.sin((tempAngle % 90) * Math.PI / 180));
            float arcHeight = (float) (radius * Math.sin((90 - tempAngle % 90) * Math.PI / 180));
            return new CPointBean(tempPaint.x - arcWidth, tempPaint.y + arcHeight);
        } else {
            float arcWidth = (float) (radius * Math.sin((90 - tempAngle % 90) * Math.PI / 180));
            float arcHeight = (float) (radius * Math.sin((tempAngle % 90) * Math.PI / 180));
            return new CPointBean(tempPaint.x + arcWidth, tempPaint.y + arcHeight);
        }
    }

    /**
     * 获取scrollview的截屏
     */
    public static Bitmap scrollViewScreenShot(ScrollView scrollView) {
        int h = 0;
        Bitmap bitmap;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();
            scrollView.getChildAt(i).setBackgroundColor(Color.parseColor("#ffffff"));
        }
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h, Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
        return bitmap;
    }
}
