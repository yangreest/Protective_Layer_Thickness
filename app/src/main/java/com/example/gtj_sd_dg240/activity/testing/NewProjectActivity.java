package com.example.gtj_sd_dg240.activity.testing;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.databinding.ActNewProjectBinding;
import com.example.gtj_sd_dg240.model.ProjectBean;
import com.example.gtj_sd_dg240.storage.GreenDaoHelper;
import com.example.gtj_sd_dg240.utils.OtherUtils;

import androidx.annotation.Nullable;


public class NewProjectActivity extends Activity {
    private ActNewProjectBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActNewProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        binding.editText.post(() -> OtherUtils.showSoftKeyboard(binding.editText));
        binding.btnCancel.setOnClickListener(this::onViewClicked);
        binding.button.setOnClickListener(this::onViewClicked);
    }

    public void onViewClicked(View view) {
        if (view.getId() == R.id.btn_cancel) {
            finish();
        } else if (view.getId() == R.id.button) {
            createProject();
        }
    }

    private void createProject() {
        String strProjectName = binding.editText.getText().toString().trim();
        if (strProjectName.isEmpty()) {
            ToastUtils.showShort("请输入工程名称");
            return;
        }
        boolean isExist = false;
        for (ProjectBean bean : StaticConstant.projectBeanList) {
            if (TextUtils.equals(bean.name.toLowerCase(), strProjectName.toLowerCase())) {
                isExist = true;
                break;
            }
        }
        if (isExist) {
            ToastUtils.showShort("已存在相同的工程名称，请修改");
            return;
        }
        GreenDaoHelper.getDaoSession().getProjectBeanDao().insert(new ProjectBean(strProjectName));
        ToastUtils.showShort("创建成功");
        setResult(RESULT_OK);
        finish();
    }
}
