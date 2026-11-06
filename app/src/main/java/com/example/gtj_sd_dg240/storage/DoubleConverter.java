package com.example.gtj_sd_dg240.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DoubleConverter implements PropertyConverter<List<Double>, String> {

    private final Gson mGson;

    public DoubleConverter() {
        mGson = new Gson();
    }

    @Override
    public List<Double> convertToEntityProperty(String databaseValue) {
        Type type = new TypeToken<ArrayList<Double>>() {
        }.getType();
        return mGson.fromJson(databaseValue, type);
    }

    @Override
    public String convertToDatabaseValue(List<Double> entityProperty) {
        return mGson.toJson(entityProperty);
    }
}
