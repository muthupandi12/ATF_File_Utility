package com.bijlipay.ATFFileGeneration.Util;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static com.bijlipay.ATFFileGeneration.Util.Constants.*;

public class DateUtil {

    public static String calToDate(Date longDate, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(longDate);
    }
    public static String allTxnDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String currentDate = DateUtil.calToDate(cal.getTime(), DateFormat);
        return currentDate;
    }

    public static String currentDateATF() {
        Calendar cal = Calendar.getInstance();
        String currentDate = DateUtil.calToDate(cal.getTime(), DateFormat);
        return currentDate;
    }

    public static String allTxnDate2() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        String currentDate = DateUtil.calToDate(cal.getTime(), DateFormat);
        return currentDate;
    }

    public static String previousDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String previousDate = DateUtil.calToDate(cal.getTime(), "yyyy-MM-dd");
        return previousDate;
    }

    public static Date stringToDate(String date) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = dateFormatter.parse(date);
        return date1;
    }


    public static String parseSimpleDate(Date date) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return dateFormatter.format(date);
    }

    public static String parseSimpleDateForRules(Date date) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormatter.format(date);
    }

    public static String dateToString(Date date) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormatter.format(date);
    }

    public static String dateToStringForMail(Date date) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH");
        String unique = dateFormatter.format(date);
        String total = unique +"1";
        return total;
    }

    public static String dateComparison(Date date) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM");
        return dateFormatter.format(date);
    }


//    public static String dateComparison(Date date) throws ParseException {
//        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy");
//        return dateFormatter.format(date);
//    }

    public static String currentDate1() {
        Calendar cal = Calendar.getInstance();
        String currentDate = DateUtil.calToDate(cal.getTime(), DateFormat);
        return currentDate;
    }

    public static String previousDateATF() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String currentDate = DateUtil.calToDate(cal.getTime(), DateFormat);
        return currentDate;
    }
    public static String currentDate2() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String currentDate = DateUtil.calToDate(cal.getTime(), DateFormat1);
        return currentDate;
    }

    public static String twoDayBefore() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        String currentDate = DateUtil.calToDate(cal.getTime(), DateFormat1);
        return currentDate;
    }

    public static String currentDateForAxis() {
        Calendar cal = Calendar.getInstance();
        String currentDate = DateUtil.calToDate(cal.getTime(), DateFormat_Axis);
        return currentDate;
    }
    public static String currentDate(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date date = new Date();
        String currentDate = formatter.format(date);
        return currentDate;
    }

    public static Date currentDate() {
        Date date = new Date();
        return date;
    }


    public static Date oneHourBeforeDate(Date date){
        Date newDate = DateUtils.addHours(date, -1);
        return newDate;
    }

    public static String addOneDay(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(date));
        c.add(Calendar.DATE, 1);  // number of days to add
        return sdf.format(c.getTime());
    }

    public static String minusOneDay(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(date));
        c.add(Calendar.DATE, -1);  // number of days to minus
        return sdf.format(c.getTime());
    }

    public static String splitDateTime(String date) throws ParseException {
        Date original = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        String newstr = new SimpleDateFormat("yyyy-MM-dd").format(original);
        return  newstr;
    }

    public static String epochTime(String date) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
        Date date1 = df.parse(date);
        long epoch = date1.getTime();
        return String.valueOf(epoch);
    }
}
