package io.github.jark006.weather;

import static io.github.jark006.weather.utils.DateUtils.getFormatDate;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import io.github.jark006.weather.qweather.rain.Rain;
import io.github.jark006.weather.qweather.realtime.RealTime;
import io.github.jark006.weather.qweather.threeDay.Daily;
import io.github.jark006.weather.qweather.threeDay.ThreeDay;
import io.github.jark006.weather.utils.DateUtils;
import io.github.jark006.weather.utils.ImageUtils;

/**
 * 天气小部件
 */
public class Widget2 extends WidgetBase {

    @Override
    public void showTips(Context context, String tips) {
        ComponentName componentName = new ComponentName(context, this.getClass());
        RemoteViews remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.widget2);
        remoteViews.setTextViewText(R.id.description, tips);
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void updateAppWidget(Context context,
                                RealTime realTime,
                                ThreeDay threeDay,
                                Rain rain,
                                String districtName) {

        ComponentName componentName = new ComponentName(context, this.getClass());
        RemoteViews remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.widget2);

        // 点击手动刷新的Intent
        remoteViews.setOnClickPendingIntent(R.id.widget_rl, createUpdatePendingIntent(context));

        var tips = new StringBuilder();

        if (realTime == null) {
            tips.append("realTime == null ");
        } else if (realTime.now == null) {
            tips.append("realTime.now == null ");
        } else {
            String updateDate = getFormatDate(System.currentTimeMillis(), DateUtils.HHmm) + context.getString(R.string.widget_update_time);
            remoteViews.setTextViewText(R.id.updateTime, updateDate+"\n"+districtName);
            remoteViews.setTextViewText(R.id.today_tem, realTime.now.temp + "°");

            // 是否白天
            long hours = (System.currentTimeMillis() / 3600000 + 8) % 24;
            boolean isDay = hours >= 6 && hours < 18;
            // 设置背景
            remoteViews.setInt(R.id.widget_rl, "setBackgroundResource",
                    ImageUtils.getBgResourceId(Integer.parseInt(realTime.now.icon), isDay));
        }

        if (rain == null) {
            tips.append("rain == null ");
        } else if (rain.summary == null) {
            tips.append("rain.summary == null ");
        } else {
            remoteViews.setTextViewText(R.id.description, rain.summary);
        }

        if (threeDay == null) {
            tips.append("threeDay == null ");
        } else if (threeDay.code != null && !threeDay.code.equals("200")) {
            tips.append("threeDay.code ").append(threeDay.code);
        } else if (threeDay.daily == null) {
            tips.append("threeDay.daily == null ");
        } else if (threeDay.daily.size() < 3) {
            tips.append("threeDay.daily.size() ").append(threeDay.daily.size());
        } else {
            // 明天预报
            Daily tomorrow = threeDay.daily.get(1);
            remoteViews.setImageViewResource(R.id.tomorrowImg, ImageUtils.getWeatherIcon(
                    Integer.parseInt(tomorrow.iconDay)));
            remoteViews.setTextViewText(R.id.tomorrow,
                    (Integer.parseInt(tomorrow.tempMin) + Integer.parseInt(tomorrow.tempMax)) / 2
                            + "° " + tomorrow.textDay);
        }
        if (tips.length() > 0)
            remoteViews.setTextViewText(R.id.description, tips);

        // 刷新小部件UI
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    }

}