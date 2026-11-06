package com.example.gtj_sd_dg240.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.converter.PropertyConverter;

public class CurveDoubleConverter implements PropertyConverter<double[], String> {
    @Override
    public double[] convertToEntityProperty(String value) {
        return mGson.fromJson(value, new TypeToken<double[]>() {
        }.getType());
    }

    @Override
    public String convertToDatabaseValue(double[] entityProperty) {
        return mGson.toJson(entityProperty);
    }

    private final Gson mGson;

    public CurveDoubleConverter() {
        mGson = new Gson();
    }
}
