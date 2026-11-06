package com.example.gtj_sd_dg240.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.gtj_sd_dg240.functions.database.greenDao.db.DaoMaster;
import com.example.gtj_sd_dg240.functions.database.greenDao.db.ProjectBeanDao;
import com.example.gtj_sd_dg240.functions.database.greenDao.db.RebarMinMaxBeanDao;
import com.example.gtj_sd_dg240.functions.database.greenDao.db.ZoneBeanDao;
import com.github.yuweiguocn.library.greendao.MigrationHelper;

import org.greenrobot.greendao.database.Database;

public class DaoOpenHelper extends DaoMaster.OpenHelper {
    DaoOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {
            @Override
            public void onCreateAllTables(Database db, boolean ifNotExists) {
                DaoMaster.createAllTables(db, ifNotExists);
            }

            @Override
            public void onDropAllTables(Database db, boolean ifExists) {
                DaoMaster.dropAllTables(db, ifExists);
            }
        }, ProjectBeanDao.class, ZoneBeanDao.class, RebarMinMaxBeanDao.class);
    }
}