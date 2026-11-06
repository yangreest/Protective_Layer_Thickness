package com.example.gtj_sd_dg240.activity.about;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.blankj.utilcode.util.SPUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.activity.setting.CalibrationDeviceActivity;
import com.example.gtj_sd_dg240.activity.setting.MeteringDeviceActivity;
import com.example.gtj_sd_dg240.activity.setting.ScanningBlueboothActivity;
import com.example.gtj_sd_dg240.activity.start.BaseActivity;
import com.example.gtj_sd_dg240.databinding.ActAboutUsBinding;
import com.example.gtj_sd_dg240.view.DialogLogin;

@SuppressLint("SetTextI18n")
public class AboutUsActivity extends BaseActivity<ActAboutUsBinding> {
    @Override
    protected ActAboutUsBinding getBinding() {
        return ActAboutUsBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.navigationBar.setTitleText("系统设置");
        binding.ivThemeSelected.setImageResource(TextUtils.equals(StaticConstant.APP_DEFAULT, StaticConstant.APP_GTJ) ? R.mipmap.icon_single_selected_blue : R.mipmap.icon_single_selected);
        initBatteryReceiver(binding.navigationBar);
        binding.btnCancel.setOnClickListener(this::onViewClicked);
        binding.btnConnect.setOnClickListener(this::onViewClicked);
        binding.btnCalibration.setOnClickListener(this::onViewClicked);
        binding.btnMetering.setOnClickListener(this::onViewClicked);
        binding.tvTime.setText("202604141318");
        if (StaticConstant.isNeutral) {
            binding.layoutAboutUs.setVisibility(View.GONE);
        } else {
            switch (StaticConstant.APP_DEFAULT) {
                case StaticConstant.APP_LIERDA:
                case StaticConstant.APP_MINGSHENGHENGZHUO:
                    binding.layoutAboutUs.setVisibility(View.VISIBLE);
                    break;
                case StaticConstant.APP_DIANKEYUAN:
                    binding.layoutAboutUs.setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.tvDevice.setText(SPUtils.getInstance().getString("bluetooth-name"));
    }

    public void onViewClicked(View view) {
        Intent intent;
        if (view.getId() == R.id.btn_connect) {
            intent = new Intent(this, ScanningBlueboothActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.btn_calibration) {
            DialogLogin dialogLogin = new DialogLogin(this);
            dialogLogin.setDialogListener((flag, type) -> {
                if (flag) {
                    Intent it = new Intent(this, CalibrationDeviceActivity.class);
                    startActivity(it);
                }
            }).show();
        } else if (view.getId() == R.id.btn_metering) {
            Intent it = new Intent(this, MeteringDeviceActivity.class);
            startActivity(it);
        } else if (view.getId() == R.id.btn_cancel) {
            finish();
        }
    }
}
