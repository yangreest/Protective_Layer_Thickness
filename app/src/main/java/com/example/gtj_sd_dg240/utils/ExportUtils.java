package com.example.gtj_sd_dg240.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.model.RebarPageBean;
import com.example.gtj_sd_dg240.model.ZoneBean;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ExportUtils {

    /**
     * 生成导出数据
     */
    public static String getExportString(String appName, ZoneBean zoneBean) {
        if (zoneBean.rebarPageList.size() == 0) {
            return null;
        }
        StringBuilder sbd = new StringBuilder();
        sbd.append(appName).append("检测记录");
        sbd.append("\n工程信息")
                .append("\n工程名称：,").append(zoneBean.projectName)
                .append("\n电杆编号：,").append(zoneBean.poleNo)
                .append("\n适用规范：,").append(StaticConstant.poleStandardList.get(zoneBean.poleStandard));
        sbd.append("\n试验参数")
                .append("\n电杆类型：,").append(StaticConstant.configPoleTypeList.get(zoneBean.poleType))
                .append("\n电杆结构：,").append(StaticConstant.configpoleStructureList.get(zoneBean.poleStructure))
                .append("\n电杆长度：,").append(zoneBean.poleHeight).append("m")
                .append("\n电杆根径：,").append(zoneBean.poleBottomDiameter).append("mm").append(",电杆梢径：,").append(zoneBean.poleTopDiameter).append("mm")
                .append("\n纵筋直径：,").append(StaticConstant.rebarDiameterList.get(zoneBean.rebarDiameter)).append("mm").append(",螺旋筋直径：,").append(zoneBean.hoopingDiameter).append("mm")
                .append("\n设计保护层厚度：,").append(zoneBean.designThickness).append("（+8 -2）mm");
        sbd.append("\n保护层厚度（单位：mm）");
        StringBuilder strCount = new StringBuilder();
        for (int i = 1; i <= zoneBean.poleBottomCount; i++) {
            strCount.append(i).append(",");
        }
        strCount.append("平均厚度\n");
        sbd.append("\n位置|数量,").append(strCount);
        for (RebarPageBean pageBean : zoneBean.rebarPageList) {
            StringBuilder string = new StringBuilder(pageBean.segment + ",");
            StringBuilder string1 = new StringBuilder(pageBean.segment + ",");
            for (int i = 0; i < zoneBean.poleBottomCount; i++) {
                if (i < pageBean.beanList.size()) {
                    string.append(pageBean.beanList.get(i).thickness);
                    string1.append(pageBean.beanList.get(i).thickness1);
                }
                string.append(",");
                string1.append(",");
            }
            string.append(pageBean.averageThickness).append("\n");
            string1.append(pageBean.averageThickness1).append("\n");
            sbd.append(string1);
            sbd.append(string);
        }
        return sbd.toString();
    }

    /**
     * 保存图片到本地
     *
     * @param context：context
     * @param data：流
     * @return 本地图片地址
     */
    public static String saveBitmapFile(Context context, String fileName, byte[] data) {
        String path = createFilePath(fileName);
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        String authority = context.getPackageName() + ".fileProvider";
        Uri imageFileUri = FileProvider.getUriForFile(context, authority, new File(path));
        String filePath = null;
        try {
            OutputStream imageFileOS = context.getContentResolver().openOutputStream(imageFileUri);
            if (imageFileOS != null) {
                imageFileOS.write(data);
                imageFileOS.flush();
                imageFileOS.close();
                filePath = path;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    /**
     * 保存文件到本地
     */
    public static String saveDataFile(String fileName, byte[] data) {
        File file = new File(fileName);
        if (file.exists()) {
            Logger.d(file.delete());
        }
        String filePath = null;
        try {
            OutputStream fos = new FileOutputStream(fileName);
            fos.write(data);
            fos.flush();
            fos.close();
            filePath = file.getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    private static String createFilePath(String fileName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(fileName.substring(0, fileName.lastIndexOf("/")));
            if (!dir.exists()) {
                Logger.d(dir.mkdirs());
            }
            File file = new File(fileName);
            if (file.exists()) {
                Logger.d(file.delete());
            }
            return fileName;
        }
        return null;
    }
}
