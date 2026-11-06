package com.example.gtj_sd_dg240.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.blankj.utilcode.util.ToastUtils;

import androidx.annotation.NonNull;

public class CustomDensityUtils {
    private static float sNoncompatDensity = 0;
    private static float sNoncompatScaldeDensity = 0;

    public static void setCustomDensity(@NonNull Activity activity, @NonNull final Application application) {
        final DisplayMetrics appDisplayMetrics = application.getResources().getDisplayMetrics();
        if (sNoncompatDensity == 0) {
            sNoncompatDensity = appDisplayMetrics.density;
            sNoncompatScaldeDensity = appDisplayMetrics.scaledDensity;
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    if (newConfig != null && newConfig.fontScale > 0) {
                        sNoncompatScaldeDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {
                }
            });
        }
        //360表示这个项目设计图的宽度为360dp
        final float targetDensity = appDisplayMetrics.widthPixels / 853.33f;//每dp等于targetDensity px
        final float targetScaledDensity = (sNoncompatScaldeDensity / sNoncompatDensity) * targetDensity;
        final int targetDensityDpi = (int) (160 * targetDensity);//重新计算设备的dpi
        appDisplayMetrics.density = targetDensity;
        appDisplayMetrics.scaledDensity = targetScaledDensity;
        appDisplayMetrics.densityDpi = targetDensityDpi;
        final DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetScaledDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;
    }

    public static void showDensityDpi(@NonNull Activity activity) {
        WindowManager windowManager = activity.getWindow().getWindowManager();
        Point point = new Point();
        windowManager.getDefaultDisplay().getRealSize(point);
        //屏幕实际宽度（像素个数）
        int width = point.x;
        //屏幕实际高度（像素个数）
        int height = point.y;
        int densityDpi = activity.getResources().getDisplayMetrics().densityDpi;
        float density = activity.getResources().getDisplayMetrics().density;
        ToastUtils.showLong(width + " , " + height + "\n" + density + " , " + densityDpi);
    }
}
