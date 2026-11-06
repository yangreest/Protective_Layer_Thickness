package com.example.gtj_sd_dg240.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.model.MeteringBean;

public class MeteringDeviceAdapter extends BaseQuickAdapter<MeteringBean, BaseViewHolder> {

    public MeteringDeviceAdapter() {
        super(R.layout.item_metering_device, null);
    }

    @Override
    protected void convert(BaseViewHolder helper, MeteringBean item) {
        helper.setText(R.id.textView, "直　径：" + item.diameter + "\n保护层：" + item.thickness);
    }
}
