package com.example.gtj_sd_dg240.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.model.BluetoothDeviceBean;

public class BluetoothDeviceListAdapter extends BaseQuickAdapter<BluetoothDeviceBean, BaseViewHolder> {

    public BluetoothDeviceListAdapter() {
        super(R.layout.item_bluetooth_device_list, null);
    }

    @Override
    protected void convert(BaseViewHolder helper, BluetoothDeviceBean item) {
        helper.setText(R.id.tv_title, item.name);
        helper.setText(R.id.tv_content0, item.mac);
        if (TextUtils.equals(StaticConstant.APP_DEFAULT, StaticConstant.APP_GTJ)) {
            helper.setImageResource(R.id.imageView, item.isSelected ? R.mipmap.icon_single_selected_blue : R.mipmap.icon_single_unselected);
        } else {
            helper.setImageResource(R.id.imageView, item.isSelected ? R.mipmap.icon_single_selected : R.mipmap.icon_single_unselected);
        }
    }
}
