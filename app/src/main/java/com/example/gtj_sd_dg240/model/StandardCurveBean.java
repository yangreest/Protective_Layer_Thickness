package com.example.gtj_sd_dg240.model;

import androidx.annotation.NonNull;

import com.example.gtj_sd_dg240.storage.CurveConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

@Entity
public class StandardCurveBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    public Long id;  //（序号id）
    @Unique
    public String uniqueKey;
    //
    public String diameter;  //钢筋直径
    public double thickness;  //保护层厚度
    public double changeRate;  //变化率
    @Convert(converter = CurveConverter.class, columnType = String.class)
    public int[] signalArray;//信号波形
    @Transient
    public int compareValue;
    @Transient
    public boolean isSelected;

    public StandardCurveBean(String diameter, double thickness, int[] signalArray) {
        this.diameter = diameter;
        this.thickness = thickness;
        this.signalArray = signalArray;
        generateUniqueKey();
    }

    @Generated(hash = 882079526)
    public StandardCurveBean(Long id, String uniqueKey, String diameter, double thickness, double changeRate, int[] signalArray) {
        this.id = id;
        this.uniqueKey = uniqueKey;
        this.diameter = diameter;
        this.thickness = thickness;
        this.changeRate = changeRate;
        this.signalArray = signalArray;
    }

    @Generated(hash = 1814938663)
    public StandardCurveBean() {
    }

    // 生成唯一键
    public void generateUniqueKey() {
        this.uniqueKey = diameter + "_" + thickness;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + id + ", " + uniqueKey + ", " + signalArray.length + "}";
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueKey() {
        return this.uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getDiameter() {
        return this.diameter;
    }

    public void setDiameter(String diameter) {
        this.diameter = diameter;
    }

    public double getThickness() {
        return this.thickness;
    }

    public void setThickness(double thickness) {
        this.thickness = thickness;
    }

    public int[] getSignalArray() {
        return this.signalArray;
    }

    public void setSignalArray(int[] signalArray) {
        this.signalArray = signalArray;
    }

    public double getChangeRate() {
        return this.changeRate;
    }

    public void setChangeRate(double changeRate) {
        this.changeRate = changeRate;
    }
}
