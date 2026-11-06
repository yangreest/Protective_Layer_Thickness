package com.example.gtj_sd_dg240.adapter;

import android.content.Context;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.model.EntrustBean;

public class EntrustListAdapter extends BaseQuickAdapter<EntrustBean, BaseViewHolder> {
    private final int colorFFF, color4CB4B9, color333;

    public EntrustListAdapter(Context context) {
        super(R.layout.item_project_list, null);
        colorFFF = ContextCompat.getColor(context, R.color.colorFFF);
        color4CB4B9 = ContextCompat.getColor(context, R.color.color006F5F);
        color333 = ContextCompat.getColor(context, R.color.color333);
    }

    @Override
    protected void convert(BaseViewHolder helper, EntrustBean item) {
        helper.setText(R.id.textView, item.entrust_code);
        helper.setTextColor(R.id.textView, item.isSelected ? colorFFF : color333);
        helper.setBackgroundColor(R.id.textView, item.isSelected ? color4CB4B9 : colorFFF);
        if (TextUtils.equals(StaticConstant.APP_DEFAULT, StaticConstant.APP_GTJ)) {
            helper.setImageResource(R.id.imageView, item.isSelected ? R.mipmap.icon_multiple_blue : R.mipmap.icon_multiple0);
        } else {
            helper.setImageResource(R.id.imageView, item.isSelected ? R.mipmap.icon_multiple1 : R.mipmap.icon_multiple0);
        }
        helper.addOnClickListener(R.id.textView);
        helper.addOnClickListener(R.id.imageView);
    }
}
