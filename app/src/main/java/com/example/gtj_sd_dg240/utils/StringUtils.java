package com.example.gtj_sd_dg240.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gtj_sd_dg240.StaticConstant;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

@SuppressLint("SimpleDateFormat")
public class StringUtils {
    private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getTime() {
        return format.format(new Date());
    }

    public static String getTime(long time, String format) {
        DateFormat ft = new SimpleDateFormat(format);
        return ft.format(new Date(time));
    }

    public static int formatInteger(String string) {
        return Integer.valueOf(string, 16);
    }

    public static String formatDouble(String string, float divisor, String format) {
        float d = Integer.valueOf(string, 16) / divisor;
        return String.format(format, d);
    }

    public static short parseHex4(String num) {
        if (num.length() != 4) {
            throw new NumberFormatException("Wrong length: " + num.length() + ", must be 4.");
        }
        int ret = Integer.parseInt(num, 16);
        ret = ((ret & 0x8000) > 0) ? (ret - 0x10000) : (ret);
        return (short) ret;
    }

    public static String getFormatNumber(String num, int length) {
        int size = length - num.length();
        if (size <= 0) {
            return num;
        }
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < size; i++) {
            temp.append("0");
        }
        return temp + num;
    }

    public static double getFormatNumber(double d) {
        return (double) Math.round(d * 1000) / 1000;
    }

    public static double getDivide(double d1, double d2) {
        BigDecimal bdl_temp = new BigDecimal(d1).divide(new BigDecimal(d2), 3, BigDecimal.ROUND_HALF_UP);
        return bdl_temp.doubleValue();
    }

    public static double getSettlement(double a0, double a1, double a2, double pressure) {
        BigDecimal bdlp = new BigDecimal(pressure);
        BigDecimal a1p = new BigDecimal(a1).multiply(bdlp);
        BigDecimal a2p = new BigDecimal(a2).multiply(bdlp).multiply(bdlp);
        BigDecimal bdl_result = new BigDecimal(a0).add(a1p).add(a2p);
        return bdl_result.doubleValue();
    }

    public static void stringFormatFilter(CharSequence s, TextView textView, ImageView imageView) {
        StringBuilder sbstr = new StringBuilder(s.toString());
        if (imageView != null) {
            if (sbstr.length() > 0) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.GONE);
            }
        }
        if (sbstr.length() > 0) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
        //D8:B0:4C:CC:74:AF
        if (sbstr.length() > 2) {
            sbstr.insert(2, ":");
        }
        if (sbstr.length() > 5) {
            sbstr.insert(5, ":");
        }
        if (sbstr.length() > 8) {
            sbstr.insert(8, ":");
        }
        if (sbstr.length() > 11) {
            sbstr.insert(11, ":");
        }
        if (sbstr.length() > 14) {
            sbstr.insert(14, ":");
        }
        textView.setText(sbstr.toString().toUpperCase());
    }

    public static void stringFormatFilter(CharSequence s, TextView textView) {
        StringBuilder sbstr = new StringBuilder(s.toString());
        if (sbstr.length() > 0) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
        //D8:B0:4C:CC:74:AF
        if (sbstr.length() > 2) {
            sbstr.insert(2, "　");
        }
        if (sbstr.length() > 5) {
            sbstr.insert(5, "　");
        }
        if (sbstr.length() > 8) {
            sbstr.insert(8, "　");
        }
        if (sbstr.length() > 11) {
            sbstr.insert(11, "　\n");
        }
        if (sbstr.length() > 15) {
            sbstr.insert(15, "　");
        }
        if (sbstr.length() > 18) {
            sbstr.insert(18, "　");
        }
        if (sbstr.length() > 21) {
            sbstr.insert(21, "　");
        }
        if (sbstr.length() > 24) {
            sbstr.insert(24, "　\n");
        }
        if (sbstr.length() > 28) {
            sbstr.insert(28, "　");
        }
        if (sbstr.length() > 31) {
            sbstr.insert(31, "　");
        }
        if (sbstr.length() > 34) {
            sbstr.insert(34, "　");
        }
        if (sbstr.length() > 37) {
            sbstr.insert(37, "　\n");
        }
        if (sbstr.length() > 41) {
            sbstr.insert(41, "　");
        }
        if (sbstr.length() > 44) {
            sbstr.insert(44, "　");
        }
        if (sbstr.length() > 47) {
            sbstr.insert(47, "　");
        }
        textView.setText(sbstr.toString().toUpperCase());
    }

    @SuppressLint("DefaultLocale")
    public static String getRandom() {
        Random random = new Random();
        int ends = random.nextInt(99);
        return String.format("%02d", ends);
    }

    public static String D2Dms(double d_data) {
        int d = (int) d_data;
        int m = (int) ((d_data - d) * 60);
        int s = (int) (((d_data - d) * 60 - m) * 60);
        return d + "°" + m + "′" + s + "″";
    }

    public static String getStringTime(long time, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(new Date(time));
    }

    /**
     * 四舍五入
     * 当数足够小的时候，double默认显示小数点后3位，如0.001，4位需要BigDecimal转String显示（0.0001等于1.0E-4）
     */
    public static double getRounding(double d, int rate) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            d = 0;
        }
        BigDecimal big = new BigDecimal(d + "");
        return big.setScale(rate, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String getRoundingString(double d, int rate) {
        if (Double.isNaN(d)) {
            d = 0;
        }
        BigDecimal big = new BigDecimal(d + "");
        return big.setScale(rate, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    /**
     * 获取字符串里最后数字index
     */
    public static int getNumberIndex(String str) {
        int index = -1;
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                index = i + 1;
                break;
            }
        }
        if (index == -1) {
            if (Pattern.compile("^[-\\+]?[\\d]*$").matcher(str).matches()) {
                return 0;
            }
        }
        if (index >= str.length()) {
            index = -1;
        }
        return index;
    }

    /**
     * 求内插数
     */
    public static double getInterpolationValue(double x1, double y1, double x2, double y2, double x) {
        // K＝(y2-y1)/(x2-x1)
        double K = (y2 - y1) / (x2 - x1);
        // y-y0=k(x-x0)
        return K * (x - x1) + y1;
    }

    /**
     * 求内插数
     */
    public static int getInterpolationValueInt(double x1, double y1, double x2, double y2, double x) {
        // K＝(y2-y1)/(x2-x1)
        double K = (y2 - y1) / (x2 - x1);
        // y-y0=k(x-x0)
        return (int) (K * (x - x1) + y1);
    }

    /**
     * 求内插数
     */
    public static double getInterpolationValueBBB(double x1, double y1, double x2, double y2, double x) {
        // K＝(y2-y1)/(x2-x1)
        double K = (y2 - y1) / (x2 - x1);
        // y-y0=k(x-x0)
        double temp = K * (x - x1) + y1;
        return Math.min(temp, y2);
    }

    /**
     * 16进制转10进制
     */
    public static int hex2decimal(String hex) {
        return Integer.parseInt(hex, 16);
    }

    /**
     * byte[]转换为16进制字符串
     */
    public static String[] bytesToHexStrArray(byte[] bArray) {
        String[] strArray = new String[bArray.length];
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) {
                sTemp = "0" + sTemp;
            }
            strArray[i] = sTemp.toUpperCase();
        }
        return strArray;
    }

    public static String ByteArraytoHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String bs = String.format("%02X ", b);
            sb.append(bs);
        }
        return sb.toString();
    }

    public static int getPoleTypeIndex(String string) {
        for (int i = 0; i < StaticConstant.configPoleTypeList.size(); i++) {
            if (TextUtils.equals(string, StaticConstant.configPoleTypeList.get(i))) {
                return i;
            }
        }
        return 0;
    }
}
