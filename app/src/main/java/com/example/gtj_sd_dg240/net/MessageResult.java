package com.example.gtj_sd_dg240.net;

public class MessageResult<T> {
    public int code;
    public String msg;
    public T data;

    @Override
    public String toString() {
        return "{" + code + "," + msg + "}";
    }
}
