package com.example.gtj_sd_dg240.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.Poi;
import com.baidu.location.PoiRegion;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.activity.about.AboutUsActivity;
import com.example.gtj_sd_dg240.activity.data.DataFileManagerActivity;
import com.example.gtj_sd_dg240.activity.start.BaseMainActivity;
import com.example.gtj_sd_dg240.activity.start.StartActivity;
import com.example.gtj_sd_dg240.activity.testing.DetectActivity2;
import com.example.gtj_sd_dg240.databinding.ActivityMainBinding;
import com.example.gtj_sd_dg240.functions.database.greenDao.db.ConfigBeanDao;
import com.example.gtj_sd_dg240.functions.database.greenDao.db.RebarMinMaxBeanDao;
import com.example.gtj_sd_dg240.model.ConfigBean;
import com.example.gtj_sd_dg240.model.RebarMinMaxBean;
import com.example.gtj_sd_dg240.storage.GreenDaoHelper;
import com.example.gtj_sd_dg240.utils.ComputeUtils;
import com.example.gtj_sd_dg240.utils.ExportMinMaxBeanUtils;
import com.example.gtj_sd_dg240.utils.LocService;
import com.example.gtj_sd_dg240.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;


@SuppressLint("SetTextI18n")
public class MainActivity extends BaseMainActivity<ActivityMainBinding> {
    @Override
    protected ActivityMainBinding getBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBatteryReceiver(binding.navigationBar);
        initBluetoothStatus(binding.navigationBar);
        binding.ivTesting.setOnClickListener(this::onViewClicked);
        binding.ivData.setOnClickListener(this::onViewClicked);
        binding.ivSetting.setOnClickListener(this::onViewClicked);
        binding.tvTesting.setOnClickListener(this::onViewClicked);
        binding.tvData.setOnClickListener(this::onViewClicked);
        binding.tvSetting.setOnClickListener(this::onViewClicked);
        initLocationConfig();
        startupPage();
        insertConfig();
        inputAllData();
//        aaa();
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionUtils.initPermission(this, null);
    }

    private void startupPage() {
        if (!StaticConstant.isNeutral) {
            if (TextUtils.equals(StaticConstant.APP_DEFAULT, StaticConstant.APP_GTJ)) {
                binding.tvName.setText(getString(R.string.app_name) + "  " + getString(R.string.app_name_zh));
            } else {
                if (SystemClock.elapsedRealtime() < 40000) {
                    startActivity(new Intent(this, StartActivity.class));
                }
            }
        }
    }

    private void initLocationConfig() {
        StaticConstant.locService.registerListener(mListener);
        LocService.setLocationOption(StaticConstant.locService.getDefaultLocationClientOption());
        StaticConstant.locService.start();
    }

    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                ToastUtils.showShort("再按一次退出程序");
                mExitTime = System.currentTimeMillis();
                return false;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    public void onViewClicked(View view) {
        Intent intent;
        if (view.getId() == R.id.iv_testing || view.getId() == R.id.tv_testing) {
            String deviceMac = SPUtils.getInstance().getString("bluetooth-mac");
            if (TextUtils.isEmpty(deviceMac)) {
                ToastUtils.showShort("请先在系统设置里连接设备主机");
                return;
            }
            String strCalibrationStandardList = SPUtils.getInstance().getString("calibration-standard-list");
            String strCalibrationList = SPUtils.getInstance().getString("calibration-list");
            if (TextUtils.isEmpty(strCalibrationStandardList) || TextUtils.isEmpty(strCalibrationList)) {
                ToastUtils.showShort("请先在系统设置里校准设备主机");
                return;
            }
            intent = new Intent(this, DetectActivity2.class);
            startActivity(intent);
        } else if (view.getId() == R.id.iv_data || view.getId() == R.id.tv_data) {
            intent = new Intent(this, DataFileManagerActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.iv_setting || view.getId() == R.id.tv_setting) {
            intent = new Intent(this, AboutUsActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) {
            bluetoothService.disconnect();
        }
        //注销
        StaticConstant.locService.unregisterListener(mListener);
        StaticConstant.locService.stop();
    }

    private final BDAbstractLocationListener mListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                StringBuilder sb = new StringBuilder(256);
                sb.append("time : ");
                //时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                //location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                sb.append(location.getTime());
                sb.append("\nsysTime : ");
                sb.append(SystemClock.elapsedRealtime());
                sb.append("\nlocType : ");// 定位类型
                sb.append(location.getLocType());
                sb.append("\nlocType description : ");// *****对应的定位类型说明*****
                sb.append(location.getLocTypeDescription());
                sb.append("\nlatitude : ");// 纬度
                sb.append(location.getLatitude());
                sb.append("\nlongtitude : ");// 经度
                sb.append(location.getLongitude());
                sb.append("\nradius : ");// 半径
                sb.append(location.getRadius());
                sb.append("\nCountryCode : ");// 国家码
                sb.append(location.getCountryCode());
                sb.append("\nProvince : ");// 获取省份
                sb.append(location.getProvince());
                sb.append("\nCountry : ");// 国家名称
                sb.append(location.getCountry());
                sb.append("\ncitycode : ");// 城市编码
                sb.append(location.getCityCode());
                sb.append("\ncity : ");// 城市
                sb.append(location.getCity());
                sb.append("\nDistrict : ");// 区
                sb.append(location.getDistrict());
                sb.append("\nTown : ");// 获取镇信息
                sb.append(location.getTown());
                sb.append("\nStreet : ");// 街道
                sb.append(location.getStreet());
                sb.append("\naddr : ");// 地址信息
                sb.append(location.getAddrStr());
                sb.append("\nStreetNumber : ");// 获取街道号码
                sb.append(location.getStreetNumber());
                sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
                sb.append(location.getUserIndoorState());
                sb.append("\nDirection(not all devices have value): ");
                sb.append(location.getDirection());// 方向
                sb.append("\nlocationdescribe: ");
                sb.append(location.getLocationDescribe());// 位置语义化信息
                sb.append("\nPoi: ");// POI信息
                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                    for (int i = 0; i < location.getPoiList().size(); i++) {
                        Poi poi = location.getPoiList().get(i);
                        sb.append("poiName:");
                        sb.append(poi.getName()).append(", ");
                        sb.append("poiTag:");
                        sb.append(poi.getTags()).append("\n");
                    }
                }
                if (location.getPoiRegion() != null) {
                    sb.append("PoiRegion: ");// 返回定位位置相对poi的位置关系，仅在开发者设置需要POI信息时才会返回，在网络不通或无法获取时有可能返回null
                    PoiRegion poiRegion = location.getPoiRegion();
                    sb.append("DerectionDesc:"); // 获取POIREGION的位置关系，ex:"内"
                    sb.append(poiRegion.getDerectionDesc()).append("; ");
                    sb.append("Name:"); // 获取POIREGION的名字字符串
                    sb.append(poiRegion.getName()).append("; ");
                    sb.append("Tags:"); // 获取POIREGION的类型
                    sb.append(poiRegion.getTags()).append("; ");
                    sb.append("\nSDK版本: ");
                }
                sb.append(StaticConstant.locService.getSDKVersion()); // 获取SDK版本
                int permission = LocService.checkPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
                sb.append("\npermsission: ").append(permission);
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 速度 单位：km/h
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());// 卫星数目
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 海拔高度 单位：米
                    sb.append("\ngps status : ");
                    sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    if (location.hasAltitude()) {// *****如果有海拔高度*****
                        sb.append("\nheight : ");
                        sb.append(location.getAltitude());// 单位：米
                    }
                    sb.append("\noperationers : ");// 运营商信息
                    sb.append(location.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
//                Logger.d(sb);
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            super.onConnectHotSpotMessage(s, i);
        }

        /**
         * 回调定位诊断信息，开发者可以根据相关信息解决定位遇到的一些问题
         * @param locType 当前定位类型
         * @param diagnosticType 诊断类型（1~9）
         * @param diagnosticMessage 具体的 诊断信息释义
         */
        @Override
        public void onLocDiagnosticMessage(int locType, int diagnosticType, String diagnosticMessage) {
            super.onLocDiagnosticMessage(locType, diagnosticType, diagnosticMessage);
            StringBuilder sb = new StringBuilder(256);
            sb.append("诊断结果: ");
            if (locType == BDLocation.TypeNetWorkLocation) {
                if (diagnosticType == 1) {
                    sb.append("网络定位成功，没有开启GPS，建议打开GPS会更好");
                    sb.append("\n").append(diagnosticMessage);
                } else if (diagnosticType == 2) {
                    sb.append("网络定位成功，没有开启Wi-Fi，建议打开Wi-Fi会更好");
                    sb.append("\n").append(diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeOffLineLocationFail) {
                if (diagnosticType == 3) {
                    sb.append("定位失败，请您检查您的网络状态");
                    sb.append("\n").append(diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeCriteriaException) {
                if (diagnosticType == 4) {
                    sb.append("定位失败，无法获取任何有效定位依据");
                    sb.append("\n").append(diagnosticMessage);
                } else if (diagnosticType == 5) {
                    sb.append("定位失败，无法获取有效定位依据，请检查运营商网络或者Wi-Fi网络是否正常开启，尝试重新请求定位");
                    sb.append(diagnosticMessage);
                } else if (diagnosticType == 6) {
                    sb.append("定位失败，无法获取有效定位依据，请尝试插入一张sim卡或打开Wi-Fi重试");
                    sb.append("\n").append(diagnosticMessage);
                } else if (diagnosticType == 7) {
                    sb.append("定位失败，飞行模式下无法获取有效定位依据，请关闭飞行模式重试");
                    sb.append("\n").append(diagnosticMessage);
                } else if (diagnosticType == 9) {
                    sb.append("定位失败，无法获取任何有效定位依据");
                    sb.append("\n").append(diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeServerError) {
                if (diagnosticType == 8) {
                    sb.append("定位失败，请确认您定位的开关打开状态，是否赋予APP定位权限");
                    sb.append("\n").append(diagnosticMessage);
                }
            }
//            ToastUtils.showShort(sb);
        }
    };

    private void insertConfig() {
        //普通混凝土电杆
        addConfig(StaticConstant.poleType_PT, 0, 17, "190mm，10米杆，I级", "10", 190, 323, 10, 4);
        addConfig(StaticConstant.poleType_PT, 0, 17, "190mm，12米杆，M级", "12", 190, 350, 14, 4);
        addConfig(StaticConstant.poleType_PT, 0, 17, "190mm，15米杆，M级", "15", 190, 390, 14, 4);
        //预应力混凝土电杆Y
        addConfig(StaticConstant.poleType_Y, 0, 17, "Z Ø150×8×G×Y", "8", 150, 257, 7, 3);
        addConfig(StaticConstant.poleType_Y, 0, 17, "Z Ø190×10×K×Y", "10", 190, 323, 7, 3);
        addConfig(StaticConstant.poleType_Y, 0, 17, "Z Ø190×12×K×Y", "12", 190, 350, 7, 3);
        addConfig(StaticConstant.poleType_Y, 0, 17, "Z Ø190×15×K×Y", "15", 190, 390, 7, 3);
        addConfig(StaticConstant.poleType_Y, 1, 17, "Z Ø190×12(6+6)×K×Y", "12", 190, 390, 7, 3);
        addConfig(StaticConstant.poleType_Y, 1, 17, "Z Ø190×15(9+6)×K×Y", "15", 190, 390, 7, 3);
        //部分预应力混凝土电杆BY
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø190×12×100×BY", "12", 190, 350, 11, 3);
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø190×12×M×BY", "12", 190, 350, 9, 3);
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø190×15×M×BY", "15", 190, 390, 9, 4);
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø230×10×O×BY", "10", 230, 323, 9, 4);
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø230×12×125×BY", "12", 230, 390, 11, 4);
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø230×15×150×BY", "15", 230, 430, 9, 4);
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø350×15×U×BY", "15", 350, 550, 11, 4);
        addConfig(StaticConstant.poleType_BY, 1, 17, "Z Ø190×15(9+6)×100×BY", "15", 190, 390, 9, 3);
        addConfig(StaticConstant.poleType_BY, 1, 17, "Z Ø190×18(9+9)×M×BY", "18", 190, 430, 9, 4);
        addConfig(StaticConstant.poleType_BY, 1, 17, "Z Ø310×12(6+6)×O×BY", "12", 310, 470, 9, 4);
        addConfig(StaticConstant.poleType_BY, 1, 17, "Z Ø310×12(6+6)×U×BY", "12", 310, 470, 9, 4);
        addConfig(StaticConstant.poleType_BY, 1, 17, "Z Ø230×12(6+6)×O×BY", "12", 230, 390, 9, 4);
    }

    private void addConfig(String poleType, int poleStructure, int designThickness, String name, String height, int topDiameter, int bottomDiameter, int rebarDiameter, int hoopingDiameter) {
        List<ConfigBean> configBeanList = GreenDaoHelper.getDaoSession().getConfigBeanDao().queryBuilder().where(ConfigBeanDao.Properties.PoleName.eq(name)).list();
        long id = -1;
        if (!configBeanList.isEmpty()) {
            id = configBeanList.get(0).id;
        }
        ConfigBean configBean = new ConfigBean();
        if (id > 0) {
            configBean.id = id;
        }
        configBean.poleType = poleType;
        configBean.poleStructure = poleStructure;
        configBean.poleName = name;
        configBean.poleTopDiameter = topDiameter;
        configBean.poleBottomDiameter = bottomDiameter;
        configBean.rebarDiameter = rebarDiameter;
        configBean.hoopingDiameter = hoopingDiameter;
        configBean.poleHeight = height;
        configBean.designThickness = designThickness + "";
        //
        GreenDaoHelper.getDaoSession().getConfigBeanDao().insertOrReplace(configBean);
    }

    /**
     * 导入
     */
    private void inputAllData() {
        //导入数据
        List<RebarMinMaxBean> minMaxBeanList = ExportMinMaxBeanUtils.getRebarMinMaxBeanList(this, "RebarMinMax.csv");
        if (minMaxBeanList == null || minMaxBeanList.isEmpty()) {
            return;
        }
        RebarMinMaxBeanDao minMaxBeanDao = GreenDaoHelper.getDaoSession().getRebarMinMaxBeanDao();
        //查找已存在id
        for (RebarMinMaxBean bean : minMaxBeanList) {
            RebarMinMaxBean temp = minMaxBeanDao.queryBuilder().where(RebarMinMaxBeanDao.Properties.UniqueKey.eq(bean.uniqueKey)).unique();
            if (temp != null) {
                bean.id = temp.id;
            }
        }
        //存储
        minMaxBeanDao.insertOrReplaceInTx(minMaxBeanList);
        //查询
//        int size = minMaxBeanDao.loadAll().size();
//        ToastUtils.showShort("导入成功，" + size);
    }

    private void aaa() {
        List<int[]> spaces = new ArrayList<>();
        spaces.add(new int[]{35, 35});
        spaces.add(new int[]{40, 40});
        spaces.add(new int[]{45, 45});
        spaces.add(new int[]{50, 50});
        spaces.add(new int[]{55, 55});
        spaces.add(new int[]{60, 60});
        spaces.add(new int[]{65, 65});
        spaces.add(new int[]{70, 70});
        spaces.add(new int[]{75, 75});
        spaces.add(new int[]{35, 65});
        spaces.add(new int[]{40, 80});
        spaces.add(new int[]{50, 100});
        //
        //10
//        Log.e("====", "================================= 10");
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{52, 58});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{57, 61});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{60, 107});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{0, 0});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{57, 106});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{57, 109});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{57, 70});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{45, 57});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{43, 53});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{53, 57});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{48, 57});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{54, 58});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{53, 54});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{32, 92});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{41, 76});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{38, 84});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{55, 61});
//        //
//        //14
//        Log.e("====", "================================= 14");
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{71, 73});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{74, 92});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{30, 68});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{0, 0});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{29, 69});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{66, 68});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{65, 68});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{65, 94});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{91, 95});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{48, 93});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{48, 49});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{49, 50});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{49, 89});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{40, 98});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{40, 50});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{51, 54});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{47, 96});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{60, 80});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{67, 73});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{39, 50});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{40, 49});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{41, 52});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{53, 61});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{53, 61});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{56, 67});
//        ComputeUtils.findMatchingSpaces(spaces, new double[]{61, 66});
        Log.e("========", "========================START===");
        for (int i = 20; i < 120; i++) {
            int ss = 80;
            int min = Math.min(i, ss);
            int max = Math.max(i, ss);
            ComputeUtils.findMatchingSpaces(spaces, new double[]{min, max});
        }
        Log.e("========", "========================END===");
    }
}
