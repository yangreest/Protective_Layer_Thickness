package com.example.gtj_sd_dg240;

import android.annotation.SuppressLint;

import com.blackhao.utillibrary.usbHelper.UsbHelper;
import com.blankj.utilcode.util.SPUtils;
import com.example.gtj_sd_dg240.model.ProjectBean;
import com.example.gtj_sd_dg240.utils.LocService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressLint("SdCardPath")
public class StaticConstant {
    //1.修改 APP_DEFAULT
    //2.修改 colors.xml
    //3.修改 drawable.xml
    public static final boolean isNeutral = true;  //中性版本（隐藏图标版本）
    public static final String APP_LIERDA = "立尔达";
    public static final String APP_DIANKEYUAN = "电科院";
    public static final String APP_MINGSHENGHENGZHUO = "明生恒卓";
    public static final String APP_GTJ = "";
    public static String APP_DEFAULT = APP_LIERDA;  //当前版本

    public static final String httpUrl = "https://tims.sidajiance.com/pro-api/open-api/";

    //数据库名称
    public static final String DB_NAME = "gtj_anchorbolt.db";
    //崩溃信息是否存文件
    public static final boolean CRASH_DEBUG = true;
    //崩溃信息存储地址
    public static final String crashFilePath = "/sdcard/gtj_anchorbolt_crash/";
    public static final String pwd = "123456";
    public static final String pwd1 = "123456789";
    @SuppressLint("StaticFieldLeak")
    public static UsbHelper usbHelper;
    public static LocService locService;
    //百度地图定位
    public static double mapLatitude;
    public static double mapLontitude;
    public static double mapAltitude;
    //电杆类型
    public static final String poleType_PT = "普通混凝土电杆";
    public static final String poleType_Y = "预应力混凝土电杆Y";
    public static final String poleType_BY = "部分预应力混凝土电杆BY";
    public static final List<String> configPoleTypeList = Arrays.asList(poleType_PT, poleType_Y, poleType_BY);
    //电杆结构
    public static final String poleStructure_ZX = "锥形杆";
    public static final String poleStructure_FL = "法兰杆";
    public static final String poleStructure_DJ = "等径杆";
    public static final List<String> configpoleStructureList = Arrays.asList(poleStructure_ZX, poleStructure_FL, poleStructure_DJ);
    //规范
    public static final List<String> poleStandardList = List.of("GB/T 4623-2014 环形混凝土电杆");
    //纵筋直径
//    public static final List<String> rebarDiameterList = Arrays.asList("7", "9", "10", "11", "14");
    public static final List<String> rebarDiameterList = Arrays.asList("6", "7", "8", "9", "10", "11", "12", "14", "16", "18");
    public static final List<String> rebarDiameterList1 = Arrays.asList("10", "14");  //计量
    public static final List<String> hoopingDiameterList = Arrays.asList("3", "4");
    //检测部位
//    public static final List<String> checkPointList = Arrays.asList("根部", "梢部", "距根部");
    public static final List<String> stirrupSpacingList = Arrays.asList("30", "40", "50", "60", "70", "120");
    public static List<ProjectBean> projectBeanList = new ArrayList<>();

    public static List<String> configList = Arrays.asList("190mm，10米杆，I级", "190mm，12米杆，M级", "190mm，15米杆，M级", "Z Ø150×8×G×Y", "Z Ø190×10×K×Y", "Z Ø190×12×K×Y", "Z Ø190×15×K×Y", "Z Ø190×12(6+6)×K×Y", "Z Ø190×15(9+6)×K×Y", "Z Ø190×12×100×BY", "Z Ø190×12×M×BY", "Z Ø190×15×M×BY", "Z Ø230×10×O×BY", "Z Ø230×12×125×BY", "Z Ø230×15×150×BY", "Z Ø350×15×U×BY", "Z Ø190×15(9+6)×100×BY", "Z Ø190×18(9+9)×M×BY", "Z Ø310×12(6+6)×O×BY", "Z Ø310×12(6+6)×U×BY", "Z Ø230×12(6+6)×O×BY");

    //低功耗蓝牙
    public static final String USR_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String USR_CHARACTER_NOTIFY = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static final String USR_CHARACTER_WRITE = "0000ffe1-0000-1000-8000-00805f9b34fb";
    //1：中文；2：英文
    private static int languageType;

    public static void setLanguage(int type) {
        languageType = type;
        SPUtils.getInstance().put("languageType", type);
    }

    public static boolean getLanguage() {
        if (languageType == 0) {
            //默认值切换中英文，1：中文；2：英文
            languageType = SPUtils.getInstance().getInt("languageType", 1);
        }
        return languageType == 1;
    }
}
