package com.example.gtj_sd_dg240.net;

public class Result<T> {
    public boolean success;
    public String message;
    public T data;

    @Override
    public String toString() {
        return "{" + success + "," + message + "}";
    }
}
