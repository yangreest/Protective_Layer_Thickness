package com.example.gtj_sd_dg240.bluetooth;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 蓝牙服务管理类 - 单例模式
 * 统一管理蓝牙连接、重连、数据监听
 */
public class BluetoothService {

    private static volatile BluetoothService instance;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 连接状态
    private volatile boolean isConnected = false;
    private volatile String connectedMac = null;

    // 监听器列表（线程安全）
    private final CopyOnWriteArrayList<BleConnectStateListener> connectListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<BleDataCallback> dataCallbacks = new CopyOnWriteArrayList<>();

    // 内部监听器
    private BleConnectStatusListener internalConnectListener;

    // 重连配置
    private static final long RECONNECT_INTERVAL_MS = 2000;
    private boolean autoReconnectEnabled = true;
    private Runnable reconnectRunnable;

    private BluetoothService() {}

    public static BluetoothService getInstance() {
        if (instance == null) {
            synchronized (BluetoothService.class) {
                if (instance == null) {
                    instance = new BluetoothService();
                }
            }
        }
        return instance;
    }

    /**
     * 获取保存的 MAC 地址
     */
    public String getSavedMac() {
        return SPUtils.getInstance().getString("bluetooth-mac");
    }

    /**
     * 获取当前连接状态
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * 获取连接状态（通过 MAC）
     */
    public boolean isConnected(String mac) {
        if (TextUtils.isEmpty(mac)) return false;
        return com.inuker.bluetooth.library.BluetoothManager.getConnectStatus(mac);
    }

    /**
     * 开始自动重连服务
     */
    public void startAutoReconnect() {
        startAutoReconnect(true);
    }

    /**
     * 开始自动重连服务
     * @param enableAutoConnect 是否启用自动连接
     */
    public void startAutoReconnect(boolean enableAutoConnect) {
        this.autoReconnectEnabled = enableAutoConnect;

        String mac = getSavedMac();
        if (TextUtils.isEmpty(mac)) {
            return;
        }

        // 注册内部连接状态监听器
        registerInternalListener(mac);

        // 如果已连接，设置状态并注册数据通知
        if (isConnected(mac)) {
            isConnected = true;
            connectedMac = mac;
            registerDataNotify(mac);
        }

        // 启动重连定时器
        if (enableAutoConnect) {
            startReconnectTimer(mac);
        }
    }

    /**
     * 停止自动重连服务
     */
    public void stopAutoReconnect() {
        autoReconnectEnabled = false;
        stopReconnectTimer();

        String mac = getSavedMac();
        if (!TextUtils.isEmpty(mac)) {
            unregisterInternalListener(mac);
        }
    }

    /**
     * 注册内部连接状态监听器
     */
    private void registerInternalListener(String mac) {
        if (internalConnectListener != null) {
            // 先注销旧的
            com.inuker.bluetooth.library.BluetoothManager.unregisterConnectStatusListener(
                mac, internalConnectListener);
        }

        internalConnectListener = new BleConnectStatusListener() {
            @Override
            public void onConnectStatusChanged(String deviceMac, int status) {
                mainHandler.post(() -> {
                    if (status == Constants.STATUS_CONNECTED) {
                        isConnected = true;
                        connectedMac = deviceMac;
                        // 连接成功后注册数据通知
                        registerDataNotify(deviceMac);
                        // 通知所有监听器
                        notifyConnectStateChanged(true);
                        ToastUtils.showLong("连接成功，请检测");
                    } else if (status == Constants.STATUS_DISCONNECTED) {
                        isConnected = false;
                        // 断开时取消数据通知
                        com.inuker.bluetooth.library.BluetoothManager.unNotifyBluetoothDevice(deviceMac);
                        // 通知所有监听器
                        notifyConnectStateChanged(false);
                    }
                });
            }
        };

        com.inuker.bluetooth.library.BluetoothManager.registerConnectStatusListener(
            mac, internalConnectListener);
    }

    /**
     * 注销内部连接状态监听器
     */
    private void unregisterInternalListener(String mac) {
        if (internalConnectListener != null && !TextUtils.isEmpty(mac)) {
            com.inuker.bluetooth.library.BluetoothManager.unregisterConnectStatusListener(
                mac, internalConnectListener);
            internalConnectListener = null;
        }
    }

    /**
     * 注册数据通知
     */
    private void registerDataNotify(String mac) {
        if (TextUtils.isEmpty(mac)) return;

        com.inuker.bluetooth.library.BluetoothManager.notifyBluetoothDevice(
            mac, new BleNotifyResponse() {
                @Override
                public void onNotify(UUID service, UUID character, byte[] value) {
                    // 分发数据给所有回调
                    for (BleDataCallback callback : dataCallbacks) {
                        mainHandler.post(() -> callback.onDataReceived(value));
                    }
                }

                @Override
                public void onResponse(int code) {
                    if (code != 0) {
                        // 通知注册失败
                        for (BleDataCallback callback : dataCallbacks) {
                            callback.onNotifyRegisterFailed(code);
                        }
                    }
                }
            });
    }

    /**
     * 启动重连定时器
     */
    private void startReconnectTimer(String mac) {
        stopReconnectTimer();

        reconnectRunnable = () -> {
            if (!autoReconnectEnabled) return;

            if (!isConnected(mac)) {
                // 尝试连接
                com.inuker.bluetooth.library.BluetoothManager.connectBluetoothDevice(
                    mac, connected -> {
                        // 连接回调处理
                    });
            }

            // 继续下一次检查
            if (autoReconnectEnabled) {
                mainHandler.postDelayed(reconnectRunnable, RECONNECT_INTERVAL_MS);
            }
        };

        mainHandler.post(reconnectRunnable);
    }

    /**
     * 停止重连定时器
     */
    private void stopReconnectTimer() {
        if (reconnectRunnable != null) {
            mainHandler.removeCallbacks(reconnectRunnable);
            reconnectRunnable = null;
        }
    }

    /**
     * 注册连接状态监听器
     */
    public void registerConnectListener(BleConnectStateListener listener) {
        if (!connectListeners.contains(listener)) {
            connectListeners.add(listener);
            // 立即通知当前状态
            listener.onConnectStateChanged(isConnected);
        }
    }

    /**
     * 注销连接状态监听器
     */
    public void unregisterConnectListener(BleConnectStateListener listener) {
        connectListeners.remove(listener);
    }

    /**
     * 注册数据回调
     */
    public void registerDataCallback(BleDataCallback callback) {
        if (!dataCallbacks.contains(callback)) {
            dataCallbacks.add(callback);
        }
    }

    /**
     * 注销数据回调
     */
    public void unregisterDataCallback(BleDataCallback callback) {
        dataCallbacks.remove(callback);
    }

    /**
     * 通知所有连接状态监听器
     */
    private void notifyConnectStateChanged(boolean connected) {
        for (BleConnectStateListener listener : connectListeners) {
            listener.onConnectStateChanged(connected);
        }
    }

    /**
     * 发送数据
     */
    public void writeData(byte[] data) {
        String mac = getSavedMac();
        if (TextUtils.isEmpty(mac)) return;

        com.inuker.bluetooth.library.BluetoothManager.writeBluetooth(mac, data);
    }

    /**
     * 主动对蓝牙设备进行一次连接
     * 先断开旧连接以清除可能失效的“已连接”状态（如设备已关机但系统状态未刷新），
     * 再重新发起一次真实连接，连接成功后才注册数据通知并通知 UI，确保后续能收到数据
     */
    public void connectOnce() {
        String mac = getSavedMac();
        if (TextUtils.isEmpty(mac)) {
            isConnected = false;
            notifyConnectStateChanged(false);
            return;
        }

        // 无论当前状态如何，先断开旧连接，清除可能失效的“已连接”状态
        com.inuker.bluetooth.library.BluetoothManager.unConnect(mac);
        isConnected = false;
        connectedMac = null;
        notifyConnectStateChanged(false);

        // 延迟重新发起一次真实连接，避免与断开操作竞争
        mainHandler.postDelayed(() -> {
            if (TextUtils.isEmpty(getSavedMac())) return;
            com.inuker.bluetooth.library.BluetoothManager.connectBluetoothDevice(
                mac, connected -> mainHandler.post(() -> {
                    if (connected) {
                        isConnected = true;
                        connectedMac = mac;
                        // 连接成功后注册数据通知，确保能收到数据
                        registerDataNotify(mac);
                        notifyConnectStateChanged(true);
                    } else {
                        // 连接失败（如设备未开机），保持未连接状态
                        isConnected = false;
                        notifyConnectStateChanged(false);
                    }
                }));
        }, 500);
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        String mac = getSavedMac();
        if (!TextUtils.isEmpty(mac)) {
            com.inuker.bluetooth.library.BluetoothManager.unConnect(mac);
        }
        isConnected = false;
        connectedMac = null;
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        stopAutoReconnect();
        connectListeners.clear();
        dataCallbacks.clear();
        isConnected = false;
        connectedMac = null;
    }
}
