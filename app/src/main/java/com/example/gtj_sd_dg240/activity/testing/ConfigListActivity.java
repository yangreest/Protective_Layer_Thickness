package com.example.gtj_sd_dg240.activity.testing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.activity.start.BaseActivity;
import com.example.gtj_sd_dg240.adapter.ConfigListAdapter;
import com.example.gtj_sd_dg240.adapter.ConfigTypeListAdapter;
import com.example.gtj_sd_dg240.databinding.ActConfigListBinding;
import com.example.gtj_sd_dg240.functions.database.greenDao.db.ConfigBeanDao;
import com.example.gtj_sd_dg240.model.ConfigBean;
import com.example.gtj_sd_dg240.storage.GreenDaoHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置列表
 */
@SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
public class ConfigListActivity extends BaseActivity<ActConfigListBinding> {
    private ConfigTypeListAdapter typeAdapter;
    private ConfigListAdapter configAdapter;
    private ConfigBean configBean;

    @Override
    protected ActConfigListBinding getBinding() {
        return ActConfigListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.navigationBar.setTitleText("参数配置列表");
        initBatteryReceiver(binding.navigationBar);
        initBluetoothStatus(binding.navigationBar);
        configBean = (ConfigBean) getIntent().getSerializableExtra("configBean");

        binding.btnOk.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);

        initType();
        initConfigView();
    }

    private void onViewClicked(View view) {
        if (view.getId() == R.id.btn_ok) {
            if (configBean == null) {
                ToastUtils.showShort("请选择电杆配置");
                return;
            }
            setResult(RESULT_OK, new Intent().putExtra("configBean", configBean));
            finish();
        } else {
            finish();
        }
    }

    private void initType() {
        ArrayList<ConfigBean> typeList = new ArrayList<>();
        typeList.add(new ConfigBean(StaticConstant.poleType_PT, configBean != null && configBean.poleType.equals(StaticConstant.poleType_PT), false));
        typeList.add(new ConfigBean(StaticConstant.poleType_Y, configBean != null && configBean.poleType.equals(StaticConstant.poleType_Y), false));
        typeList.add(new ConfigBean(StaticConstant.poleType_BY, configBean != null && configBean.poleType.equals(StaticConstant.poleType_BY), false));
        binding.rvType.setLayoutManager(new LinearLayoutManager(this));
        typeAdapter = new ConfigTypeListAdapter(this, false);
        typeAdapter.bindToRecyclerView(binding.rvType);
        typeAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            for (int i = 0; i < adapter.getData().size(); i++) {
                typeAdapter.getData().get(i).isSelected = false;
            }
            ConfigBean bean = typeAdapter.getData().get(position);
            bean.isSelected = true;
            typeAdapter.notifyDataSetChanged();

            List<ConfigBean> configBeans = GreenDaoHelper.getDaoSession().getConfigBeanDao().queryBuilder().where(ConfigBeanDao.Properties.PoleType.eq(bean.poleType)).list();
            if (!configBeans.isEmpty()) {
                for (int i = 0; i < configBeans.size(); i++) {
                    if (configBean != null && configBean.poleName.equals(configBeans.get(i).poleName)) {
                        configBeans.get(i).isSelected = true;
                        refreshTextView();
                    } else {
                        configBeans.get(i).isSelected = false;
                    }
                }
            }
            configAdapter.setNewData(configBeans);
        });
        typeAdapter.setNewData(typeList);
    }

    private void initConfigView() {
        binding.rvConfig.setLayoutManager(new LinearLayoutManager(this));
        configAdapter = new ConfigListAdapter(this, false);
        configAdapter.bindToRecyclerView(binding.rvConfig);
        configAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            for (int i = 0; i < configAdapter.getData().size(); i++) {
                configAdapter.getData().get(i).isSelected = false;
            }
            configBean = configAdapter.getData().get(position);
            configAdapter.getData().get(position).isSelected = true;
            adapter.notifyDataSetChanged();

            refreshTextView();
        });
        if (configBean == null) {
            return;
        }
        List<ConfigBean> configBeanList = GreenDaoHelper.getDaoSession().getConfigBeanDao().queryBuilder().where(ConfigBeanDao.Properties.PoleType.eq(configBean.poleType)).list();
        if (!configBeanList.isEmpty()) {
            for (int i = 0; i < configBeanList.size(); i++) {
                if (configBean != null && configBean.poleName.equals(configBeanList.get(i).poleName)) {
                    configBeanList.get(i).isSelected = true;
                    refreshTextView();
                } else {
                    configBeanList.get(i).isSelected = false;
                }
            }
        }
        configAdapter.setNewData(configBeanList);
    }

    private void refreshTextView() {
        if (configBean == null) {
            binding.tvDetail.setText("");
            return;
        }
        StringBuilder sbd = new StringBuilder();
        sbd.append("配 置 名 称 ：").append(configBean.poleName)
                .append("\n电 杆 类 型 ：").append(configBean.poleType)
                .append("\n电 杆 结 构 ：").append(StaticConstant.configpoleStructureList.get(configBean.poleStructure))
                .append("\n电 杆 长 度 ：").append(configBean.poleHeight).append("m")
                .append("\n电 杆 根 径 ：").append(configBean.poleBottomDiameter).append("mm")
                .append("\n电 杆 梢 径 ：").append(configBean.poleTopDiameter).append("mm")
                .append("\n纵 筋 直 径 ：").append(configBean.rebarDiameter).append("mm")
                .append("\n螺旋筋直径：").append(configBean.hoopingDiameter).append("mm")
                .append("\n设计保护层厚度：").append(configBean.designThickness).append("（+8 -2）mm");
        binding.tvDetail.setText(sbd);
    }
}
