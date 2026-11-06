package com.example.gtj_sd_dg240.activity.testing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.activity.start.BaseActivity;
import com.example.gtj_sd_dg240.databinding.ActDetectConfigBinding;
import com.example.gtj_sd_dg240.functions.database.greenDao.db.ZoneBeanDao;
import com.example.gtj_sd_dg240.model.ConfigBean;
import com.example.gtj_sd_dg240.model.EntrustBean;
import com.example.gtj_sd_dg240.model.ProjectBean;
import com.example.gtj_sd_dg240.model.ZoneBean;
import com.example.gtj_sd_dg240.storage.GreenDaoHelper;
import com.example.gtj_sd_dg240.utils.OtherUtils;
import com.example.gtj_sd_dg240.utils.SpinnerUtils;
import com.example.gtj_sd_dg240.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("SetTextI18n")
public class DetectConfigActivity extends BaseActivity<ActDetectConfigBinding> {
    private ZoneBean zoneBean;
    private EntrustBean entrustBean;

    private final int REQUEST_CODE_PROJECT = 1;
    private final int REQUEST_CODE_ENTRUST = 2;
    private final int REQUEST_CODE_CONFIG = 3;
    private ConfigBean configBean;

    @Override
    protected ActDetectConfigBinding getBinding() {
        return ActDetectConfigBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.navigationBar.setTitleText("参数设置");
        initBatteryReceiver(binding.navigationBar);
        initBluetoothStatus(binding.navigationBar);
        configBean = (ConfigBean) getIntent().getSerializableExtra("ConfigBean");
        setHideKeyboard();
        binding.tvEntrust.setOnClickListener(this::onViewClicked);
        binding.tvConfig.setOnClickListener(this::onViewClicked);
        binding.tvNew.setOnClickListener(this::onViewClicked);
        binding.tvJia.setOnClickListener(this::onViewClicked);
        binding.tvJian.setOnClickListener(this::onViewClicked);
        binding.btnCancel.setOnClickListener(this::onViewClicked);
        binding.btnOk.setOnClickListener(this::onViewClicked);
        binding.tvManager.setOnClickListener(this::onViewClicked);
        attachDataSource();
        addTextChangedListener();
        attachDataSourceProject(false);
        initData();
    }

    private void initData() {
        zoneBean = (ZoneBean) getIntent().getSerializableExtra("ZoneBean");
        if (zoneBean != null) {
            setZoneBeanView(false);
        } else {
            String strZoneBean = SPUtils.getInstance().getString("last-config-ZoneBean");
            List<ZoneBean> zoneBeanList = new ArrayList<>();
            if (!TextUtils.isEmpty(strZoneBean)) {
                zoneBean = new Gson().fromJson(strZoneBean, new TypeToken<ZoneBean>() {
                }.getType());
                zoneBean.id = null;
                zoneBean.rebarPageList = new ArrayList<>();
                zoneBeanList = GreenDaoHelper.getDaoSession().getZoneBeanDao().queryBuilder().where(ZoneBeanDao.Properties.ProjectId.eq(zoneBean.projectId)).orderDesc(ZoneBeanDao.Properties.Id).offset(0).limit(1).list();
            }
            if (!zoneBeanList.isEmpty()) {
                zoneBean.poleNo = zoneBeanList.get(0).poleNo;
                setZoneBeanView(true);
            } else {
                setZoneBeanView(false);
            }
        }

    }

    private void setZoneBeanView(boolean isJia) {
        if (zoneBean != null) {
            //工程信息
            if (!TextUtils.isEmpty(zoneBean.projectName) && StaticConstant.projectBeanList.contains(new ProjectBean(zoneBean.projectName))) {
                binding.spProject.setDefaultContent(zoneBean.projectName);  //工程名称
            }
            binding.etPoleNo.setText(zoneBean.poleNo);  //电杆编号
            if (isJia) {
                setPoleNo(true);
            }
            if (!TextUtils.isEmpty(zoneBean.entrustId)) {
                binding.tvEntrust.setText(zoneBean.entrustCode);
                entrustBean = new EntrustBean();
                entrustBean.id = zoneBean.getEntrustId();
                entrustBean.entrust_code = zoneBean.getEntrustCode();
            }
            binding.tvPoleType.setText(StaticConstant.configPoleTypeList.get(zoneBean.poleType));  //电杆类型
            binding.spPoleStructure.setSelection(zoneBean.poleStructure);  //电杆结构
            binding.spPoleStandard.setSelection(zoneBean.poleStandard);  //规范
            //试验参数
            binding.etPoleHeight.setText(zoneBean.poleHeight);  //电杆长度
            binding.etPoleBottomDiameter.setText(zoneBean.poleBottomDiameter + "");  //电杆根径
            binding.etPoleTopDiameter.setText(zoneBean.poleTopDiameter + "");  //电杆梢径
            binding.spRebarDiameter.setSelection(zoneBean.rebarDiameter);  //纵筋直径
            binding.spHoopingDiameter.setSelection(getDiameterIndex(zoneBean.hoopingDiameter));  //螺旋筋直径
            binding.tvDesignThickness.setText(zoneBean.designThickness);
        } else {
            zoneBean = new ZoneBean();
            zoneBean.poleNo = "";
            zoneBean.rebarPageList = new ArrayList<>();
        }
        setImageView(binding.spPoleStructure.getSelectedItemPosition());
    }

    private int getDiameterIndex(int diameter) {
        for (int i = 0; i < StaticConstant.hoopingDiameterList.size(); i++) {
            if (diameter == Integer.parseInt(StaticConstant.hoopingDiameterList.get(i))) {
                return i;
            }
        }
        return 0;
    }

    private void attachDataSource() {
        SpinnerUtils.attachDataSource(binding.spPoleStructure, StaticConstant.configpoleStructureList, (spinner, text, position) -> setImageView(position));  //电杆结构
        SpinnerUtils.attachDataSource(binding.spPoleStandard, StaticConstant.poleStandardList, null);  //规范
        SpinnerUtils.attachDataSource(binding.spRebarDiameter, StaticConstant.rebarDiameterList, null);  //钢筋直径
        SpinnerUtils.attachDataSource(binding.spHoopingDiameter, StaticConstant.hoopingDiameterList, null);  //螺旋筋直径
    }

    private void attachDataSourceProject(boolean isLast) {
        StaticConstant.projectBeanList = GreenDaoHelper.getDaoSession().getProjectBeanDao().loadAll();
        List<String> projectList = new ArrayList<>();
        for (ProjectBean bean : StaticConstant.projectBeanList) {
            projectList.add(bean.name);
        }
        SpinnerUtils.attachDataSource(binding.spProject, projectList, null);
        if (isLast) {
            binding.spProject.setDefaultContent(projectList.get(projectList.size() - 1));  //工程名称
        }
    }

    private void addTextChangedListener() {
        binding.etPoleBottomDiameter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String strPoleType = binding.spPoleStructure.getSelectedItem() + "";
                if (TextUtils.equals(strPoleType, "等径杆")) {
                    binding.etPoleTopDiameter.setText(s.toString());
                }
            }
        });
    }

    private void setImageView(int index) {
        switch (index) {
            case 0:
                binding.imageView.setImageResource(R.mipmap.icon_pole_zx);
                binding.etPoleTopDiameter.setBackgroundResource(R.drawable.bg_edittext);
                binding.etPoleTopDiameter.setEnabled(true);
                break;
            case 1:
                binding.imageView.setImageResource(R.mipmap.icon_pole_flzx);
                binding.etPoleTopDiameter.setBackgroundResource(R.drawable.bg_edittext);
                binding.etPoleTopDiameter.setEnabled(true);
                break;
            case 2:
                binding.imageView.setImageResource(R.mipmap.icon_pole_dj);
                binding.etPoleTopDiameter.setBackgroundResource(R.drawable.bg_edittext_gray);
                binding.etPoleTopDiameter.setEnabled(false);
                binding.etPoleTopDiameter.setText(binding.etPoleBottomDiameter.getText().toString().trim());
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_PROJECT) {
            attachDataSourceProject(true);
        } else if (requestCode == REQUEST_CODE_ENTRUST && data != null) {
            entrustBean = (EntrustBean) data.getSerializableExtra("entrustBean");
            binding.tvEntrust.setText(entrustBean == null ? "" : entrustBean.entrust_code);
        } else if (requestCode == REQUEST_CODE_CONFIG && data != null) {
            configBean = (ConfigBean) data.getSerializableExtra("configBean");
            if (configBean == null) {
                return;
            }
            binding.tvConfig.setText(configBean.poleName);
            binding.tvPoleType.setText(configBean.poleType);
            binding.spPoleStructure.setSelection(configBean.poleStructure);  //电杆结构
            binding.etPoleHeight.setText(configBean.poleHeight);
            binding.tvDesignThickness.setText(configBean.designThickness);
            binding.etPoleBottomDiameter.setText(configBean.poleBottomDiameter + "");
            binding.etPoleTopDiameter.setText(configBean.poleTopDiameter + "");
            binding.spRebarDiameter.setSelection(StaticConstant.rebarDiameterList.indexOf(configBean.rebarDiameter + ""));
            binding.spHoopingDiameter.setSelection(StaticConstant.hoopingDiameterList.indexOf(configBean.hoopingDiameter + ""));
            setImageView(binding.spPoleStructure.getSelectedItemPosition());
        }
    }

    public void onViewClicked(View view) {
        OtherUtils.hideKeyboard(this);
        if (view.getId() == R.id.tv_entrust) {
            Intent intent = new Intent(this, EntrustListActivity.class);
            intent.putExtra("entrustBean", entrustBean);
            startActivityForResult(intent, REQUEST_CODE_ENTRUST);
        } else if (view.getId() == R.id.tv_config) {
            Intent intent = new Intent(this, ConfigListActivity.class);
            intent.putExtra("configBean", configBean);
            startActivityForResult(intent, REQUEST_CODE_CONFIG);
        } else if (view.getId() == R.id.tv_new) {
            Intent intent = new Intent(this, NewProjectActivity.class);
            startActivityForResult(intent, REQUEST_CODE_PROJECT);
        } else if (view.getId() == R.id.tv_jia) {
            setPoleNo(true);
        } else if (view.getId() == R.id.tv_jian) {
            setPoleNo(false);
        } else if (view.getId() == R.id.btn_cancel) {
            finish();
        } else if (view.getId() == R.id.btn_ok) {
            if (saveData()) {
                progressiveDialog.dismiss();
                setResult(RESULT_OK, new Intent().putExtra("ZoneBean", zoneBean));
                finish();
            }
        } else if (view.getId() == R.id.tv_manager) {
            Intent intent = new Intent(this, ConfigManagerActivity.class);
            startActivityForResult(intent, 3);
        }
    }

    private void setPoleNo(boolean isJia) {
        String strPoleNo = binding.etPoleNo.getText().toString().trim();
        if (TextUtils.isEmpty(strPoleNo)) {
            return;
        }
        int index = StringUtils.getNumberIndex(strPoleNo);
        if (index != -1) {
            String str1 = strPoleNo.substring(0, index);
            String str2 = strPoleNo.substring(index);
            int number = Integer.parseInt(str2);
            if (isJia) {
                number++;
            } else {
                if (number > 0) {
                    number--;
                }
            }
            binding.etPoleNo.setText(str1 + number);
        }
    }

    private boolean saveData() {
        String strProject = binding.spProject.getSelectedItem() + "";
        String strPoleNo = binding.etPoleNo.getText().toString().trim();
        String strPoleType = binding.tvPoleType.getText().toString();
        String strPoleStructure = binding.spPoleStructure.getSelectedItem() + "";
        String strPoleStandard = binding.spPoleStandard.getSelectedItem() + "";
        //
        String strPoleHeight = binding.etPoleHeight.getText().toString().trim();
        String strDesignThickness = binding.tvDesignThickness.getText().toString().trim();
        String strPoleBottomDiameter = binding.etPoleBottomDiameter.getText().toString().trim();
        String strPoleTopDiameter = binding.etPoleTopDiameter.getText().toString().trim();
        String strRebarDiameter = binding.spRebarDiameter.getSelectedItem() + "";
        String strHoopingDiameter = binding.spHoopingDiameter.getSelectedItem() + "";
        if (TextUtils.isEmpty(strProject) || TextUtils.isEmpty(strPoleNo) || TextUtils.isEmpty(strPoleType) || TextUtils.isEmpty(strPoleStructure) || TextUtils.isEmpty(strPoleStandard) || TextUtils.isEmpty(strPoleHeight) || TextUtils.isEmpty(strDesignThickness) || TextUtils.isEmpty(strPoleBottomDiameter) || TextUtils.isEmpty(strPoleTopDiameter) || TextUtils.isEmpty(strRebarDiameter) || TextUtils.isEmpty(strHoopingDiameter)) {
            ToastUtils.showShort("请填写完整");
            return false;
        }
        if (StaticConstant.projectBeanList.isEmpty() || binding.spProject.getSelectedItemPosition() == -1) {
            ToastUtils.showShort("请填写完整");
            return false;
        }
        ProjectBean projectBean = StaticConstant.projectBeanList.get(binding.spProject.getSelectedItemPosition());
        if (projectBean == null) {
            return false;
        }
        //构件编号不能重复，查重
        List<ZoneBean> zoneBeanList = GreenDaoHelper.getDaoSession().getZoneBeanDao().queryBuilder().where(ZoneBeanDao.Properties.ProjectId.eq(projectBean.id + "")).list();
        boolean isExist = false;
        for (ZoneBean bean : zoneBeanList) {
            if (TextUtils.equals(bean.poleNo, strPoleNo)) {
                isExist = true;
                break;
            }
        }
        if (isExist) {
            ToastUtils.showShort("此工程下已有电杆编号为“" + strPoleNo + "”的电杆数据，请修改电杆编号");
            return false;
        }
        progressiveDialog.show();
        //下一 电杆
        if (!TextUtils.equals(zoneBean.poleNo, strPoleNo) || !TextUtils.equals(zoneBean.projectId, projectBean.id + "")) {
            int rebarPageListSize = zoneBean.rebarPageList.size();
            if (rebarPageListSize > 0) {
                int lastBeanListSize = zoneBean.rebarPageList.get(zoneBean.rebarPageList.size() - 1).beanList.size();
                if (lastBeanListSize == 0) {
                    rebarPageListSize -= 1;
                }
            }
            if (rebarPageListSize > 0) {
                GreenDaoHelper.getDaoSession().getZoneBeanDao().insertOrReplace(zoneBean);
            }
            String strClone = new Gson().toJson(zoneBean);
            zoneBean = new Gson().fromJson(strClone, new TypeToken<ZoneBean>() {
            }.getType());
            zoneBean.id = null;
            zoneBean.rebarPageList = new ArrayList<>();
        }
        zoneBean.projectId = projectBean.id + "";
        zoneBean.projectName = projectBean.name;
        //
        zoneBean.poleNo = strPoleNo;
        zoneBean.poleType = StringUtils.getPoleTypeIndex(strPoleType);
        zoneBean.poleStructure = binding.spPoleStructure.getSelectedItemPosition();
        zoneBean.poleStandard = binding.spPoleStandard.getSelectedItemPosition();
        //
        zoneBean.poleHeight = strPoleHeight;
        zoneBean.designThickness = strDesignThickness;
        zoneBean.poleHeightReinforce = Double.parseDouble(strPoleHeight) - 1.5 + "";
        zoneBean.poleBottomDiameter = Integer.parseInt(strPoleBottomDiameter);
        zoneBean.poleTopDiameter = Integer.parseInt(strPoleTopDiameter);
        zoneBean.rebarDiameter = binding.spRebarDiameter.getSelectedItemPosition();
        zoneBean.hoopingDiameter = Integer.parseInt(strHoopingDiameter);
        zoneBean.poleBottomCount = 10;
        zoneBean.poleTopCount = 10;
        if (configBean != null) {
            zoneBean.designThickness = configBean.designThickness;
        }
        //
        if (entrustBean != null) {
            zoneBean.entrustId = entrustBean.id;
            zoneBean.entrustCode = entrustBean.entrust_code;
        } else {
            zoneBean.entrustId = "";
            zoneBean.entrustCode = "";
        }
        SPUtils.getInstance().put("last-config-ZoneBean", new Gson().toJson(zoneBean));
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StaticConstant.projectBeanList.clear();
    }
}
