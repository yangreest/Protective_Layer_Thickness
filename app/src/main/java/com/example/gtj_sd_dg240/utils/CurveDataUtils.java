package com.example.gtj_sd_dg240.utils;

import android.content.Context;

import com.example.gtj_sd_dg240.functions.database.greenDao.db.StandardCurveBeanDao;
import com.example.gtj_sd_dg240.model.StandardCurveBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CurveDataUtils {

    /**
     * 生成导出数据
     *
     * @param diameter ：直径
     */
    public static String queryCurveListString(StandardCurveBeanDao curveDao, String diameter) {
        List<StandardCurveBean> curveBeanList = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.Diameter.eq(diameter)).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        StringBuilder sbd = new StringBuilder();
        if (!curveBeanList.isEmpty()) {
            sbd.append("钢筋直径：").append(diameter).append(" 曲线表\n");
            for (StandardCurveBean curveBean : curveBeanList) {
                if (curveBean.signalArray != null && curveBean.signalArray.length >= 61) {
                    StringBuilder sbdSignal = new StringBuilder();
                    for (int signal : curveBean.signalArray) {
                        sbdSignal.append(signal).append(",");
                    }
                    sbd.append(curveBean.thickness).append(",").append(sbdSignal.substring(0, sbdSignal.length() - 1)).append("\n");
                }
            }
        }
        return sbd.toString();
    }

    public static int[][] getCurveList(Context context, String fileName) {
        InputStreamReader is = null;
        try {
            is = new InputStreamReader(context.getAssets().open(fileName));
        } catch (IOException ignored) {
        }
        if (is != null) {
            return getCarbonizeArray(is, 30, 121);
        }
        return null;
    }

    private static int[][] getCarbonizeArray(InputStreamReader is, int first, int second) {
        BufferedReader br = new BufferedReader(is);// 读取文件
        try {
            String line;
            String[] sp;
            int count = 0;
            int[][] carbonizeArray = new int[first][second];
            while ((line = br.readLine()) != null) {
                sp = line.split(",");
                if (sp.length >= 121) {
                    for (int i = 0; i < second; i++) {
                        carbonizeArray[count][i] = Integer.parseInt(sp[i + 1].trim());
                    }
                    count++;
                }
            }
            //校验数据（是否有空值）
            boolean isEmpty = false;
            for (int i = 0; i < carbonizeArray.length; i++) {
                for (int j = 0; j < carbonizeArray[i].length; j++) {
                    if (carbonizeArray[i][j] == 0) {
                        isEmpty = true;
                        break;
                    }
                }
            }
            //空值为错误格式数据，返回null
            if (isEmpty) {
                return null;
            }
            //
            return carbonizeArray;
        } catch (Exception ignored) {
        }
        return null;
    }
}
