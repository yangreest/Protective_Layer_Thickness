package com.example.gtj_sd_dg240.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static String isoDateToNormal(String isoDate) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat normalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = isoFormat.parse(isoDate);
            return normalFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return isoDate;
        }
    }
}
