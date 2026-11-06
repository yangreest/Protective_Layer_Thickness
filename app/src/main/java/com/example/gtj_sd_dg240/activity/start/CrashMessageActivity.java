package com.example.gtj_sd_dg240.activity.start;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.gtj_sd_dg240.databinding.ActCrashMessageBinding;

public class CrashMessageActivity extends BaseActivity<ActCrashMessageBinding> {
    @Override
    protected ActCrashMessageBinding getBinding() {
        return ActCrashMessageBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String string = getIntent().getStringExtra("msg");
        binding.tvMsg.setText(string);
    }
}
