package com.example.gtj_sd_dg240.net;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class CustomGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final TypeAdapter<T> adapter;

    CustomGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        //把responsebody转为string
        String response = value.string();
        Result result = gson.fromJson(response, Result.class);
        // 这里只是为了检测code是否!=1,所以只解析HttpStatus中的字段,因为只要code和message就可以了
        if (!result.success) {
            value.close();
            throw new ApiException(result.message);
        }
        try {
            return adapter.fromJson(response);
        } finally {
            value.close();
        }
    }
}