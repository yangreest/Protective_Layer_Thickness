package com.example.gtj_sd_dg240.activity.start;

import androidx.viewbinding.ViewBinding;

/**
 * 主界面 Activity 基类
 * 不隐藏虚拟按键，支持自动重连
 */
public abstract class BaseMainActivity<T extends ViewBinding> extends BaseActivity<T> {

    @Override
    protected int getActivityFlags() {
        // 不隐藏虚拟按键，但自动重连
        return FLAG_AUTO_RECONNECT;
    }
}
