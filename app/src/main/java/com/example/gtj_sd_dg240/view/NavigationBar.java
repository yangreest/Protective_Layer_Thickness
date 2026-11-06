package com.example.gtj_sd_dg240.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.SizeUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.databinding.ViewTitleBinding;

public class NavigationBar extends RelativeLayout {
    ViewTitleBinding binding;

    public NavigationBar(Context context) {
        super(context);
        initView(context);
    }

    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        //parent：将当前View作为父容器，attachToParent：自动附加到父容器
        binding = ViewTitleBinding.inflate(LayoutInflater.from(context), this, true);
        binding.layoutLogo.setVisibility(StaticConstant.isNeutral ? GONE : VISIBLE);
        switch (StaticConstant.APP_DEFAULT) {
            case StaticConstant.APP_LIERDA:
                binding.ivLogoLrd.setVisibility(VISIBLE);
                binding.ivLogoZhi.setVisibility(GONE);
                binding.vCenter.setVisibility(GONE);
                binding.ivLogoGjdw.setVisibility(GONE);
                binding.ivLogoMshz.setVisibility(GONE);
                binding.ivLogoGtj.setVisibility(GONE);
                break;
            case StaticConstant.APP_DIANKEYUAN:
                binding.ivLogoLrd.setVisibility(GONE);
                binding.ivLogoZhi.setVisibility(VISIBLE);
                binding.vCenter.setVisibility(VISIBLE);
                binding.ivLogoGjdw.setVisibility(VISIBLE);
                binding.ivLogoMshz.setVisibility(GONE);
                binding.ivLogoGtj.setVisibility(GONE);
                break;
            case StaticConstant.APP_MINGSHENGHENGZHUO:
                binding.ivLogoLrd.setVisibility(VISIBLE);
                binding.ivLogoZhi.setVisibility(GONE);
                binding.vCenter.setVisibility(VISIBLE);
                binding.ivLogoGjdw.setVisibility(GONE);
                binding.ivLogoMshz.setVisibility(VISIBLE);
                binding.ivLogoGtj.setVisibility(GONE);
                break;
            case StaticConstant.APP_GTJ:
                binding.ivLogoLrd.setVisibility(GONE);
                binding.ivLogoZhi.setVisibility(GONE);
                binding.vCenter.setVisibility(GONE);
                binding.ivLogoGjdw.setVisibility(GONE);
                binding.ivLogoMshz.setVisibility(GONE);
                binding.ivLogoGtj.setVisibility(VISIBLE);
                break;

        }
    }

    public NavigationBar setTitleText(String string) {
        binding.tvNavTitle.setText(string);
        return this;
    }

    public NavigationBar setProjectName(String string) {
        binding.tvNProject.setText(string);
        binding.layoutNProject.setVisibility(VISIBLE);
        return this;
    }

    public NavigationBar setZoneName(String string) {
        binding.tvNZone.setText(string);
        binding.layoutNZone.setVisibility(VISIBLE);
        return this;
    }

    public NavigationBar setIvBluetooth(boolean isShow) {
        binding.ivBluetooth.setVisibility(isShow ? VISIBLE : GONE);
        return this;
    }

    public void setivBattery(boolean flag) {
        binding.ivBattery.setVisibility(VISIBLE);
        binding.tvBattery.setVisibility(VISIBLE);
        binding.ivBattery.setImageResource(flag ? R.mipmap.icon_battery1 : R.mipmap.icon_battery);
        binding.tvBattery.setVisibility(flag ? GONE : VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    public void setBatteryLevel(int rawlevel, int scale) {
        binding.ivBatteryLevel.setVisibility(VISIBLE);
        int labelLevel = 1;
        float level = 0.1f;
        if (rawlevel >= 0 && scale > 0) {
            labelLevel = (rawlevel * 100) / scale;
            level = rawlevel * 1.0f / scale;
        }
        binding.tvBattery.setText(labelLevel + "");
        //45dp
        binding.ivBatteryLevel.getLayoutParams().width = (int) (SizeUtils.dp2px(45) * level);
        binding.ivBatteryLevel.setLayoutParams(binding.ivBatteryLevel.getLayoutParams());
    }
}
