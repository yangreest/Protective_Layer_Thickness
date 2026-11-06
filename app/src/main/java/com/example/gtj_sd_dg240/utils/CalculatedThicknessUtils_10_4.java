package com.example.gtj_sd_dg240.utils;

import com.example.gtj_sd_dg240.stacon.StaCon_10_4;
import com.example.gtj_sd_dg240.model.Number;

import java.util.List;

/**
 * 钢筋直径10
 */
public class CalculatedThicknessUtils_10_4 {
    /**
     * 主筋间距30
     *
     * @param strInterval : 箍筋间距
     */
    public static double[] getThickness_10_30(String strInterval, List<Number> numberList) {
        //查表
        return switch (strInterval) {
            case "40" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_30_40_min, StaCon_10_4.signalList_10_30_40_max, numberList);
            case "50" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_30_50_min, StaCon_10_4.signalList_10_30_50_max, numberList);
            case "60" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_30_60_min, StaCon_10_4.signalList_10_30_60_max, numberList);
            case "120" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_30_120_min, StaCon_10_4.signalList_10_30_120_max, numberList);
            default ->  // 70
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_30_70_min, StaCon_10_4.signalList_10_30_70_max, numberList);
        };
    }

    /**
     * 主筋间距40
     *
     * @param strInterval : 箍筋间距
     */
    public static double[] getThickness_10_40(String strInterval, List<Number> numberList) {
        //查表
        return switch (strInterval) {
            case "40" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_40_40_min, StaCon_10_4.signalList_10_40_40_max, numberList);
            case "50" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_40_50_min, StaCon_10_4.signalList_10_40_50_max, numberList);
            case "60" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_40_60_min, StaCon_10_4.signalList_10_40_60_max, numberList);
            case "120" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_40_120_min, StaCon_10_4.signalList_10_40_120_max, numberList);
            default ->  // 70
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_40_70_min, StaCon_10_4.signalList_10_40_70_max, numberList);
        };
    }

    /**
     * 主筋间距50
     *
     * @param strInterval : 箍筋间距
     */
    public static double[] getThickness_10_50(String strInterval, List<Number> numberList) {
        //查表
        return switch (strInterval) {
            case "40" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_50_40_min, StaCon_10_4.signalList_10_50_40_max, numberList);
            case "50" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_50_50_min, StaCon_10_4.signalList_10_50_50_max, numberList);
            case "60" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_50_60_min, StaCon_10_4.signalList_10_50_60_max, numberList);
            case "120" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_50_120_min, StaCon_10_4.signalList_10_50_120_max, numberList);
            default ->  // 70
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_50_70_min, StaCon_10_4.signalList_10_50_70_max, numberList);
        };
    }

    /**
     * 主筋间距60
     *
     * @param strInterval : 箍筋间距
     */
    public static double[] getThickness_10_60(String strInterval, List<Number> numberList) {
        //查表
        return switch (strInterval) {
            case "40" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_60_40_min, StaCon_10_4.signalList_10_60_40_max, numberList);
            case "50" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_60_50_min, StaCon_10_4.signalList_10_60_50_max, numberList);
            case "60" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_60_60_min, StaCon_10_4.signalList_10_60_60_max, numberList);
            case "120" ->
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_60_120_min, StaCon_10_4.signalList_10_60_120_max, numberList);
            default ->  // 70
                    CalculatedThicknessUtils.getThickness(StaCon_10_4.signalList_10_60_70_min, StaCon_10_4.signalList_10_60_70_max, numberList);
        };
    }
}
