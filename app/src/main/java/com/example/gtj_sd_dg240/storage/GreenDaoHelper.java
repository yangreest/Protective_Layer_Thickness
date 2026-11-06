package com.example.gtj_sd_dg240.storage;

import android.annotation.SuppressLint;
import android.content.Context;

import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.functions.database.greenDao.db.DaoMaster;
import com.example.gtj_sd_dg240.functions.database.greenDao.db.DaoSession;

public class GreenDaoHelper {
    @SuppressLint("StaticFieldLeak")
    private static DaoOpenHelper helper;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    public static void init(Context context) {
        helper = new DaoOpenHelper(context, StaticConstant.DB_NAME, null);
    }

    /**
     * 获取DaoMaster
     */
    private static void getDaoMaster() {
        if (daoMaster == null) {
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
    }

    /**
     * 获取DaoSession对象
     */
    public static DaoSession getDaoSession() {
        if (daoSession == null) {
            if (daoMaster == null) {
                getDaoMaster();
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }
}
