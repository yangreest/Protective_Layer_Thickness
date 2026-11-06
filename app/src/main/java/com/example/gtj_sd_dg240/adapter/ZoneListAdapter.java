package com.example.gtj_sd_dg240.adapter;

import android.content.Context;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.model.ZoneBean;

public class ZoneListAdapter extends BaseQuickAdapter<ZoneBean, BaseViewHolder> {
    private final int colorFFF, color006F5F, color333;

    public ZoneListAdapter(Context context) {
        super(R.layout.item_zone_list, null);
        colorFFF = ContextCompat.getColor(context, R.color.colorFFF);
        color006F5F = ContextCompat.getColor(context, R.color.color006F5F);
        color333 = ContextCompat.getColor(context, R.color.color333);
    }

    @Override
    protected void convert(BaseViewHolder helper, ZoneBean item) {
        helper.setText(R.id.textView, item.poleNo);
        helper.setGone(R.id.tv_entrust, !TextUtils.isEmpty(item.entrustCode));
        helper.setText(R.id.tv_entrust, TextUtils.isEmpty(item.entrustCode) ? "" : "委托单号：" + item.entrustCode);
        helper.setTextColor(R.id.textView, item.isSelected_open ? colorFFF : color333);
        helper.setBackgroundColor(R.id.textView, item.isSelected_open ? color006F5F : colorFFF);
        if (TextUtils.equals(StaticConstant.APP_DEFAULT, StaticConstant.APP_GTJ)) {
            helper.setImageResource(R.id.imageView, item.isSelected_treat ? R.mipmap.icon_multiple_blue : R.mipmap.icon_multiple0);
        } else {
            helper.setImageResource(R.id.imageView, item.isSelected_treat ? R.mipmap.icon_multiple1 : R.mipmap.icon_multiple0);
        }
        helper.setBackgroundColor(R.id.ll_content, item.isSelected_open ? color006F5F : colorFFF);
        helper.addOnClickListener(R.id.ll_content);
        helper.addOnClickListener(R.id.imageView);
    }
}
