package com.wx.wechat.Utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static final String FORMAT_DATE_PATTERN = "yyyy-MM-dd";
    public static final String FORMAT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DATE_TIME_PATERN_WITHOUTBLANK = "yyyyMMddHHmmss";
    public static final String FORMAT_DATE_TIME_PATERN_WITHOUTBLANK02 = "yyyyMMdd";
    public static final String FORMAT_DATE_TIME_PATERN_HHMMSS = "mmss";

    public static String format(Date date, String pattern) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static String formatDateNormal(Date date) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_TIME_PATTERN);
        return sdf.format(date);
    }

    public static String formatDateStr(Date date) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_PATTERN);
        return sdf.format(date);
    }

    public static String getTodayStartDay() {
        String todayDate = format(new Date(), DateUtil.FORMAT_DATE_PATTERN);
        return todayDate;
    }

    public static String getTodayStartTime() {
        String todayTime = format(new Date(), DateUtil.FORMAT_DATE_TIME_PATTERN);
        return todayTime;
    }

    public static String getTodayEndTime() {
        String todayStart = format(new Date(), DateUtil.FORMAT_DATE_PATTERN);
        return todayStart + " 23:59:59";
    }

    public static String getFormatDateTimePattern(String str) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.FORMAT_DATE_TIME_PATTERN);
            Date date = sdf.parse(str);
            if (date.toString() != null) {
                return str;
            }
            return null;
        } catch (Exception e) {

        }
        return null;
    }

    /*
     * 获取一年前的Date
     */
    public static Date getLastYear() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.YEAR, -1);
        Date y = c.getTime();
        return y;
    }

    /*
     * 将时间戳转换为Date
     */
    public static Date stampToDate(String s) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return simpleDateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * 将时间戳转换为时间串
     */
    public static String stampToDateStr(Long timeStamp) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timeStamp);
        res = simpleDateFormat.format(date);
        return res;
    }

    public static String getWeek(String sdate) {
        if (StringUtils.isEmpty(sdate) || "null".equals(sdate)) return null;
        // 再转换为时间
        Date date = strToDate(sdate);
        if (date == null) return null;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        // int hour=c.get(Calendar.DAY_OF_WEEK);
        // hour中存的就是星期几了，其范围 1~7
        // 1=星期日 7=星期六，其他类推
        return new SimpleDateFormat("EEEE").format(c.getTime());
    }

    public static Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    public static Date strToDateTime(String strDateTime) {
        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_DATE_TIME_PATTERN);
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDateTime, pos);
        return strtodate;
    }

    public static Long strToTimeStamp(String strDateTime) {
        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_DATE_TIME_PATTERN);
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDateTime, pos);
        return strtodate.getTime();
    }

    public static String getCurDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
        return f.format(cal.getTime());
    }

    public static String transDate(String date) {
        StringBuilder sb = new StringBuilder(date);
        sb.insert(4, "-").insert(7, "-");
        return sb.toString();
    }

    /*
     * 获取指定天数前的Date
     */
    public static String getBefroeDate(int amount) {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -amount);
        return f.format(c.getTime());
    }

    /*
     * 获取指定分钟前的Date
     */
    public static Date getBeforeMinuteDate(int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MINUTE, -amount);
        return c.getTime();
    }

    /*
     * 获取指定秒数前的Date
     */
    public static Date getPlusSecondsDate(Date date, int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.SECOND, amount);
        return c.getTime();
    }

    /**
     * 获得指定日期时间为零点
     *
     * @param date 指定日期
     * @return 当前日期
     */
    public static Date dateToBegin(Date date) {
        Calendar calendar1 = Calendar.getInstance();
        //获取某一天的0点0分0秒 或者 23点59分59秒
        calendar1.setTime(date);
        calendar1.set(calendar1.get(Calendar.YEAR), calendar1.get(Calendar.MONTH), calendar1.get(Calendar.DAY_OF_MONTH),
                0, 0, 0);
        calendar1.set(Calendar.MILLISECOND,0);
        Date beginOfDate = calendar1.getTime();
        return beginOfDate;
    }

    public static String getYMDHMSWithoutBlank() {
        return format(new Date(), DateUtil.FORMAT_DATE_TIME_PATERN_WITHOUTBLANK);
    }

    public static String getYMDWithoutBlank() {
        return format(new Date(), DateUtil.FORMAT_DATE_TIME_PATERN_WITHOUTBLANK02);
    }

    public static String getMSWithoutBlank() {
        return format(new Date(), DateUtil.FORMAT_DATE_TIME_PATERN_HHMMSS);
    }

}
