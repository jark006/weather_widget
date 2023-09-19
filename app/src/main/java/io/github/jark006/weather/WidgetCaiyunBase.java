package io.github.jark006.weather;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import io.github.jark006.weather.caiyun.Caiyun;
import io.github.jark006.weather.utils.NetworkUtils;
import io.github.jark006.weather.utils.Utils;

public abstract class WidgetCaiyunBase extends AppWidgetProvider {

    final static String TAG = "JARK_WidgetCaiyun";
    final static String REQUEST_MANUAL = "jark_weather_REQUEST_MANUAL_CAIYUN";
    final static String APIKEY = "XXX"; // 彩云 APIKEY

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(TAG, "onEnabled: 创建彩云小部件");
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
            SharedPreferences sf = context.getSharedPreferences("locationInfo", Context.MODE_PRIVATE);
            double longitude = sf.getFloat("longitude", 0);
            double latitude = sf.getFloat("latitude", 90); // 北极

            String districtName = sf.getString("districtName", "");
            if (districtName.length() == 0)
                districtName = sf.getString("cityName", "");

            if (Math.abs(latitude) > 88.0) {  // 靠近南北极就是位置异常
                longitude = Utils.defLongitude;
                latitude = Utils.defLatitude;
            }

            try {
                String link = String.format("https://api.caiyunapp.com/v2.5/%s/%f,%f/weather.json?alert=true&hourlysteps=12&dailysteps=3",
                        APIKEY, longitude, latitude);
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
            } catch (Exception e) {
                showTips(context, "发生异常 " + e);
                e.printStackTrace();
            }
        }).start();
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
