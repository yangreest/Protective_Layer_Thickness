package com.example.gtj_sd_dg240.activity.testing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.activity.data.DataFileManagerActivity;
import com.example.gtj_sd_dg240.activity.start.BaseActivity;
import com.example.gtj_sd_dg240.databinding.ActDetect2Binding;
import com.example.gtj_sd_dg240.functions.database.greenDao.db.RebarMinMaxBeanDao;
import com.example.gtj_sd_dg240.model.CheckDataBean;
import com.example.gtj_sd_dg240.model.RebarBean;
import com.example.gtj_sd_dg240.model.RebarMinMaxBean;
import com.example.gtj_sd_dg240.model.RebarPageBean;
import com.example.gtj_sd_dg240.model.ZoneBean;
import com.example.gtj_sd_dg240.storage.GreenDaoHelper;
import com.example.gtj_sd_dg240.utils.ComputeUtils;
import com.example.gtj_sd_dg240.utils.CustomMediaPlayer;
import com.example.gtj_sd_dg240.utils.Optimized10ElementTrimmedMeanUtils;
import com.example.gtj_sd_dg240.utils.OptimizedDataProcessor;
import com.example.gtj_sd_dg240.utils.OtherUtils;
import com.example.gtj_sd_dg240.utils.SignalArrayBufferUtils;
import com.example.gtj_sd_dg240.utils.StringUtils;
import com.example.gtj_sd_dg240.view.StyleAlertDialog;
import com.example.gtj_sd_dg240.view.StyleAlertDialog2;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 单点检测页面（混凝土电杆保护层厚度检测的核心界面）。
 *
 * <p>整体数据流：
 * 蓝牙钢筋仪原始帧 → {@link OptimizedDataProcessor}（子线程拼帧/解帧，输出位移+信号）
 * → {@link #getSignalValue(int)}（按标定表校准信号）→ {@link Optimized10ElementTrimmedMeanUtils}（截尾均值滤波）
 * → 根据当前页签与检测方向分派到横向/竖向处理或直径预估。
 *
 * <p>两个页签（{@link #checkType}）：
 * <ul>
 *     <li>0：单点检测——测保护层厚度；</li>
 *     <li>1：预估直径——根据信号变化率预估主筋直径。</li>
 * </ul>
 *
 * <p>单点检测又分两种扫描方向（{@link #horVer}）：
 * <ul>
 *     <li>1：横向——沿电杆圆周移动，采集 3 个信号峰值用于推算主筋间距；</li>
 *     <li>2：竖向——沿电杆轴向移动，采集信号最小值用于查表反推保护层厚度。</li>
 * </ul>
 *
 * <p>厚度计算采用“查表法”：由主筋直径、主筋间距、箍筋间距组成唯一键，
 * 从 GreenDao 的 {@link RebarMinMaxBean} 标定表中查出信号阈值曲线，再对实测信号做插值得到厚度。
 */
@SuppressLint({"InflateParams", "SetTextI18n", "UseCompatLoadingForDrawables"})
public class DetectActivity2 extends BaseActivity<ActDetect2Binding> {
    /** 界面常用颜色值缓存（主题绿、灰、白、黑、告警橙），避免重复调用 getColor。 */
    private int color006F5F, color8E8E8E, colorFFF, color000, colorFF9600;
    /** zoneBean：当前正在检测的电杆/测区数据；cloneZoneBean：上次保存时的快照，用于退出前的“脏数据”比对。 */
    private ZoneBean zoneBean, cloneZoneBean;
    /** 语音播报播放器，检测到峰值/异常时播放提示音。 */
    private MediaPlayer mediaPlayer;
    /** 标定标准值列表（各标准厚度对应的理论信号值），从 SharedPreferences 读取。 */
    private List<Integer> calibrationStandardList = new ArrayList<>();
    /** 标定实测值列表（本机在各标准厚度下实际测得的信号值），与 calibrationStandardList 一一对应。 */
    private List<Integer> calibrationList = new ArrayList<>();
    /** 1 秒周期的定时线程池：驱动横/竖向的超时计数与竖向厚度结算。 */
    private ScheduledExecutorService scheduledExecutorService;
    /** 蓝牙数据处理器：负责拼帧、解帧并在子线程回调位移与信号。 */
    private OptimizedDataProcessor dataProcessor;
    /** 主线程 Handler：把子线程的数据处理结果切回 UI 线程更新界面。 */
    private Handler dataProcessorHandler;
    private Optimized10ElementTrimmedMeanUtils meanUtils;  //平均信号（截尾均值滤波器，平滑抖动）
    /** 横向信号环形缓冲区：容量满时判定中间点是否为峰值，用于识别主筋位置。 */
    private SignalArrayBufferUtils signalArrayUtils;
    /** 横向检测采集到的信号峰值列表（最多 3 个，用于推算左右主筋间距）。 */
    private final List<CheckDataBean> checkDataList = new ArrayList<>();
    /** 保护层厚度合格区间下限/上限，超出则弹窗提示剔凿验证。 */
    private double thicknessMin, thicknessMax;
    /** RebarMinMaxBean 标定表的 GreenDao 数据访问对象，用于查表法反推厚度。 */
    private RebarMinMaxBeanDao minMaxBeanDao;

    /** 返回 ViewBinding 实例，由基类 BaseActivity 统一完成 setContentView。 */
    @Override
    protected ActDetect2Binding getBinding() {
        return ActDetect2Binding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        // 默认进入横向检测模式
        setHorVer(1);
    }

    /**
     * 初始化界面：缓存颜色、绑定点击事件、注册电量/蓝牙状态与数据回调，
     * 并创建滤波器、环形缓冲区、定时器与数据处理器。
     */
    private void initView() {
        // 缓存常用颜色，避免后续频繁查资源
        color006F5F = getColor(R.color.color006F5F);
        color8E8E8E = getColor(R.color.color8E8E8E);
        colorFFF = getColor(R.color.colorFFF);
        color000 = getColor(R.color.color000);
        colorFF9600 = getColor(R.color.colorFF9600);
        // 横向/竖向切换、各功能按钮统一绑定到 onViewClicked
        binding.ivHor.setOnClickListener(this::onViewClicked);
        binding.ivVer.setOnClickListener(this::onViewClicked);
        binding.tvHor.setOnClickListener(this::onViewClicked);
        binding.tvVer.setOnClickListener(this::onViewClicked);
        binding.btnReset.setOnClickListener(this::onViewClicked);
        binding.btnCheck.setOnClickListener(this::onViewClicked);
        binding.btnSetting.setOnClickListener(this::onViewClicked);
        binding.btnSave.setOnClickListener(this::onViewClicked);
        binding.btnFile.setOnClickListener(this::onViewClicked);
        binding.btnFinish.setOnClickListener(this::onViewClicked);
        binding.btnTesting.setOnClickListener(this::onViewClicked);
        //
        // 页签（单点检测 / 预估直径）与预估按钮
        binding.tvTabDetection.setOnClickListener(this::onViewClicked);
        binding.tvTabForecast.setOnClickListener(this::onViewClicked);
        binding.btnForecast.setOnClickListener(this::onViewClicked);
        setHideKeyboard();
        binding.navigationBar.setTitleText("单点检测");
        // 基类能力：电量显示、蓝牙连接状态显示、注册蓝牙数据回调
        initBatteryReceiver(binding.navigationBar);
        initBluetoothStatus(binding.navigationBar);
        registerBluetoothDataCallback();
        // 查表法标定表 DAO
        minMaxBeanDao = GreenDaoHelper.getDaoSession().getRebarMinMaxBeanDao();
        // 信号平滑滤波器
        meanUtils = new Optimized10ElementTrimmedMeanUtils();
        // 横向峰值环形缓冲区，容量 64（取中间点作为峰值候选）
        signalArrayUtils = new SignalArrayBufferUtils();
        signalArrayUtils.setSize(64);
        mediaPlayer = CustomMediaPlayer.getMediaPlayer(null);
        // 加载信号校准标定表
        calibrationSignalValue();
        // 启动 1 秒定时器与蓝牙数据处理器
        initExecutor();
        initOptimizedDataProcessor();
        // 默认展示“单点检测”页签
        showView(0);
        // 两个距离输入框（检测页/预估页）内容联动：一个输入时同步到另一个
        binding.etDistance.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // 仅当当前输入框持有焦点时才回写，避免两个监听器互相触发造成死循环
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

    /**
     * 从 SharedPreferences 加载信号校准标定表（由“钢筋校准”页面 CalibrationDeviceActivity 写入）。
     * calibrationStandardList：各标准厚度对应的理论信号值；
     * calibrationList：本机实测信号值。二者用于 {@link #getSignalValue(int)} 的插值修正。
     */
    private void calibrationSignalValue() {
        String strCalibrationStandardList = SPUtils.getInstance().getString("calibration-standard-list");
        calibrationStandardList = new Gson().fromJson(strCalibrationStandardList, new TypeToken<List<Integer>>() {
        }.getType());
        String strCalibrationList = SPUtils.getInstance().getString("calibration-list");
        calibrationList = new Gson().fromJson(strCalibrationList, new TypeToken<List<Integer>>() {
        }.getType());
    }

    /**
     * 蓝牙数据回调（基类在收到 BLE 通知时调用）。
     * 页面不可见（!isShow）时直接丢弃；否则把原始字节交给处理器入队异步解帧。
     *
     * @param data 钢筋仪上报的一包原始蓝牙数据（可能含多帧或半帧）
     */
    @Override
    protected void onBluetoothDataReceived(byte[] data) {
        if (!isShow) return;
        String string = StringUtils.ByteArraytoHex(data).trim();
        //binding.tvOrgData.setText(string);
        //binding.tvResult.setText(string);
        //binding.tvSignal.setText(string);
        // 非阻塞入队，由 OptimizedDataProcessor 内部线程完成拼帧/解帧后回调
        dataProcessor.receiveDataOptimized(data);
    }

    /**
     * 启动 1 秒周期的定时任务（回调 post 到主线程执行）。
     * <p>仅在“单点检测”页签（checkType==0）下累加横/竖向的“无信号计时”：
     * timerTimeHor/timerTimeVer 在收到数据时会被重置为 0，若持续≥ 2 秒未收到数据，
     * 则认为本次扫描结束，触发竖向厚度结算 {@link #treatVerThickness()}。
     */
    private void initExecutor() {
        Handler handlerPost = new Handler();
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() -> handlerPost.post(() -> {
            if (checkType == 0) {
                timerTimeHor++;
                timerTimeVer++;
                if (timerTimeVer >= 2) {
                    treatVerThickness();
                }
            }
        }), 0, 1, TimeUnit.SECONDS);
    }

    /** timerTimeHor/timerTimeVer：横向/竖向“距上次收到数据的秒数”，用于 2 秒超时判定。 */
    private long timerTimeHor, timerTimeVer;
    /** 上一次处理的位移值，用于去重（同一位移不重复处理）。 */
    private int cur_displacement;

    /**
     * 创建蓝牙数据处理器及其回调。回调运行在子线程，处理链如下：
     * <ol>
     *     <li>将“位移,信号,帧”追写入外部存储 DetecteData/detect_data.txt（调试用原始数据落盘）；</li>
     *     <li>切回 UI 线程显示原始帧；</li>
     *     <li>{@link #getSignalValue(int)} 按标定表校准信号；</li>
     *     <li>{@link Optimized10ElementTrimmedMeanUtils#addValue} 截尾均值滤波平滑信号；</li>
     *     <li>按页签分派：单点检测走 {@link #checkThickness(int, int)}，预估直径走 {@link #forecastDiameter(int)}。</li>
     * </ol>
     */
    private void initOptimizedDataProcessor() {
        dataProcessorHandler = new Handler(Looper.getMainLooper());
        dataProcessor = new OptimizedDataProcessor((displacement, signal, frameData) -> {

            // 将原始数据追写到文件，便于事后回放与算法调试（异常不影响主流程）
            try {
                java.io.File dir = getApplicationContext().getExternalFilesDir("DetecteData");
                if (dir != null) {
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    java.io.File file = new java.io.File(dir, "detect_data.txt");
                    try (java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(new java.io.FileOutputStream(file, true))) {
                        //String hexData = StringUtils.ByteArraytoHex(frameData).trim();
                        String line = displacement + "," + signal + "," + frameData + "\n";
                        bos.write(line.getBytes());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //显示信号（原始帧十六进制字符串）
            dataProcessorHandler.post(() -> binding.tvOrgData.setText(frameData));
            //校准信号：消除本机与标准设备的系统偏差
            int signalValue = getSignalValue(signal);
            //
            //平均信号：截尾均值滤波，去除毛刺后再参与后续计算
            int tempSignal = (int) meanUtils.addValue(signalValue);
            //
            // 根据页签分派：0-单点检测（测厚），1-预估直径
            if (checkType == 0) {
                checkThickness(displacement, tempSignal);
            } else {
                forecastDiameter(tempSignal);
            }
        });
    }

    /**
     * 单点检测分派：根据当前扫描方向（horVer）走横向或竖向处理。
     *
     * @param displacement 当前位移（小车位置）
     * @param signal       校准+滤波后的信号值
     */
    private void checkThickness(int displacement, int signal) {
        if (horVer == 1) {  //横向：沿圆周扫描，采集主筋峰值
            treatHor(displacement, signal);
        } else {  //竖向：沿轴向扫描，采集信号最小值
            treatVer(signal);
        }
    }

    /**
     * 横向检测处理：沿电杆圆周移动，从信号波形中识别主筋峰值。
     * <p>流程：
     * <ol>
     *     <li>位移去重（同一位移不重复处理）；</li>
     *     <li>若≥ 2 秒无数据，清空缓冲区与已采集峰值（上一轮扫描作废）；</li>
     *     <li>写入环形缓冲区，当中间点为峰值且信号&gt;50、与上次峰值位移间隔≥ 30 时返回一个 {@link CheckDataBean}；</li>
     *     <li>播报提示音并校验参数（必须先设置参数、添加检测部位）；</li>
     *     <li>最多采集 3 个峰值存入 checkDataList，并刷新三个采集指示器。</li>
     * </ol>
     * <p>集齐 3 个峰值后即可切换到竖向模式（参见 onViewClicked 中的竖向分支）。
     */
    private void treatHor(int displacement, int signal) {
        // 位移去重：同一位移只处理一次
        if (cur_displacement == displacement) {
            return;
        }
        cur_displacement = displacement;
        //显示信号（位移,信号）
        dataProcessorHandler.post(() -> binding.tvSignal.setText(cur_displacement + "," + signal));
        //超过2秒无数据：视为新一轮扫描，清空缓冲区与已采集峰值
        {
            if (timerTimeHor >= 2) {
                signalArrayUtils.clear();
                checkDataList.clear();
                dataProcessorHandler.post(() -> refreshTvCheck(false));
            }
            timerTimeHor = 0;
        }
        //
        //记录数据|获取最大值：写入环形缓冲区，满足峰值条件时返回非 null
        CheckDataBean maxBean = signalArrayUtils.putAndCheck(displacement, signal);
        //
        // 未命中峰值，继续等待下一个采样点
        if (maxBean == null) {
            return;
        }
        // 命中峰值，播报提示音
        CustomMediaPlayer.startSpeaking(this, mediaPlayer, 1);
        // 以下校验：必须已完成参数设置、已添加检测部位且当前部位未满
        if (zoneBean == null) {
            ToastUtils.showShort("请进行参数设置");
            return;
        }
        if (zoneBean.rebarPageList.isEmpty()) {
            ToastUtils.showShort("请添加检测部位");
            return;
        }
        // 取最后一个（当前）检测部位
        RebarPageBean pageBean = zoneBean.rebarPageList.get(zoneBean.rebarPageList.size() - 1);
        if (!isAdd(pageBean)) {
            ToastUtils.showShort("请添加检测部位");
            return;
        }
        // 已集齐 3 个峰值则重新开始一轮采集
        if (checkDataList.size() == 3) {
            checkDataList.clear();
        }
        //添加峰值
        checkDataList.add(maxBean);
        // 刷新三个采集进度指示器
        dataProcessorHandler.post(() -> refreshTvCheck(true));
    }

    /** signalTime：竖向采集的起算时间戳，用于“开始采集后满 1 秒才统计最值”。 */
    private long signalTime;

    /**
     * 竖向检测处理：沿电杆轴向移动，持续跟踪信号的最小/最大值。
     * <p>最小信号值（valueMinT）对应钢筋正上方的最强响应，后续用于查表反推厚度。
     * 若≥ 2 秒无数据则重置最值并重新记时起算；开始采集 1 秒后才纳入统计（避开启动抖动）。
     */
    private void treatVer(int signal) {
        //超过2秒无数据：重置最值，重新记时起算
        {
            if (timerTimeVer >= 2) {
                valueMinT = 10000;
                valueMaxT = 0;
                signalTime = System.currentTimeMillis();
            }
            timerTimeVer = 0;
        }
        long tempTime = System.currentTimeMillis();
        // 起算 1 秒后才统计最值，滤除刚开始的不稳定信号
        if (tempTime - signalTime > 1000) {
            if (valueMinT > signal) {
                valueMinT = signal;
            }
            if (valueMaxT < signal) {
                valueMaxT = signal;
            }
        }
        //显示信号与当前最值
        dataProcessorHandler.post(() -> {
            binding.tvSignal.setText(cur_displacement + " , " + signal);
            binding.tvMin.setText(valueMinT + "");
            binding.tvMax.setText(valueMaxT + "");
        });
    }

    /**
     * 竖向厚度结算：由 1 秒定时器在“竖向扫描停止≥ 2 秒”后触发。
     * <p>校验信号有效性与检测部位后，用最小信号值查表计算厚度，
     * 封装为 {@link RebarBean} 存入当前部位，重算平均值并刷新网格与按钮；
     * 若厚度超出合格区间则弹窗提示剔凿验证。
     */
    private void treatVerThickness() {
        // valueMinT==10000 表示本轮未采集到有效信号，直接返回
        if (valueMinT == 10000) {
            valueMaxT = 0;
            return;
        }
        // 信号为 0 无法计算，提示并重置
        if (valueMinT == 0 || valueMaxT == 0) {
            ToastUtils.showShort("信号值为0");
            valueMinT = 10000;
            valueMaxT = 0;
            return;
        }
        //
        // 校验：必须已设置参数且存在检测部位
        if (zoneBean == null || zoneBean.rebarPageList == null || zoneBean.rebarPageList.isEmpty()) {
            return;
        }
        RebarPageBean pageBean = zoneBean.rebarPageList.get(zoneBean.rebarPageList.size() - 1);
        if (!isAdd(pageBean)) {
            valueMinT = 10000;
            valueMaxT = 0;
            ToastUtils.showShort("请添加检测部位");
            return;
        }
        // 横向未集齐 3 个峰值时无法推算主筋间距，不结算厚度
        if (checkDataList.size() < 3) {
            return;
        }
        // 核心：用最小信号值 + 当前部位距离查表计算保护层厚度
        double thickness = computeResult(valueMinT, pageBean.distance);
        //重置最值，为下一次竖向测量做准备
        valueMinT = 10000;
        valueMaxT = 0;
        //存储：封装为 RebarBean（thickness1 为取整后的厚度，用于显示）加入当前部位
        RebarBean rebarBean = new RebarBean(pageBean.segment, thickness);
        rebarBean.thickness1 = StringUtils.getRounding(rebarBean.thickness, 0);
        pageBean.beanList.add(rebarBean);
        //求平均：重算当前部位的平均厚度
        treatAverage(pageBean);
        //
        dataProcessorHandler.post(() -> {
            CustomMediaPlayer.startSpeaking(this, mediaPlayer, 1);
            binding.tvResult.setText("厚度：" + thickness + " mm, " + rebarSpacingMin + ", " + rebarSpacingMax);
            //刷新钢筋列表
            refreshGrid();
            //刷新按钮
            refreshButon(true, pageBean);
            //数据异常、剔凿验证：厚度超出合格区间时弹窗提醒
            if (thickness > thicknessMax || thickness < thicknessMin) {
                showErrorThickness();
            }
        });
    }

    /** rebarSpacingMin/rebarSpacingMax：由横向 3 个峰值推算出的左/右主筋间距（弦长）。 */
    private double rebarSpacingMin, rebarSpacingMax;

    /**
     * 由实测信号推算保护层厚度（先由横向 3 个峰值推算主筋间距，再查表得厚度）。
     *
     * <p>主筋间距推算原理：
     * <ol>
     *     <li>相邻峰值的位移差× 0.54 换算为电杆表面的弧长（left/right）；</li>
     *     <li>弧长 / 周长 × 360 得到对应圆心角 angleMin/angleMax；</li>
     *     <li>主筋中心位于半径 a = poleRadius - 17 - 主筋半径 处（17 为表层到主筋中心的基准偏差）；</li>
     *     <li>用余弦定理 calculateThirdSide(a, a, angle) 求出两相邻主筋的弦长，即主筋间距。</li>
     * </ol>
     *
     * @param signal   竖向采集到的最小信号值
     * @param distance 当前检测部位距根部的距离（m）
     * @return 中间主筋位置处的保护层厚度（mm）
     */
    private double computeResult(int signal, double distance) {
        //1. 主筋直径（根据配置索引从常量表取出）
        String strRebarDiameter = StaticConstant.rebarDiameterList.get(zoneBean.rebarDiameter);
        double rebarDiameter = Double.parseDouble(strRebarDiameter);
        //
        //2. 主筋间距：由 3 个峰值的位移差换算弧长（0.54 为位移单位→mm 的系数）
        double left = (checkDataList.get(1).x - checkDataList.get(0).x) * 0.54;
        double right = (checkDataList.get(2).x - checkDataList.get(1).x) * 0.54;
        double min = Math.min(left, right);
        double max = Math.max(left, right);
        //
        // 弧长 → 圆心角（度）：弧长 / 周长 × 360
        double angleMin = min / (2 * Math.PI * poleRadius) * 360;
        double angleMax = max / (2 * Math.PI * poleRadius) * 360;
        // 主筋中心所在半径：电杆半径 - 17（表层基准偏差） - 主筋半径
        double a = poleRadius - 17 - rebarDiameter / 2;
        // 余弦定理求弦长（两相邻主筋的直线间距）
        rebarSpacingMin = (int) ComputeUtils.calculateThirdSide(a, a, angleMin);
        rebarSpacingMax = (int) ComputeUtils.calculateThirdSide(a, a, angleMax);
        double leftThickness, rightThickness;
        // 以下为左/右主筋分别测厚的预留分支，当前未启用（仅用中间主筋结果）
        if (left < right) {
        //            leftThickness = computeThickness(strRebarDiameter, rebarSpacingMin, rebarSpacingMin, checkDataList.get(0), false);
        //            rightThickness = computeThickness(strRebarDiameter, rebarSpacingMax, rebarSpacingMax, checkDataList.get(2), false);
        } else {
        //            rightThickness = computeThickness(strRebarDiameter, rebarSpacingMin, rebarSpacingMin, checkDataList.get(0), false);
        //            leftThickness = computeThickness(strRebarDiameter, rebarSpacingMax, rebarSpacingMax, checkDataList.get(2), false);
        }
        // 以推算出的主筋间距为条件，用实测信号查表得到厚度
        double middleThickness = computeThickness(strRebarDiameter, distance, rebarSpacingMin, rebarSpacingMax, signal, false);
        //        dataProcessorHandler.post(() -> {
        //            StringBuilder sbd = new StringBuilder();
        //            sbd.append("钢筋直径：").append(rebarDiameter);
        //            sbd.append("，　电杆直径：").append(StringUtils.getRounding(poleRadius * 2, 0));
        //            sbd.append("，\n主筋间距 Min：").append(StringUtils.getRounding(rebarSpacingMin, 0));
        //            sbd.append("，　Max：").append(StringUtils.getRounding(rebarSpacingMax, 0));
        //            sbd.append("，　电杆直径补偿：").append(poleCompensation);
        //            sbd.append("\nleftThickness: ").append(leftThickness);
        //            sbd.append("\nmiddleThickness: ").append(middleThickness);
        //            sbd.append("\nrightThickness: ").append(rightThickness);
        //            StyleAlertDialog2 styleAlertDialog = new StyleAlertDialog2(this);
        //            styleAlertDialog.setContent(sbd).setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
        return middleThickness;
    }

    /**
     * 查表法计算保护层厚度（核心算法）。
     *
     * <p>步骤：
     * <ol>
     *     <li>将实测主筋间距 (spacingMin, spacingMax) 匹配到预定义的标准间距档位 spaces；</li>
     *     <li>根据部位距离判定箍筋间距（中间段 120、两端 70）；</li>
     *     <li>用“主筋直径_0_间距min_间距max_箍筋间距”组成唯一键查标定表，取出信号阈值曲线 signalMin；</li>
     *     <li>signalMin 单调递减，索引 i 对应厚度 (i+1)mm；找到 signal ≥ signalMin[i] 的位置后在相邻两点间插值得厚度；</li>
     *     <li>多个匹配档位取均值（两个取平均、一个直接用、无匹配则缺省 15），最后叠加电杆直径补偿 poleCompensation。</li>
     * </ol>
     *
     * @param rebarDiameter 主筋直径（字符串）
     * @param distance      当前部位距根部距离（m），用于判定箍筋间距
     * @param spacingMin    推算出的主筋间距最小值
     * @param spacingMax    推算出的主筋间距最大值
     * @param signal        实测（校准+滤波后）信号值
     * @param isShowLog     是否弹窗展示计算过程日志（调试用）
     * @return 保护层厚度（mm）
     */
    private double computeThickness(String rebarDiameter, double distance, double spacingMin, double spacingMax, int signal, boolean isShowLog) {
        // 预定义的标准主筋间距档位（{min,max}），作为查表的键之一
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
        //统计主筋间距：把实测间距匹配到最接近的标准档位（可能命中多个）
        List<int[]> rebarSpacings = ComputeUtils.findMatchingSpaces(spaces, new double[]{spacingMin, spacingMax});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{53, 62});  // 遗留的调试调用，结果未使用
        // 以下为调试日志拼接（仅 isShowLog 时展示）
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("=====\n");
        stringBuilder.append(spacingMin).append(" , ").append(spacingMax);
        for (int[] ints : rebarSpacings) {
            stringBuilder.append("\n间距: ").append(Arrays.toString(ints));
        }
        stringBuilder.append("\n=====");
        // 判定箍筋间距：电杆中间段（距两端 >2m）箍筋较稀为 120，两端较密为 70
        String strSpiralSpacing;
        if (distance > 2 && distance < Double.parseDouble(zoneBean.poleHeight) - 2) {
            strSpiralSpacing = "120";
        } else {
            strSpiralSpacing = "70";
        }
        //
        //数据库查询条数：为每个匹配到的标准间距构造一个查表键（poleDiameter 传 "0" 表示不区分电杆直径）
        List<RebarMinMaxBean> rebarMinMaxBeanList = new ArrayList<>();
        for (int[] arr : rebarSpacings) {
            rebarMinMaxBeanList.add(new RebarMinMaxBean(rebarDiameter, "0", arr[0] + "", arr[1] + "", strSpiralSpacing));
        }
        //查数据库：按唯一键取出标定表的信号阈值曲线 signalMin/signalMax
        for (RebarMinMaxBean bean : rebarMinMaxBeanList) {
            RebarMinMaxBean temp = minMaxBeanDao.queryBuilder().where(RebarMinMaxBeanDao.Properties.UniqueKey.eq(bean.uniqueKey)).unique();
            if (temp != null) {
                bean.signalMin = temp.signalMin;
                bean.signalMax = temp.signalMax;
                stringBuilder.append("\nku: ").append(bean.rebarSpacingMin).append(" , ").append(bean.rebarSpacingMax).append(" ~ ").append(temp.signalMin[0]);
            }
        }
        stringBuilder.append("\n=====");
        //计算厚度：在信号阈值曲线上定位实测信号所处区间并插值
        for (RebarMinMaxBean bean : rebarMinMaxBeanList) {
            int index = 0;
            if (bean.signalMin != null) {
                // signalMin 单调递减，索引 i 对应厚度 (i+1)mm；找到第一个 signal ≥ signalMin[i] 的位置
                for (int i = 0; i < bean.signalMin.length; i++) {
                    if (signal >= bean.signalMin[i]) {
                        index = i;
                        bean.signal = bean.signalMin[i];
                        break;
                    }
                }
            }
            if (index == 0) {
                // 未命中任何阈值（信号偏小），厚度取最小值 1mm
                bean.thickness = 1;
            } else {
                // 在 (index-1) 与 index 两个厚度点之间对实测信号做线性插值
                bean.thickness = StringUtils.getRounding(StringUtils.getInterpolationValue(bean.signalMin[index - 1], index, bean.signalMin[index], index + 1, signal), 2);
            }
            stringBuilder.append("\nindex: ").append(index).append(", signal: ").append(bean.signal).append(" , ").append(signal).append(", thickness: ").append(bean.thickness);
        }
        //整合计算厚度：根据命中档位数量合并结果
        double ths;
        StringBuilder sbdMessage = new StringBuilder();
        if (rebarMinMaxBeanList.size() >= 2) {  //2：命中两个间距档位，取两者厚度的均值
            //主筋间距内插
            ths = StringUtils.getRounding((rebarMinMaxBeanList.get(0).thickness + rebarMinMaxBeanList.get(1).thickness) / 2d, 2);
            //            double rebarSpacing0 = Double.parseDouble(rebarMinMaxBeanList.get(0).rebarSpacingMin);
            //            double rebarSpacing1 = Double.parseDouble(rebarMinMaxBeanList.get(1).rebarSpacingMin);
            //            ths = StringUtils.getRounding(StringUtils.getInterpolationValue(rebarSpacing0, , rebarSpacing1, rebarMinMaxBeanList.get(1).thickness, spacingMin), 2);
            sbdMessage.append("主筋间距NX：\n").append(rebarMinMaxBeanList.get(0).uniqueKey).append("　～　").append(rebarMinMaxBeanList.get(1).uniqueKey).append("　厚度：").append(ths);
        } else if (rebarMinMaxBeanList.size() == 1) {  //1：仅命中一个档位，直接用其厚度
            ths = rebarMinMaxBeanList.get(0).thickness;
        } else {  // 无匹配档位，缺省 15mm
            ths = 15;
        }
        // 叠加电杆直径补偿（因探头在曲面上测量产生的几何偏差，见 configCalibration）
        double thickness = StringUtils.getRounding(ths + poleCompensation, 2);
        // 调试：弹窗展示完整的计算过程（钢筋直径/电杆直径/间距/信号/补偿/匹配结果）
        if (isShowLog) {
            dataProcessorHandler.post(() -> {
                StringBuilder sbd = new StringBuilder();
                sbd.append("钢筋直径：").append(rebarDiameter);
                sbd.append("，　电杆直径：").append(StringUtils.getRounding(poleRadius * 2, 0));
                sbd.append("，\n主筋间距 Min：").append(StringUtils.getRounding(spacingMin, 0));
                sbd.append("，　Max：").append(StringUtils.getRounding(spacingMax, 0));
                sbd.append("，\n箍筋间距：").append(strSpiralSpacing);
                sbd.append("，\n信号值：").append(signal);
                sbd.append("，\n电杆直径补偿：").append(poleCompensation);
                sbd.append("，\n结果：").append(thickness);
                sbd.append("\n\n\n");
                sbd.append("匹配结果: ");
                for (RebarMinMaxBean bean : rebarMinMaxBeanList) {
                    sbd.append("\n厚度: ").append(bean.thickness).append(",　查表: ").append(bean.uniqueKey);
                }
                if (rebarMinMaxBeanList.isEmpty()) {
                    sbd.append("\n匹配结果：无");
                }
                sbd.append("\n\n\n");
                sbd.append(sbdMessage);
                sbd.append("\n\n====\n").append(stringBuilder);
                StyleAlertDialog2 styleAlertDialog = new StyleAlertDialog2(this);
                styleAlertDialog.setContent(sbd).setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
            });
        }
        return thickness;
    }

    /** valueMinT/valueMaxT：竖向测厚时跟踪的信号最小/最大值（初值 10000 表示尚未采集）。 */
    private int valueMinT = 10000, valueMaxT;
    /** valueMinF/valueMaxF：预估直径时跟踪的信号最小/最大值。 */
    private int valueMinF = 10000, valueMaxF;

    /**
     * 预估直径模式下的信号处理：持续记录信号最值并实时显示。
     * <p>实际直径预估在点击“结束”时根据 (max-min)/min 的变化率完成（参见 onViewClicked）。
     */
    private void forecastDiameter(int signal) {
        if (valueMinF > signal) {
            valueMinF = signal;
        }
        if (valueMaxF < signal) {
            valueMaxF = signal;
        }
        dataProcessorHandler.post(() -> {
            binding.tvForecastValue.setText("信号：" + signal);
            binding.tvForecastMin.setText("Min：" + valueMinF);
            binding.tvForecastMax.setText("Max：" + valueMaxF);
        });
    }


    /**
     * 厚度异常提醒：连播两次提示音并弹黄色告警框，提示重复测量或剔凿验证。
     */
    private void showErrorThickness() {
        CustomMediaPlayer.startSpeaking(this, mediaPlayer, 1);
        new Handler().postDelayed(() -> CustomMediaPlayer.startSpeaking(this, mediaPlayer, 1), 1000);
        StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this, "yellow");
        styleAlertDialog.setContent("数据异常，请重复测量或剔凿验证").setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
    }

    /**
     * 信号校准：把本机实测信号修正为接近标准设备的信号值。
     * <p>calibrationList 为各标准厚度下的本机实测值（递减），calibrationStandardList 为对应理论值。
     * 先定位 value 落在哪个标定区间，再在该区间两端偏差（实测-理论）之间线性插值出修正量 offset，
     * 最终返回 value - offset。
     *
     * @param value 本机实测信号值
     * @return 校准后的信号值
     */
    private int getSignalValue(int value) {
        int offset = 0;
        for (int i = 0; i < calibrationList.size(); i++) {
            // 找到第一个满足 value ≥ calibrationList[i] 的标定区间（列表递减）
            if (value >= calibrationList.get(i)) {
                if (i - 1 >= 0) {
                    // indexMax：相邻两个标定点的实测值跨度；indexCurrent：当前值距下边界的偏移
                    int indexMax = calibrationList.get(i - 1) - calibrationList.get(i);
                    int indexMin = 0;
                    // max/min：两个标定点处的偏差（实测值 - 理论值）
                    int max = calibrationList.get(i - 1) - calibrationStandardList.get(i - 1);
                    int min = calibrationList.get(i) - calibrationStandardList.get(i);
                    int indexCurrent = value - calibrationList.get(i);
                    // 在 [indexMin,indexMax]→[min,max] 之间线性插值出当前修正量
                    offset = (int) StringUtils.getRounding(StringUtils.getInterpolationValue(indexMax, max, indexMin, min, indexCurrent), 0);
                }
                break;
            }
        }
        // 扣除偏差，得到校准后的信号
        return value - offset;
    }

    /**
     * 重算当前检测部位的平均厚度（仅统计 thickness>0 的有效数据）。
     * <p>averageThickness 保留 1 位小数（原始值），averageThickness1 为取整后厚度的均值（显示用）。
     */
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
        } else {

            average0 = 0;
            average1 = 0;
        }
        pageBean.averageThickness = average0;
        pageBean.averageThickness1 = average1;
    }

    /**
     * 判断当前部位是否还可继续添加测点：已测数量小于根部主筋数 poleBottomCount 时返回 true。
     */
    private boolean isAdd(RebarPageBean pageBean) {
        return pageBean.beanList.size() < zoneBean.poleBottomCount;
    }

    /**
     * 接收参数设置页（DetectConfigActivity）返回的电杆/测区配置。
     * <p>设置导航标题、厚度合格区间（当前被放宽为 [-1,100]，即不做超限拦截），
     * 并根据根部主筋数配置表头与网格。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            assert data != null;
            zoneBean = (ZoneBean) data.getSerializableExtra("ZoneBean");
            assert zoneBean != null;
            // 新建（无 id）的测区清空快照，避免脏数据比对时空指针
            if (zoneBean.id == null) {
                cloneZoneBean = null;
            }
            binding.navigationBar.setProjectName(zoneBean.projectName).setZoneName(zoneBean.poleNo);
            binding.navigationBar.setTitleText("");
            double designThickness = Double.parseDouble(zoneBean.designThickness);
            //+8 -2 剔凿验证：原本按设计厚度±偏差设定合格区间
            thicknessMin = StringUtils.getRounding(designThickness - 2, 1);
            thicknessMax = StringUtils.getRounding(designThickness + 8, 1);
            //+8 -2 剔凿验证：当前覆盖为宽松区间 [-1,100]，实际不拦截
            thicknessMin = -1;
            thicknessMax = 100;
            //
            // 根据根部主筋数配置表头列数并刷新网格
            configGridTop(zoneBean.poleBottomCount);
            refreshGrid();
        }
    }

    private double poleCompensation;  //电杆直径补偿（曲面测量几何偏差）
    /** poleRadius：当前检测高度处的电杆半径（由根/梢直径插值得到）。 */
    private double poleRadius;

    /**
     * 计算当前检测高度处的电杆半径与直径补偿。
     * <p>电杆为锥形，半径由底部直径与梢部直径按高度线性插值得到；
     * 补偿量基于钢筋仪滑轮几何（滑轮半径 22mm、两滑轮跨距 52mm）：
     * ac = poleRadius + 22（轴心到滑轮中心），ad = √(ac² - 26²)，补偿 = ac - ad，
     * 用于修正探头在曲面上测量时因接触几何产生的偏差。
     *
     * @param strDistance 当前检测部位距根部的距离（m）
     */
    private void configCalibration(String strDistance) {
        if (zoneBean != null) {
            double tempDistance = Double.parseDouble(strDistance);
            // 根据高度在底径与梢径之间插值出直径，/2 得半径
            poleRadius = StringUtils.getInterpolationValue(0.001, zoneBean.poleBottomDiameter, Double.parseDouble(zoneBean.poleHeight), zoneBean.poleTopDiameter, tempDistance) / 2;
            double ac = poleRadius + 22;  //22：钢筋仪轱辘半径22mm
            double cd = 52 / 2.0;  // 两滑轮跨距的一半
            double ad = Math.sqrt(ac * ac - cd * cd);
            //电杆直径补偿：曲面测量几何偏差
            poleCompensation = StringUtils.getRounding(ac - ad, 2);
        }
    }

    /**
     * 刷新钢筋列表：清空容器后为每个检测部位（rebarPageList）inflate 一行网格，
     * 以行号作为 itemView 的 id（供长按删除时定位），并绑定长按监听。
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
     * 网格行长按监听：弹红色确认框删除对应检测部位的数据，
     * 删除后刷新网格；若仍有部位则按最后一个部位刷新按钮，否则禁用按钮。
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
     * 刷新检测/设置/保存/文件四个按钮的可用状态与背景色。
     * <p>flag==true 时再看当前部位是否已有数据（beanList 非空则启用）；
     * flag==false 直接禁用。启用时用绿色背景，禁用时用灰色背景。
     *
     * @param flag     是否需根据部位数据量进一步判定
     * @param pageBean 当前检测部位（flag==true 时使用）
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

    /**
     * 统一点击事件分发（所有按钮/页签/横竖向切换均绑定到此）。
     * <p>处理：开始检测、参数设置、保存、数据文件、退出、页签切换、预估直径、横/竖向切换。
     */
    public void onViewClicked(View view) {
        // 点击任意按钮先隐藏键盘
        OtherUtils.hideKeyboard(this);
        if (view.getId() == binding.btnCheck.getId()) {
            // 开始检测：新增一个检测部位
            checkThickness();
        } else if (view.getId() == binding.btnSetting.getId()) {
            // 参数设置：携带当前 zoneBean 跳转配置页，等待返回结果
            {
                Intent intent = new Intent(this, DetectConfigActivity.class);
                intent.putExtra("ZoneBean", zoneBean);
                startActivityForResult(intent, 1);
            }
        } else if (view.getId() == binding.btnTesting.getId()){
            // 预留：测试按钮，暂无逻辑

        } else if (view.getId() == binding.btnSave.getId()) {
            // 保存：校验后写入数据库，并生成快照用于后续脏数据比对
            if (zoneBean == null) {
                ToastUtils.showShort("请进行参数设置");
                return;
            }
            if (zoneBean.rebarPageList.isEmpty()) {
                ToastUtils.showShort("请先检测数据");
                return;
            }
            zoneBean.startTime = StringUtils.getTime();
            // insertOrReplace 写库，回写主键 id
            zoneBean.id = GreenDaoHelper.getDaoSession().getZoneBeanDao().insertOrReplace(zoneBean);
            ToastUtils.showShort("保存成功");
            // 通过 Gson 序列化/反序列化生成一份深拷贝快照
            String strClone = new Gson().toJson(zoneBean);
            cloneZoneBean = new Gson().fromJson(strClone, new TypeToken<ZoneBean>() {
            }.getType());
            // 若最后一个部位尚无数据，从快照中移除（与 getShowText 的有效部位计数保持一致）
            int lastIndex = cloneZoneBean.rebarPageList.size() - 1;
            int lastBeanListSize = cloneZoneBean.rebarPageList.get(lastIndex).beanList.size();
            if (lastBeanListSize == 0) {
                cloneZoneBean.rebarPageList.remove(lastIndex);
            }
            // 保存后置灰保存按钮，避免重复保存
            binding.btnSave.setEnabled(false);
            binding.btnSave.setBackgroundResource(R.drawable.bg_button_gray);
        } else if (view.getId() == binding.btnFile.getId()) {
            // 数据文件：携带工程名与电杆 id 进入数据管理页
            Intent intent = new Intent(this, DataFileManagerActivity.class);
            intent.putExtra("projectName", zoneBean == null ? "" : zoneBean.projectName);
            intent.putExtra("poleId", zoneBean == null ? "" : zoneBean.id);
            startActivity(intent);
        } else if (view.getId() == binding.btnFinish.getId()) {
            // 退出：根据是否有未保存数据给出不同提示，确认后 finish
            String showText = getShowText();
            StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this, "red");
            styleAlertDialog.setContent(showText).setLeftButton("取消", v1 -> styleAlertDialog.dismiss()).setRightButton("确定", v1 -> {
                styleAlertDialog.dismiss();
                finish();
            }).show();
        } else if (view.getId() == binding.tvTabDetection.getId()) {
            // 切换到“单点检测”页签
            showView(0);
        } else if (view.getId() == binding.tvTabForecast.getId()) {
            // 切换到“预估直径”页签
            showView(1);
        } else if (view.getId() == binding.btnForecast.getId()) {
            // 预估直径：开始/结束切换，结束时根据信号变化率预估主筋直径
            double currentDistance;
            {
                // 校验参数与距离输入
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
            // 切换开始/结束状态
            isForecastStart = !isForecastStart;
            if (isForecastStart) {
                // 开始：重置信号最值，按钮文本改为“结束”
                binding.btnForecast.setText("结束");
                valueMinF = 10000;
                valueMaxF = 0;
            } else {
                // 结束：根据 (max-min)/min 变化率预估直径
                binding.btnForecast.setText("开始");
                if (valueMinF == 0 || valueMaxF == 0) {
                    ToastUtils.showShort("信号值为0，无法预估");
                    return;
                }
                //预估直径：信号变化率越大表明钢筋越细
                double rate = StringUtils.getRounding(100.0 * (valueMaxF - valueMinF) / valueMinF, 2);
                String strForecast;
                // 靠近梢部（距顶 ≤3m）与中下部采用不同的判定阈值
                boolean flag = currentDistance >= Double.parseDouble(zoneBean.poleHeight) - 3;
                if (flag) {
                    strForecast = rate <= 20 ? "14" : "10";
                } else {
                    strForecast = rate <= 40 ? "14" : "10";
                }
                binding.tvForecast.setText(Html.fromHtml("<font color=#000000>预估直径(mm)：</font><font color=#006F5F>" + strForecast + "</font>"));
            }
        } else if (view.getId() == binding.ivHor.getId() || view.getId() == binding.tvHor.getId()) {
            //切到横向：清空已采集的横向峰值数据
            checkDataList.clear();
            refreshTvCheck(false);
            setHorVer(1);
        } else if (view.getId() == binding.ivVer.getId() || view.getId() == binding.tvVer.getId()) {
            //切到竖向：仅当横向已集齐 3 个峰值时才允许
            if (checkDataList.size() == 3) {
                setHorVer(2);
            }
        }
    }

    private int horVer;  //当前扫描方向：1.横向（沿圆周），2.竖向（沿轴向）

    /**
     * 切换横向/竖向模式，同步更新两个选项的图标（选中/未选中）与文字颜色。
     *
     * @param hover 1-横向，2-竖向
     */
    private void setHorVer(int hover) {
        horVer = hover;
        if (hover == 1) {
            // 横向选中、竖向未选中
            binding.ivHor.setImageResource(R.mipmap.icon_single_selected);
            binding.tvHor.setTextColor(ContextCompat.getColor(this, R.color.color006F5F));
            binding.ivVer.setImageResource(R.mipmap.icon_single_unselected);
            binding.tvVer.setTextColor(ContextCompat.getColor(this, R.color.color999));
        } else if (hover == 2) {
            // 竖向选中、横向未选中
            binding.ivHor.setImageResource(R.mipmap.icon_single_unselected);
            binding.tvHor.setTextColor(ContextCompat.getColor(this, R.color.color999));
            binding.ivVer.setImageResource(R.mipmap.icon_single_selected);
            binding.tvVer.setTextColor(ContextCompat.getColor(this, R.color.color006F5F));
        }
    }

    /** isForecastStart：预估直径是否处于“采集中”状态（控制开始/结束按钮）。 */
    private boolean isForecastStart;

    /**
     * 开始检测（新增一个检测部位）：校验参数与距离输入后，
     * 计算当前高度的半径/补偿，新增一个 RebarPageBean，刷新网格并滚动到底部，
     * 同时暂时禁用四个功能按钮（待采集到数据后再由 refreshButon 启用）。
     */
    private void checkThickness() {
        // 校验：必须先设置参数
        if (zoneBean == null) {
            ToastUtils.showShort("请进行参数设置");
            return;
        }
        // 最多检测 10 个位置
        if (zoneBean.rebarPageList.size() >= 10) {
            ToastUtils.showShort("最多检测10个位置");
            return;
        }
        // 校验距离输入非空且可解析为数字
        String strDistance = binding.etDistance.getText().toString().trim();
        if (TextUtils.isEmpty(strDistance)) {
            ToastUtils.showShort("请填写完整");
            return;
        }
        try {
            Double.parseDouble(strDistance);
        } catch (NumberFormatException ignored) {
            ToastUtils.showShort("请填写完整");
            return;
        }
        // 根据当前距离计算电杆半径与直径补偿
        configCalibration(strDistance);
        // 新增一个检测部位（部位名形如“距根部 X m”）
        String segment = "距根部" + strDistance + "m";
        zoneBean.rebarPageList.add(new RebarPageBean(segment, Double.parseDouble(strDistance)));
        refreshGrid();
        // 滚动到底部，展示新增的部位
        binding.scrollView.post(() -> binding.scrollView.fullScroll(View.FOCUS_DOWN));
        //
        // 暂时禁用四个功能按钮，待采集到数据后再启用
        binding.btnCheck.setEnabled(false);
        binding.btnCheck.setBackgroundResource(R.drawable.bg_button_gray);
        binding.btnSetting.setEnabled(false);
        binding.btnSetting.setBackgroundResource(R.drawable.bg_button_gray);
        binding.btnSave.setEnabled(false);
        binding.btnSave.setBackgroundResource(R.drawable.bg_button_gray);
        binding.btnFile.setEnabled(false);
        binding.btnFile.setBackgroundResource(R.drawable.bg_button_gray);
    }

    private int checkType;  //当前页签：0-单点检测，1-预估直径

    /**
     * 切换页签（单点检测 / 预估直径）。
     * <p>先将两个页签重置为未选中样式（灰色、常规字体、白色下划线、内容隐藏），
     * 再将目标页签置为选中样式（主题绿、加粗、绿色下划线、内容可见）。
     *
     * @param position 0-单点检测，1-预估直径
     */
    private void showView(int position) {
        checkType = position;
        // 重置两个页签为未选中样式
        binding.tvTabDetection.setTextColor(color8E8E8E);
        binding.tvTabForecast.setTextColor(color8E8E8E);
        Typeface typefaceNormal = Typeface.create("sans-serif-bold", Typeface.NORMAL);
        binding.tvTabDetection.setTypeface(typefaceNormal);
        binding.tvTabForecast.setTypeface(typefaceNormal);
        binding.viewTabDetection.setBackgroundColor(colorFFF);
        binding.viewTabForecast.setBackgroundColor(colorFFF);
        binding.layoutDetection.setVisibility(View.GONE);
        binding.layoutForecast.setVisibility(View.GONE);
        // 将目标页签置为选中样式并显示对应内容
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

    /**
     * 刷新横向采集的三个进度指示器（tvCheck11/12/13）。
     * <p>isShow==true 时按已采集峰值数量逐个点亮；isShow==false 时全部隐藏。
     */
    private void refreshTvCheck(boolean isShow) {
        if (isShow) {
            binding.tvCheck11.setVisibility(!checkDataList.isEmpty() ? View.VISIBLE : View.INVISIBLE);
            binding.tvCheck12.setVisibility(checkDataList.size() >= 2 ? View.VISIBLE : View.INVISIBLE);
            binding.tvCheck13.setVisibility(checkDataList.size() >= 3 ? View.VISIBLE : View.INVISIBLE);
        } else {
            binding.tvCheck11.setVisibility(View.INVISIBLE);
            binding.tvCheck12.setVisibility(View.INVISIBLE);
            binding.tvCheck13.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 生成退出确认文案：通过比对当前 zoneBean 与保存快照 cloneZoneBean 判断是否有未保存修改。
     * <p>比对维度：有效部位数、各部位测点数、各测点位移；任一不一致则视为有未保存数据。
     * （最后一个无数据的部位不计入有效部位数）
     *
     * @return 有未保存数据时返回“数据未存储，确定退出检测？”，否则返回“确定退出检测？”
     */
    @NonNull
    private String getShowText() {
        boolean isShowSave = false;
        if (zoneBean != null) {
            if (!zoneBean.rebarPageList.isEmpty()) {
                // 有效部位数：若最后一个部位尚无测点则不计入
                int rebarPageListSize = zoneBean.rebarPageList.size();
                int lastBeanListSize = zoneBean.rebarPageList.get(zoneBean.rebarPageList.size() - 1).beanList.size();
                if (lastBeanListSize == 0) {
                    rebarPageListSize -= 1;
                }
                if (zoneBean.id == null) {
                    // 从未保存过：只要有有效部位就视为未保存
                    isShowSave = rebarPageListSize > 0;
                } else {
                    // 已保存过：部位数变化则未保存
                    if (rebarPageListSize != cloneZoneBean.rebarPageList.size()) {
                        isShowSave = true;
                    }
                    // 各部位测点数变化则未保存
                    if (!isShowSave) {
                        for (int i = 0; i < rebarPageListSize; i++) {
                            if (zoneBean.rebarPageList.get(i).beanList.size() != cloneZoneBean.rebarPageList.get(i).beanList.size()) {
                                isShowSave = true;
                            }
                        }
                    }
                    // 逐测点比对位移，任一不同则未保存（flag 用于提前退出双层循环）
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
        return isShowSave ? "数据未存储，确定退出检测？" : "确定退出检测？";
    }

    /**
     * 配置一行网格（一个检测部位）的显示。
     * <p>分三步：
     * <ol>
     *     <li>根据主筋数 rebarCount 控制 1~10 列单元格与分隔线的可见性；</li>
     *     <li>按 beanList 顺序把每个测点的厚度填入对应列；</li>
     *     <li>计算并在末列展示该部位的平均厚度（上行取整值、下行原始值）。</li>
     * </ol>
     *
     * @param itemView   已 inflate 的行布局
     * @param rebarCount 根部主筋数（决定显示多少列）
     * @param bean       当前检测部位数据
     */
    private void configGrid(View itemView, int rebarCount, RebarPageBean bean) {
        TextView tvNumber = itemView.findViewById(R.id.tv_grid_number);
        // 按主筋数控制 1~10 列（文本单元格 tv + 分隔线 v）的可见性
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
        // 设置部位名（如“距根部 X m”）
        tvNumber.setText(bean.segment);
        if (bean.beanList == null) {
            return;
        }
        // 按 beanList 顺序把每个测点的厚度填入对应列（最多 10 列）
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
        // 计算该部位的平均厚度（仅统计 thickness>0），上行取整值 average1、下行原始值 average0
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

    /**
     * 设置单个测点单元格：上行为取整厚度 thickness1、下行为原始厚度 thickness；
     * 若取整厚度超出合格区间 [thicknessMin, thicknessMax] 则用告警橙色，否则用黑色。
     */
    private void setTextView(TextView textView, RebarBean bean) {
        textView.setText(bean.thickness1 + "\n" + bean.thickness);
        textView.setTextColor((bean.thickness1 > thicknessMax || bean.thickness1 < thicknessMin) ? colorFF9600 : color000);
    }

    /**
     * 配置网格表头：根据主筋数 rebarCount 控制表头 1~10 列及平均列的可见性（与内容行列数对齐）。
     */
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

    /**
     * 页面销毁时释放资源：关闭定时线程池、释放语音播放器。
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        scheduledExecutorService.shutdownNow();
        CustomMediaPlayer.releasePlayer(mediaPlayer);
    }
}