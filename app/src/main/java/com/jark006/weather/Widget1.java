package com.jark006.weather;

import static android.content.ContentValues.TAG;
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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.zip.CRC32;

/**
 * 天气小部件
 */
public class Widget1 extends AppWidgetProvider {
    final double finalLongitude = 113.381917;//默认在广州大学城
    final double finalLatitude = 23.039316;

    double curLongitude = finalLongitude;
    double curLatitude = finalLatitude;

    String locationTime = "00-00 00:00";
    public static final String ACTION_UPDATE = "action_update";

    public final int UPDATE_SUCCESS = 0x03;
    public final int UPDATE_FAILED = 0x04;
    public final int UPDATE_ONGOING = 0x05;
    static HashSet<String> hasNotify = new HashSet<>();
    AMapLocationClient mLocationClient;

    // 01台风 02暴雨 ... 18沙尘
    final String[] warnTypeStr = {
            "其他", "台风", "暴雨", "暴雪", "寒潮", "大风",
            "沙尘暴", "高温", "干旱", "雷电", "冰雹", "霜冻",
            "大雾", "霾", "道路结冰", "森林火灾", "雷雨大风",
            "春季沙尘天气趋势预警", "沙尘"
    };
    //00白色 ... 04红色
    final String[] warnLevelStr = {"白色预警", "蓝色预警", "黄色预警", "橙色预警", "红色预警"};

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

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);


        getLocationAmap(context);
    }


    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        // 手动刷新
        if (ACTION_UPDATE.equals(action)) {
            Log.d(TAG, "onReceive: 手动刷新");
            updateAppWidget(context, UPDATE_ONGOING, "更新中...");
            getLocationAmap(context);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate: 周期刷新");
        getLocationAmap(context);
    }

    void getLocationAmap(Context context) {
//        //context.getPackageName(),
        //PackageManager.GET_META_DATA);

        AMapLocationClient.updatePrivacyShow(context, true, true);
        AMapLocationClient.updatePrivacyAgree(context, true);
//        AMapLocationClient.setApiKey("50407ea2ca3f8f53e63b5524f791f0fe");

        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            AMapLocationClient.setApiKey(appInfo.metaData.getString("com.amap.api.v2.apikey"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //声明AMapLocationClient类对象
        //声明定位回调监听器
        @SuppressLint("DefaultLocale")
        AMapLocationListener mLocationListener = aMapLocation -> {
            if (aMapLocation.getErrorCode() == 0) {
                curLongitude = aMapLocation.getLongitude();
                curLatitude = aMapLocation.getLatitude();
                locationTime = getFormatDate(new Date(aMapLocation.getTime()), "MM-dd HH:mm");
                Log.i(TAG, "Location:" + curLongitude + "," + curLatitude + "," + aMapLocation.getAddress());
            } else {
                curLongitude = finalLongitude;
                curLatitude = finalLatitude;

                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                String errorTips = getFormatDate(System.currentTimeMillis(), DateUtils.HHmm)
                        + ", location Error, ErrCode:" + aMapLocation.getErrorCode()
                        + ", errInfo:" + aMapLocation.getErrorInfo();
                Log.e(TAG, "getLocationAmap: " + errorTips);
                saveLog(context, "log.log", errorTips);
                Log.i(TAG, "LocationFail,Using default:" + curLongitude + "," + curLatitude);
            }
            getWeatherData(context);
        };

        //初始化定位
        try {
            mLocationClient = new AMapLocationClient(context);
        } catch (Exception e) {
            String errorTips = getFormatDate(System.currentTimeMillis(), DateUtils.HHmm)
                    + "mLocationClient = new AMapLocationClient(context) Fail.";
            Log.e(TAG, "getLocationAmap: " + errorTips);
            updateAppWidget(context, UPDATE_FAILED, errorTips);
            e.printStackTrace();
            return;
        }
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        //声明AMapLocationClientOption对象
        AMapLocationClientOption mLocationOption;
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();

        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        mLocationClient.setLocationOption(option);
        //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
        mLocationClient.stopLocation();

        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);

        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(true);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);


        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

    }

    /**
     * 获取天气数据
     * <a href=https://api.caiyunapp.com/v2.6/wh9aWLYieE1akfGi/113.381429,23.039126/weather.json?alert=true>链接</a>
     */
    private void getWeatherData(final Context context) {

        new Thread(() -> {
            @SuppressLint("DefaultLocale")
            String link = String.format("https://api.caiyunapp.com/v2.6/wh9aWLYieE1akfGi/%f,%f/weather.json?alert=true",
                    curLongitude, curLatitude);

            int retryTimes = 3;
            String res = null;
            for (int i = 1; i <= retryTimes; i++) {
                try {
                    res = NetworkUtils.getData(link);
                } catch (Exception e) {
                    e.printStackTrace();
                    @SuppressLint("DefaultLocale")
                    String log = String.format("getWeatherData: 第%d次网络异常, 剩余%d次", i, retryTimes - i);
                    Log.e(TAG, log + e);
                    saveLog(context, "log.log", "getWeatherData: " + log);
                }
                if (res != null)
                    break;

                @SuppressLint("DefaultLocale")
                String log = String.format("getWeatherData: 第%d次ResponseNull, 剩余%d次", i, retryTimes - i);
                Log.e(TAG, log);
                saveLog(context, "log.log", "getWeatherData: " + log);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (res == null) {

                String errorTips = getFormatDate(System.currentTimeMillis(), DateUtils.HHmm) + "获取天气数据失败";
                Log.e(TAG, "getWeatherData: " + errorTips);
                saveLog(context, "log.log", "getWeatherData: " + errorTips);
                updateAppWidget(context, UPDATE_FAILED, errorTips);
                return;
            }

            try {
                updateAppWidget(context, UPDATE_SUCCESS, res);
            } catch (Exception e) {
                String errorTips = getFormatDate(System.currentTimeMillis(), DateUtils.HHmm) + "解析数据失败";
                Log.e(TAG, "getWeatherData: " + errorTips + e);
                saveLog(context, "log.log", "getWeatherData: " + errorTips + e);
                updateAppWidget(context, UPDATE_FAILED, errorTips);
            }
        }).start();
    }

//    /**
//     * 创建跳转首界面 PendingIntent
//     */
//    protected PendingIntent createLaunchPendingIntent(Context context) {
//        Intent launchIntent = new Intent(context, MainActivity.class);
//        return PendingIntent.getActivity(context, 0, launchIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//    }

    /**
     * 创建更新数据 PendingIntent
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    protected PendingIntent createUpdatePendingIntent(Context context) {
        Intent updateIntent = new Intent(ACTION_UPDATE);
        updateIntent.setClass(context, this.getClass());
        Log.d(TAG, "createUpdatePendingIntent: ");
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
     * @param weatherJson 天气数据
     */
    private void updateAppWidget(Context context, int status, String weatherJson) {
        ComponentName componentName = new ComponentName(context, this.getClass());
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget1);

        // 打开APP首页的Intent
//        PendingIntent launchPendingIntent = createLaunchPendingIntent(context);
//        remoteViews.setOnClickPendingIntent(R.id.location, launchPendingIntent);
        // 刷新的Intent
        PendingIntent updatePendingIntent = createUpdatePendingIntent(context);
        remoteViews.setOnClickPendingIntent(R.id.widget_rl, updatePendingIntent);

        if (status == UPDATE_SUCCESS) {
            showAppWidgetData(context, remoteViews, weatherJson);
        } else {
            remoteViews.setTextViewText(R.id.today_other, weatherJson);
        }
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    }

    @SuppressLint("DefaultLocale")
    private void showAppWidgetData(Context context, RemoteViews remoteViews, String weatherJson) {
        WeatherBean weatherBean = new Gson().fromJson(weatherJson, WeatherBean.class);
        StringBuilder hourTemp = new StringBuilder();
        List<DoubleValue> list = weatherBean.result.hourly.temperature;
        double gap = (list.get(1).value - list.get(0).value);
        hourTemp.append(list.get(1).datetime.substring(11, 13)).append("时[");
        for (int i = 1; i <= 10; i++) {
            hourTemp.append(String.format("%2d° ", (int) (list.get(i).value - gap)));
        }
        hourTemp.setCharAt(hourTemp.length() - 1, ']');
        hourTemp.append(list.get(10).datetime.substring(11, 13)).append("时");
        remoteViews.setTextViewText(R.id.hour_temp, hourTemp.toString().trim());

        Realtime realtime = weatherBean.result.realtime;
        Daily daily = weatherBean.result.daily;
        AirQuality air = realtime.air_quality;

        List<CodeName> adcodes = weatherBean.result.alert.adcodes;
        String district = "本地";
        if (adcodes.size() > 0)
            district = adcodes.get(adcodes.size() - 1).name;

        String forecast = weatherBean.result.forecast_keypoint;
        String description = weatherBean.result.minutely.description;
        String otherInfo = String.format("%d%% PM2.5:%.0f PM10:%.0f O₃:%.0f SO₂:%.0f NO₂:%.0f CO:%.1f %s",
                (int) (realtime.humidity * 100), air.pm25, air.pm10, air.o3, air.so2, air.no2, air.co, air.description.chn);
        remoteViews.setTextViewText(R.id.location, district);
        remoteViews.setTextViewText(R.id.today_other, forecast.equals(description) ? otherInfo : forecast);
        String updateDate = getFormatDate(System.currentTimeMillis(), DateUtils.HHmm);
        remoteViews.setTextViewText(R.id.updateTime, context.getString(R.string.widget_update_time, updateDate) +
                "\n" + locationTime);
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
        String weather = weatherBean.result.realtime.skycon;
        // 降雨（雪）强度
        double intensity = weatherBean.result.realtime.precipitation.local.intensity;

        // 是否是白天
        long hours = System.currentTimeMillis() / 3600000 + 8;
        hours %= 24;
        boolean isDay = hours > 6 && hours < 18;

        // 设置背景
        remoteViews.setInt(R.id.widget_rl, "setBackgroundResource", ImageUtils.getBgResourceId(weather, intensity, isDay));

        //预警信息通知
        List<WarnInfo> warnInfo = weatherBean.result.alert.content;
        for (WarnInfo info : warnInfo) {
            if (hasNotify.contains(info.alertId))
                continue;

            hasNotify.add(info.alertId);

            Log.i(TAG, "showAppWidgetData: " + info.description);

            int warnLevel = 0;
            String title = info.location + " " + info.status;
            int importantLevel = NotificationManager.IMPORTANCE_DEFAULT;

            try {
                int warnType = Integer.parseInt(info.code); // 2位预警类型编码 ＋ 2位预警级别编码
                warnLevel = warnType % 100; // 预警级别 00 ~ 04

                if (warnLevel > 4)
                    warnLevel = 4;
                else if (warnLevel < 0)
                    warnLevel = 0;

                warnType /= 100; // 预警类型 01 ~ 18， 00未知
                if (warnType < 1 || warnType > 18)
                    warnType = 0;
                title = info.location + " " + warnTypeStr[warnType] + " " + info.status;
                importantLevel = IMPORTANT_INT[warnLevel + 1];
            } catch (Exception e) {
                e.printStackTrace();
            }

            String channelId = info.alertId;

            RemoteViews cusRemoveExpandView = new RemoteViews(context.getPackageName(), R.layout.layout_notify_large);
            cusRemoveExpandView.setTextViewText(R.id.title, info.title);
            cusRemoveExpandView.setTextViewText(R.id.content, info.description);
            cusRemoveExpandView.setImageViewResource(R.id.icon, warnIconIndex[warnLevel]);

            Notification notification = new Notification.Builder(context, channelId)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_sunny)
//                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), warnIcoIndex[warnLevel]))
                    .setContentTitle(title)
                    .setContentText(info.title)
                    .setStyle(new Notification.DecoratedCustomViewStyle())
                    .setCustomBigContentView(cusRemoveExpandView)
                    .build();
            // 2. 获取系统的通知管理器
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            // 3. 创建NotificationChannel(这里传入的channelId要和创建的通知channelId一致，才能为指定通知建立通知渠道)
            NotificationChannel channel = new NotificationChannel(channelId, warnLevelStr[warnLevel], importantLevel);
            notificationManager.createNotificationChannel(channel);
            // 4. 发送通知
            CRC32 crc32 = new CRC32();
            crc32.update(info.alertId.getBytes());
            notificationManager.notify((int) crc32.getValue(), notification);
        }
    }

    public void saveLog(Context context, String path, String text) {
        try {
            //默认保存到data/data/包名/files/目录下

            int logFileMode = Context.MODE_APPEND;  //默认模式 追加日志
            File logFile = context.getFileStreamPath(path);

            if (logFile.length() > 100 * 1024) {  //超过100kb就清空覆盖
                logFileMode = Context.MODE_PRIVATE;
            }

            FileOutputStream fileOut = context.openFileOutput(path, logFileMode);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOut));

            bufferedWriter.write(DateUtils.getLogTime() + ": " + text + "\n");
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}