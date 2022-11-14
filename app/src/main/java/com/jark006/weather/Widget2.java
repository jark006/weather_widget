package com.jark006.weather;

import static com.jark006.weather.utils.DateUtils.getFormatDate;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.jark006.weather.bean.CodeName;
import com.jark006.weather.bean.Daily;
import com.jark006.weather.bean.Realtime;
import com.jark006.weather.bean.Skycon;
import com.jark006.weather.bean.Temperature;
import com.jark006.weather.bean.WeatherBean;
import com.jark006.weather.utils.DateUtils;
import com.jark006.weather.utils.ImageUtils;

import java.util.List;

/**
 * 天气小部件
 */
public class Widget2 extends BaseWidget {

    /**
     * 根据状态更新天气小部件
     *
     * @param context           上下文
     * @param status            更新状态
     * @param weatherJsonOrTips 天气数据
     */
    @Override
    @SuppressLint("DefaultLocale")
    public void updateAppWidget(Context context, int status, String weatherJsonOrTips, boolean noLocation) {
        ComponentName componentName = new ComponentName(context, this.getClass());
        RemoteViews remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.widget2);

        // 点击手动刷新的Intent
        remoteViews.setOnClickPendingIntent(R.id.widget_rl, createUpdatePendingIntent(context));

        if (status != UPDATE_SUCCESS) {
            remoteViews.setTextViewText(R.id.description, weatherJsonOrTips);
            AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
            return;
        }

        // UPDATE_SUCCESS
        WeatherBean weatherBean = new Gson().fromJson(weatherJsonOrTips, WeatherBean.class);


        Realtime realtime = weatherBean.result.realtime;
        Daily daily = weatherBean.result.daily;

        List<CodeName> adCodes = weatherBean.result.alert.adcodes;
        String district;
        if (adCodes != null && adCodes.size() > 0)
            district = adCodes.get(adCodes.size() - 1).name;
        else
            district = context.getString(R.string.district);
        remoteViews.setTextViewText(R.id.location, district);

        String updateTime = getFormatDate(System.currentTimeMillis(), DateUtils.HHmm);
        remoteViews.setTextViewText(R.id.updateTime, updateTime);
        remoteViews.setTextViewText(R.id.today_tem, (int) realtime.temperature + "°");

        if (noLocation) {
            remoteViews.setTextViewText(R.id.description, "请打开APP更新你的位置信息");
        } else {
            String description = weatherBean.result.minutely.description;
            remoteViews.setTextViewText(R.id.description, description);
        }

        // 明天预报
        Skycon skycon = daily.skycon.get(1);
        Temperature temperature1 = daily.temperature.get(1);
        remoteViews.setImageViewResource(R.id.tomorrowImg, ImageUtils.getWeatherIcon(skycon.value));
        remoteViews.setTextViewText(R.id.tomorrow, "明天 "+(int) temperature1.avg + "°");
        remoteViews.setTextViewText(R.id.tomorrowRange, (int) temperature1.min
                + " ~ " + (int) temperature1.max + "°");


        // 天气描述
        String skyCon = weatherBean.result.realtime.skycon;
        // 降雨（雪）强度
        double intensity = weatherBean.result.realtime.precipitation.local.intensity;

        // 是否白天
        long hours = (System.currentTimeMillis() / 3600000 + 8) % 24;
        boolean isDay = hours > 6 && hours < 18;

        // 设置背景
        remoteViews.setInt(R.id.widget_rl, "setBackgroundResource", ImageUtils.getBgResourceId(skyCon, intensity, isDay));

        // 刷新小部件UI
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);

        // 预警信息通知
        notify(context, weatherBean.result.alert.content);
    }

}