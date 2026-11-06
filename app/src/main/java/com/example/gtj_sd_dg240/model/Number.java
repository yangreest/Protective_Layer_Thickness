package com.example.gtj_sd_dg240.model;

import androidx.annotation.NonNull;

public class Number {
    public int number;  //信号值
    public int distance;  //位置

    public Number(int distance, int number) {
        this.number = number;
        this.distance = distance;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + distance + ", " + number + "}";
    }
}
