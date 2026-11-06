package com.example.gtj_sd_dg240.utils;

import com.example.gtj_sd_dg240.stacon.StaCon_14_4;
import com.example.gtj_sd_dg240.model.Number;

import java.util.List;

/**
 * 钢筋直径14
 */
public class CalculatedThicknessUtils_14_4 {
    /**
     * 主筋间距30
     *
     * @param strInterval : 箍筋间距
     */
    public static double[] getThickness_14_30(String strInterval, List<Number> numberList) {
        //查表
        return switch (strInterval) {
            case "40" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_30_40_min, StaCon_14_4.signalList_14_30_40_max, numberList);
            case "50" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_30_50_min, StaCon_14_4.signalList_14_30_50_max, numberList);
            case "60" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_30_60_min, StaCon_14_4.signalList_14_30_60_max, numberList);
            case "120" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_30_120_min, StaCon_14_4.signalList_14_30_120_max, numberList);
            default ->  // 70
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_30_70_min, StaCon_14_4.signalList_14_30_70_max, numberList);
        };
    }

    /**
     * 主筋间距40
     *
     * @param strInterval : 箍筋间距
     */
    public static double[] getThickness_14_40(String strInterval, List<Number> numberList) {
        //查表
        return switch (strInterval) {
            case "40" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_40_40_min, StaCon_14_4.signalList_14_40_40_max, numberList);
            case "50" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_40_50_min, StaCon_14_4.signalList_14_40_50_max, numberList);
            case "60" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_40_60_min, StaCon_14_4.signalList_14_40_60_max, numberList);
            case "120" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_40_120_min, StaCon_14_4.signalList_14_40_120_max, numberList);
            default ->  // 70
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_40_70_min, StaCon_14_4.signalList_14_40_70_max, numberList);
        };
    }

    /**
     * 主筋间距50
     *
     * @param strInterval : 箍筋间距
     */
    public static double[] getThickness_14_50(String strInterval, List<Number> numberList) {
        //查表
        return switch (strInterval) {
            case "40" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_50_40_min, StaCon_14_4.signalList_14_50_40_max, numberList);
            case "50" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_50_50_min, StaCon_14_4.signalList_14_50_50_max, numberList);
            case "60" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_50_60_min, StaCon_14_4.signalList_14_50_60_max, numberList);
            case "120" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_50_120_min, StaCon_14_4.signalList_14_50_120_max, numberList);
            default ->  // 70
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_50_70_min, StaCon_14_4.signalList_14_50_70_max, numberList);
        };
    }

    /**
     * 主筋间距60
     *
     * @param strInterval : 箍筋间距
     */
    public static double[] getThickness_14_60(String strInterval, List<Number> numberList) {
        //查表
        return switch (strInterval) {
            case "40" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_60_40_min, StaCon_14_4.signalList_14_60_40_max, numberList);
            case "50" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_60_50_min, StaCon_14_4.signalList_14_60_50_max, numberList);
            case "60" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_60_60_min, StaCon_14_4.signalList_14_60_60_max, numberList);
            case "120" ->
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_60_120_min, StaCon_14_4.signalList_14_60_120_max, numberList);
            default ->  // 70
                    CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_60_70_min, StaCon_14_4.signalList_14_60_70_max, numberList);
        };
    }
}
