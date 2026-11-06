package com.example.gtj_sd_dg240.net;

import android.util.Log;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SPUtils;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.model.EntrustBean;
import com.example.gtj_sd_dg240.model.EntrustWithPageBean;
import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitPresenter {
    private static final String noAddress = "No address associated with hostname";

    private static Retrofit retrofit;
    private static OkHttpClient mOkHttpClient;

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(StaticConstant.httpUrl).client(getOkHttpClient()).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }

    private static OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                //打印retrofit日志
                Log.e("RetrofitLog", "onResponse Data = " + message);
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            mOkHttpClient = builder.readTimeout(60, TimeUnit.SECONDS)//设置读取超时时间
                    .writeTimeout(60, TimeUnit.SECONDS)//设置写的超时时间
                    .connectTimeout(60, TimeUnit.SECONDS)//设置连接超时时间
                    .addInterceptor(new RetryInterceptor(2, 500)).addInterceptor(loggingInterceptor).retryOnConnectionFailure(true).build();
        }
        return mOkHttpClient;
    }

    private static String getStrMessage(String strMessage) {
        if (strMessage != null && strMessage.contains(noAddress)) {
            return "检查网络连接";
        }
        return strMessage;
    }

    public static void requestEntrustList(final RetrofitPresenterResultListener<List<EntrustBean>> resultListener) {
        RetrofitInterfaceServiceUrl request = getRetrofit().create(RetrofitInterfaceServiceUrl.class);
        Call<MessageResult<List<EntrustBean>>> call = request.requestEntrustList(1, SPUtils.getInstance().getString("bluetooth-name"));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<MessageResult<List<EntrustBean>>> call, Response<MessageResult<List<EntrustBean>>> response) {
                if (response.body() == null) {
                    resultListener.onError("请求失败");
                    return;
                }
                if (response.body().code != 200) {
                    resultListener.onError("请求失败：" + response.body().msg);
                    return;
                }
                resultListener.onSuccess(response.body().data);
            }

            @Override
            public void onFailure(Call<MessageResult<List<EntrustBean>>> call, Throwable throwable) {
                if (resultListener != null) {
                    resultListener.onError(getStrMessage(throwable.getMessage()));
                }
                Logger.d(throwable.getMessage());
            }
        });
    }

    public static void requestEntrustListWithPage(int page, int pageSize, final RetrofitPresenterResultListener<EntrustWithPageBean> resultListener) {
        RetrofitInterfaceServiceUrl request = getRetrofit().create(RetrofitInterfaceServiceUrl.class);
        Call<EntrustWithPageBean> call = request.requestEntrustListWithPage(1, SPUtils.getInstance().getString("bluetooth-name"), page, pageSize);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<EntrustWithPageBean> call, Response<EntrustWithPageBean> response) {
                if (response.body() == null) {
                    resultListener.onError("请求失败");
                    return;
                }
                if (response.body().code != 200) {
                    resultListener.onError("请求失败：" + response.body().msg);
                    return;
                }
                resultListener.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<EntrustWithPageBean> call, Throwable throwable) {
                if (resultListener != null) {
                    resultListener.onError(getStrMessage(throwable.getMessage()));
                }
                Logger.d(throwable.getMessage());
            }
        });
    }

    public static void requestUpload(String jsonString, final RetrofitPresenterResultListener<MessageResult<?>> resultListener) {
        RequestBody requestBody = RequestBody.create(okhttp3.MediaType.parse("application/json"), jsonString);
        RetrofitInterfaceServiceUrl request = getRetrofit().create(RetrofitInterfaceServiceUrl.class);
        Call<MessageResult<?>> call = request.requestUploadData(requestBody);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<MessageResult<?>> call, @NonNull Response<MessageResult<?>> response) {
                if (response.body() == null) {
                    resultListener.onError("请求失败");
                    return;
                }
                if (response.body().code != 200) {
                    resultListener.onError(response.body().code + "_" + response.body().msg);
                    return;
                }
                resultListener.onSuccess(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<MessageResult<?>> call, @NonNull Throwable throwable) {
                if (resultListener != null) {
                    resultListener.onError(getStrMessage(throwable.getMessage()));
                }
                Logger.d(throwable.getMessage());
            }
        });
    }
}