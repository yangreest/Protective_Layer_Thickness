package com.example.gtj_sd_dg240.activity.start;

import androidx.viewbinding.ViewBinding;

/**
 * 不自动连接蓝牙的 Activity 基类
 * 隐藏虚拟按键，不自动重连
 */
public abstract class BaseNoConnectActivity<T extends ViewBinding> extends BaseActivity<T> {

    @Override
    protected int getActivityFlags() {
        // 隐藏虚拟按键，但不自动重连
        return FLAG_HIDE_NAVIGATION;
    }
}
