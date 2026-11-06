//package com.example.gtj_sd_dg240.activity.testing;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.graphics.Typeface;
//import android.media.MediaPlayer;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.text.Editable;
//import android.text.Html;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.blankj.utilcode.util.SPUtils;
//import com.blankj.utilcode.util.ToastUtils;
//import com.example.gtj_sd_dg240.R;
//import com.example.gtj_sd_dg240.StaticConstant;
//import com.example.gtj_sd_dg240.activity.data.DataFileManagerActivity;
//import com.example.gtj_sd_dg240.activity.start.BaseActivity;
//import com.example.gtj_sd_dg240.databinding.ActDetect2Binding;
//import com.example.gtj_sd_dg240.functions.database.greenDao.db.StandardCurveBeanDao;
//import com.example.gtj_sd_dg240.model.CheckDataBean;
//import com.example.gtj_sd_dg240.model.PoleThicknessBean;
//import com.example.gtj_sd_dg240.model.RebarBean;
//import com.example.gtj_sd_dg240.model.RebarPageBean;
//import com.example.gtj_sd_dg240.model.StandardCurveBean;
//import com.example.gtj_sd_dg240.model.ZoneBean;
//import com.example.gtj_sd_dg240.storage.CorrosionCurve;
//import com.example.gtj_sd_dg240.storage.GreenDaoHelper;
//import com.example.gtj_sd_dg240.utils.BluetoothManager;
//import com.example.gtj_sd_dg240.utils.CheckDataUtils;
//import com.example.gtj_sd_dg240.utils.ComputeCurveUtils;
//import com.example.gtj_sd_dg240.utils.CustomMediaPlayer;
//import com.example.gtj_sd_dg240.utils.OptimizedDataProcessor;
//import com.example.gtj_sd_dg240.utils.OtherUtils;
//import com.example.gtj_sd_dg240.utils.StringUtils;
//import com.example.gtj_sd_dg240.view.StyleAlertDialog;
//import com.example.gtj_sd_dg240.view.StyleAlertDialog2;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import com.inuker.bluetooth.library.Constants;
//import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
//import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
//import com.orhanobut.logger.Logger;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//@SuppressLint({"InflateParams", "SetTextI18n", "UseCompatLoadingForDrawables"})
//public class DetectActivity1 extends BaseActivity<ActDetect2Binding> {
//    private int color006F5F, color8E8E8E, colorFFF, color000, colorFF9600;
//    private ZoneBean zoneBean, cloneZoneBean;
//    private String spMac;
//    private MediaPlayer mediaPlayer;
//    private List<Integer> calibrationStandardList = new ArrayList<>();
//    private List<Integer> calibrationList = new ArrayList<>();
//    private ScheduledExecutorService scheduledExecutorService;
//    private OptimizedDataProcessor dataProcessor;
//    private Handler dataProcessorHandler;
//    private final List<CheckDataBean> checkDataList = new ArrayList<>();
//    private double thicknessMin, thicknessMax;
//    private StyleAlertDialog2 styleAlertDialog;
//
//    @Override
//    protected ActDetect2Binding getBinding() {
//        return ActDetect2Binding.inflate(getLayoutInflater());
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        initView();
//    }
//
//    private void initView() {
//        color006F5F = getColor(R.color.color006F5F);
//        color8E8E8E = getColor(R.color.color8E8E8E);
//        colorFFF = getColor(R.color.colorFFF);
//        color000 = getColor(R.color.color000);
//        colorFF9600 = getColor(R.color.colorFF9600);
//        styleAlertDialog = new StyleAlertDialog2(this);
//        binding.btnCheck.setOnClickListener(this::onViewClicked);
//        binding.btnSetting.setOnClickListener(this::onViewClicked);
//        binding.btnSave.setOnClickListener(this::onViewClicked);
//        binding.btnFile.setOnClickListener(this::onViewClicked);
//        binding.btnFinish.setOnClickListener(this::onViewClicked);
//        //
//        binding.tvTabDetection.setOnClickListener(this::onViewClicked);
//        binding.tvTabForecast.setOnClickListener(this::onViewClicked);
//        binding.btnForecast.setOnClickListener(this::onViewClicked);
//        setHideKeyboard();
//        binding.navigationBar.setTitleText("单点检测");
//        initBatteryReceiver(binding.navigationBar);
//        initBluetoothExecutor(binding.navigationBar);
//        spMac = SPUtils.getInstance().getString("bluetooth-mac");
//        mediaPlayer = CustomMediaPlayer.getMediaPlayer(null);
//        calibrationSignalValue();
//        initBluetooth();
//        initExecutor();
//        initOptimizedDataProcessor();
//        showView(0);
//        binding.etDistance.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (binding.etDistance.isFocused()) {
//                    binding.etDistance1.setText(s);
//                }
//            }
//        });
//        binding.etDistance1.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (binding.etDistance1.isFocused()) {
//                    binding.etDistance.setText(s);
//                }
//            }
//        });
//    }
//
//    private void calibrationSignalValue() {
//        String strCalibrationStandardList = SPUtils.getInstance().getString("calibration-standard-list");
//        calibrationStandardList = new Gson().fromJson(strCalibrationStandardList, new TypeToken<List<Integer>>() {
//        }.getType());
//        String strCalibrationList = SPUtils.getInstance().getString("calibration-list");
//        calibrationList = new Gson().fromJson(strCalibrationList, new TypeToken<List<Integer>>() {
//        }.getType());
//        //
//        StandardCurveBeanDao curveDao = GreenDaoHelper.getDaoSession().getStandardCurveBeanDao();
//        CorrosionCurve.curveBeanList6 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.Diameter.eq("6")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
//        CorrosionCurve.curveBeanList7 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.Diameter.eq("7")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
//        CorrosionCurve.curveBeanList8 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.Diameter.eq("8")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
//        CorrosionCurve.curveBeanList9 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.Diameter.eq("9")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
//        CorrosionCurve.curveBeanList10 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.Diameter.eq("10")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
//        CorrosionCurve.curveBeanList11 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.Diameter.eq("11")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
//        CorrosionCurve.curveBeanList12 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.Diameter.eq("12")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
//        CorrosionCurve.curveBeanList14 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.Diameter.eq("14")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
//        CorrosionCurve.curveBeanList16 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.Diameter.eq("16")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
//        CorrosionCurve.curveBeanList18 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.Diameter.eq("18")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
//    }
//
//    private void initBluetooth() {
//        BluetoothManager.registerConnectStatusListener(spMac, mBleConnectStatusListener);
//        if (BluetoothManager.getConnectStatus(spMac)) {
//            notifyBluetoothDevice();
//        }
//    }
//
//    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {
//        @Override
//        public void onConnectStatusChanged(String mac, int status) {
//            if (status == Constants.STATUS_CONNECTED) {
//                notifyBluetoothDevice();
//            } else if (status == Constants.STATUS_DISCONNECTED) {
//                BluetoothManager.unNotifyBluetoothDevice(spMac);
//            }
//        }
//    };
//
//    private void notifyBluetoothDevice() {
//        BluetoothManager.notifyBluetoothDevice(spMac, new BleNotifyResponse() {
//            @Override
//            public void onNotify(UUID service, UUID character, byte[] value) {
//                if (!isShow) {
//                    return;
//                }
//                dataProcessor.receiveDataOptimized(value);
//            }
//
//            @Override
//            public void onResponse(int code) {
//            }
//        });
//        ToastUtils.showLong("连接成功，请检测");
//    }
//
//    private void initExecutor() {
//        Handler handlerPost = new Handler();
//        scheduledExecutorService = Executors.newScheduledThreadPool(1);
//        scheduledExecutorService.scheduleWithFixedDelay(() -> handlerPost.post(() -> timerTime++), 0, 1, TimeUnit.SECONDS);
//    }
//
//    private long timerTime;
//    private boolean isCollectStop;
//    private final List<PoleThicknessBean> poleList = new ArrayList<>();
//    private final StringBuilder sbdMaxBean = new StringBuilder();
//
//    private void initOptimizedDataProcessor() {
//        dataProcessorHandler = new Handler(Looper.getMainLooper());
//        dataProcessor = new OptimizedDataProcessor((displacement, signal, frameData) -> {
//            if (checkType == 0) {
//                checkThickness(displacement, signal);
//            } else {
//                forecastDiameter(signal);
//            }
//        });
//    }
//
//    private void checkThickness(int displacement, int signal) {
//        //停止
//        if (isCollectStop) {
//            return;
//        }
//        //显示信号
//        dataProcessorHandler.post(() -> binding.tvSignal.setText("信号：" + signal));
//        //超过2秒清空数据
//        {
//            if (timerTime >= 2) {
//                CheckDataUtils.clear();
//                checkDataList.clear();
//                sbdMaxBean.delete(0, sbdMaxBean.length());
//            }
//            timerTime = 0;
//        }
//        //校准信号
//        int signalValue = getSignalValue(signal);
//        //
//        //记录数据|获取最大值
//        CheckDataBean maxBean = CheckDataUtils.addData(displacement, signalValue);
//        //
//        if (maxBean == null) {
//            return;
//        }
//        Logger.e("---maxBean: " + maxBean.showString());
//        sbdMaxBean.append(maxBean.showString()).append("\n");
/// /            dataProcessorHandler.post(() -> styleAlertDialog.setContent(sbdMaxBean).setOKButton("确定", v -> styleAlertDialog.dismiss()).show());
//        //
//        CustomMediaPlayer.startSpeaking(this, mediaPlayer, 1);
//        if (zoneBean == null) {
//            ToastUtils.showShort("请进行参数设置");
//            return;
//        }
//        if (zoneBean.rebarPageList.isEmpty()) {
//            ToastUtils.showShort("请添加检测部位");
//            return;
//        }
//        RebarPageBean pageBean = zoneBean.rebarPageList.get(zoneBean.rebarPageList.size() - 1);
//        if (!isAdd(pageBean)) {
//            ToastUtils.showShort("请添加检测部位");
//            return;
//        }
//        //添加
//        checkDataList.add(maxBean);
//        int checkDataListSize = checkDataList.size();
//        if (checkDataListSize == 3) {
//            //停止检测
//            isCollectStop = true;
//            //钢筋直径
//            String rebarDiameter = StaticConstant.rebarDiameterList.get(zoneBean.rebarDiameter);
//            //计算钢筋保护层厚度
//            CheckDataBean cdb1 = getThickness(rebarDiameter, checkDataList.get(0).y);
//            cdb1.curve_x = checkDataList.get(0).curve_x;
//            CheckDataBean cdb2 = getThickness(rebarDiameter, checkDataList.get(1).y);
//            cdb2.curve_x = checkDataList.get(1).curve_x;
//            CheckDataBean cdb3 = getThickness(rebarDiameter, checkDataList.get(2).y);
//            cdb3.curve_x = checkDataList.get(2).curve_x;
//            //
//            double tempThicknessL = StringUtils.getRounding(cdb1.thickness + poleCompensation + rebarCompensation, 1);
//            double tempThicknessM = StringUtils.getRounding(cdb2.thickness + poleCompensation + rebarCompensation, 1);
//            double tempThicknessR = StringUtils.getRounding(cdb3.thickness + poleCompensation + rebarCompensation, 1);
//            int spaceNumberL = cdb2.curve_x[60] - cdb1.curve_x[60];
//            int spaceNumberR = cdb3.curve_x[60] - cdb2.curve_x[60];
//            //主筋间距夹角,主筋间距
//            String stringL = "左";
//            double angleL = ComputeCurveUtils.calculateAngleInDegrees(radius, spaceNumberL);
//            double tempSpacingL = ComputeCurveUtils.calculateThirdSide(radius - tempThicknessL, radius - tempThicknessM, angleL);
//            //
//            String stringR = "右";
//            double angleR = ComputeCurveUtils.calculateAngleInDegrees(radius, spaceNumberR);
//            double tempSpacingR = ComputeCurveUtils.calculateThirdSide(radius - tempThicknessM, radius - tempThicknessR, angleR);
//            //
//            //主筋间距补偿
//            double[] spaceCompensationL = CorrosionCurve.getSpaceCompensation(rebarDiameter, tempThicknessM, tempThicknessL, tempSpacingL);
//            String spaceL = stringL + " , 间距：" + tempSpacingL + " , 厚度：" + tempThicknessM + " , " + tempThicknessL + " , 邻筋：" + spaceCompensationL[0];
//            double[] spaceCompensationR = CorrosionCurve.getSpaceCompensation(rebarDiameter, tempThicknessM, tempThicknessR, tempSpacingR);
//            String spaceR = stringR + " , 间距：" + tempSpacingR + " , 厚度：" + tempThicknessM + " , " + tempThicknessR + " , 邻筋：" + spaceCompensationR[0];
//            //
//            //箍筋间距补偿（根、稍）
//            double spaceCompensationScrew = CorrosionCurve.getHeightCompensation(rebarDiameter, Double.parseDouble(zoneBean.poleHeight), pageBean.distance);
//            String spaceScrew = CorrosionCurve.isTopBottom(Double.parseDouble(zoneBean.poleHeight), pageBean.distance) ? "根稍" : "中段";
//            //
//            //重新计算 钢筋保护层厚度
//            double rebarThickness = StringUtils.getRounding(tempThicknessM + spaceCompensationL[1] + spaceCompensationR[1] + spaceCompensationScrew, 1);
//            //
//            //添加
//            poleList.add(new PoleThicknessBean((int) checkDataList.get(1).y, cdb2.thickness, poleCompensation, rebarCompensation, spaceL, spaceCompensationL[1], spaceR, spaceCompensationR[1], spaceCompensationScrew, spaceScrew, rebarThickness));
//            //清空
//            checkDataList.clear();
//            //
//            double ths;
//            if (poleList.size() == 3) {
//                ths = Collections.max(poleList, (o1, o2) -> (int) (o1.thickness * 100 - o2.thickness * 100)).thickness;
//                //存储
//                RebarBean rebarBean = new RebarBean(pageBean.segment, ths);
//                rebarBean.thickness1 = StringUtils.getRounding(rebarBean.thickness, 0);
//                pageBean.beanList.add(rebarBean);
//                //求平均
//                treatAverage(pageBean);
//                String strMessage = poleList.toString();
//                //清空
//                if (poleList.size() == 3) {
//                    poleList.clear();
//                }
//                dataProcessorHandler.post(() -> {
////                        styleAlertDialog.setContent(strMessage).setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
//                    //刷新钢筋列表
//                    refreshGrid();
//                    //刷新按钮
//                    refreshButon(true, pageBean);
//                    //数据异常、剔凿验证
//                    if (rebarThickness > thicknessMax || rebarThickness < thicknessMin) {
//                        showErrorThickness();
//                    }
//                });
//            } else {
//                ths = 0;
//            }
//            //
//            dataProcessorHandler.post(() -> {
//                binding.tvThickness.setText("厚度：" + ths + " mm");
//                isCollectStop = false;  //继续检测
//            });
//        }
//        dataProcessorHandler.post(() -> {
//            ToastUtils.showShort("检测进度：" + checkDataListSize);
//            refreshTvCheck();
//        });
//    }
//
//    private int valueMin, valueMax;
//
//    private void forecastDiameter(int signal) {
//        if (valueMin > signal) {
//            valueMin = signal;
//        }
//        if (valueMax < signal) {
//            valueMax = signal;
//        }
//        dataProcessorHandler.post(() -> {
//            binding.tvForecastValue.setText("信号：" + signal);
//            binding.tvForecastMin.setText("Min：" + valueMin);
//            binding.tvForecastMax.setText("Max：" + valueMax);
//        });
//    }
//
//    private CheckDataBean getThickness(String poleNorm, double signal) {
//        //"6", "7", "8", "9", "10", "11", "12", "14", "16", "18"
//        List<StandardCurveBean> curveBeanList = switch (poleNorm) {
//            case "6" -> CorrosionCurve.curveBeanList6;
//            case "7" -> CorrosionCurve.curveBeanList7;
//            case "8" -> CorrosionCurve.curveBeanList8;
//            case "9" -> CorrosionCurve.curveBeanList9;
//            case "11" -> CorrosionCurve.curveBeanList11;
//            case "12" -> CorrosionCurve.curveBeanList12;
//            case "14" -> CorrosionCurve.curveBeanList14;
//            case "16" -> CorrosionCurve.curveBeanList16;
//            case "18" -> CorrosionCurve.curveBeanList18;
//            default -> CorrosionCurve.curveBeanList10;
//        };
//        int index = curveBeanList.size() - 1;
//        for (int i = 0; i < curveBeanList.size(); i++) {
//            int[] temp = curveBeanList.get(i).signalArray;
//            int sign = temp[temp.length / 2];
//            if (signal >= sign) {
//                index = i;
//                break;
//            }
//        }
//        StandardCurveBean minBean = curveBeanList.get(Math.max(index - 1, 0));
//        StandardCurveBean current = curveBeanList.get(index);
//        CheckDataBean dataBean = new CheckDataBean(poleNorm);
//        if (TextUtils.equals(minBean.uniqueKey, current.uniqueKey)) {
//            dataBean.thickness = minBean.thickness;
//            dataBean.curve_y = minBean.signalArray;
//        } else {
//            dataBean.thickness = StringUtils.getRounding(StringUtils.getInterpolationValue(minBean.signalArray[minBean.signalArray.length / 2], minBean.thickness, current.signalArray[current.signalArray.length / 2], current.thickness, signal), 2);
//            dataBean.curve_y = new int[minBean.signalArray.length];
//            for (int i = 0; i < minBean.signalArray.length; i++) {
//                dataBean.curve_y[i] = StringUtils.getInterpolationValueInt(minBean.thickness, minBean.signalArray[i], current.thickness, current.signalArray[i], dataBean.thickness);
//            }
//        }
//        return dataBean;
//    }
//
//    private void showErrorThickness() {
//        CustomMediaPlayer.startSpeaking(this, mediaPlayer, 1);
//        new Handler().postDelayed(() -> CustomMediaPlayer.startSpeaking(this, mediaPlayer, 1), 1000);
//        StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this, "yellow");
//        styleAlertDialog.setContent("数据异常，请重复测量或剔凿验证").setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
//    }
//
//    private int getSignalValue(int value) {
//        int offset = 0;
//        for (int i = 0; i < calibrationList.size(); i++) {
//            if (value >= calibrationList.get(i)) {
//                if (i - 1 >= 0) {
//                    int indexMax = calibrationList.get(i - 1) - calibrationList.get(i);
//                    int indexMin = 0;
//                    int max = calibrationList.get(i - 1) - calibrationStandardList.get(i - 1);
//                    int min = calibrationList.get(i) - calibrationStandardList.get(i);
//                    int indexCurrent = value - calibrationList.get(i);
//                    offset = (int) StringUtils.getRounding(StringUtils.getInterpolationValue(indexMax, max, indexMin, min, indexCurrent), 0);
//                }
//                break;
//            }
//        }
//        return value - offset;
//    }
//
//    private void treatAverage(RebarPageBean pageBean) {
//        double total0 = 0, average0, total1 = 0, average1;
//        double size = 0;
//        if (!pageBean.beanList.isEmpty()) {
//            for (RebarBean b : pageBean.beanList) {
//                if (b.thickness > 0) {
//                    total0 += b.thickness;
//                    total1 += b.thickness1;
//                    size++;
//                }
//            }
//            average0 = StringUtils.getRounding(total0 / size, 1);
//            average1 = StringUtils.getRounding(total1 / size, 1);
////            average0 = StringUtils.getRounding(average0, 0);
//        } else {
//            average0 = 0;
//            average1 = 0;
//        }
//        pageBean.averageThickness = average0;
//        pageBean.averageThickness1 = average1;
//    }
//
//    private boolean isAdd(RebarPageBean pageBean) {
//        return pageBean.beanList.size() < zoneBean.poleBottomCount;
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (RESULT_OK == resultCode) {
//            assert data != null;
//            zoneBean = (ZoneBean) data.getSerializableExtra("ZoneBean");
//            assert zoneBean != null;
//            if (zoneBean.id == null) {
//                cloneZoneBean = null;
//            }
//            binding.navigationBar.setProjectName(zoneBean.projectName).setZoneName(zoneBean.poleNo);
//            binding.navigationBar.setTitleText("");
//            double designThickness = Double.parseDouble(zoneBean.designThickness);
//            //+8 -2 剔凿验证
//            thicknessMin = StringUtils.getRounding(designThickness - 2, 1);
//            thicknessMax = StringUtils.getRounding(designThickness + 8, 1);
//            //+8 -2 剔凿验证
//            thicknessMin = -1;
//            thicknessMax = 100;
//            //
//            configGridTop(zoneBean.poleBottomCount);
//            refreshGrid();
//        }
//    }
//
//    private double poleCompensation;  //电杆直径补偿
//    private double rebarCompensation;  //钢筋直径补偿
//    private double radius;
//
//    private void configCalibration(String strDistance) {
//        if (zoneBean != null) {
//            double tempDistance = Double.parseDouble(strDistance);
//            radius = StringUtils.getInterpolationValue(0.001, zoneBean.poleBottomDiameter, Double.parseDouble(zoneBean.poleHeight), zoneBean.poleTopDiameter, tempDistance) / 2;
//            double ac = radius + 22;  //22：钢筋仪轱辘半径22mm
//            double cd = 52 / 2.0;
//            double ad = Math.sqrt(ac * ac - cd * cd);
//            //电杆直径补偿
//            poleCompensation = StringUtils.getRounding(ac - ad, 2);
//            //钢筋直径补偿
//            rebarCompensation = getRebarCompensation();
//        }
//    }
//
//    private double getRebarCompensation() {
//        String rebarDiameter = StaticConstant.rebarDiameterList.get(zoneBean.rebarDiameter);
//        return switch (rebarDiameter) {
//            case "6" -> 0.425;
//            case "7" -> 0.315;
//            case "8" -> 0.86;
//            case "9" -> 0.43;
//            case "10" -> 0.86;
//            case "11" -> 0.475;
//            case "12" -> 0.8;
//            case "14" -> 0.86;
//            case "16" -> 0.86;
//            case "18" -> 0.86;
//            default -> 0;
//        };
//    }
//
//    /**
//     * 刷新钢筋列表
//     */
//    private void refreshGrid() {
//        binding.layoutGridContent.removeAllViews();
//        for (int i = 0; i < zoneBean.rebarPageList.size(); i++) {
//            View itemView = LayoutInflater.from(this).inflate(R.layout.view_grid_content, null, false);
//            configGrid(itemView, zoneBean.poleBottomCount, zoneBean.rebarPageList.get(i));
//            itemView.setId(i);
//            itemView.setOnLongClickListener(gridItemView);
//            binding.layoutGridContent.addView(itemView);
//        }
//    }
//
//    /**
//     * 删除
//     */
//    private final View.OnLongClickListener gridItemView = v -> {
//        RebarPageBean pageBean = zoneBean.rebarPageList.get(v.getId());
//        StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this, "red");
//        styleAlertDialog.setContent("确定删除“" + pageBean.segment + "”的数据？").setLeftButton("取消", v1 -> styleAlertDialog.dismiss()).setRightButton("确定", v1 -> {
//            styleAlertDialog.dismiss();
//            zoneBean.rebarPageList.remove(v.getId());
//            //刷新钢筋列表
//            refreshGrid();
//            //刷新按钮
//            if (!zoneBean.rebarPageList.isEmpty()) {
//                refreshButon(true, zoneBean.rebarPageList.get(zoneBean.rebarPageList.size() - 1));
//            } else {
//                refreshButon(false, null);
//            }
//        }).show();
//        return false;
//    };
//
//    /**
//     * 刷新检测按钮状态
//     */
//    private void refreshButon(boolean flag, RebarPageBean pageBean) {
//        if (flag) {
//            flag = pageBean.beanList.isEmpty();
//        }
//        binding.btnCheck.setEnabled(!flag);
//        binding.btnCheck.setBackgroundResource(!flag ? R.drawable.bg_button_btn_green : R.drawable.bg_button_gray);
//        binding.btnSetting.setEnabled(!flag);
//        binding.btnSetting.setBackgroundResource(!flag ? R.drawable.bg_button_btn_green : R.drawable.bg_button_gray);
//        binding.btnSave.setEnabled(!flag);
//        binding.btnSave.setBackgroundResource(!flag ? R.drawable.bg_button_btn_green : R.drawable.bg_button_gray);
//        binding.btnFile.setEnabled(!flag);
//        binding.btnFile.setBackgroundResource(!flag ? R.drawable.bg_button_btn_green : R.drawable.bg_button_gray);
//    }
//
//    public void onViewClicked(View view) {
//        OtherUtils.hideKeyboard(this);
//        if (view.getId() == binding.btnCheck.getId()) {
//            checkThickness();
//        } else if (view.getId() == binding.btnSetting.getId()) {
//            {
//                Intent intent = new Intent(this, DetectConfigActivity.class);
//                intent.putExtra("ZoneBean", zoneBean);
//                startActivityForResult(intent, 1);
//            }
//        } else if (view.getId() == binding.btnSave.getId()) {
//            if (zoneBean == null) {
//                ToastUtils.showShort("请进行参数设置");
//                return;
//            }
//            if (zoneBean.rebarPageList.isEmpty()) {
//                ToastUtils.showShort("请先检测数据");
//                return;
//            }
//            zoneBean.startTime = StringUtils.getTime();
//            zoneBean.id = GreenDaoHelper.getDaoSession().getZoneBeanDao().insertOrReplace(zoneBean);
//            ToastUtils.showShort("保存成功");
//            String strClone = new Gson().toJson(zoneBean);
//            cloneZoneBean = new Gson().fromJson(strClone, new TypeToken<ZoneBean>() {
//            }.getType());
//            int lastIndex = cloneZoneBean.rebarPageList.size() - 1;
//            int lastBeanListSize = cloneZoneBean.rebarPageList.get(lastIndex).beanList.size();
//            if (lastBeanListSize == 0) {
//                cloneZoneBean.rebarPageList.remove(lastIndex);
//            }
//            binding.btnSave.setEnabled(false);
//            binding.btnSave.setBackgroundResource(R.drawable.bg_button_gray);
//        } else if (view.getId() == binding.btnFile.getId()) {
//            Intent intent = new Intent(this, DataFileManagerActivity.class);
//            intent.putExtra("projectName", zoneBean == null ? "" : zoneBean.projectName);
//            intent.putExtra("poleId", zoneBean == null ? "" : zoneBean.id);
//            startActivity(intent);
//        } else if (view.getId() == binding.btnFinish.getId()) {
//            String showText = getShowText();
//            StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this, "red");
//            styleAlertDialog.setContent(showText).setLeftButton("取消", v1 -> styleAlertDialog.dismiss()).setRightButton("确定", v1 -> {
//                styleAlertDialog.dismiss();
//                finish();
//            }).show();
//        } else if (view.getId() == binding.tvTabDetection.getId()) {
//            showView(0);
//        } else if (view.getId() == binding.tvTabForecast.getId()) {
//            showView(1);
//        } else if (view.getId() == binding.btnForecast.getId()) {
//            double currentDistance;
//            {
//                if (zoneBean == null) {
//                    ToastUtils.showShort("请进行参数设置");
//                    return;
//                }
//                String strDistance = binding.etDistance1.getText().toString().trim();
//                if (TextUtils.isEmpty(strDistance)) {
//                    ToastUtils.showShort("请填写完整");
//                    return;
//                }
//                currentDistance = Double.parseDouble(strDistance);
//            }
//            isForecastStart = !isForecastStart;
//            if (isForecastStart) {
//                binding.btnForecast.setText("结束");
//                valueMin = 10000;
//                valueMax = 0;
//            } else {
//                binding.btnForecast.setText("开始");
//                if (valueMin == 0 || valueMax == 0) {
//                    ToastUtils.showShort("信号值为0，无法预估");
//                    return;
//                }
//                //预估直径
//                double rate = StringUtils.getRounding(100.0 * (valueMax - valueMin) / valueMin, 2);
//                String strForecast;
//                boolean flag = currentDistance >= Double.parseDouble(zoneBean.poleHeight) - 3;
//                if (flag) {
//                    strForecast = rate <= 20 ? "14" : "10";
//                } else {
//                    strForecast = rate <= 40 ? "14" : "10";
//                }
//                binding.tvForecast.setText(Html.fromHtml("<font color=#000000>预估直径(mm)：</font><font color=#006F5F>" + strForecast + "</font>"));
//            }
//        }
//    }
//
//    private boolean isForecastStart;
//
//    private void checkThickness() {
//        if (zoneBean == null) {
//            ToastUtils.showShort("请进行参数设置");
//            return;
//        }
//        if (zoneBean.rebarPageList.size() >= 10) {
//            ToastUtils.showShort("最多检测10个位置");
//            return;
//        }
//        String strDistance = binding.etDistance.getText().toString().trim();
//        if (TextUtils.isEmpty(strDistance)) {
//            ToastUtils.showShort("请填写完整");
//            return;
//        }
//        try {
//            Double.parseDouble(strDistance);
//        } catch (NumberFormatException ignored) {
//            ToastUtils.showShort("请填写完整");
//            return;
//        }
//        poleList.clear();
//        setINVISIBLE();
//        configCalibration(strDistance);
//        String segment = "距根部" + strDistance + "m";
//        zoneBean.rebarPageList.add(new RebarPageBean(segment, Double.parseDouble(strDistance)));
//        refreshGrid();
//        binding.scrollView.post(() -> binding.scrollView.fullScroll(View.FOCUS_DOWN));
//        //
//        binding.btnCheck.setEnabled(false);
//        binding.btnCheck.setBackgroundResource(R.drawable.bg_button_gray);
//        binding.btnSetting.setEnabled(false);
//        binding.btnSetting.setBackgroundResource(R.drawable.bg_button_gray);
//        binding.btnSave.setEnabled(false);
//        binding.btnSave.setBackgroundResource(R.drawable.bg_button_gray);
//        binding.btnFile.setEnabled(false);
//        binding.btnFile.setBackgroundResource(R.drawable.bg_button_gray);
//    }
//
//    private int checkType;  //0:单点检测，1：预估直径
//
//    private void showView(int position) {
//        checkType = position;
//        binding.tvTabDetection.setTextColor(color8E8E8E);
//        binding.tvTabForecast.setTextColor(color8E8E8E);
//        Typeface typefaceNormal = Typeface.create("sans-serif-bold", Typeface.NORMAL);
//        binding.tvTabDetection.setTypeface(typefaceNormal);
//        binding.tvTabForecast.setTypeface(typefaceNormal);
//        binding.viewTabDetection.setBackgroundColor(colorFFF);
//        binding.viewTabForecast.setBackgroundColor(colorFFF);
//        binding.layoutDetection.setVisibility(View.GONE);
//        binding.layoutForecast.setVisibility(View.GONE);
//        Typeface typefaceBold = Typeface.create("sans-serif-bold", Typeface.BOLD);
//        switch (position) {
//            case 0:
//                binding.tvTabDetection.setTextColor(color006F5F);
//                binding.tvTabDetection.setTypeface(typefaceBold);
//                binding.viewTabDetection.setBackgroundColor(color006F5F);
//                binding.layoutDetection.setVisibility(View.VISIBLE);
//                break;
//            case 1:
//                binding.tvTabForecast.setTextColor(color006F5F);
//                binding.tvTabForecast.setTypeface(typefaceBold);
//                binding.viewTabForecast.setBackgroundColor(color006F5F);
//                binding.layoutForecast.setVisibility(View.VISIBLE);
//                break;
//        }
//    }
//
//    private void refreshTvCheck() {
//        setINVISIBLE();
//        if (poleList.isEmpty()) {
//            if (!checkDataList.isEmpty()) binding.tvCheck11.setVisibility(View.VISIBLE);
//            if (checkDataList.size() >= 2) binding.tvCheck12.setVisibility(View.VISIBLE);
//            if (checkDataList.size() >= 3) binding.tvCheck13.setVisibility(View.VISIBLE);
//        } else {
//            binding.tvCheck11.setVisibility(View.VISIBLE);
//            binding.tvCheck12.setVisibility(View.VISIBLE);
//            binding.tvCheck13.setVisibility(View.VISIBLE);
//        }
//        //
//        if (poleList.size() == 1) {
//            if (!checkDataList.isEmpty()) binding.tvCheck21.setVisibility(View.VISIBLE);
//            if (checkDataList.size() >= 2) binding.tvCheck22.setVisibility(View.VISIBLE);
//            if (checkDataList.size() >= 3) binding.tvCheck23.setVisibility(View.VISIBLE);
//        } else if (poleList.size() > 1) {
//            binding.tvCheck21.setVisibility(View.VISIBLE);
//            binding.tvCheck22.setVisibility(View.VISIBLE);
//            binding.tvCheck23.setVisibility(View.VISIBLE);
//        }
//        //
//        if (poleList.size() == 2) {
//            if (!checkDataList.isEmpty()) binding.tvCheck31.setVisibility(View.VISIBLE);
//            if (checkDataList.size() >= 2) binding.tvCheck32.setVisibility(View.VISIBLE);
//            if (checkDataList.size() >= 3) binding.tvCheck33.setVisibility(View.VISIBLE);
//        } else if (poleList.size() > 2) {
//            binding.tvCheck31.setVisibility(View.VISIBLE);
//            binding.tvCheck32.setVisibility(View.VISIBLE);
//            binding.tvCheck33.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void setINVISIBLE() {
//        binding.tvCheck11.setVisibility(View.INVISIBLE);
//        binding.tvCheck12.setVisibility(View.INVISIBLE);
//        binding.tvCheck13.setVisibility(View.INVISIBLE);
//        binding.tvCheck21.setVisibility(View.INVISIBLE);
//        binding.tvCheck22.setVisibility(View.INVISIBLE);
//        binding.tvCheck23.setVisibility(View.INVISIBLE);
//        binding.tvCheck31.setVisibility(View.INVISIBLE);
//        binding.tvCheck32.setVisibility(View.INVISIBLE);
//        binding.tvCheck33.setVisibility(View.INVISIBLE);
//    }
//
//    @NonNull
//    private String getShowText() {
//        boolean isShowSave = false;
//        if (zoneBean != null) {
//            if (!zoneBean.rebarPageList.isEmpty()) {
//                int rebarPageListSize = zoneBean.rebarPageList.size();
//                int lastBeanListSize = zoneBean.rebarPageList.get(zoneBean.rebarPageList.size() - 1).beanList.size();
//                if (lastBeanListSize == 0) {
//                    rebarPageListSize -= 1;
//                }
//                if (zoneBean.id == null) {
//                    isShowSave = rebarPageListSize > 0;
//                } else {
//                    if (rebarPageListSize != cloneZoneBean.rebarPageList.size()) {
//                        isShowSave = true;
//                    }
//                    if (!isShowSave) {
//                        for (int i = 0; i < rebarPageListSize; i++) {
//                            if (zoneBean.rebarPageList.get(i).beanList.size() != cloneZoneBean.rebarPageList.get(i).beanList.size()) {
//                                isShowSave = true;
//                            }
//                        }
//                    }
//                    if (!isShowSave) {
//                        boolean flag = false;
//                        for (int i = 0; i < rebarPageListSize; i++) {
//                            if (flag) {
//                                break;
//                            }
//                            RebarPageBean rPageBean = zoneBean.rebarPageList.get(i);
//                            RebarPageBean tPageBean = cloneZoneBean.rebarPageList.get(i);
//                            for (int j = 0; j < rPageBean.beanList.size(); j++) {
//                                RebarBean rRebarBean = rPageBean.beanList.get(j);
//                                RebarBean tRebarBean = tPageBean.beanList.get(j);
//                                if (rRebarBean.distance != tRebarBean.distance) {
//                                    isShowSave = true;
//                                    flag = true;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return isShowSave ? "数据未存储，确定退出检测？" : "确定退出检测？";
//    }
//
//    private void configGrid(View itemView, int rebarCount, RebarPageBean bean) {
//        TextView tvNumber = itemView.findViewById(R.id.tv_grid_number);
//        TextView tv1 = itemView.findViewById(R.id.tv_grid_1);
//        View v1 = itemView.findViewById(R.id.v_grid_1);
//        tv1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
//        v1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
//        TextView tv2 = itemView.findViewById(R.id.tv_grid_2);
//        View v2 = itemView.findViewById(R.id.v_grid_2);
//        tv2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
//        v2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
//        TextView tv3 = itemView.findViewById(R.id.tv_grid_3);
//        View v3 = itemView.findViewById(R.id.v_grid_3);
//        tv3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
//        v3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
//        TextView tv4 = itemView.findViewById(R.id.tv_grid_4);
//        View v4 = itemView.findViewById(R.id.v_grid_4);
//        tv4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
//        v4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
//        TextView tv5 = itemView.findViewById(R.id.tv_grid_5);
//        View v5 = itemView.findViewById(R.id.v_grid_5);
//        tv5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
//        v5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
//        TextView tv6 = itemView.findViewById(R.id.tv_grid_6);
//        View v6 = itemView.findViewById(R.id.v_grid_6);
//        tv6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
//        v6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
//        TextView tv7 = itemView.findViewById(R.id.tv_grid_7);
//        View v7 = itemView.findViewById(R.id.v_grid_7);
//        tv7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
//        v7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
//        TextView tv8 = itemView.findViewById(R.id.tv_grid_8);
//        View v8 = itemView.findViewById(R.id.v_grid_8);
//        tv8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
//        v8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
//        TextView tv9 = itemView.findViewById(R.id.tv_grid_9);
//        View v9 = itemView.findViewById(R.id.v_grid_9);
//        tv9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
//        v9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
//        TextView tv10 = itemView.findViewById(R.id.tv_grid_10);
//        View v10 = itemView.findViewById(R.id.v_grid_10);
//        tv10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
//        v10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
//        TextView tvAver = itemView.findViewById(R.id.tv_grid_average);
//        tvAver.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
//        //
//        tvNumber.setText(bean.segment);
//        if (bean.beanList == null) {
//            return;
//        }
//        if (!bean.beanList.isEmpty()) {
//            setTextView(tv1, bean.beanList.get(0));
//        }
//        if (bean.beanList.size() > 1) {
//            setTextView(tv2, bean.beanList.get(1));
//        }
//        if (bean.beanList.size() > 2) {
//            setTextView(tv3, bean.beanList.get(2));
//        }
//        if (bean.beanList.size() > 3) {
//            setTextView(tv4, bean.beanList.get(3));
//        }
//        if (bean.beanList.size() > 4) {
//            setTextView(tv5, bean.beanList.get(4));
//        }
//        if (bean.beanList.size() > 5) {
//            setTextView(tv6, bean.beanList.get(5));
//        }
//        if (bean.beanList.size() > 6) {
//            setTextView(tv7, bean.beanList.get(6));
//        }
//        if (bean.beanList.size() > 7) {
//            setTextView(tv8, bean.beanList.get(7));
//        }
//        if (bean.beanList.size() > 8) {
//            setTextView(tv9, bean.beanList.get(8));
//        }
//        if (bean.beanList.size() > 9) {
//            setTextView(tv10, bean.beanList.get(9));
//        }
//        double total0 = 0, average0, total1 = 0, average1;
//        double size = 0;
//        if (!bean.beanList.isEmpty()) {
//            for (RebarBean b : bean.beanList) {
//                if (b.thickness > 0) {
//                    total0 += b.thickness;
//                    total1 += b.thickness1;
//                    size++;
//                }
//            }
//            average0 = StringUtils.getRounding(total0 / size, 2);
//            average1 = StringUtils.getRounding(total1 / size, 2);
//            average0 = StringUtils.getRounding(average0, 1);
//            average1 = StringUtils.getRounding(average1, 1);
//        } else {
//            average0 = 0;
//            average1 = 0;
//        }
//        tvAver.setText(average1 + "\n" + average0);
//    }
//
//    private void setTextView(TextView textView, RebarBean bean) {
//        textView.setText(bean.thickness1 + "\n" + bean.thickness);
//        textView.setTextColor((bean.thickness1 > thicknessMax || bean.thickness1 < thicknessMin) ? colorFF9600 : color000);
//    }
//
//    private void configGridTop(int rebarCount) {
//        binding.layoutGridTop.tvGrid1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.vGrid1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.tvGrid2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.vGrid2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.tvGrid3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.vGrid3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.tvGrid4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.vGrid4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.tvGrid5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.vGrid5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.tvGrid6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.vGrid6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.tvGrid7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.vGrid7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.tvGrid8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.vGrid8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.tvGrid9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.vGrid9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.tvGrid10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.vGrid10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
//        binding.layoutGridTop.tvGridAverage.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        scheduledExecutorService.shutdownNow();
//        CustomMediaPlayer.releasePlayer(mediaPlayer);
//        BluetoothManager.unNotifyBluetoothDevice(spMac);
//        BluetoothManager.unregisterConnectStatusListener(spMac, mBleConnectStatusListener);
//    }
//}