package com.example.gtj_sd_dg240.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.databinding.ProgressViewBinding;

public class ProgressiveDialog extends Dialog {
    private String contentString;
    private com.example.gtj_sd_dg240.databinding.ProgressViewBinding binding;

    @SuppressLint("InflateParams")
    public ProgressiveDialog(Context context) {
        super(context, R.style.ProgressiveDialog);
        setCanceledOnTouchOutside(false);
        this.setCancelable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ProgressViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    public void show() {
        super.show();
        if (!TextUtils.isEmpty(contentString)) {
            binding.tvContent.setText(contentString);
        }
    }

    public ProgressiveDialog setTvContent(String string) {
        contentString = string;
        return this;
    }
}
