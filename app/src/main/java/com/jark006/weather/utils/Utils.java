package com.jark006.weather.utils;

import android.app.NotificationManager;
import android.content.Context;

import com.jark006.weather.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {

    public static final String UPDATE_UI = "jark_weather_UPDATE_UI";
    public static final String REQUEST_MANUAL = "jark_weather_REQUEST_MANUAL";
    public static final String REQUEST_FREQ = "jark_weather_REQUEST_FREQ";

    public static final double defLongitude = 113.381917;//默认在广州大学城
    public static final double defLatitude = 23.039316;


    // 01台风 02暴雨 ... 18沙尘
    public final static String[] warnTypeStr = {
            "其他", "台风", "暴雨", "暴雪", "寒潮", "大风",
            "沙尘暴", "高温", "干旱", "雷电", "冰雹", "霜冻",
            "大雾", "霾", "道路结冰", "森林火灾", "雷雨大风",
            "春季沙尘天气趋势预警", "沙尘"
    };
    //00白色 ... 04红色
    public final static String[] warnLevelStr = {"白色预警", "蓝色预警", "黄色预警", "橙色预警", "红色预警"};
    public final static String[] warnLevelDescription = {
            "台风或热带气旋预警",
            "Ⅳ级（一般）预警",
            "Ⅲ级（较重）预警",
            "Ⅱ级（严重）预警",
            "Ⅰ级（特别严重）预警",
    };

    public final static int[] IMPORTANT_INT = {
            NotificationManager.IMPORTANCE_NONE,
            NotificationManager.IMPORTANCE_MIN,
            NotificationManager.IMPORTANCE_LOW,
            NotificationManager.IMPORTANCE_DEFAULT,
            NotificationManager.IMPORTANCE_HIGH,
            NotificationManager.IMPORTANCE_MAX,
    };
    public static final int[] warnIconIndex = {
            R.drawable.ic_warning_white,
            R.drawable.ic_warning_blue,
            R.drawable.ic_warning_yellow,
            R.drawable.ic_warning_orange,
            R.drawable.ic_warning_red,
    };


    public static void saveLog(Context context, String path, String text) {
        try {
            String str = "[" + DateUtils.getLogTime() + "] " + text + "\n";
            File logFile = context.getFileStreamPath(path);// /data/data/包名/files
            int logFileMode = (logFile.length() > 100 * 1024) ? Context.MODE_PRIVATE : Context.MODE_APPEND;
            FileOutputStream fileOut = context.openFileOutput(path, logFileMode);
            fileOut.write(str.getBytes());
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
