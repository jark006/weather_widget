package io.github.jark006.weather.utils;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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


    public static boolean isNoPermission(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED;
    }

    public static void createNotificationChannel(Activity activity, Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                isNoPermission(activity, Manifest.permission.POST_NOTIFICATIONS)) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS,}, 1);

            if (isNoPermission(activity, Manifest.permission.POST_NOTIFICATIONS)) {
                Toast.makeText(context, context.getString(R.string.notification_tips), Toast.LENGTH_LONG).show();

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                activity.startActivity(intent);
                return;
            }
        }

        // 创建预警信息通知通道
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        for (int warnLevel = 0; warnLevel < 5; warnLevel++) {
            String channelId = Utils.warnLevelStr[warnLevel];//00白色 ... 04红色
            NotificationChannel channel = new NotificationChannel(channelId, channelId, Utils.IMPORTANT_INT[warnLevel + 1]);
            channel.setDescription(Utils.warnLevelDescription[warnLevel]);
            notificationManager.createNotificationChannel(channel);
        }
    }

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
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
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

    public static Object readObj(Context context, String path) {
        try {
            FileInputStream fis = context.openFileInput(path);
            ObjectInputStream ois = new ObjectInputStream(fis);
            var obj = ois.readObject();
            ois.close();
            fis.close();
            return obj;
        } catch (FileNotFoundException ignore) {
            saveLog(context, "读取 [" + path + "] 失败，文件不存在。\n");
        } catch (Exception e) {
            saveLog(context, "读取 [" + path + "] 失败\n" + e);
        }
        return null;
    }

    public static void saveObj(Context context, String path, Object obj) {
        try {
            FileOutputStream fos = context.openFileOutput(path, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            oos.close();
            fos.close();
        } catch (Exception e) {
            saveLog(context, "保存 [" + path + "] 失败\n" + e);
        }
    }

}
