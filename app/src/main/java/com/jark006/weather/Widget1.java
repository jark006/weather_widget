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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.jark006.weather.bean.AirQuality;
import com.jark006.weather.bean.CodeName;
import com.jark006.weather.bean.Daily;
import com.jark006.weather.bean.DoubleValue;
import com.jark006.weather.bean.Realtime;
import com.jark006.weather.bean.Skycon;
import com.jark006.weather.bean.Temperature;
import com.jark006.weather.bean.WarnInfo;
import com.jark006.weather.bean.WeatherBean;
import com.jark006.weather.utils.DateUtils;
import com.jark006.weather.utils.ImageUtils;
import com.jark006.weather.utils.NetworkUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.zip.CRC32;

/**
 * 天气小部件
 */
public class Widget1 extends AppWidgetProvider {
    final static String TAG = "Widget";

    final String ACTION_UPDATE = "jark_weather_action_update";
    final double defLongitude = 113.381917;//默认在广州大学城
    final double defLatitude = 23.039316;
    final int UPDATE_SUCCESS = 0;
    final int UPDATE_FAILED = 1;
    final int UPDATE_ONGOING = 2;

    // 01台风 02暴雨 ... 18沙尘
    final String[] warnTypeStr = {
            "其他", "台风", "暴雨", "暴雪", "寒潮", "大风",
            "沙尘暴", "高温", "干旱", "雷电", "冰雹", "霜冻",
            "大雾", "霾", "道路结冰", "森林火灾", "雷雨大风",
            "春季沙尘天气趋势预警", "沙尘"
    };
    //00白色 ... 04红色
    final String[] warnLevelStr = {"白色预警", "蓝色预警", "黄色预警", "橙色预警", "红色预警"};
    final String[] warnLevelDescription = {
            "台风或热带气旋预警",
            "Ⅳ级（一般）预警",
            "Ⅲ级（较重）预警",
            "Ⅱ级（严重）预警",
            "Ⅰ级（特别严重）预警",
    };

    final int[] IMPORTANT_INT = {
            NotificationManager.IMPORTANCE_NONE,
            NotificationManager.IMPORTANCE_MIN,
            NotificationManager.IMPORTANCE_LOW,
            NotificationManager.IMPORTANCE_DEFAULT,
            NotificationManager.IMPORTANCE_HIGH,
            NotificationManager.IMPORTANCE_MAX,
    };

    final int[] warnIconIndex = {
            R.drawable.ic_warning_white,
            R.drawable.ic_warning_blue,
            R.drawable.ic_warning_yellow,
            R.drawable.ic_warning_orange,
            R.drawable.ic_warning_red,
    };
    boolean isFirst = true;
    boolean noLocation = true;
    HashSet<String> hasNotify = new HashSet<>();

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(TAG, "onEnabled: 创建小部件");
        saveLog(context, "log.log","onEnabled: 创建小部件");

        // 创建预警信息通知通道
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        for (int warnLevel = 0; warnLevel < 5; warnLevel++) {
            String channelId = warnLevelStr[warnLevel];
            NotificationChannel channel = new NotificationChannel(channelId, channelId, IMPORTANT_INT[warnLevel + 1]);
            channel.setDescription(warnLevelDescription[warnLevel]);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE.equals(intent.getAction())) {// 手动刷新
            Log.d(TAG, "onReceive: 手动刷新");
            saveLog(context, "log.log","onReceive: 手动刷新");
            getWeatherData(context, "手动刷新...");
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if(isFirst){ // 创建小部件后首次强制刷新一次，否则凌晨时候创建的小部件不会刷新
            isFirst = false;
            saveLog(context, "log.log","首次刷新...");
            getWeatherData(context, "首次刷新...");
            return;
        }

        long hours = (System.currentTimeMillis() / 3600000 + 8) % 24; // UTC+8
        if (hours >= 6) {
            Log.d(TAG, "onUpdate: 定时刷新");// widget1_info.xml     android:updatePeriodMillis
            getWeatherData(context, "定时刷新...");
        } else { // 凌晨 00:00 - 05:59 不更新天气
            Log.d(TAG, "onUpdate: 现在凌晨" + hours + "时，暂停刷新天气");
        }
    }


    /**
     * 获取天气数据
     * <a href=https://api.caiyunapp.com/v2.6/wh9aWLYieE1akfGi/113.381429,23.039126/weather.json?alert=true>链接</a>
     */
    @SuppressLint("DefaultLocale")
    private void getWeatherData(final Context context, String tips) {

        updateAppWidget(context, UPDATE_ONGOING, tips);

        new Thread(() -> {

            SharedPreferences sf = context.getSharedPreferences("locationInfo", Context.MODE_PRIVATE);
            double longitude = sf.getFloat("longitude", 0);
            double latitude = sf.getFloat("latitude", 90); // 北极

            if (Math.abs(latitude) > 88.0) {  // 靠近北极就是位置异常
                noLocation = true;
                longitude = defLongitude;
                latitude = defLatitude;
            } else {
                noLocation = false;
            }

            String link = String.format("https://api.caiyunapp.com/v2.6/wh9aWLYieE1akfGi/%f,%f/weather.json?alert=true",
                    longitude, latitude);

            final int retryTimes = 3;
            String res = null;
            for (int i = 1; i <= retryTimes; i++) {
                try {
                    res = NetworkUtils.getData(link);
                } catch (Exception e) {
                    String log = String.format("getWeatherData: 第%d次网络异常, 剩余%d次", i, retryTimes - i);
                    Log.e(TAG, log + e);
                    saveLog(context, "log.log", "getWeatherData: " + log);
                }
                if (res != null)
                    break;

                String log = String.format("getWeatherData: 第%d次ResponseNull, 剩余%d次", i, retryTimes - i);
                Log.e(TAG, log);
                saveLog(context, "log.log", "getWeatherData: " + log);
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }
            if (res == null) {
                String errorTips = getFormatDate(System.currentTimeMillis(), DateUtils.HHmm) + "获取天气数据失败";
                Log.e(TAG, "getWeatherData: " + errorTips);
                saveLog(context, "log.log", "getWeatherData: " + errorTips);
                updateAppWidget(context, UPDATE_FAILED, errorTips);
                return;
            }
            updateAppWidget(context, UPDATE_SUCCESS, res);
        }).start();
    }

    /**
     * 创建更新数据 PendingIntent
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    protected PendingIntent createUpdatePendingIntent(Context context) {
        Intent updateIntent = new Intent(ACTION_UPDATE);
        updateIntent.setClass(context, this.getClass());
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            return PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_MUTABLE);
        else
            return PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 根据状态更新天气小部件
     *
     * @param status      更新状态
     * @param context     上下文
     * @param weatherJsonOrTips 天气数据
     */
    @SuppressLint("DefaultLocale")
    private void updateAppWidget(Context context, int status, String weatherJsonOrTips) {
        ComponentName componentName = new ComponentName(context, this.getClass());
        RemoteViews remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.widget1);

        // 点击手动刷新的Intent
        remoteViews.setOnClickPendingIntent(R.id.widget_rl, createUpdatePendingIntent(context));

        if (status != UPDATE_SUCCESS) {
            remoteViews.setTextViewText(R.id.today_other, weatherJsonOrTips);
            AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
            return;
        }

        WeatherBean weatherBean = new Gson().fromJson(weatherJsonOrTips, WeatherBean.class);

        StringBuilder tempIn10hours = new StringBuilder(); // 未来10小时的温度
        List<DoubleValue> tempList = weatherBean.result.hourly.temperature;
        tempIn10hours.append(tempList.get(1).datetime.substring(11, 13)).append("时[");
        double gap = (tempList.get(1).value - tempList.get(0).value); // 误差校准
        for (int i = 1; i <= 10; i++)
            tempIn10hours.append((int) (tempList.get(i).value - gap)).append("° ");
        tempIn10hours.setCharAt(tempIn10hours.length() - 1, ']'); // 把最后的空格换成 ']'
        tempIn10hours.append(tempList.get(10).datetime.substring(11, 13)).append("时");
        remoteViews.setTextViewText(R.id.hour_temp, tempIn10hours.toString());

        Realtime realtime = weatherBean.result.realtime;
        Daily daily = weatherBean.result.daily;
        AirQuality air = realtime.air_quality;

        List<CodeName> adCodes = weatherBean.result.alert.adcodes;
        String district;
        if (adCodes != null && adCodes.size() > 0)
            district = adCodes.get(adCodes.size() - 1).name;
        else
            district = context.getString(R.string.district);
        remoteViews.setTextViewText(R.id.location, district);

        String description = weatherBean.result.minutely.description;
        if (noLocation) {
            remoteViews.setTextViewText(R.id.today_other, "请打开APP更新位置信息");
        } else {
            String forecast = weatherBean.result.forecast_keypoint;
            String otherInfo = String.format("%d%% PM2.5:%.0f PM10:%.0f O₃:%.0f SO₂:%.0f NO₂:%.0f CO:%.1f %s",
                    (int) (realtime.humidity * 100), air.pm25, air.pm10, air.o3, air.so2, air.no2, air.co,
                    air.description.chn);
            remoteViews.setTextViewText(R.id.today_other, forecast.equals(description) ? otherInfo : forecast);
        }
        String updateDate = getFormatDate(System.currentTimeMillis(), DateUtils.HHmm) + context.getString(R.string.widget_update_time);
        remoteViews.setTextViewText(R.id.updateTime, updateDate);
        remoteViews.setTextViewText(R.id.today_tem, (int) realtime.temperature + "°");
        remoteViews.setTextViewText(R.id.description, description);

        // 明天预报
        Skycon skycon = daily.skycon.get(1);
        Temperature temperature1 = daily.temperature.get(1);
        remoteViews.setImageViewResource(R.id.tomorrowImg, ImageUtils.getWeatherIcon(skycon.value));
        remoteViews.setTextViewText(R.id.tomorrow, (int) temperature1.avg + "°");
        remoteViews.setTextViewText(R.id.tomorrowRange, (int) temperature1.min
                + " ~ " + (int) temperature1.max + "°");

        // 后天预报
        skycon = daily.skycon.get(2);
        remoteViews.setImageViewResource(R.id.bigTomorrowImg, ImageUtils.getWeatherIcon(skycon.value));
        Temperature temperature2 = daily.temperature.get(2);
        remoteViews.setTextViewText(R.id.bigTomorrow, (int) temperature2.avg + "°");
        remoteViews.setTextViewText(R.id.bigTomorrowRange, (int) temperature2.min
                + " ~ " + (int) temperature2.max + "°");

        // 天气描述
        String skyconStr = weatherBean.result.realtime.skycon;
        // 降雨（雪）强度
        double intensity = weatherBean.result.realtime.precipitation.local.intensity;

        // 是否白天
        long hours = (System.currentTimeMillis() / 3600000 + 8) % 24;
        boolean isDay = hours > 6 && hours < 18;

        // 设置背景
        remoteViews.setInt(R.id.widget_rl, "setBackgroundResource", ImageUtils.getBgResourceId(skyconStr, intensity, isDay));

        // 刷新小部件UI
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);


        //预警信息通知
        List<WarnInfo> warnInfo = weatherBean.result.alert.content;
        for (WarnInfo info : warnInfo) {
            if (hasNotify.contains(info.alertId))
                continue;

            hasNotify.add(info.alertId);

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

    public void saveLog(Context context, String path, String text) {
        try {
            String str = "["+DateUtils.getLogTime() + "] " + text + "\n";
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