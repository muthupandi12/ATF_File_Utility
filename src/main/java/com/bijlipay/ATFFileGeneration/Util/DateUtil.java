package com.bijlipay.ATFFileGeneration.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.bijlipay.ATFFileGeneration.Util.Constants.DateFormat;
import static com.bijlipay.ATFFileGeneration.Util.Constants.DateFormat1;

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

    public static String currentDate1() {
        Calendar cal = Calendar.getInstance();
//         cal.add(Calendar.DATE, -1);
        String currentDate = DateUtil.calToDate(cal.getTime(), DateFormat);
        return currentDate;
    }


    public static String currentDate2() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String currentDate = DateUtil.calToDate(cal.getTime(), DateFormat1);
        return currentDate;
    }


    public static String currentDate(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date date = new Date();
        String currentDate = formatter.format(date);
        return currentDate;
    }
}
