package com.example.gtj_sd_dg240.activity.testing;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.activity.start.BaseActivity;
import com.example.gtj_sd_dg240.adapter.ConfigListAdapter;
import com.example.gtj_sd_dg240.adapter.ConfigTypeListAdapter;
import com.example.gtj_sd_dg240.databinding.ActConfigManagerBinding;
import com.example.gtj_sd_dg240.functions.database.greenDao.db.ConfigBeanDao;
import com.example.gtj_sd_dg240.model.ConfigBean;
import com.example.gtj_sd_dg240.storage.GreenDaoHelper;
import com.example.gtj_sd_dg240.utils.SpinnerUtils;
import com.example.gtj_sd_dg240.utils.StringUtils;
import com.example.gtj_sd_dg240.view.StyleAlertDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 电杆配置管理
 */
@SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
public class ConfigManagerActivity extends BaseActivity<ActConfigManagerBinding> {
    private ConfigTypeListAdapter typeAdapter;
    private ConfigListAdapter configAdapter;
    private ConfigBean configBean;
    private int typeShowIndex = 0;
    private int deleteNum = 0;
    private ArrayList<ConfigBean> typeList;
    private List<ConfigBean> configList0;
    private List<ConfigBean> configList1;
    private List<ConfigBean> configList2;

    @Override
    protected ActConfigManagerBinding getBinding() {
        return ActConfigManagerBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.navigationBar.setTitleText("参数配置管理");
        initBatteryReceiver(binding.navigationBar);
        initBluetoothStatus(binding.navigationBar);

        initView();
        initRecyclerData();
        initTypeView();
        initConfigView();
    }

    private void initView() {
        SpinnerUtils.attachDataSource(binding.spType1, StaticConstant.configPoleTypeList, null);  //电杆类型
        SpinnerUtils.attachDataSource(binding.spPoleStructure, StaticConstant.configpoleStructureList, null);  //电杆结构
        SpinnerUtils.attachDataSource(binding.spRebarDiameter, StaticConstant.rebarDiameterList, null);  //钢筋直径
        SpinnerUtils.attachDataSource(binding.spHoopingDiameter, StaticConstant.hoopingDiameterList, null);  //螺旋筋直径
        binding.btnCreate.setOnClickListener(this::onViewClicked);
        binding.btnDelete.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);
    }

    private void initRecyclerData() {
        typeList = new ArrayList<>();
        typeList.add(new ConfigBean(StaticConstant.poleType_PT, true, false));
        typeList.add(new ConfigBean(StaticConstant.poleType_Y, false, false));
        typeList.add(new ConfigBean(StaticConstant.poleType_BY, false, false));

        configList0 = GreenDaoHelper.getDaoSession().getConfigBeanDao().queryBuilder().where(ConfigBeanDao.Properties.PoleType.eq(StaticConstant.poleType_PT)).list();
        for (ConfigBean bean : configList0) {
            bean.isDeleteSelected = false;
        }
        configList1 = GreenDaoHelper.getDaoSession().getConfigBeanDao().queryBuilder().where(ConfigBeanDao.Properties.PoleType.eq(StaticConstant.poleType_Y)).list();
        for (ConfigBean bean : configList1) {
            bean.isDeleteSelected = false;
        }
        configList2 = GreenDaoHelper.getDaoSession().getConfigBeanDao().queryBuilder().where(ConfigBeanDao.Properties.PoleType.eq(StaticConstant.poleType_BY)).list();
        for (ConfigBean bean : configList2) {
            bean.isDeleteSelected = false;
        }
    }

    private void initTypeView() {
        binding.rvType.setLayoutManager(new LinearLayoutManager(this));
        typeAdapter = new ConfigTypeListAdapter(this, true);
        typeAdapter.bindToRecyclerView(binding.rvType);
        typeAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.imageView) {
                ConfigBean bean = typeAdapter.getData().get(position);
                bean.isDeleteSelected = !bean.isDeleteSelected;
                switch (bean.poleType) {
                    case StaticConstant.poleType_PT:
                        for (int i = 0; i < configList0.size(); i++) {
                            configList0.get(i).isDeleteSelected = bean.isDeleteSelected;
                        }
                        break;
                    case StaticConstant.poleType_Y:
                        for (int i = 0; i < configList1.size(); i++) {
                            configList1.get(i).isDeleteSelected = bean.isDeleteSelected;
                        }
                        break;
                    case StaticConstant.poleType_BY:
                        for (int i = 0; i < configList2.size(); i++) {
                            configList2.get(i).isDeleteSelected = bean.isDeleteSelected;
                            configList2.get(i).isSelected = configBean != null && configBean.poleName.equals(configList2.get(i).poleName);
                        }
                        break;
                }
                if (typeShowIndex == position) {
                    configAdapter.notifyDataSetChanged();
                }
                deleteNum = 0;
                for (int i = 0; i < configList0.size(); i++) {
                    if (configList0.get(i).isDeleteSelected) {
                        ++deleteNum;
                    }
                }
                for (int i = 0; i < configList1.size(); i++) {
                    if (configList1.get(i).isDeleteSelected) {
                        ++deleteNum;
                    }
                }
                for (int i = 0; i < configList2.size(); i++) {
                    if (configList2.get(i).isDeleteSelected) {
                        ++deleteNum;
                    }
                }
                binding.btnDelete.setText("删　　除（ " + deleteNum + " ）");
            } else if (view.getId() == R.id.textView) {
                for (ConfigBean bean : typeAdapter.getData()) {
                    bean.isSelected = false;
                }
                typeAdapter.getData().get(position).isSelected = true;
                switch (typeAdapter.getData().get(position).poleType) {
                    case StaticConstant.poleType_PT:
                        for (ConfigBean bean : configList0) {
                            bean.isSelected = configBean != null && configBean.poleName.equals(bean.poleName);
                        }
                        configAdapter.setNewData(configList0);
                        break;
                    case StaticConstant.poleType_Y:
                        for (ConfigBean bean : configList1) {
                            bean.isSelected = configBean != null && configBean.poleName.equals(bean.poleName);
                        }
                        configAdapter.setNewData(configList1);
                        break;
                    case StaticConstant.poleType_BY:
                        for (ConfigBean bean : configList2) {
                            bean.isSelected = configBean != null && configBean.poleName.equals(bean.poleName);
                        }
                        configAdapter.setNewData(configList2);
                        break;
                }
                typeShowIndex = position;
            }
            typeAdapter.notifyDataSetChanged();
        });
        typeAdapter.setNewData(typeList);
    }

    private void initConfigView() {
        binding.rvConfig.setLayoutManager(new LinearLayoutManager(this));
        configAdapter = new ConfigListAdapter(this, true);
        configAdapter.bindToRecyclerView(binding.rvConfig);
        configAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.imageView) {
                configAdapter.getData().get(position).isDeleteSelected = !configAdapter.getData().get(position).isDeleteSelected;
                if (configAdapter.getData().get(position).isDeleteSelected) {
                    deleteNum += 1;
                } else {
                    deleteNum -= 1;
                }
                binding.btnDelete.setText("删　　除（ " + deleteNum + " ）");
                boolean isAllSelected = true;
                for (int i = 0; i < configAdapter.getData().size(); i++) {
                    if (!configAdapter.getData().get(i).isDeleteSelected) {
                        isAllSelected = false;
                        break;
                    }
                }
                typeAdapter.getData().get(typeShowIndex).isDeleteSelected = isAllSelected;
                typeAdapter.notifyDataSetChanged();
            } else if (view.getId() == R.id.textView) {
                for (ConfigBean bean : configAdapter.getData()) {
                    bean.isSelected = false;
                }
                configBean = configAdapter.getData().get(position);
                configBean.isSelected = true;
                setInfo(configBean);
            }
            adapter.notifyDataSetChanged();
        });
        configAdapter.setNewData(configList0);
    }

    private void onViewClicked(View view) {
        if (view.getId() == R.id.btn_create) {
            create();
        } else if (view.getId() == R.id.btn_return) {
            finish();
        } else if (view.getId() == R.id.btn_delete) {
            delete();
        }
    }

    private void create() {
        String strPoleName = binding.etPoleName.getText().toString().trim();
        String strPoleHeight = binding.etPoleHeight.getText().toString().trim();
        String strPoleBottomDiameter = binding.etPoleBottomDiameter.getText().toString().trim();
        String strPoleTopDiameter = binding.etPoleTopDiameter.getText().toString().trim();
        String strPoleType = binding.spType1.getSelectedItem() + "";
        String strPoleStructure = binding.spPoleStructure.getSelectedItem() + "";
        String strRebarDiameter = binding.spRebarDiameter.getSelectedItem() + "";
        String strHoopingDiameter = binding.spHoopingDiameter.getSelectedItem() + "";
        String strDesignThickness = binding.etDesignThickness.getText().toString().trim();
        if (TextUtils.isEmpty(strPoleName) || TextUtils.isEmpty(strPoleHeight) || TextUtils.isEmpty(strPoleBottomDiameter) || TextUtils.isEmpty(strPoleTopDiameter) || TextUtils.isEmpty(strPoleType) || TextUtils.isEmpty(strPoleStructure) || TextUtils.isEmpty(strRebarDiameter) || TextUtils.isEmpty(strHoopingDiameter) || TextUtils.isEmpty(strDesignThickness)) {
            ToastUtils.showShort("请填写完整");
            return;
        }
        try {
            double thickness = Double.parseDouble(strDesignThickness);
            if (thickness <= 0) {
                ToastUtils.showShort("设计保护层厚度需 ≥0mm");
            }
        } catch (NumberFormatException e) {
            ToastUtils.showShort("请填写完整");
            return;
        }

        List<ConfigBean> configBeanList = GreenDaoHelper.getDaoSession().getConfigBeanDao().queryBuilder().where(ConfigBeanDao.Properties.PoleName.eq(strPoleName)).list();
        if (!configBeanList.isEmpty()) {
            ToastUtils.showShort("配置名称不可重复");
            return;
        }

        ConfigBean configBean = new ConfigBean();
        configBean.poleType = strPoleType;
        configBean.poleStructure = binding.spPoleStructure.getSelectedItemPosition();
        configBean.poleName = strPoleName;
        configBean.poleHeight = strPoleHeight;
        configBean.poleBottomDiameter = Integer.parseInt(strPoleBottomDiameter);
        configBean.poleTopDiameter = Integer.parseInt(strPoleTopDiameter);
        configBean.rebarDiameter = Integer.parseInt(strRebarDiameter);
        configBean.hoopingDiameter = Integer.parseInt(strHoopingDiameter);
        configBean.designThickness = strDesignThickness;
        GreenDaoHelper.getDaoSession().getConfigBeanDao().insert(configBean);

        switch (strPoleType) {
            case StaticConstant.poleType_PT:
                configList0.add(configBean);
                if (typeList.get(0).isDeleteSelected) {
                    typeList.get(0).isDeleteSelected = false;
                    typeAdapter.notifyDataSetChanged();
                }
                break;
            case StaticConstant.poleType_Y:
                configList1.add(configBean);
                if (typeList.get(1).isDeleteSelected) {
                    typeList.get(1).isDeleteSelected = false;
                    typeAdapter.notifyDataSetChanged();
                }
                break;
            case StaticConstant.poleType_BY:
                configList2.add(configBean);
                if (typeList.get(2).isDeleteSelected) {
                    typeList.get(2).isDeleteSelected = false;
                    typeAdapter.notifyDataSetChanged();
                }
                break;
        }

        if (!typeAdapter.getData().get(typeShowIndex).poleType.equals(strPoleType)) {
            ToastUtils.showShort("创建成功");
            return;
        }
        configAdapter.notifyDataSetChanged();
        ToastUtils.showShort("创建成功");
    }

    private void delete() {
        if (deleteNum == 0) {
            ToastUtils.showShort("请选择需要删除的配置");
            return;
        }
        StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this, "red");
        styleAlertDialog.setContent("确定删除选中配置数据？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
            styleAlertDialog.dismiss();
            for (int i = configList0.size() - 1; i >= 0; i--) {
                ConfigBean bean = configList0.get(i);
                if (!StaticConstant.configList.contains(bean.poleName) && bean.isDeleteSelected) {
                    GreenDaoHelper.getDaoSession().getConfigBeanDao().delete(bean);
                    configList0.remove(bean);
                } else {
                    bean.isDeleteSelected = false;
                }
                typeAdapter.getData().get(0).isDeleteSelected = false;
            }
            for (int i = configList1.size() - 1; i >= 0; i--) {
                ConfigBean bean = configList1.get(i);
                if (!StaticConstant.configList.contains(bean.poleName) && bean.isDeleteSelected) {
                    GreenDaoHelper.getDaoSession().getConfigBeanDao().delete(bean);
                    configList1.remove(bean);
                } else {
                    bean.isDeleteSelected = false;
                }
                typeAdapter.getData().get(1).isDeleteSelected = false;
            }
            for (int i = configList2.size() - 1; i >= 0; i--) {
                ConfigBean bean = configList2.get(i);
                if (!StaticConstant.configList.contains(bean.poleName) && bean.isDeleteSelected) {
                    GreenDaoHelper.getDaoSession().getConfigBeanDao().delete(bean);
                    configList2.remove(bean);
                } else {
                    bean.isDeleteSelected = false;
                }
                typeAdapter.getData().get(2).isDeleteSelected = false;
            }
            deleteNum = 0;
            binding.btnDelete.setText("删　　除（ " + deleteNum + " ）");
            typeAdapter.notifyDataSetChanged();
            configAdapter.notifyDataSetChanged();
        }).show();
    }

    private void setInfo(ConfigBean configBean) {
        binding.spType1.setSelection(StringUtils.getPoleTypeIndex(configBean.poleType));
        binding.spPoleStructure.setSelection(configBean.poleStructure);
        binding.etPoleName.setText(configBean.poleName);
        binding.etPoleHeight.setText(configBean.poleHeight);
        binding.etPoleBottomDiameter.setText(configBean.poleBottomDiameter + "");
        binding.etPoleTopDiameter.setText(configBean.poleTopDiameter + "");
        binding.spRebarDiameter.setSelection(StaticConstant.rebarDiameterList.indexOf(configBean.rebarDiameter + ""));
        binding.spHoopingDiameter.setSelection(StaticConstant.hoopingDiameterList.indexOf(configBean.hoopingDiameter + ""));
        binding.etDesignThickness.setText(configBean.designThickness);
    }
}
