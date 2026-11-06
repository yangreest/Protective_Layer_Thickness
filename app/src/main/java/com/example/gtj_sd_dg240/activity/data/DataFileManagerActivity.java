package com.example.gtj_sd_dg240.activity.data;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blackhao.utillibrary.usbHelper.USBBroadCastReceiver;
import com.blackhao.utillibrary.usbHelper.UsbHelper;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.activity.start.BaseActivity;
import com.example.gtj_sd_dg240.activity.testing.ResultActivity;
import com.example.gtj_sd_dg240.adapter.ProjectListAdapter;
import com.example.gtj_sd_dg240.adapter.ZoneListAdapter;
import com.example.gtj_sd_dg240.databinding.ActDataFileManagerBinding;
import com.example.gtj_sd_dg240.functions.database.greenDao.db.ZoneBeanDao;
import com.example.gtj_sd_dg240.model.ProjectBean;
import com.example.gtj_sd_dg240.model.ZoneBean;
import com.example.gtj_sd_dg240.net.NetworkUtils;
import com.example.gtj_sd_dg240.storage.GreenDaoHelper;
import com.example.gtj_sd_dg240.utils.OtherUtils;
import com.example.gtj_sd_dg240.view.StyleAlertDialog;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 数据管理
 */
@SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
public class DataFileManagerActivity extends BaseActivity<ActDataFileManagerBinding> {
    private ProjectListAdapter projectListAdapter;
    private ProjectBean projectBean;
    private ZoneListAdapter zoneListAdapter;
    private ZoneBean zoneBean;
    private final Set<Long> treatZoneIdSet = new HashSet<>();
    private String projectName;
    private long poleId;

    @Override
    protected ActDataFileManagerBinding getBinding() {
        return ActDataFileManagerBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.navigationBar.setTitleText("数据管理");

        projectName = getIntent().getStringExtra("projectName");
        poleId = getIntent().getLongExtra("poleId", -1);

        initBatteryReceiver(binding.navigationBar);
        initBluetoothStatus(binding.navigationBar);
        initUsbHelper();
        setProjectRecyclerView();
        setZoneRecyclerView();
        queryPageListProject();
        binding.btnOpen.setOnClickListener(this::onViewClicked);
        binding.btnUpload.setOnClickListener(this::onViewClicked);
        binding.btnExport.setOnClickListener(this::onViewClicked);
        binding.btnDelete.setOnClickListener(this::onViewClicked);
        binding.btnFinish.setOnClickListener(this::onViewClicked);
    }

    private int projectShowIndex;

    private void setProjectRecyclerView() {
        binding.recyclerViewProject.setLayoutManager(new LinearLayoutManager(this));
        projectListAdapter = new ProjectListAdapter(this);
        projectListAdapter.bindToRecyclerView(binding.recyclerViewProject);
        projectListAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            projectBean = projectListAdapter.getItem(position);
            if (view.getId() == R.id.textView) {
                projectShowIndex = position;
                for (int i = 0; i < projectListAdapter.getData().size(); i++) {
                    projectListAdapter.getData().get(i).isSelected_open = false;
                }
                projectListAdapter.getData().get(position).isSelected_open = true;
                projectListAdapter.notifyDataSetChanged();
                //搜索zoneList
                queryShowZoneList();
                //清空detail
                zoneBean = null;
                refreshTextView(false);
            } else if (view.getId() == R.id.imageView) {
                boolean flag = !projectListAdapter.getData().get(position).isSelected_treat;
                projectListAdapter.getData().get(position).isSelected_treat = flag;
                projectListAdapter.notifyDataSetChanged();
                queryTreatZoneList(projectListAdapter.getData().get(position));
                if (!zoneListAdapter.getData().isEmpty()) {
                    if (TextUtils.equals(zoneListAdapter.getData().get(0).projectId, projectListAdapter.getData().get(position).id + "")) {
                        for (ZoneBean bean : zoneListAdapter.getData()) {
                            bean.isSelected_treat = flag;
                        }
                    }
                    zoneListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void setZoneRecyclerView() {
        binding.recyclerViewZone.setLayoutManager(new LinearLayoutManager(this));
        zoneListAdapter = new ZoneListAdapter(this);
        zoneListAdapter.bindToRecyclerView(binding.recyclerViewZone);
        zoneListAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.ll_content) {
                Log.e("点击事件----", "__________");
                for (int i = 0; i < zoneListAdapter.getData().size(); i++) {
                    zoneListAdapter.getData().get(i).isSelected_open = false;
                }
                zoneListAdapter.getData().get(position).isSelected_open = true;
                zoneListAdapter.notifyDataSetChanged();
                zoneBean = zoneListAdapter.getItem(position);
                //显示detail
                refreshTextView(true);
            } else if (view.getId() == R.id.imageView) {
                boolean flag = !zoneListAdapter.getData().get(position).isSelected_treat;
                zoneListAdapter.getData().get(position).isSelected_treat = flag;
                zoneListAdapter.notifyDataSetChanged();
                if (flag) {
                    treatZoneIdSet.add(zoneListAdapter.getData().get(position).id);
                } else {
                    treatZoneIdSet.remove(zoneListAdapter.getData().get(position).id);
                }
                boolean isUnSelected = false;
                for (ZoneBean bean : zoneListAdapter.getData()) {
                    if (!bean.isSelected_treat) {
                        isUnSelected = true;
                        break;
                    }
                }
                projectListAdapter.getData().get(projectShowIndex).isSelected_treat = !isUnSelected;
                projectListAdapter.notifyDataSetChanged();
            }
        });
    }

    public void queryPageListProject() {
        List<ProjectBean> projectBeanList = GreenDaoHelper.getDaoSession().getProjectBeanDao().loadAll();
        for (ProjectBean bean : projectBeanList) {
            bean.isSelected_open = false;
            bean.isSelected_treat = false;
        }
        projectListAdapter.setNewData(projectBeanList);
    }

    public void queryShowZoneList() {
        if (projectBean == null) {
            zoneListAdapter.getData().clear();
            zoneListAdapter.notifyDataSetChanged();
            return;
        }
        List<ZoneBean> zoneBeanList = GreenDaoHelper.getDaoSession().getZoneBeanDao().queryBuilder().where(ZoneBeanDao.Properties.ProjectId.eq(projectBean.id + "")).list();
        for (ZoneBean bean : zoneBeanList) {
            bean.isSelected_open = false;
            bean.isSelected_treat = false;
        }
        for (ZoneBean bean : zoneBeanList) {
            for (Long aLong : treatZoneIdSet) {
                if (bean.id.equals(aLong)) {
                    bean.isSelected_treat = true;
                    break;
                }
            }
        }
        zoneListAdapter.setNewData(zoneBeanList);
    }

    private void refreshTextView(boolean flagShow) {
        if (flagShow) {
            StringBuilder sbd = new StringBuilder();
            sbd.append("工程信息")
                    .append("\n工程名称：").append(projectBean.name)
                    .append("\n电杆编号：").append(zoneBean.poleNo)
                    .append("\n适用规范：").append(StaticConstant.poleStandardList.get(zoneBean.poleStandard));
            sbd.append("\n试验参数")
                    .append("\n电杆类型：").append(StaticConstant.configPoleTypeList.get(zoneBean.poleType))
                    .append("\n电杆结构：").append(StaticConstant.configpoleStructureList.get(zoneBean.poleStructure))
                    .append("\n设计保护层厚度：").append(zoneBean.designThickness).append("（+8 -2）mm")
                    .append("\n电杆长度：").append(zoneBean.poleHeight).append("m")
                    .append("\n电杆根径：").append(zoneBean.poleBottomDiameter).append("mm").append("\t\t电杆梢径：").append(zoneBean.poleTopDiameter).append("mm")
                    .append("\n纵筋直径：").append(StaticConstant.rebarDiameterList.get(zoneBean.rebarDiameter)).append("mm").append("\t\t螺旋筋直径：").append(zoneBean.hoopingDiameter).append("mm");
            sbd.append("\n检测时间：").append(zoneBean.startTime);
            if (!TextUtils.isEmpty(zoneBean.entrustId)) {
                sbd.append("\n委托单号：").append(zoneBean.entrustCode);
            }
            binding.tvDetail.setText(sbd);
        } else {
            binding.tvDetail.setText("");
        }
    }

    public void queryTreatZoneList(ProjectBean projectBean) {
        List<ZoneBean> zoneBeanList = GreenDaoHelper.getDaoSession().getZoneBeanDao().queryBuilder().where(ZoneBeanDao.Properties.ProjectId.eq(projectBean.id + "")).list();
        boolean flag = projectBean.isSelected_treat;
        for (ZoneBean bean : zoneBeanList) {
            if (flag) {
                treatZoneIdSet.add(bean.id);
            } else {
                treatZoneIdSet.remove(bean.id);
            }
        }
    }

    public void onViewClicked(View view) {
        OtherUtils.hideKeyboard(this);
        if (view.getId() == R.id.btn_open) {
            if (zoneBean == null) {
                ToastUtils.showShort("请选择一个电杆");
                return;
            }
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("ProjectBean", projectBean);
            intent.putExtra("ZoneBean", zoneBean);
            startActivity(intent);
        } else if (view.getId() == R.id.btn_upload) {
            if (treatZoneIdSet.isEmpty()) {
                ToastUtils.showShort("暂无可上传的数据");

                return;
            }
            if (!NetworkUtils.isOpenNetwork(this)) {
                ToastUtils.showShort("请检查网络是否可用");
                return;
            }
            showUploadDialog();
//            ToastUtils.showShort("暂无上传功能");
        } else if (view.getId() == R.id.btn_export) {
            if (treatZoneIdSet.isEmpty()) {
                ToastUtils.showShort("暂无可导出的数据");
                return;
            }
            if (StaticConstant.usbHelper.rootFolder == null) {
                ToastUtils.showShort("未识别到U盘");
            }
            showExportDialog();
        } else if (view.getId() == R.id.btn_delete) {
            int projectCount = 0;
            for (int i = projectListAdapter.getData().size() - 1; i >= 0; i--) {
                if (projectListAdapter.getData().get(i).isSelected_treat) {
                    projectCount++;
                }
            }
            if (projectCount > 0 || !treatZoneIdSet.isEmpty()) {
                StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this, "red");
                styleAlertDialog.setContent("确定删除勾选项？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
                    styleAlertDialog.dismiss();
                    removeTreatZoneList();
                }).show();
            } else {
                ToastUtils.showShort("请勾选删除项");
            }
        } else if (view.getId() == R.id.btn_finish) {
            finish();
        }
    }

    private void removeTreatZoneList() {
        if (poleId != -1 && treatZoneIdSet.contains(poleId)) {
            ToastUtils.showLong("正在检测中的电杆数据不可删除");
            return;
        }
        //删除
        if (zoneBean != null) {
            for (Long aLong : treatZoneIdSet) {
                if (aLong.equals(zoneBean.id)) {
                    zoneBean = null;
                    refreshTextView(false);
                    break;
                }
            }
        }
        //ZoneBean数据库删除
        GreenDaoHelper.getDaoSession().getZoneBeanDao().deleteByKeyInTx(treatZoneIdSet);
        //zoneListAdapter删除
        for (int i = zoneListAdapter.getData().size() - 1; i >= 0; i--) {
            for (Long aLong : treatZoneIdSet) {
                if (aLong.equals(zoneListAdapter.getData().get(i).id)) {
                    zoneListAdapter.getData().remove(i);
                    break;
                }
            }
        }
        zoneListAdapter.notifyDataSetChanged();
        treatZoneIdSet.clear();
        //projectListAdapter删除
        for (int i = projectListAdapter.getData().size() - 1; i >= 0; i--) {
            if (projectListAdapter.getData().get(i).isSelected_treat) {
                if (!TextUtils.isEmpty(projectName) && projectName.equals(projectListAdapter.getData().get(i).name)) {
                    projectListAdapter.getData().get(i).isSelected_treat = false;
                } else {
                    GreenDaoHelper.getDaoSession().getProjectBeanDao().deleteByKey(projectListAdapter.getData().get(i).id);
                    projectListAdapter.getData().remove(i);
                }
            }
        }
        projectListAdapter.notifyDataSetChanged();
        if (projectListAdapter.getData().isEmpty()) {
            SPUtils.getInstance().put("last-config-ZoneBean", "");
        }
    }

    private void showUploadDialog() {
        StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this, "blue");
        styleAlertDialog.setContent("确定上传数据？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
            styleAlertDialog.dismiss();
            Intent intent = new Intent(this, DataUploadActivity.class);
            intent.putExtra("treatZoneIdSet", new Gson().toJson(treatZoneIdSet));
            startActivityForResult(intent, 1);
        }).show();
    }

    private void showExportDialog() {
        StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this, "blue");
        styleAlertDialog.setContent("确定导出数据？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
            styleAlertDialog.dismiss();
            Intent intent = new Intent(this, DataExportActivity.class);
            intent.putExtra("treatZoneIdSet", new Gson().toJson(treatZoneIdSet));
            startActivityForResult(intent, 1);
        }).show();
    }

    //---------------------USB--------------------------
    private void initUsbHelper() {
        //U盘只能识别Pad格式化的U盘或格式化选FAT32的文件系统
        StaticConstant.usbHelper = new UsbHelper(this, new USBBroadCastReceiver.UsbListener() {
            @Override
            public void insertUsb(UsbDevice device_add) {
                ToastUtils.showShort("U盘插入");
                initCurrentFolder();
            }

            @Override
            public void removeUsb(UsbDevice device_remove) {
                ToastUtils.showShort("U盘被拔出");
                StaticConstant.usbHelper.rootFolder = null;
            }

            @Override
            public void getReadUsbPermission(UsbDevice usbDevice) {
                initCurrentFolder();
            }

            @Override
            public void failedReadUsb(UsbDevice usbDevice) {
            }
        });
        initCurrentFolder();
    }

    private final CompositeDisposable mRxEvent = new CompositeDisposable();

    private void initCurrentFolder() {
        Observable<Integer> observableIdentificationUsb = Observable.create(e -> {
            UsbMassStorageDevice[] usbMassStorageDevices = StaticConstant.usbHelper.getDeviceList();
            if (usbMassStorageDevices.length > 0) {
                if (StaticConstant.usbHelper.rootFolder == null) {
                    StaticConstant.usbHelper.readDevice(usbMassStorageDevices[0]);
                }
                if (StaticConstant.usbHelper.rootFolder != null) {
                    ToastUtils.showShort("U盘识别成功");
                }
            }
        });
        Disposable disposable = observableIdentificationUsb.subscribeOn(Schedulers.newThread()).subscribe();
        mRxEvent.add(disposable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRxEvent.clear();
        StaticConstant.usbHelper.finishUsbHelper();
    }
}
