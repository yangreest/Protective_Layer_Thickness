package com.example.gtj_sd_dg240.activity.testing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.activity.data.DataFileManagerActivity;
import com.example.gtj_sd_dg240.activity.start.BaseActivity;
import com.example.gtj_sd_dg240.databinding.ActDetectBinding;
import com.example.gtj_sd_dg240.model.Number;
import com.example.gtj_sd_dg240.model.NumberBean1;
import com.example.gtj_sd_dg240.model.RebarBean;
import com.example.gtj_sd_dg240.model.RebarPageBean;
import com.example.gtj_sd_dg240.model.ZoneBean;
import com.example.gtj_sd_dg240.stacon.StaCon;
import com.example.gtj_sd_dg240.stacon.StaCon_14_4;
import com.example.gtj_sd_dg240.storage.GreenDaoHelper;
import com.example.gtj_sd_dg240.utils.BytesConversion;
import com.example.gtj_sd_dg240.utils.CalculatedThicknessUtils;
import com.example.gtj_sd_dg240.utils.CalculatedThicknessUtils_10_4;
import com.example.gtj_sd_dg240.utils.CalculatedThicknessUtils_11_3;
import com.example.gtj_sd_dg240.utils.CalculatedThicknessUtils_11_4;
import com.example.gtj_sd_dg240.utils.CalculatedThicknessUtils_7_3;
import com.example.gtj_sd_dg240.utils.CalculatedThicknessUtils_9_3;
import com.example.gtj_sd_dg240.utils.CalculatedThicknessUtils_9_4;
import com.example.gtj_sd_dg240.utils.CustomMediaPlayer;
import com.example.gtj_sd_dg240.utils.HexUtil;
import com.example.gtj_sd_dg240.utils.OtherUtils;
import com.example.gtj_sd_dg240.utils.SpinnerUtils;
import com.example.gtj_sd_dg240.utils.StringUtils;
import com.example.gtj_sd_dg240.view.StyleAlertDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressLint({"InflateParams", "SetTextI18n", "UseCompatLoadingForDrawables"})
public class DetectActivity extends BaseActivity<ActDetectBinding> {
    private int color006F5F, color8E8E8E, colorFFF, color000, colorFF9600;
    private ZoneBean zoneBean, cloneZoneBean;
    private double currentDistance;
    private List<NumberBean1> signalValue = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private List<Integer> calibrationStandardList = new ArrayList<>();
    private List<Integer> calibrationList = new ArrayList<>();
    private int checkType;  //0:单点检测，1：预估直径
    private String strColor;

    @Override
    protected ActDetectBinding getBinding() {
        return ActDetectBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        showView(0);
    }

    private void initView() {
        color006F5F = getColor(R.color.color006F5F);
        color8E8E8E = getColor(R.color.color8E8E8E);
        colorFFF = getColor(R.color.colorFFF);
        color000 = getColor(R.color.color000);
        colorFF9600 = getColor(R.color.colorFF9600);
        strColor = TextUtils.equals(StaticConstant.APP_DEFAULT, StaticConstant.APP_GTJ) ? "#00C3C6" : "#006F5F";
        binding.btnCheck.setOnClickListener(this::onViewClicked);
        binding.btnCheckSingle.setOnClickListener(this::onViewClicked);
        binding.btnSetting.setOnClickListener(this::onViewClicked);
        binding.btnSave.setOnClickListener(this::onViewClicked);
        binding.btnFile.setOnClickListener(this::onViewClicked);
        binding.btnFinish.setOnClickListener(this::onViewClicked);
        binding.btnDelete.setOnClickListener(this::onViewClicked);
        binding.tvTabDetection.setOnClickListener(this::onViewClicked);
        binding.tvTabForecast.setOnClickListener(this::onViewClicked);
        binding.btnForecast.setOnClickListener(this::onViewClicked);
        setHideKeyboard();
        binding.navigationBar.setTitleText("单点检测");
        initBatteryReceiver(binding.navigationBar);
        initBluetoothStatus(binding.navigationBar);
        registerBluetoothDataCallback();
        mediaPlayer = CustomMediaPlayer.getMediaPlayer(null);
        SpinnerUtils.attachDataSource(binding.spInterval, StaticConstant.stirrupSpacingList, null);  //箍筋间距
        aaa();
        calibrationSignalValue();
    }

    private final List<Number> tempList = new ArrayList<>();
    private int index;

    private void aaa() {
        tempList.add(new Number(71, 1647));
        tempList.add(new Number(147, 1910));
        tempList.add(new Number(214, 1960));
        tempList.add(new Number(304, 2038));
        tempList.add(new Number(373, 2342));
        tempList.add(new Number(434, 2150));
        tempList.add(new Number(499, 1830));
        tempList.add(new Number(575, 1989));
        tempList.add(new Number(636, 1784));
    }

    private void calibrationSignalValue() {
        String strCalibrationStandardList = SPUtils.getInstance().getString("calibration-standard-list");
        calibrationStandardList = new Gson().fromJson(strCalibrationStandardList, new TypeToken<List<Integer>>() {
        }.getType());
        String strCalibrationList = SPUtils.getInstance().getString("calibration-list");
        calibrationList = new Gson().fromJson(strCalibrationList, new TypeToken<List<Integer>>() {
        }.getType());
        binding.etDistance.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.etDistance.isFocused()) {
                    binding.etDistance1.setText(s);
                }
            }
        });
        binding.etDistance1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.etDistance1.isFocused()) {
                    binding.etDistance.setText(s);
                }
            }
        });
    }

    private final StringBuffer stringBuffer = new StringBuffer();

    @Override
    protected void onBluetoothDataReceived(byte[] data) {
        if (isShow) {
            String string = StringUtils.ByteArraytoHex(data);
            if (checkType == 0) {  //单点检测
                String[] array = string.split(" ");
                if (TextUtils.equals("F1", array[0]) && array.length == 8) {
                    //F1	位置高	位置低	左信号高	左信号低	右信号高	右信号低	FF
                    notifyDetectionData(array);
                }
            } else if (checkType == 1) {  //预估直径
                if (isForecastStart) {
                    stringBuffer.append(string);
                    while (stringBuffer.length() >= 18) {
                        showForecast(stringBuffer.substring(0, 18));
                        stringBuffer.delete(0, 18);
                    }
                }
            }
        }
    }

    private void refreshTvCheck() {
        setINVISIBLE();
        if (!signalValue.isEmpty()) {
            NumberBean1 bean = signalValue.get(0);
            if (!bean.numberList.isEmpty()) binding.tvCheck11.setVisibility(View.VISIBLE);
            if (bean.numberList.size() > 1) binding.tvCheck12.setVisibility(View.VISIBLE);
            if (bean.numberList.size() > 2) binding.tvCheck13.setVisibility(View.VISIBLE);
        }
        if (signalValue.size() > 1) {
            NumberBean1 bean = signalValue.get(1);
            if (!bean.numberList.isEmpty()) binding.tvCheck21.setVisibility(View.VISIBLE);
            if (bean.numberList.size() > 1) binding.tvCheck22.setVisibility(View.VISIBLE);
            if (bean.numberList.size() > 2) binding.tvCheck23.setVisibility(View.VISIBLE);
        }
        if (signalValue.size() > 2) {
            NumberBean1 bean = signalValue.get(2);
            if (!bean.numberList.isEmpty()) binding.tvCheck31.setVisibility(View.VISIBLE);
            if (bean.numberList.size() > 1) binding.tvCheck32.setVisibility(View.VISIBLE);
            if (bean.numberList.size() > 2) binding.tvCheck33.setVisibility(View.VISIBLE);
        }
    }

    private void setINVISIBLE() {
        binding.tvCheck11.setVisibility(View.INVISIBLE);
        binding.tvCheck12.setVisibility(View.INVISIBLE);
        binding.tvCheck13.setVisibility(View.INVISIBLE);
        binding.tvCheck21.setVisibility(View.INVISIBLE);
        binding.tvCheck22.setVisibility(View.INVISIBLE);
        binding.tvCheck23.setVisibility(View.INVISIBLE);
        binding.tvCheck31.setVisibility(View.INVISIBLE);
        binding.tvCheck32.setVisibility(View.INVISIBLE);
        binding.tvCheck33.setVisibility(View.INVISIBLE);
    }

    private void refreshBtnCheck(boolean flag) {
        isStartCheck = flag;
        binding.btnCheckSingle.setEnabled(!flag);
        binding.btnCheckSingle.setBackgroundResource(!flag ? R.drawable.bg_button_btn_green : R.drawable.bg_button_gray);
    }

    private boolean isStartCheck;
    private NumberBean1 numberBean1;

    private void treatData(int distance, int signalLeft, int signalRight, int value) {
        if (signalValue != null && signalValue.size() == 3 && signalValue.get(2).numberList != null && signalValue.get(2).numberList.size() == 3) {
//            ToastUtils.showShort("检测完成");
            return;
        }
        if (!isStartCheck) {
            return;
        }
        if (numberBean1 == null) {
            numberBean1 = new NumberBean1();
            signalValue.add(numberBean1);
        }
        if (numberBean1.numberList.size() < 3) {
            numberBean1.numberList.add(new Number(distance, value));
        }
        if (numberBean1.numberList.size() == 3) {
            numberBean1 = null;
        }
        refreshBtnCheck(!(signalValue != null && signalValue.size() == 3 && signalValue.get(2).numberList.size() == 3));
        RebarPageBean pageBean = zoneBean.rebarPageList.get(zoneBean.rebarPageList.size() - 1);
        if (signalValue.size() == 3 && signalValue.get(2).numberList.size() == 3) {
            List<Number> s1 = new ArrayList<>();
            s1.add(signalValue.get(0).numberList.get(0));
            s1.add(signalValue.get(1).numberList.get(0));
            s1.add(signalValue.get(2).numberList.get(0));
            List<Number> s2 = new ArrayList<>();
            s2.add(signalValue.get(0).numberList.get(1));
            s2.add(signalValue.get(1).numberList.get(1));
            s2.add(signalValue.get(2).numberList.get(1));
            List<Number> s3 = new ArrayList<>();
            s3.add(signalValue.get(0).numberList.get(2));
            s3.add(signalValue.get(1).numberList.get(2));
            s3.add(signalValue.get(2).numberList.get(2));
            int rebarDiameter = Integer.parseInt(StaticConstant.rebarDiameterList.get(zoneBean.rebarDiameter));
            double[] d1 = getThickness(s1, 0, rebarDiameter);
            double[] d2 = getThickness(s2, 0, rebarDiameter);
            double[] d3 = getThickness(s3, 0, rebarDiameter);
            double maxThickness = 0;
            switch (rebarDiameter) {
                case StaCon.POLE_D_7:
                case StaCon.POLE_D_9:
                case StaCon.POLE_D_10:
                case StaCon.POLE_D_11: {
                    //计算平均厚度
                    maxThickness = (d1[0] + d2[0] + d3[0]) / 3;
                }
                break;
                case StaCon.POLE_D_14: {
                    //计算最大厚度
                    maxThickness = d1[0];
                    if (d2[0] > maxThickness) {
                        maxThickness = d2[0];
                    }
                    if (d3[0] > maxThickness) {
                        maxThickness = d3[0];
                    }
                }
                break;
            }
            //根据最大厚度计算半径
            double radiusMin = radius - maxThickness;
            //计算最小间距弧距
            int minNumber0, minNumber1, minNumber2;
            String string0, string1, string2;
            {
                int number1 = signalValue.get(0).numberList.get(1).distance - signalValue.get(0).numberList.get(0).distance;
                int number2 = signalValue.get(0).numberList.get(2).distance - signalValue.get(0).numberList.get(1).distance;
                if (number1 < number2) {
                    minNumber0 = number1;
                    string0 = "左";
                } else {
                    minNumber0 = number2;
                    string0 = "右";
                }
            }
            {
                int number1 = signalValue.get(1).numberList.get(1).distance - signalValue.get(1).numberList.get(0).distance;
                int number2 = signalValue.get(1).numberList.get(2).distance - signalValue.get(1).numberList.get(1).distance;
                if (number1 < number2) {
                    minNumber1 = number1;
                    string1 = "左";
                } else {
                    minNumber1 = number2;
                    string1 = "右";
                }
            }
            {
                int number1 = signalValue.get(2).numberList.get(1).distance - signalValue.get(2).numberList.get(0).distance;
                int number2 = signalValue.get(2).numberList.get(2).distance - signalValue.get(2).numberList.get(1).distance;
                if (number1 < number2) {
                    minNumber2 = number1;
                    string2 = "左";
                } else {
                    minNumber2 = number2;
                    string2 = "右";
                }
            }
            int minNumber = minNumber0;
            String string = string0;
            if (minNumber > minNumber1) {
                minNumber = minNumber1;
                string = string1;
            }
            if (minNumber > minNumber2) {
                minNumber = minNumber2;
                string = string2;
            }
            //计算最小间距角度
            double angle = StringUtils.getRounding(minNumber / (2 * Math.PI * radius) * 360 / 2.0, 1);
            double radian = Math.toRadians(angle);
            double minSpacing = StringUtils.getRounding(radiusMin * Math.sin(radian) * 2, 1);
            double tempSpacing = minSpacing;
            double rebarD2 = 0, compensation = 0;
            switch (rebarDiameter) {
                case StaCon.POLE_D_7:
                case StaCon.POLE_D_9:
                case StaCon.POLE_D_10:
                case StaCon.POLE_D_11: {
                    tempSpacing = StringUtils.getRounding(minSpacing * 0.95, 1);
                    double rebar1;
                    if (TextUtils.equals("左", string)) {
                        rebar1 = getThickness(s1, tempSpacing, rebarDiameter)[0] + zoneBean.hoopingDiameter + calibration;
                    } else {
                        rebar1 = getThickness(s3, tempSpacing, rebarDiameter)[0] + zoneBean.hoopingDiameter + calibration;
                    }
                    rebarD2 = getThickness(s2, tempSpacing, rebarDiameter)[0] + zoneBean.hoopingDiameter + calibration;
                    if (currentDistance >= Double.parseDouble(zoneBean.poleHeight) - 1.5) {
                        //梢部 补偿
                        if (rebarD2 >= 15) {
                            if (rebarD2 > rebar1) {
                                compensation = 0;
                            } else {
                                compensation = 0;
                            }
                        } else {
                            if (rebarD2 > rebar1) {
                                compensation = 0;
                            }
                        }
                    } else {
                        //中部、根部补偿
                        if (rebarD2 >= 15) {
                            compensation = 0.5;
                        } else {
                            compensation = 1;
                        }
                    }
                }
                break;
                case StaCon.POLE_D_14: {
                    int betweenIndex = 0;  //厚度补偿index
                    rebarD2 = d2[0] + zoneBean.hoopingDiameter + calibration;
                    for (int i = 0; i < CalculatedThicknessUtils.spaceBetween.length; i++) {
                        if (rebarD2 < CalculatedThicknessUtils.spaceBetween[i]) {
                            betweenIndex = i;
                            break;
                        }
                    }
                    if (tempSpacing < 60) {
                        if (tempSpacing >= 50) {
                            compensation = getCompensation_14(50, betweenIndex);
                        } else if (tempSpacing >= 40) {
                            compensation = getCompensation_14(40, 50, betweenIndex, tempSpacing);
                        } else if (tempSpacing >= 30) {
                            compensation = getCompensation_14(30, 40, betweenIndex, tempSpacing);
                        } else if (tempSpacing >= 25) {
                            compensation = getCompensation_14(25, 30, betweenIndex, tempSpacing);
                        } else {
                            compensation = getCompensation_14(25, betweenIndex);
                        }
                    }
                }
                break;
            }
            double rebarThickness = StringUtils.getRounding(rebarD2 + compensation, 1);
            double rThickness = StringUtils.getRounding(rebarThickness, 0);
            StringBuilder sbd = new StringBuilder();
            sbd.append("厚度：").append(d1[0]).append("、").append(d1[1]).append(",　").append(d2[0]).append("、").append(d2[1]).append(",　").append(d3[0]).append("、").append(d3[1]).append(",\n减去厚度的半径：").append(radiusMin);
            sbd.append("\n间距：").append(minNumber0).append(",　").append(minNumber1).append(",　").append(minNumber2).append(",　").append(string).append(minNumber).append(",　").append(angle).append("°,　").append(minSpacing);
            sbd.append("\n中段0.95间距: ").append(tempSpacing).append(",　间距补偿厚度：").append(compensation).append("\n");
            sbd.append(d2[0]).append(" + ").append(zoneBean.hoopingDiameter).append(" + ").append(calibration).append("+ ").append(compensation).append(" = ").append(rebarThickness).append(", ").append(rThickness).append("\n最小间距：").append(string).append(", ").append(tempSpacing);
//            tvSingle.setText(sbd);
            binding.tvThickness.setText(Html.fromHtml("<font color=#000000>当前厚度(mm)：</font><font color=" + strColor + ">" + rebarThickness + "</font>"));
            RebarBean rebarBean = new RebarBean(distance, signalLeft, signalRight, rThickness, pageBean.segment);
            rebarBean.thickness1 = rebarThickness;
            pageBean.beanList.add(rebarBean);
            //求平均
            treatAverage(pageBean);
            if (rebarThickness < 15) {
                showErrorThickness();
            }
        }
        refreshTvCheck();
        //刷新钢筋列表
        refreshGrid();
        //刷新按钮
        refreshButon(true, pageBean);
    }

    private int valueMin = 10000, valueMax = 0;
    private boolean isInvalid;  //无效时刻

    /**
     * 预估直径
     */
    private void showForecast(String string) {
        if (isInvalid) {
            return;
        }
        String[] array = string.split(" ");
        int min1 = BytesConversion.hex2decimal(array[1] + array[2]);
        int max1 = BytesConversion.hex2decimal(array[3] + array[4]);
        int min = getSignalValue(min1);
        int max = getSignalValue(max1);
        if (valueMin > min) {
            valueMin = min;
        }
        if (valueMax < max) {
            valueMax = max;
        }
        binding.tvForecastMin.setText(valueMin + "");
        binding.tvForecastMax.setText(valueMax + "");
        binding.tvForecastValue.setText(max + "");
    }

    private void showErrorThickness() {
        CustomMediaPlayer.startSpeaking(this, mediaPlayer, 1);
        new Handler().postDelayed(() -> CustomMediaPlayer.startSpeaking(this, mediaPlayer, 1), 1000);
        StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this, "yellow");
        styleAlertDialog.setContent("数据异常，请重复测量或剔凿验证").setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
    }

    private double getCompensation_14(int spMin, int spMax, int betweenIndex, double minSpacing) {
        double[] spaceMin, spaceMax;
        if (currentDistance <= 1.5 || currentDistance >= Double.parseDouble(zoneBean.poleHeight) - 1.5) {
            spaceMin = getSpace_14_70(spMin);
            spaceMax = getSpace_14_70(spMax);
        } else {
            spaceMin = getSpace_14_120(spMin);
            spaceMax = getSpace_14_120(spMax);
        }
        double space0 = spaceMin[betweenIndex];
        double space1 = spaceMax[betweenIndex];
        double compensation = StringUtils.getInterpolationValue(spMin, space0, spMax, space1, minSpacing);
        return StringUtils.getRounding(compensation, 1);
    }

    private double getCompensation_14(int spMin, int betweenIndex) {
        double[] spaceMin;
        if (currentDistance <= 1.5 || currentDistance >= Double.parseDouble(zoneBean.poleHeight) - 1.5) {
            spaceMin = getSpace_14_70(spMin);
        } else {
            spaceMin = getSpace_14_120(spMin);
        }
        return spaceMin[betweenIndex];
    }

    private double[] getSpace_14_70(int space) {
        return switch (space) {
            case 50 -> CalculatedThicknessUtils.space_14_70_50;
            case 40 -> CalculatedThicknessUtils.space_14_70_40;
            case 30 -> CalculatedThicknessUtils.space_14_70_30;
            case 25 -> CalculatedThicknessUtils.space_14_70_25;
            default -> CalculatedThicknessUtils.space_14_70_60;
        };
    }

    private double[] getSpace_14_120(int space) {
        return switch (space) {
            case 50 -> CalculatedThicknessUtils.space_14_120_50;
            case 40 -> CalculatedThicknessUtils.space_14_120_40;
            case 30, 25 -> CalculatedThicknessUtils.space_14_120_30;
            default -> CalculatedThicknessUtils.space_14_120_60;
        };
    }


    private double[] getThickness(List<Number> numberList, double effectiveSpacing, int rebarDiameter) {
        numberList.sort(Comparator.comparingInt(o -> o.number));
        double[] thickness = new double[2];
        switch (rebarDiameter) {
            case StaCon.POLE_D_7: {
                String strInterval = binding.spInterval.getSelectedItem() + "";
                if (effectiveSpacing != 0) {
                    if (effectiveSpacing >= 60) {
                        thickness = CalculatedThicknessUtils_7_3.getThickness_7_3_60(strInterval, numberList);
                    } else if (effectiveSpacing >= 50) {
                        thickness = CalculatedThicknessUtils_7_3.getThickness_7_3_50(strInterval, numberList);
                    } else if (effectiveSpacing >= 40) {
                        thickness = CalculatedThicknessUtils_7_3.getThickness_7_3_40(strInterval, numberList);
                    } else if (effectiveSpacing >= 30) {
                        thickness = CalculatedThicknessUtils_7_3.getThickness_7_3_30(strInterval, numberList);
                    } else {
                        thickness = CalculatedThicknessUtils_7_3.getThickness_7_3_30(strInterval, numberList);
                    }
                } else {
                    thickness = CalculatedThicknessUtils_7_3.getThickness_7_3_60(strInterval, numberList);
                }
            }
            break; //
            case StaCon.POLE_D_9: {
                String strInterval = binding.spInterval.getSelectedItem() + "";
                if (zoneBean.hoopingDiameter == StaCon.POLE_HD_3) {
                    if (effectiveSpacing != 0) {
                        if (effectiveSpacing >= 60) {
                            thickness = CalculatedThicknessUtils_9_3.getThickness_9_3_60(strInterval, numberList);
                        } else if (effectiveSpacing >= 50) {
                            thickness = CalculatedThicknessUtils_9_3.getThickness_9_3_50(strInterval, numberList);
                        } else if (effectiveSpacing >= 40) {
                            thickness = CalculatedThicknessUtils_9_3.getThickness_9_3_40(strInterval, numberList);
                        } else if (effectiveSpacing >= 30) {
                            thickness = CalculatedThicknessUtils_9_3.getThickness_9_3_30(strInterval, numberList);
                        } else {
                            thickness = CalculatedThicknessUtils_9_3.getThickness_9_3_30(strInterval, numberList);
                        }
                    } else {
                        thickness = CalculatedThicknessUtils_9_3.getThickness_9_3_60(strInterval, numberList);
                    }
                } else {
                    if (effectiveSpacing != 0) {
                        if (effectiveSpacing >= 60) {
                            thickness = CalculatedThicknessUtils_9_4.getThickness_9_4_60(strInterval, numberList);
                        } else if (effectiveSpacing >= 50) {
                            thickness = CalculatedThicknessUtils_9_4.getThickness_9_4_50(strInterval, numberList);
                        } else if (effectiveSpacing >= 40) {
                            thickness = CalculatedThicknessUtils_9_4.getThickness_9_4_40(strInterval, numberList);
                        } else if (effectiveSpacing >= 30) {
                            thickness = CalculatedThicknessUtils_9_4.getThickness_9_4_30(strInterval, numberList);
                        } else {
                            thickness = CalculatedThicknessUtils_9_4.getThickness_9_4_30(strInterval, numberList);
                        }
                    } else {
                        thickness = CalculatedThicknessUtils_9_4.getThickness_9_4_60(strInterval, numberList);
                    }
                }
            }
            break;
            case StaCon.POLE_D_10: {
                String strInterval = binding.spInterval.getSelectedItem() + "";
                if (effectiveSpacing != 0) {
                    if (effectiveSpacing >= 60) {
                        thickness = CalculatedThicknessUtils_10_4.getThickness_10_60(strInterval, numberList);
                    } else if (effectiveSpacing >= 50) {
                        thickness = CalculatedThicknessUtils_10_4.getThickness_10_50(strInterval, numberList);
                    } else if (effectiveSpacing >= 40) {
                        thickness = CalculatedThicknessUtils_10_4.getThickness_10_40(strInterval, numberList);
                    } else if (effectiveSpacing >= 30) {
                        thickness = CalculatedThicknessUtils_10_4.getThickness_10_30(strInterval, numberList);
                    } else {
                        thickness = CalculatedThicknessUtils_10_4.getThickness_10_30(strInterval, numberList);
                    }
                } else {
                    thickness = CalculatedThicknessUtils_10_4.getThickness_10_60(strInterval, numberList);
                }
            }
            break;
            case StaCon.POLE_D_11: {
                String strInterval = binding.spInterval.getSelectedItem() + "";
                if (zoneBean.hoopingDiameter == StaCon.POLE_HD_3) {
                    if (effectiveSpacing != 0) {
                        if (effectiveSpacing >= 60) {
                            thickness = CalculatedThicknessUtils_11_3.getThickness_11_3_60(strInterval, numberList);
                        } else if (effectiveSpacing >= 50) {
                            thickness = CalculatedThicknessUtils_11_3.getThickness_11_3_50(strInterval, numberList);
                        } else if (effectiveSpacing >= 40) {
                            thickness = CalculatedThicknessUtils_11_3.getThickness_11_3_40(strInterval, numberList);
                        } else if (effectiveSpacing >= 30) {
                            thickness = CalculatedThicknessUtils_11_3.getThickness_11_3_30(strInterval, numberList);
                        } else {
                            thickness = CalculatedThicknessUtils_11_3.getThickness_11_3_30(strInterval, numberList);
                        }
                    } else {
                        thickness = CalculatedThicknessUtils_11_3.getThickness_11_3_60(strInterval, numberList);
                    }
                } else {
                    if (effectiveSpacing != 0) {
                        if (effectiveSpacing >= 60) {
                            thickness = CalculatedThicknessUtils_11_4.getThickness_11_4_60(strInterval, numberList);
                        } else if (effectiveSpacing >= 50) {
                            thickness = CalculatedThicknessUtils_11_4.getThickness_11_4_50(strInterval, numberList);
                        } else if (effectiveSpacing >= 40) {
                            thickness = CalculatedThicknessUtils_11_4.getThickness_11_4_40(strInterval, numberList);
                        } else if (effectiveSpacing >= 30) {
                            thickness = CalculatedThicknessUtils_11_4.getThickness_11_4_30(strInterval, numberList);
                        } else {
                            thickness = CalculatedThicknessUtils_11_4.getThickness_11_4_30(strInterval, numberList);
                        }
                    } else {
                        thickness = CalculatedThicknessUtils_11_4.getThickness_11_4_60(strInterval, numberList);
                    }
                }
            }
            break;
            case StaCon.POLE_D_14: {
                if (currentDistance <= 1.5 || currentDistance >= Double.parseDouble(zoneBean.poleHeight) - 1.5) {
                    thickness = CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_70_min, StaCon_14_4.signalList_14_70_max, numberList);
                } else {
                    thickness = CalculatedThicknessUtils.getThickness(StaCon_14_4.signalList_14_120_min, StaCon_14_4.signalList_14_120_max, numberList);
                }
            }
            break;

        }
        return thickness;
    }

    private int cuDistance;  //小车位置

    /**
     * 单点检测
     */
    private void notifyDetectionData(String[] array) {
        if (zoneBean == null) {
            return;
        }
        //F1	位置高	位置低	左信号高	左信号低	右信号高	右信号低	FF
        int distance = BytesConversion.hex2decimal(array[1] + array[2]);  //位置
        int signalLeft = BytesConversion.hex2decimal(array[3] + array[4]);  //左信号
        int signalRight = BytesConversion.hex2decimal(array[5] + array[6]);  //右信号
        int value = (signalLeft + signalRight) / 2;
        if (Math.abs(distance - cuDistance) < 10) {
            return;
        }
        cuDistance = distance;
        //
        if (zoneBean.rebarPageList.isEmpty()) {
            return;
        }
        //校准钢筋10信号值
        int value1 = getSignalValue(value);
//            tvSingle1.setText(value + " , " + value1);
        treatData(distance, signalLeft, signalRight, value1);
    }

    private int getSignalValue(int value) {
        int offset = 0;
        for (int i = 0; i < calibrationList.size(); i++) {
            if (value >= calibrationList.get(i)) {
                if (i - 1 >= 0) {
                    int indexMax = calibrationList.get(i - 1) - calibrationList.get(i);
                    int indexMin = 0;
                    int max = calibrationList.get(i - 1) - calibrationStandardList.get(i - 1);
                    int min = calibrationList.get(i) - calibrationStandardList.get(i);
                    int indexCurrent = value - calibrationList.get(i);
                    offset = (int) StringUtils.getRounding(StringUtils.getInterpolationValue(indexMax, max, indexMin, min, indexCurrent), 0);
                }
                break;
            }
        }
//        ToastUtils.showShort("offset=" + offset);
        return value - offset;
    }

    private void treatAverage(RebarPageBean pageBean) {
        double total0 = 0, average0, total1 = 0, average1;
        double size = 0;
        if (!pageBean.beanList.isEmpty()) {
            for (RebarBean b : pageBean.beanList) {
                if (b.thickness > 0) {
                    total0 += b.thickness;
                    total1 += b.thickness1;
                    size++;
                }
            }
            average0 = StringUtils.getRounding(total0 / size, 1);
            average1 = StringUtils.getRounding(total1 / size, 1);
//            average0 = StringUtils.getRounding(average0, 0);
        } else {
            average0 = 0;
            average1 = 0;
        }
        pageBean.averageThickness = average0;
        pageBean.averageThickness1 = average1;
    }

    private boolean isAdd(RebarPageBean pageBean) {
        return pageBean.beanList.size() < zoneBean.poleBottomCount;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            assert data != null;
            zoneBean = (ZoneBean) data.getSerializableExtra("ZoneBean");
            assert zoneBean != null;
            if (zoneBean.id == null) {
                cloneZoneBean = null;
            }
            binding.navigationBar.setProjectName(zoneBean.projectName).setZoneName(zoneBean.poleNo);
            binding.navigationBar.setTitleText("");
            //
            configGridTop(zoneBean.poleBottomCount);
            refreshGrid();
        }
    }

    private double calibration;
    private double radius;

    private void configCalibration() {
        if (zoneBean != null) {
            String strDistance = binding.etDistance.getText().toString().trim();
            double tempDistance = Double.parseDouble(strDistance);
            radius = StringUtils.getInterpolationValue(0, zoneBean.poleBottomDiameter, Double.parseDouble(zoneBean.poleHeight), zoneBean.poleTopDiameter, tempDistance) / 2;
            double a1 = radius + 22;  //22：钢筋仪轱辘半径22mm
            double a2 = 52 / 2.0;
            double a3 = Math.sqrt(a1 * a1 - a2 * a2);
            double a4 = a3 - radius;
            calibration = StringUtils.getRounding(22 - a4, 2);
            Logger.d(calibration + "");
        }
    }

    /**
     * 刷新钢筋列表
     */
    private void refreshGrid() {
        binding.layoutGridContent.removeAllViews();
        for (int i = 0; i < zoneBean.rebarPageList.size(); i++) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.view_grid_content, null, false);
            configGrid(itemView, zoneBean.poleBottomCount, zoneBean.rebarPageList.get(i));
            itemView.setId(i);
            itemView.setOnLongClickListener(gridItemView);
            binding.layoutGridContent.addView(itemView);
        }
    }

    /**
     * 删除
     */
    private final View.OnLongClickListener gridItemView = v -> {
        RebarPageBean pageBean = zoneBean.rebarPageList.get(v.getId());
        StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this, "red");
        styleAlertDialog.setContent("确定删除“" + pageBean.segment + "”的数据？").setLeftButton("取消", v1 -> styleAlertDialog.dismiss()).setRightButton("确定", v1 -> {
            styleAlertDialog.dismiss();
            zoneBean.rebarPageList.remove(v.getId());
            //刷新钢筋列表
            refreshGrid();
            //刷新按钮
            if (!zoneBean.rebarPageList.isEmpty()) {
                refreshButon(true, zoneBean.rebarPageList.get(zoneBean.rebarPageList.size() - 1));
            } else {
                refreshButon(false, null);
            }
        }).show();
        return false;
    };

    /**
     * 刷新检测按钮状态
     */
    private void refreshButon(boolean flag, RebarPageBean pageBean) {
        if (flag) {
            flag = pageBean.beanList.isEmpty();
        }
        binding.btnCheck.setEnabled(!flag);
        binding.btnCheck.setBackgroundResource(!flag ? R.drawable.bg_button_btn_green : R.drawable.bg_button_gray);
        binding.btnSetting.setEnabled(!flag);
        binding.btnSetting.setBackgroundResource(!flag ? R.drawable.bg_button_btn_green : R.drawable.bg_button_gray);
        binding.btnSave.setEnabled(!flag);
        binding.btnSave.setBackgroundResource(!flag ? R.drawable.bg_button_btn_green : R.drawable.bg_button_gray);
        binding.btnFile.setEnabled(!flag);
        binding.btnFile.setBackgroundResource(!flag ? R.drawable.bg_button_btn_green : R.drawable.bg_button_gray);
    }

    private boolean isForecastStart;

    public void onViewClicked(View view) {
        OtherUtils.hideKeyboard(this);
        if (view.getId() == R.id.tv_tab_detection) {
            showView(0);
        } else if (view.getId() == R.id.tv_tab_forecast) {
            showView(1);
        } else if (view.getId() == R.id.btn_forecast) {
            {
                if (zoneBean == null) {
                    ToastUtils.showShort("请进行参数设置");
                    return;
                }
                String strDistance = binding.etDistance1.getText().toString().trim();
                if (TextUtils.isEmpty(strDistance)) {
                    ToastUtils.showShort("请填写完整");
                    return;
                }
                currentDistance = Double.parseDouble(strDistance);
            }
            isForecastStart = !isForecastStart;
            if (isForecastStart) {
                binding.btnForecast.setText("结束");
                valueMin = 10000;
                valueMax = 0;
                stringBuffer.delete(0, stringBuffer.length());
                isInvalid = true;
                bluetoothService.writeData(HexUtil.stringToBytes("F3010000000000FF"));  //预估直径（开　　始）---预估直径
                new Handler().postDelayed(() -> isInvalid = false, 1000);
            } else {
                bluetoothService.writeData(HexUtil.stringToBytes("F2010000000000FF"));  //检测归零（预估直径-结　　束）---预估直径
                binding.btnForecast.setText("开始");
                if (valueMin == 0) {
                    ToastUtils.showShort("最小信号值为0，无法预估");
                    return;
                }
                //预估直径
                double rate = StringUtils.getRounding(100.0 * (valueMax - valueMin) / valueMin, 2);
                String strForecast;
                boolean flag = currentDistance >= Double.parseDouble(zoneBean.poleHeight) - 3;
                if (flag) {
                    strForecast = rate <= 20 ? "14" : "10";
                } else {
                    strForecast = rate <= 40 ? "14" : "10";
                }
                binding.tvForecast.setText(Html.fromHtml("<font color=#000000>预估直径(mm)：</font><font color=" + strColor + ">" + strForecast + "</font>"));
//                tvForecastTest.setText(currentDistance + ", " + zoneBean.poleHeight + ", " + rate + strForecast);
            }
        } else if (view.getId() == R.id.btn_check) {
            if (zoneBean == null) {
                ToastUtils.showShort("请进行参数设置");
                return;
            }
            if (zoneBean.rebarPageList.size() >= 10) {
                ToastUtils.showShort("最多检测10个位置");
                return;
            }
            String strDistance = binding.etDistance.getText().toString().trim();
            if (TextUtils.isEmpty(strDistance)) {
                ToastUtils.showShort("请填写完整");
                return;
            }
            String segment = "距根部" + strDistance + "m";
            currentDistance = Double.parseDouble(strDistance);
            zoneBean.rebarPageList.add(new RebarPageBean(segment, currentDistance));
            refreshGrid();
            binding.scrollView.post(() -> binding.scrollView.fullScroll(View.FOCUS_DOWN));
            configCalibration();
            //
            binding.btnCheck.setEnabled(false);
            binding.btnCheck.setBackgroundResource(R.drawable.bg_button_gray);
            binding.btnSetting.setEnabled(false);
            binding.btnSetting.setBackgroundResource(R.drawable.bg_button_gray);
            binding.btnSave.setEnabled(false);
            binding.btnSave.setBackgroundResource(R.drawable.bg_button_gray);
            binding.btnFile.setEnabled(false);
            binding.btnFile.setBackgroundResource(R.drawable.bg_button_gray);
            bluetoothService.writeData(HexUtil.stringToBytes("F2010000000000FF"));  //检测归零（确　　定） ---单根检测
            numberBean1 = null;
            signalValue = new ArrayList<>();
            refreshTvCheck();
            refreshBtnCheck(false);
        } else if (view.getId() == R.id.btn_check_single) { //检测
            {
                if (zoneBean == null) {
                    ToastUtils.showShort("请进行参数设置");
                    return;
                }
                if (zoneBean.rebarPageList.isEmpty()) {
                    ToastUtils.showShort("请添加检测部位");
                    return;
                }
                RebarPageBean pageBean = zoneBean.rebarPageList.get(zoneBean.rebarPageList.size() - 1);
                if (!isAdd(pageBean)) {
                    ToastUtils.showShort("请添加检测部位");
                    return;
                }
                bluetoothService.writeData(HexUtil.stringToBytes("F2010000000000FF"));  //检测归零（检测） ---单根检测
                numberBean1 = null;
                signalValue = new ArrayList<>();
                refreshTvCheck();
                refreshBtnCheck(true);
            }
        } else if (view.getId() == R.id.btn_delete) {//删除
            {
                bluetoothService.writeData(HexUtil.stringToBytes("F2010000000000FF"));  //检测归零（删除） ---单根检测
                numberBean1 = null;
                if (!signalValue.isEmpty()) {
                    signalValue.remove(signalValue.size() - 1);
                    refreshTvCheck();
                    refreshBtnCheck(!signalValue.isEmpty());
                    return;
                }
                signalValue = new ArrayList<>();
                if (zoneBean == null || zoneBean.rebarPageList.isEmpty()) {
                    return;
                }
                RebarPageBean pageBean = zoneBean.rebarPageList.get(zoneBean.rebarPageList.size() - 1);
                if (pageBean.beanList.isEmpty()) {
                    return;
                }
                StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this, "red");
                styleAlertDialog.setContent("确定删除" + pageBean.segment + "的最后一根数据？").setLeftButton("取消", v1 -> styleAlertDialog.dismiss()).setRightButton("确定", v1 -> {
                    styleAlertDialog.dismiss();
                    pageBean.beanList.remove(pageBean.beanList.size() - 1);
                    refreshGrid();
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setBackgroundResource(R.drawable.bg_button_btn_green);
                }).show();
                //标表
//                valueMin = 10000;
//                valueMax = 0;
//                binding.tvSingle1.setText(valueMin + " , " + valueMax);
            }
        } else if (view.getId() == R.id.btn_setting) {
            {
                Intent intent = new Intent(this, DetectConfigActivity.class);
                intent.putExtra("ZoneBean", zoneBean);
                startActivityForResult(intent, 1);
            }
        } else if (view.getId() == R.id.btn_save) {
            if (zoneBean == null) {
                ToastUtils.showShort("请进行参数设置");
                return;
            }
            if (zoneBean.rebarPageList.isEmpty()) {
                ToastUtils.showShort("请先检测数据");
                return;
            }
            zoneBean.startTime = StringUtils.getTime();
            zoneBean.id = GreenDaoHelper.getDaoSession().getZoneBeanDao().insertOrReplace(zoneBean);
            ToastUtils.showShort("保存成功");
            String strClone = new Gson().toJson(zoneBean);
            cloneZoneBean = new Gson().fromJson(strClone, new TypeToken<ZoneBean>() {
            }.getType());
            int lastIndex = cloneZoneBean.rebarPageList.size() - 1;
            int lastBeanListSize = cloneZoneBean.rebarPageList.get(lastIndex).beanList.size();
            if (lastBeanListSize == 0) {
                cloneZoneBean.rebarPageList.remove(lastIndex);
            }
            binding.btnSave.setEnabled(false);
            binding.btnSave.setBackgroundResource(R.drawable.bg_button_gray);
        } else if (view.getId() == R.id.btn_file) {
            Intent intent = new Intent(this, DataFileManagerActivity.class);
            intent.putExtra("projectName", zoneBean == null ? "" : zoneBean.projectName);
            intent.putExtra("poleId", zoneBean == null ? "" : zoneBean.id);
            startActivity(intent);
        } else if (view.getId() == R.id.btn_finish) {
            boolean isShowSave = false;
            if (zoneBean != null) {
                if (!zoneBean.rebarPageList.isEmpty()) {
                    int rebarPageListSize = zoneBean.rebarPageList.size();
                    int lastBeanListSize = zoneBean.rebarPageList.get(zoneBean.rebarPageList.size() - 1).beanList.size();
                    if (lastBeanListSize == 0) {
                        rebarPageListSize -= 1;
                    }
                    if (zoneBean.id == null) {
                        isShowSave = rebarPageListSize > 0;
                    } else {
                        if (rebarPageListSize != cloneZoneBean.rebarPageList.size()) {
                            isShowSave = true;
                        }
                        if (!isShowSave) {
                            for (int i = 0; i < rebarPageListSize; i++) {
                                if (zoneBean.rebarPageList.get(i).beanList.size() != cloneZoneBean.rebarPageList.get(i).beanList.size()) {
                                    isShowSave = true;
                                }
                            }
                        }
                        if (!isShowSave) {
                            boolean flag = false;
                            for (int i = 0; i < rebarPageListSize; i++) {
                                if (flag) {
                                    break;
                                }
                                RebarPageBean rPageBean = zoneBean.rebarPageList.get(i);
                                RebarPageBean tPageBean = cloneZoneBean.rebarPageList.get(i);
                                for (int j = 0; j < rPageBean.beanList.size(); j++) {
                                    RebarBean rRebarBean = rPageBean.beanList.get(j);
                                    RebarBean tRebarBean = tPageBean.beanList.get(j);
                                    if (rRebarBean.distance != tRebarBean.distance) {
                                        isShowSave = true;
                                        flag = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            String showText = isShowSave ? "数据未存储，确定退出检测？" : "确定退出检测？";
            StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this, "red");
            styleAlertDialog.setContent(showText).setLeftButton("取消", v1 -> styleAlertDialog.dismiss()).setRightButton("确定", v1 -> {
                styleAlertDialog.dismiss();
                finish();
            }).show();
//                treatData(tempList.get(index % 9).distance, 1, 1, tempList.get(index % 9).number);
//                index++;
        }
    }

    private void showView(int position) {
        checkType = position;
        binding.tvTabDetection.setTextColor(color8E8E8E);
        binding.tvTabForecast.setTextColor(color8E8E8E);
        Typeface typefaceNormal = Typeface.create("sans-serif-bold", Typeface.NORMAL);
        binding.tvTabDetection.setTypeface(typefaceNormal);
        binding.tvTabForecast.setTypeface(typefaceNormal);
        binding.viewTabDetection.setBackgroundColor(colorFFF);
        binding.viewTabForecast.setBackgroundColor(colorFFF);
        binding.layoutDetection.setVisibility(View.GONE);
        binding.layoutForecast.setVisibility(View.GONE);
        Typeface typefaceBold = Typeface.create("sans-serif-bold", Typeface.BOLD);
        switch (position) {
            case 0:
                binding.tvTabDetection.setTextColor(color006F5F);
                binding.tvTabDetection.setTypeface(typefaceBold);
                binding.viewTabDetection.setBackgroundColor(color006F5F);
                binding.layoutDetection.setVisibility(View.VISIBLE);
                break;
            case 1:
                binding.tvTabForecast.setTextColor(color006F5F);
                binding.tvTabForecast.setTypeface(typefaceBold);
                binding.viewTabForecast.setBackgroundColor(color006F5F);
                binding.layoutForecast.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void configGrid(View itemView, int rebarCount, RebarPageBean bean) {
        TextView tvNumber = itemView.findViewById(R.id.tv_grid_number);
        TextView tv1 = itemView.findViewById(R.id.tv_grid_1);
        View v1 = itemView.findViewById(R.id.v_grid_1);
        tv1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        v1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        TextView tv2 = itemView.findViewById(R.id.tv_grid_2);
        View v2 = itemView.findViewById(R.id.v_grid_2);
        tv2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
        v2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
        TextView tv3 = itemView.findViewById(R.id.tv_grid_3);
        View v3 = itemView.findViewById(R.id.v_grid_3);
        tv3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
        v3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
        TextView tv4 = itemView.findViewById(R.id.tv_grid_4);
        View v4 = itemView.findViewById(R.id.v_grid_4);
        tv4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
        v4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
        TextView tv5 = itemView.findViewById(R.id.tv_grid_5);
        View v5 = itemView.findViewById(R.id.v_grid_5);
        tv5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
        v5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
        TextView tv6 = itemView.findViewById(R.id.tv_grid_6);
        View v6 = itemView.findViewById(R.id.v_grid_6);
        tv6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
        v6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
        TextView tv7 = itemView.findViewById(R.id.tv_grid_7);
        View v7 = itemView.findViewById(R.id.v_grid_7);
        tv7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
        v7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
        TextView tv8 = itemView.findViewById(R.id.tv_grid_8);
        View v8 = itemView.findViewById(R.id.v_grid_8);
        tv8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
        v8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
        TextView tv9 = itemView.findViewById(R.id.tv_grid_9);
        View v9 = itemView.findViewById(R.id.v_grid_9);
        tv9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
        v9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
        TextView tv10 = itemView.findViewById(R.id.tv_grid_10);
        View v10 = itemView.findViewById(R.id.v_grid_10);
        tv10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
        v10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
        TextView tvAver = itemView.findViewById(R.id.tv_grid_average);
        tvAver.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        //
        tvNumber.setText(bean.segment);
        if (bean.beanList == null) {
            return;
        }
        if (!bean.beanList.isEmpty()) {
            setTextView(tv1, bean.beanList.get(0));
        }
        if (bean.beanList.size() > 1) {
            setTextView(tv2, bean.beanList.get(1));
        }
        if (bean.beanList.size() > 2) {
            setTextView(tv3, bean.beanList.get(2));
        }
        if (bean.beanList.size() > 3) {
            setTextView(tv4, bean.beanList.get(3));
        }
        if (bean.beanList.size() > 4) {
            setTextView(tv5, bean.beanList.get(4));
        }
        if (bean.beanList.size() > 5) {
            setTextView(tv6, bean.beanList.get(5));
        }
        if (bean.beanList.size() > 6) {
            setTextView(tv7, bean.beanList.get(6));
        }
        if (bean.beanList.size() > 7) {
            setTextView(tv8, bean.beanList.get(7));
        }
        if (bean.beanList.size() > 8) {
            setTextView(tv9, bean.beanList.get(8));
        }
        if (bean.beanList.size() > 9) {
            setTextView(tv10, bean.beanList.get(9));
        }
        double total0 = 0, average0, total1 = 0, average1;
        double size = 0;
        if (!bean.beanList.isEmpty()) {
            for (RebarBean b : bean.beanList) {
                if (b.thickness > 0) {
                    total0 += b.thickness;
                    total1 += b.thickness1;
                    size++;
                }
            }
            average0 = StringUtils.getRounding(total0 / size, 2);
            average1 = StringUtils.getRounding(total1 / size, 2);
            average0 = StringUtils.getRounding(average0, 1);
            average1 = StringUtils.getRounding(average1, 1);
        } else {
            average0 = 0;
            average1 = 0;
        }
        tvAver.setText(average1 + "\n" + average0);
    }

    private void setTextView(TextView textView, RebarBean bean) {
        textView.setText(bean.thickness1 + "\n" + bean.thickness);
        textView.setTextColor(bean.thickness1 < 15 ? colorFF9600 : color000);
    }

    private void configGridTop(int rebarCount) {
        binding.layoutGridTop.tvGrid1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGridAverage.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CustomMediaPlayer.releasePlayer(mediaPlayer);
    }
}