package com.example.gtj_sd_dg240;

import android.app.Application;
import android.graphics.Color;
import android.view.Gravity;

import com.baidu.location.LocationClient;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.example.gtj_sd_dg240.storage.GreenDaoHelper;
import com.example.gtj_sd_dg240.utils.CrashHandler;
import com.example.gtj_sd_dg240.utils.LocService;
import com.inuker.bluetooth.library.BluetoothManager;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public class GTJApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothManager.init(this, StaticConstant.USR_SERVICE, StaticConstant.USR_CHARACTER_NOTIFY, StaticConstant.USR_CHARACTER_WRITE);
        //Crash
        CrashHandler.getInstance().init(this);
        //Logger
        Logger.addLogAdapter(new AndroidLogAdapter(PrettyFormatStrategy.newBuilder().build()) {
        });
        //Utils
        Utils.init(this);
        //GreenDao
        GreenDaoHelper.init(this);
        //定位
        LocationClient.setAgreePrivacy(true);
        StaticConstant.locService = new LocService(this);
        //Toast
        ToastUtils.getDefaultMaker().setGravity(Gravity.CENTER, 0, 0);
        ToastUtils.getDefaultMaker().setBgColor(Color.parseColor("#000000"));
        ToastUtils.getDefaultMaker().setTextColor(Color.parseColor("#FFFFFF"));
        ToastUtils.getDefaultMaker().setTextSize(20);
    }
}
