package io.github.jark006.weather.utils;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.github.jark006.weather.BuildConfig;
import io.github.jark006.weather.R;

public class Utils {
    public static final double defLongitude = 113.381917;//默认在广州
    public static final double defLatitude = 23.039316;

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


    public static void saveLog(Context context, String text) {
        final String logFileName = "log.txt";
        try {
            String str = "[" + DateUtils.getLogTime() + "] " + text + "\n";
            File logFile = context.getFileStreamPath(logFileName);// /data/data/包名/files
            int logFileMode = (logFile.length() > 100 * 1024) ? Context.MODE_PRIVATE : Context.MODE_APPEND;
            FileOutputStream fileOut = context.openFileOutput(logFileName, logFileMode);
            fileOut.write(str.getBytes());
            fileOut.close();
        } catch (IOException ignore) {
        }
    }
    public static String getMetaValue(Context context, String metaName) {
        try {
            ApplicationInfo info=context.getPackageManager().getApplicationInfo(
                    BuildConfig.APPLICATION_ID, PackageManager.GET_META_DATA);
            return info.metaData.getString(metaName);
        } catch (Exception e) {
            return "";
        }
    }

    public static void textDialog(Context context, int titleResID, int contentResID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleResID).setMessage(contentResID).create().show();
    }

}
