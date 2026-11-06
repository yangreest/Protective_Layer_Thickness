package com.example.gtj_sd_dg240.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.view.StyleAlertDialog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtherUtils {

    public static void callPhone(Context context, String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        context.startActivity(intent);
    }

    public static void notifySystemToScan(Context context, String filePath) {
        MediaScannerConnection.scanFile(context, new String[]{new File(filePath).getAbsolutePath()}, null, null);
    }

    public static <T> List<T> deepCopy(List<T> src) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            return (List<T>) in.readObject();
        } catch (Exception e) {
            return new ArrayList<T>();
        }
    }

    public static void showAppUpdateDialog(Context context, String version, String content, final String fileUrl) {
        String currentVersion = AppUtils.getAppVersionName();
        if (currentVersion.compareTo(version) >= 0) {
            ToastUtils.showShort("已是最新版本");
            return;
        }
        final StyleAlertDialog styleAlertDialog = new StyleAlertDialog(context, "blue");
        styleAlertDialog.setContent(content)
                .setLeftButton("取消", v -> styleAlertDialog.dismiss())
                .setRightButton("下载", v -> {
                    styleAlertDialog.dismiss();
                    // 下载操作
                    DownloadAppTask downloadTask = new DownloadAppTask(context);
                    downloadTask.execute(fileUrl);
                }).show();
    }

    /**
     * 弹出软键盘
     */
    public static void showSoftKeyboard(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * 隐藏软键盘
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = activity.getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    /**
     * 禁止EditText输入特殊字符(windows限制)
     */
    public static void setEditTextInhibitInputSpeChat(EditText editText) {
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            String speChat = "[:*?\"<>|/\\\\]";
            Pattern pattern = Pattern.compile(speChat);
            Matcher matcher = pattern.matcher(source.toString());
            if (matcher.find()) return "";
            else return null;
        };
        editText.setFilters(new InputFilter[]{filter});
    }

    public static String getDPIInfo(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int densityDPI = dm.densityDpi; // 屏幕密度（每寸像素：120/160/240/320）
        int screenWidth = dm.widthPixels; // 屏幕宽（像素）
        int screenHeight = dm.heightPixels; // 屏幕高（像素）
        return "屏幕DPI：" + densityDPI + ", " + screenWidth + " : " + screenHeight;
    }

    public static boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
