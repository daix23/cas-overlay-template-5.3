package com.zjasm.util;

import sun.misc.BASE64Decoder;

import java.util.Calendar;

public class CommonUtil {

    /*
     * 获取当前系统时间，返回拼成的字符串
     */
    public static String GetTimeString() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;// 这种获取日期的模式需要加1
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        String timeStr = Integer.toString(year) + lpad(2, month) + lpad(2, date) + lpad(2, hour) + lpad(2, minute)
                + lpad(2, second);
        return timeStr;
    }

    /**
     * 补齐不足长度
     *
     * @param length
     *            长度
     * @param number
     *            数字
     * @return
     */
    private static String lpad(int length, int number) {
        String f = "%0" + length + "d";
        return String.format(f, number);
    }

    /**
     * 判断字符串为否为空
     *
     * @param str
     * @return
     */
    public static Boolean isNullOrEmpty(String str) {
        return (str == null || str.length() <= 0);
    }

    // 将 BASE64 编码的字符串 s 进行解码
    @SuppressWarnings("restriction")
    public static String getFromBASE64(String s) {
        if (s == null)
            return null;

        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] b = decoder.decodeBuffer(s);
            return new String(b);
        } catch (Exception e) {
            return null;
        }
    }

}
