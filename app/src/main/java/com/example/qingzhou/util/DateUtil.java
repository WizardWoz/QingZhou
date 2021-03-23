/*用于获取当前时间*/
package com.example.qingzhou.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateUtil {
    @SuppressLint("SimpleDateFormat")
    public static String getNowDateTime() {
        SimpleDateFormat s_format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");        //大写HH 24小时制
        return s_format.format(new Date());
    }

    @SuppressLint("SimpleDateFormat")
    public static String getNowTime() {
        SimpleDateFormat s_format = new SimpleDateFormat("HH:mm:ss");           //大写HH 24小时制
        return s_format.format(new Date());
    }

    @SuppressLint("SimpleDateFormat")
    public static Date getDate(String datestr){
        Log.d("getdate", "getDate: "+datestr);
        //从服务器返回的Date类型格式
        SimpleDateFormat s_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");        //大写HH 24小时制
        Date timeDate = null;
        try {
            timeDate = s_format.parse(datestr);
//            timeDate = s_format.parse("2020-08-08 22:22:22");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d("getdate", "getDate: "+timeDate.getTime());
        return timeDate;
    }

}
