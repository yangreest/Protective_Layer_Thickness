package com.example.gtj_sd_dg240.activity.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.activity.start.BaseNoConnectActivity;
import com.example.gtj_sd_dg240.adapter.BluetoothDeviceListAdapter;
import com.example.gtj_sd_dg240.databinding.ActScanningBluetoothBinding;
import com.example.gtj_sd_dg240.model.BluetoothDeviceBean;
import com.example.gtj_sd_dg240.utils.PermissionUtils;
import com.inuker.bluetooth.library.BluetoothManager;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.util.ArrayList;
import java.util.List;

@SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
public class ScanningBlueboothActivity extends BaseNoConnectActivity<ActScanningBluetoothBinding> {
    private final List<SearchResult> mDevices = new ArrayList<>();
    private BluetoothDeviceListAdapter deviceListAdapter;
    private String strConnectSuccess;
    private String deviceMac, deviceName;

    @Override
    protected ActScanningBluetoothBinding getBinding() {
        return ActScanningBluetoothBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLable();
        initData();
    }

    private void setLable() {
        if (StaticConstant.getLanguage()) {
            binding.navigationBar.setTitleText("连接设备主机");
            binding.tv01.setText("连接设备：");
            binding.btnScanning.setText("扫描设备");
            strConnectSuccess = "连接成功";
        } else {
            binding.navigationBar.setTitleText("Sensor probe connection");
            binding.btnScanning.setText("Scanning equipment");
            binding.tv01.setText("Connecting device：");
            strConnectSuccess = "Connection successful";
        }
    }

    protected void initData() {
        initBatteryReceiver(binding.navigationBar);
        initBluetoothStatus(binding.navigationBar);
        initRecyclerView();
        binding.btnScanning.setOnClickListener(this::onViewClicked);
        binding.btnCancel.setOnClickListener(this::onViewClicked);
        deviceMac = SPUtils.getInstance().getString("bluetooth-mac");
        deviceName = SPUtils.getInstance().getString("bluetooth-name");
        setDeviceName();
        BluetoothManager.unConnect(deviceMac);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!deviceListAdapter.getData().isEmpty()) {
            return;
        }
        PermissionUtils.initPermission(this, this::scanningBlueboothDevice);
    }

    public void onViewClicked(View view) {
        if (view.getId() == R.id.btn_scanning) {
            PermissionUtils.initPermission(this, this::scanningBlueboothDevice);
        } else if (view.getId() == R.id.btn_cancel) {
            finish();
        }
    }

    private void scanningBlueboothDevice() {
        BluetoothManager.searchDeviceDevice(new SearchResponse() {
            @Override
            public void onSearchStarted() {
                if (isDestroyed() || isFinishing()) {
                    return;
                }
                progressiveDialog.setTvContent("搜索中...").show();
                //清空列表
                mDevices.clear();
                deviceListAdapter.getData().clear();
                deviceListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                if (!TextUtils.equals("NULL", device.getName().toUpperCase())) {
                    if (!mDevices.contains(device)) {
                        mDevices.add(device);
                    }
                }
            }

            @Override
            public void onSearchStopped() {
                if (isDestroyed() || isFinishing()) {
                    return;
                }
                List<BluetoothDeviceBean> tempList = new ArrayList<>();
                for (int i = 0; i < mDevices.size(); i++) {
                    tempList.add(new BluetoothDeviceBean(mDevices.get(i).getAddress(), mDevices.get(i).getName()));
                }
                deviceListAdapter.setNewData(tempList);
                progressiveDialog.dismiss();
                //
                for (int i = 0; i < tempList.size(); i++) {
                    if (TextUtils.equals(tempList.get(i).mac, deviceMac)) {
                        connectDevice(i);
                        break;
                    }
                }
            }

            @Override
            public void onSearchCanceled() {
                if (isDestroyed() || isFinishing()) {
                    return;
                }
                progressiveDialog.dismiss();
            }
        });
    }

    private void connectDevice(int position) {
        progressiveDialog.setTvContent("连接中...").show();
        BluetoothDeviceBean deviceBean = deviceListAdapter.getData().get(position);
        BluetoothManager.connectBluetoothDevice(deviceBean.mac, flag -> {
            if (isDestroyed() || isFinishing()) {
                return;
            }
            progressiveDialog.dismiss();
            if (flag) {
                for (int i = 0; i < deviceListAdapter.getData().size(); i++) {
                    deviceListAdapter.getData().get(i).isSelected = false;
                }
                deviceListAdapter.getData().get(position).isSelected = true;
                deviceListAdapter.notifyDataSetChanged();
                SPUtils.getInstance().put("bluetooth-mac", deviceBean.mac);
                SPUtils.getInstance().put("bluetooth-name", deviceBean.name);
                deviceMac = deviceBean.mac;
                deviceName = deviceBean.name;
                setDeviceName();
                ToastUtils.showShort(strConnectSuccess);
            }
        });
    }

    private void initRecyclerView() {
        binding.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceListAdapter = new BluetoothDeviceListAdapter();
        deviceListAdapter.bindToRecyclerView(binding.mRecyclerView);
        deviceListAdapter.setOnItemClickListener((adapter, view, position) -> connectDevice(position));
    }

    private void setDeviceName() {
        if (!TextUtils.isEmpty(deviceMac)) {
            binding.tvContent.setText(deviceName + "（" + deviceMac + "）");
        } else {
            binding.tvContent.setText("");
        }
    }
}
