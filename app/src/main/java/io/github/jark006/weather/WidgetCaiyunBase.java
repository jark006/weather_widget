package io.github.jark006.weather;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.HashMap;

import io.github.jark006.weather.caiyun.Caiyun;
import io.github.jark006.weather.caiyun.Caiyun.Result.Alert;
import io.github.jark006.weather.utils.LocationStruct;
import io.github.jark006.weather.utils.NetworkUtils;
import io.github.jark006.weather.utils.Utils;

public abstract class WidgetCaiyunBase extends AppWidgetProvider {

    final static String REQUEST_MANUAL = "jark_weather_REQUEST_MANUAL_CAIYUN";
    final static String APIKEY = "96Ly7wgKGq6FhllM"; // 彩云 APIKEY

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // widget1_info.xml     android:updatePeriodMillis 定时1小时
        getWeatherData(context, "定时刷新彩云...");
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        if (REQUEST_MANUAL.equals(intent.getAction())) {
            getWeatherData(context, "手动刷新彩云...");
        }
    }


    /**
     * 获取天气数据
     */
    @SuppressLint("DefaultLocale")
    private void getWeatherData(Context context, @NonNull String tips) {

        showTips(context, tips);

        new Thread(() -> {
            LocationStruct locationStruct = (LocationStruct) Utils.readObj(context, "locationStruct");
            if (locationStruct == null) {
                locationStruct = new LocationStruct();
            }

            String districtName = locationStruct.districtName;
            if (districtName.isEmpty())
                districtName = locationStruct.cityName;

            if (Math.abs(locationStruct.latitude) > 88.0) {  // 靠近南北极就是位置异常
                locationStruct.longitude = Utils.defLongitude;
                locationStruct.latitude = Utils.defLatitude;
            }

            try {
                String link = String.format("https://api.caiyunapp.com/v2.5/%s/%f,%f/weather.json?alert=true&hourlysteps=12&dailysteps=3",
                        APIKEY, locationStruct.longitude, locationStruct.latitude);
                String caiyunData = NetworkUtils.getData(link);
                if (caiyunData == null) {
                    Thread.sleep(2000);
                    caiyunData = NetworkUtils.getData(link); // 重试
                }
                Caiyun caiyun = new Gson().fromJson(caiyunData, Caiyun.class);
                if (caiyun != null && caiyun.status != null && !caiyun.status.equals("ok")) {
                    throw new Exception("status: " + caiyun.status);
                }
                updateAppWidget(context, caiyun, districtName);

                if (caiyun != null && caiyun.result != null && caiyun.result.alert != null)
                    notify(context, caiyun.result.alert);

            } catch (Exception e) {
                showTips(context, "发生异常 " + e);
            }
        }).start();
    }

    @SuppressLint("DefaultLocale")
    String timestamp2HHMM(long ts) {
        ts = (ts + 8 * 3600) % 86400;
        return String.format("%02d:%02d", (ts / 3600), (ts % 3600) / 60);
    }

    @SuppressWarnings("unchecked")
    public void notify(Context context, @NonNull Alert alert) {
        if (!alert.status.equals("ok") || alert.content == null || alert.content.isEmpty())
            return;

        var alertMap = (HashMap<String, Alert.Content>) Utils.readObj(context, "alertMap");
        if (alertMap == null)
            alertMap = new HashMap<>();

        boolean isNewAlertCome = false;
        for (Alert.Content info : alert.content) {
            if (alertMap.containsKey(info.alertId))
                continue;

            alertMap.put(info.alertId, info);
            isNewAlertCome = true;

            int warnLevel = Integer.parseInt(info.code) % 100; // 0(白色预警) ~ 4(红色预警)
            String channelId = Utils.warnLevelStr[warnLevel];

            RemoteViews cusRemoveExpandView = new RemoteViews(context.getPackageName(), R.layout.layout_notify_large);
            cusRemoveExpandView.setTextViewText(R.id.title, info.title + " 发布于" + timestamp2HHMM(info.pubtimestamp));
            cusRemoveExpandView.setTextViewText(R.id.content, info.description);
            cusRemoveExpandView.setImageViewResource(R.id.icon, Utils.warnIconIndex[warnLevel]);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var notification = new Notification.Builder(context, channelId)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(Utils.warnIconIndex[warnLevel])
                        .setContentTitle(info.location + info.status)
                        .setContentText(info.title)
                        .setStyle(new Notification.DecoratedCustomViewStyle())
                        .setCustomBigContentView(cusRemoveExpandView)
                        .build();

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(info.alertId.hashCode(), notification);
            }
        }
        if (isNewAlertCome) {
            while (alertMap.size() > 50) {
                removeMinPub(alertMap);
            }
            Utils.saveObj(context, "alertMap", alertMap);
        }
    }

    private static void removeMinPub(HashMap<String, Alert.Content> map) {
        String minKey = null;
        long minP = Long.MAX_VALUE;

        for (var entry : map.entrySet()) {
            if (entry.getValue().pubtimestamp < minP) {
                minP = entry.getValue().pubtimestamp;
                minKey = entry.getKey();
            }
        }

        if (minKey != null) {
            map.remove(minKey);
        }
    }

    /**
     * 创建更新数据 PendingIntent
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    protected PendingIntent createUpdatePendingIntent(Context context) {
        Intent updateIntent = new Intent(REQUEST_MANUAL);
        updateIntent.setClass(context, this.getClass());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            return PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_MUTABLE);
        else
            return PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 根据状态更新天气小部件
     *
     * @param context      上下文
     * @param caiyun       彩云实时天气
     * @param districtName 县、乡、区 名称
     */
    @SuppressLint("DefaultLocale")
    abstract public void updateAppWidget(Context context, Caiyun caiyun, String districtName);

    abstract public void showTips(Context context, String tips);
}
