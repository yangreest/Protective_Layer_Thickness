package com.example.gtj_sd_dg240.bluetooth;

/**
 * 蓝牙连接状态监听器接口
 */
public interface BleConnectStateListener {
    /**
     * 连接状态变化回调
     * @param connected true: 已连接, false: 已断开
     */
    void onConnectStateChanged(boolean connected);
}
