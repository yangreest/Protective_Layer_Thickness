package com.example.gtj_sd_dg240.bluetooth;

/**
 * 蓝牙数据回调接口
 */
public interface BleDataCallback {
    /**
     * 收到蓝牙数据
     * @param data 原始字节数据
     */
    void onDataReceived(byte[] data);

    /**
     * 通知注册失败
     * @param errorCode 错误码
     */
    default void onNotifyRegisterFailed(int errorCode) {}
}
