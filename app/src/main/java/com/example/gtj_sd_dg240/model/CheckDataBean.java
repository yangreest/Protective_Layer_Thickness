package com.example.gtj_sd_dg240.model;

public class CheckDataBean {
    public String poleNorm;  //电杆规格，直径
    public double thickness;  //保护层厚度
    public int index;  //取采集数据最大信号bean时，代表下标index | 取标准表数据里的厚度曲线index即厚度index
    public int x;  //位移
    public double y;  //信号值
    public double[] curve_y;  //信号值
    public int[] curve_x;  //信号值

    //
    public CheckDataBean(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public CheckDataBean(String poleNorm) {
        this.poleNorm = poleNorm;
    }

    public CheckDataBean(String poleNorm, int index, int x, double y) {
        this.poleNorm = poleNorm;
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public String showString() {
        return "{" + index + " , " + x + " , " + y + "}";
    }
}
