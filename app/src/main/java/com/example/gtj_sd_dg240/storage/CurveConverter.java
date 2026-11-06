package com.example.gtj_sd_dg240.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.converter.PropertyConverter;


public class CurveConverter implements PropertyConverter<int[], String> {
    @Override
    public int[] convertToEntityProperty(String value) {
        return mGson.fromJson(value, new TypeToken<int[]>() {
        }.getType());
    }

    @Override
    public String convertToDatabaseValue(int[] entityProperty) {
        return mGson.toJson(entityProperty);
    }

    private final Gson mGson;

    public CurveConverter() {
        mGson = new Gson();
    }
}
