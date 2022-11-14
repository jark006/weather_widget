package com.jark006.weather;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.jark006.weather.utils.DateUtils.getFormatDate;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.jark006.weather.bean.WarnInfo;
import com.jark006.weather.utils.DateUtils;
import com.jark006.weather.utils.NetworkUtils;
import com.jark006.weather.utils.Utils;

import java.util.List;
import java.util.zip.CRC32;

/**
 * 天气小部件
 */
public abstract class BaseWidget extends AppWidgetProvider {
    final static String TAG = "Widget";

    final int UPDATE_SUCCESS = 0;
    final int UPDATE_FAILED = 1;
    final int UPDATE_ONGOING = 2;

    final String[] warnTypeStr = Utils.warnTypeStr;// 01台风 02暴雨 ... 18沙尘
    final String[] warnLevelStr = Utils.warnLevelStr;//00白色 ... 04红色
    final String[] warnLevelDescription = Utils.warnLevelDescription;
    final int[] IMPORTANT_INT = Utils.IMPORTANT_INT;
    final int[] warnIconIndex = Utils.warnIconIndex;

    static boolean isFirst = false;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(TAG, "onEnabled: 创建小部件");
        isFirst = true;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // widget1_info.xml     android:updatePeriodMillis
        long hours = (System.currentTimeMillis() / 3600000 + 8) % 24; // UTC+8
        if (isFirst || hours >= 6) {
            Log.d(TAG, "onUpdate: 定时刷新");
            getWeatherData(context, "定时刷新...");
        } else { // 凌晨 00:00 - 05:59 不更新天气
            Log.d(TAG, "onUpdate: 现在凌晨" + hours + "时，暂停刷新天气");
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        if (Utils.REQUEST_MANUAL.equals(intent.getAction())) {
            Log.d(TAG, "onReceive: 手动刷新" + isFirst);
            getWeatherData(context, "手动刷新...");
        }
    }


    /**
     * 获取天气数据
     * <a href=https://api.caiyunapp.com/v2.6/wh9aWLYieE1akfGi/113.381429,23.039126/weather.json?alert=true>链接</a>
     */
    @SuppressLint("DefaultLocale")
    private void getWeatherData(final Context context, String tips) {

        updateAppWidget(context, UPDATE_ONGOING, tips, false);

        new Thread(() -> {
            boolean noLocation = false;
            SharedPreferences sf = context.getSharedPreferences("locationInfo", Context.MODE_PRIVATE);
            double longitude = sf.getFloat("longitude", 0);
            double latitude = sf.getFloat("latitude", 90); // 北极

            if (Math.abs(latitude) > 88.0) {  // 靠近北极就是位置异常
                longitude = Utils.defLongitude;
                latitude = Utils.defLatitude;
                noLocation = true;
            }

            String link = String.format("https://api.caiyunapp.com/v2.6/wh9aWLYieE1akfGi/%f,%f/weather.json?alert=true",
                    longitude, latitude);

            final int retryTimes = 3;
            String res = null;
            for (
                    int i = 1;
                    i <= retryTimes; i++) {
                try {
                    res = NetworkUtils.getData(link);
                } catch (Exception e) {
                    String log = String.format("getWeatherData: 第%d次网络异常, 剩余%d次", i, retryTimes - i);
                    Log.e(TAG, log + e);
                    Utils.saveLog(context, "log.log", "getWeatherData: " + log);
                }
                if (res != null)
                    break;

                String log = String.format("getWeatherData: 第%d次ResponseNull, 剩余%d次", i, retryTimes - i);
                Log.e(TAG, log);
                Utils.saveLog(context, "log.log", "getWeatherData: " + log);
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }

            if (res == null) {
                res = getFormatDate(System.currentTimeMillis(), DateUtils.HHmm) + "获取天气数据失败";
                updateAppWidget(context, UPDATE_FAILED, res, noLocation);
            } else {
                updateAppWidget(context, UPDATE_SUCCESS, res, noLocation);
            }
        }).start();
    }

    public void notify(Context context, List<WarnInfo> warnInfo) {
        for (WarnInfo info : warnInfo) {
            int warnLevel = 0;
            String title = info.location + " " + info.status;
            int importantLevel = NotificationManager.IMPORTANCE_DEFAULT;

            try {
                int warnType = Integer.parseInt(info.code); // 2位预警类型编码 ＋ 2位预警级别编码
                warnLevel = warnType % 100; // 预警级别 00 ~ 04

                if (warnLevel < 0 || warnLevel > 4)
                    warnLevel = 0;

                warnType /= 100; // 预警类型 01 ~ 18， 00未知
                if (warnType < 1 || warnType > 18)
                    warnType = 0;
                title = info.location + " " + warnTypeStr[warnType] + " " + info.status;
                importantLevel = IMPORTANT_INT[warnLevel + 1]; // 预警级别 00 ~ 04 对应 通知重要级别 01-05
            } catch (Exception e) {
                e.printStackTrace();
            }

            String channelId = warnLevelStr[warnLevel];

            RemoteViews cusRemoveExpandView = new RemoteViews(context.getPackageName(), R.layout.layout_notify_large);
            cusRemoveExpandView.setTextViewText(R.id.title, info.title);
            cusRemoveExpandView.setTextViewText(R.id.content, info.description);
            cusRemoveExpandView.setImageViewResource(R.id.icon, warnIconIndex[warnLevel]);

            Notification notification = new Notification.Builder(context, channelId)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_sunny)
                    .setContentTitle(title)
                    .setContentText(info.title)
                    .setStyle(new Notification.DecoratedCustomViewStyle())
                    .setCustomBigContentView(cusRemoveExpandView)
                    .build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelId, channelId, importantLevel);
            channel.setDescription(warnLevelDescription[warnLevel]);
            notificationManager.createNotificationChannel(channel);

            CRC32 crc32 = new CRC32();
            crc32.update(info.alertId.getBytes());
            notificationManager.notify((int) crc32.getValue(), notification);
        }
    }

    /**
     * 创建更新数据 PendingIntent
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    protected PendingIntent createUpdatePendingIntent(Context context) {
        Intent updateIntent = new Intent(Utils.REQUEST_MANUAL);
        updateIntent.setClass(context, this.getClass());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            return PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_MUTABLE);
        else
            return PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    abstract public void updateAppWidget(Context context, int status, String weatherJsonOrTips, boolean noLocation);

}