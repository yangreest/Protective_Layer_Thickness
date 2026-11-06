package com.example.gtj_sd_dg240.model;

public class MeteringBean {
    public int signalValue;  //信号值
    public double thickness;  //保护层厚度
    public String diameter;  //钢筋直径
    public boolean isSelected;

    public MeteringBean(String diameter, int signalValue, double thickness) {
        this.diameter = diameter;
        this.signalValue = signalValue;
        this.thickness = thickness;
    }
}
