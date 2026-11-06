package com.example.gtj_sd_dg240.net;

public interface RetrofitPresenterResultListener<T> {

    void onSuccess(T result);

    void onError(String error);
}
