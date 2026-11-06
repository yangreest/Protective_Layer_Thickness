package com.example.gtj_sd_dg240.adapter;

import android.content.Context;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.model.ConfigBean;

public class ConfigTypeListAdapter extends BaseQuickAdapter<ConfigBean, BaseViewHolder> {
    private final int colorFFF, color4CB4B9, color333;
    private final boolean isCreatePage;

    public ConfigTypeListAdapter(Context context, boolean isCreatePage) {
        super(R.layout.item_project_list, null);
        colorFFF = ContextCompat.getColor(context, R.color.colorFFF);
        color4CB4B9 = ContextCompat.getColor(context, R.color.color006F5F);
        color333 = ContextCompat.getColor(context, R.color.color333);
        this.isCreatePage = isCreatePage;
    }

    @Override
    protected void convert(BaseViewHolder helper, ConfigBean item) {
        helper.setText(R.id.textView, item.poleType);
        helper.setTextColor(R.id.textView, item.isSelected ? colorFFF : color333);
        helper.setBackgroundColor(R.id.textView, item.isSelected ? color4CB4B9 : colorFFF);

        if (TextUtils.equals(StaticConstant.APP_DEFAULT, StaticConstant.APP_GTJ)) {
            if (isCreatePage) {
                helper.setImageResource(R.id.imageView, item.isDeleteSelected ? R.mipmap.icon_multiple_blue : R.mipmap.icon_multiple0);
            } else {
                helper.setImageResource(R.id.imageView, item.isSelected ? R.mipmap.icon_multiple_blue : R.mipmap.icon_multiple0);
            }
        } else {
            if (isCreatePage) {
                helper.setImageResource(R.id.imageView, item.isDeleteSelected ? R.mipmap.icon_multiple1 : R.mipmap.icon_multiple0);
            } else {
                helper.setImageResource(R.id.imageView, item.isSelected ? R.mipmap.icon_multiple1 : R.mipmap.icon_multiple0);
            }
        }
        helper.addOnClickListener(R.id.textView);
        helper.addOnClickListener(R.id.imageView);
    }
}
