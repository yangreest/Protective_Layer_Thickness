package com.example.gtj_sd_dg240.utils;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

public class ComputeCurveUtils {

    /**
     * 修正后的曲线数据
     *
     * @param coefficients：曲线系数
     * @param size：数据长度
     * @return 曲线数据
     */
    public static int[] getCurveY(double[] coefficients, int size) {
        int[] signals = new int[size];
        for (int i = 0; i < size; i++) {
            double y = getY(coefficients, i + 1);
            signals[i] = (int) y;
            if (i == 60) {
            }
        }

        return signals;
    }

    /**
     * 直线行走距离数据
     *
     * @param R：电杆半径
     * @param r：钢筋半径
     * @param d：厚度
     * @param size：数据长度
     * @return 直线行走距离数据
     */
    public static double[] getDistance(String poleNorm, double R, double r, double d, int size) {
        double[] distances = new double[size];
        for (int i = 0; i < size; i++) {
            distances[i] = calculateDistance(poleNorm, R, r, d, i + 1 - 61);
        }
        for (int i = 0; i < size; i++) {
            distances[i] += (size >> 1) + 1;
        }
        return distances;
    }

    /**
     * 直线行走距离
     *
     * @param R：电杆半径
     * @param r：钢筋半径
     * @param d：厚度
     * @param x：行走距离
     * @return 行走距离
     */
    private static double calculateDistance(String poleNorm, double R, double r, double d, int x) {
        double angle = calculateAngleInDegrees(R, x);
        double C = calculateThirdSide(R - d - r, R, angle);
        double distance = calculateDistance(C, d, r);
        if (!Double.isFinite(distance)) {
            distance = 0;
        }
        if (angle < 0) {
            distance = -distance;
        }
        return distance;
    }

    /**
     * 行走夹角
     * ∠c = (n * 0.54 / πD) * 360
     *
     * @param R：半径
     * @param x：行走距离
     * @return 夹角
     */
    public static double calculateAngleInDegrees(double R, int x) {
        //∠c
        return (x * 0.54d) / (Math.PI * R * 2) * 360;
    }

    /**
     * 计算三角形第三边
     * C² = a² + b² - 2ab * cos(c)
     *
     * @param a：a边
     * @param b：b边
     * @param angleDegrees：夹角
     * @return 第三边
     */
    public static double calculateThirdSide(double a, double b, double angleDegrees) {
        // 将角度转换为弧度
        double angleRadians = Math.toRadians(angleDegrees);
        // 使用余弦定理计算第三边
        double cSquared = a * a + b * b - 2 * a * b * Math.cos(angleRadians);
        // 返回平方根
        return StringUtils.getRounding(Math.sqrt(cSquared), 1);
    }

    /**
     * 直线行走距离
     * L² = C² - (d+r)²
     *
     * @param C：第三边
     * @param d：厚度
     * @param r：钢筋半径
     * @return 距离
     */
    private static double calculateDistance(double C, double d, double r) {
        double lSquared = C * C - (d + r) * (d + r);
        return Math.sqrt(lSquared);
    }

    /**
     * 拟合曲线（最小二乘法）
     *
     * @param x：xs
     * @param y：ys
     * @return 曲线系数
     */
    public static double[] getCoefficient(double[] x, int[] y) {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (int i = 0; i < x.length; i++) {
            obs.add(x[i], y[i]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        return fitter.fit(obs.toList());
    }

    /**
     * 求 Y
     *
     * @param coefficients：曲线系数
     * @param x：x
     * @return y
     */
    private static double getY(double[] coefficients, double x) {
        return StringUtils.getRounding(coefficients[0] + coefficients[1] * x + coefficients[2] * x * x, 1);
    }
}
