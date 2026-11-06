package com.example.gtj_sd_dg240.activity.start;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewbinding.ViewBinding;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.bluetooth.BleConnectStateListener;
import com.example.gtj_sd_dg240.bluetooth.BleDataCallback;
import com.example.gtj_sd_dg240.bluetooth.BluetoothService;
import com.example.gtj_sd_dg240.utils.CustomDensityUtils;
import com.example.gtj_sd_dg240.utils.OtherUtils;
import com.example.gtj_sd_dg240.view.NavigationBar;
import com.example.gtj_sd_dg240.view.ProgressiveDialog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 统一的 Activity 基类
 * 支持 ViewBinding 和蓝牙管理
 */
public abstract class BaseActivity<T extends ViewBinding> extends AppCompatActivity {

    // 配置标志
    protected static final int FLAG_HIDE_NAVIGATION = 1;      // 隐藏虚拟按键
    protected static final int FLAG_AUTO_RECONNECT = 2;       // 自动重连

    // 成员变量
    private BroadcastReceiver batteryReceiver;
    protected ProgressiveDialog progressiveDialog;
    protected ExecutorService executorService;
    protected Handler uiHandler;
    private boolean isHideKeyboard;
    protected boolean isShow;
    protected T binding;

    // 蓝牙服务
    protected BluetoothService bluetoothService;
    private BleConnectStateListener connectStateListener;
    private BleDataCallback dataCallback;

    /**
     * 子类实现此方法返回对应的 ViewBinding
     */
    protected abstract T getBinding();

    /**
     * 子类可重写此方法返回配置标志
     * 默认: 隐藏虚拟按键 + 自动重连
     */
    protected int getActivityFlags() {
        return FLAG_HIDE_NAVIGATION | FLAG_AUTO_RECONNECT;
    }

    /**
     * 子类可重写此方法处理蓝牙数据
     */
    protected void onBluetoothDataReceived(byte[] data) {
        // 默认空实现，子类可重写
    }

    /**
     * 子类可重写此方法处理连接状态变化
     */
    protected void onBluetoothConnectionChanged(boolean connected) {
        // 默认空实现，子类可重写
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        setupViewBinding();
        initCommonResources();
        initBluetoothService();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        applyStatusBarPadding(view);
    }

    private static final int TAG_ORIGINAL_PADDING_TOP = R.id.status_bar_padding_tag;

    protected void applyStatusBarPadding(View rootView) {
        if (rootView.getTag(TAG_ORIGINAL_PADDING_TOP) == null) {
            rootView.setTag(TAG_ORIGINAL_PADDING_TOP, rootView.getPaddingTop());
        }

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int originalPaddingTop = (v.getTag(TAG_ORIGINAL_PADDING_TOP) instanceof Integer) ? (Integer) v.getTag(TAG_ORIGINAL_PADDING_TOP) : 0;
            v.setPadding(v.getPaddingLeft(), originalPaddingTop + statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(rootView);
    }

    /**
     * 配置窗口
     */
    private void configureWindow() {
        setupStatusBar();
        //
        int flags = getActivityFlags();

        // 去掉状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 隐藏虚拟按键
        if ((flags & FLAG_HIDE_NAVIGATION) != 0) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
            getWindow().setAttributes(params);
        }

        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 适配分辨率
        CustomDensityUtils.setCustomDensity(this, getApplication());
    }

    private static final int STATUS_BAR_COLOR = Color.parseColor("#FF009FA8");
    private View statusBarBgView;

    private void setupStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().getDecorView().setSystemUiVisibility(0);

        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        statusBarBgView = new View(this);
        statusBarBgView.setBackgroundColor(STATUS_BAR_COLOR);
        decorView.addView(statusBarBgView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));

        ViewCompat.setOnApplyWindowInsetsListener(decorView, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            if (statusBarBgView != null) {
                ViewGroup.LayoutParams lp = statusBarBgView.getLayoutParams();
                lp.height = statusBarHeight;
                statusBarBgView.setLayoutParams(lp);
            }
            return insets;
        });
    }

    /**
     * 设置 ViewBinding
     */
    private void setupViewBinding() {
        binding = getBinding();
        setContentView(binding.getRoot());
    }

    /**
     * 初始化通用资源
     */
    private void initCommonResources() {
        progressiveDialog = new ProgressiveDialog(this);
        executorService = Executors.newFixedThreadPool(1);
        uiHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 初始化蓝牙服务
     */
    private void initBluetoothService() {
        int flags = getActivityFlags();
        boolean autoReconnect = (flags & FLAG_AUTO_RECONNECT) != 0;

        if (autoReconnect) {
            bluetoothService = BluetoothService.getInstance();
            bluetoothService.startAutoReconnect(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.isShow = true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.isShow = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.isShow = false;
    }

    /**
     * 初始化电池电量监听
     */
    protected void initBatteryReceiver(NavigationBar navigationBar) {
        batteryReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                    if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                        navigationBar.setivBattery(true);
                    } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                        navigationBar.setivBattery(false);
                    }
                }
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 0);
                navigationBar.setBatteryLevel(level, scale);
            }
        };
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    /**
     * 初始化蓝牙连接状态监听（仅显示图标状态）
     * 使用统一的 BluetoothService
     */
    protected void initBluetoothStatus(final NavigationBar navigationBar) {
        if (bluetoothService == null) {
            // 未启用自动重连，直接从 SP 读取 MAC 并检查状态
            String mac = SPUtils.getInstance().getString("bluetooth-mac");
            if (!TextUtils.isEmpty(mac)) {
                boolean connected = com.inuker.bluetooth.library.BluetoothManager.getConnectStatus(mac);
                navigationBar.setIvBluetooth(connected);
            } else {
                navigationBar.setIvBluetooth(false);
            }
            return;
        }

        // 创建连接状态监听器
        connectStateListener = connected -> {
            if (isShow) {
                navigationBar.setIvBluetooth(connected);
            }
        };

        // 注册监听器
        bluetoothService.registerConnectListener(connectStateListener);

        // 主动对蓝牙设备进行一次真实连接，清除可能失效的“已连接”状态，确保后续能收到数据
        //bluetoothService.connectOnce();
    }

    /**
     * 注册蓝牙数据回调
     * 子类调用此方法来接收蓝牙数据
     */
    protected void registerBluetoothDataCallback() {
        if (bluetoothService == null) return;

        dataCallback = data -> {
            if (isShow) {
                onBluetoothDataReceived(data);
            }
        };

        bluetoothService.registerDataCallback(dataCallback);
    }

    /**
     * 设置隐藏软键盘
     */
    protected void setHideKeyboard() {
        this.isHideKeyboard = true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isHideKeyboard && ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (OtherUtils.isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 注销电池监听
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }

        // 注销蓝牙监听器
        if (bluetoothService != null) {
            if (connectStateListener != null) {
                bluetoothService.unregisterConnectListener(connectStateListener);
            }
            if (dataCallback != null) {
                bluetoothService.unregisterDataCallback(dataCallback);
            }
        }

        // 关闭线程池
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }

        ToastUtils.cancel();
    }
}
