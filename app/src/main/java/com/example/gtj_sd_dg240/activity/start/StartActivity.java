package com.example.gtj_sd_dg240.activity.start;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.databinding.ActStartBinding;

public class StartActivity extends BaseActivity<ActStartBinding> {
    private Handler timeHandler;
    private int time;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    protected ActStartBinding getBinding() {
        return ActStartBinding.inflate(getLayoutInflater());
    }

    private void initView() {
        switch (StaticConstant.APP_DEFAULT) {
            case StaticConstant.APP_LIERDA -> binding.imageView.setImageResource(R.mipmap.bg_start);
            case StaticConstant.APP_DIANKEYUAN ->
                    binding.imageView.setImageResource(R.mipmap.bg_start1);
            case StaticConstant.APP_MINGSHENGHENGZHUO ->
                    binding.imageView.setImageResource(R.mipmap.bg_start2);
        }
        timeHandler = new Handler(Looper.getMainLooper());
        sendHandlerTime();  //跳转
    }

    private void sendHandlerTime() {
        if (time >= 8) {
            finish();
        } else {
            time++;
            timeHandler.postDelayed(this::sendHandlerTime, 1000);
        }
    }
}
