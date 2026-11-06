package com.example.gtj_sd_dg240.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class PoleThicknessBean implements Serializable {
    public int signal;  //信号
    public double sThickness;  //保护层厚度（信号）
    public double poleCompensation;  //电杆直径补偿
    public double rebarCompensation;  //钢筋直径补偿
    public double spaceCompensationL;  //主筋间距厚度补偿
    public double spaceCompensationR;  //主筋间距厚度补偿
    public double spaceCompensationScrew;  //螺旋筋间距厚度补偿
    public String spaceL;  //间距的左、右和间距
    public String spaceR;  //间距的左、右和间距
    public String spaceScrew;  //间距的左、右和间距
    public double thickness;  //保护层厚度

    public PoleThicknessBean(int signal, double sThickness, double poleCompensation, double rebarCompensation, String spaceL, double spaceCompensationL, String spaceR, double spaceCompensationR, double spaceCompensationScrew, String spaceScrew, double thickness) {
        this.signal = signal;
        this.sThickness = sThickness;
        this.poleCompensation = poleCompensation;
        this.rebarCompensation = rebarCompensation;
        this.spaceL = spaceL;
        this.spaceCompensationL = spaceCompensationL;
        this.spaceR = spaceR;
        this.spaceCompensationR = spaceCompensationR;
        this.spaceCompensationScrew = spaceCompensationScrew;
        this.spaceScrew = spaceScrew;
        this.thickness = thickness;
    }

    @NonNull
    @Override
    public String toString() {
        return "{ 信号: " + signal + " , 厚度: " + sThickness + " + " + poleCompensation + " + " + rebarCompensation + " + " + spaceCompensationL + " (" + spaceL + ")" + " + " + spaceCompensationR + " (" + spaceR + ")" + " + " + spaceCompensationScrew + " (" + spaceScrew + ")" + " = " + thickness + " }\n";
    }
}
