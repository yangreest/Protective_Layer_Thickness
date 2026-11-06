package com.example.gtj_sd_dg240.utils;

import com.example.gtj_sd_dg240.model.Number;
import com.example.gtj_sd_dg240.model.NumberBean;

import java.util.ArrayList;
import java.util.List;

public class CalculatedThicknessUtils {
    public static final int[] spaceBetween = {10, 15, 20, 25, 30};
    public static final double[] space_14_70_60 = {0, 0, 0, 0, 0};
    public static final double[] space_14_70_50 = {0, 0, 0, 0, 0};
    public static final double[] space_14_70_40 = {0, 0.5, 0.2, 0.3, 1};
    public static final double[] space_14_70_30 = {1, 2.5, 2.5, 2.2, 3};
    public static final double[] space_14_70_25 = {2, 2.3, 2.5, 2.8, 3.3};
    //
    public static final double[] space_14_120_60 = {0, 0, 0, 0, 0};
    public static final double[] space_14_120_50 = {0, 0.4, 0.4, 0.6, 0.8};
    public static final double[] space_14_120_40 = {0.4, 0.8, 0.8, 0.8, 1};
    public static final double[] space_14_120_30 = {1, 1.6, 1.6, 1.7, 2};

    public static double[] getThickness(List<Integer> signalList0, List<Integer> signalList2, List<Number> numberList) {
        int indexStart = getIndexAscend(signalList0, numberList.get(0).number);
        int indexEnd = getIndexDescend(signalList2, numberList.get(2).number);
        int max = Math.max(indexStart, indexEnd);
        int min = Math.min(indexStart, indexEnd);
        max = max < 39 ? max + 1 : max;
        min = min > 1 ? min - 1 : min;
        List<NumberBean> signalList = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            signalList.add(new NumberBean(signalList0.get(i), signalList2.get(i)));
        }
        List<Integer> markList = new ArrayList<>();
        for (int i = 0; i < signalList.size(); i++) {
            int count = 0;
            for (Number bean : numberList) {
                if (bean.number >= signalList.get(i).numberMin && bean.number <= signalList.get(i).numberMax) {
                    count++;
                }
            }
            markList.add(count);

        }
        double maxIndex;  //最终厚度index
        int count = 3;
        maxIndex = getMaxIndex(markList, count);
        if (maxIndex == -1) {
            count = 2;
            maxIndex = getMaxIndex(markList, count);
        }
        if (maxIndex == -1) {
            count = 1;
            maxIndex = getMaxIndex(markList, count);
        }
        double[] thickness = new double[2];
        thickness[0] = maxIndex + min + 1;
        thickness[1] = count;
        return thickness;
    }

    private static int getIndexAscend(List<Integer> signalList, int value) {
        for (int i = 0; i < signalList.size(); i++) {
            if (value >= signalList.get(i)) {
                return i;
            }
        }
        return 0;
    }

    private static int getIndexDescend(List<Integer> signalList, int value) {
        for (int i = signalList.size() - 1; i >= 0; i--) {
            if (value <= signalList.get(i)) {
                return i;
            }
        }
        return 0;
    }

    private static double getMaxIndex(List<Integer> markList, int count) {
        double maxIndex = -1;  //最终厚度index
        List<Integer> temp = new ArrayList<>();
        for (int i = 0; i < markList.size(); i++) {
            if (markList.get(i) == count) {
                temp.add(i);
            }
        }
        if (count == 3) {
            if (temp.size() == 1) {
                maxIndex = temp.get(0);
            } else if (temp.size() >= 2) {
                int index0 = temp.get(0);
                int index1 = temp.get(temp.size() - 1);
                maxIndex = StringUtils.getRounding((index0 + index1) / 2.0, 1);
            }
        } else if (count == 2) {
            if (temp.size() == 1) {
                List<Integer> t = new ArrayList<>();
                for (int i = 0; i < markList.size(); i++) {
                    if (markList.get(i) >= 1) {
                        t.add(i);
                    }
                }
                if (t.size() == 1) {
                    maxIndex = t.get(0);
                } else if (t.size() == 2) {
                    maxIndex = temp.get(0);
                } else {
                    int index0 = t.get(0);
                    int index1 = t.get(t.size() - 1);
                    maxIndex = StringUtils.getRounding((index0 + index1) / 2.0, 1);
                }
            } else if (temp.size() >= 2) {
                int index0 = temp.get(0);
                int index1 = temp.get(temp.size() - 1);
                maxIndex = StringUtils.getRounding((index0 + index1) / 2.0, 1);
            }
        } else if (count == 1) {
            if (temp.isEmpty()) {
                maxIndex = StringUtils.getRounding(markList.size() / 2.0, 1);
            } else if (temp.size() == 1) {
                maxIndex = temp.get(0);
            } else {
                int index0 = temp.get(0);
                int index1 = temp.get(temp.size() - 1);
                maxIndex = StringUtils.getRounding((index0 + index1) / 2.0, 1);
            }
        }
        return maxIndex;
    }
}
