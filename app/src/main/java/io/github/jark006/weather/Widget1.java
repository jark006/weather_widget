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
public class Widget1 extends WidgetBase {

    @Override
    public void showTips(Context context, String tips) {
        ComponentName componentName = new ComponentName(context, this.getClass());
        RemoteViews remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.widget1);
        remoteViews.setTextViewText(R.id.today_other, tips);
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
        RemoteViews remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.widget1);

        // 点击手动刷新的Intent
        remoteViews.setOnClickPendingIntent(R.id.widget_rl, createUpdatePendingIntent(context));

        if (!districtName.isEmpty())
            remoteViews.setTextViewText(R.id.districtName, districtName);

        var tips = new StringBuilder();

        if (realTime == null) {
            tips.append("realTime == null ");
        } else if (realTime.now == null) {
            tips.append("realTime.now == null ");
        } else {
            String updateDate = getFormatDate(System.currentTimeMillis(), DateUtils.HHmm) + context.getString(R.string.widget_update_time);
            remoteViews.setTextViewText(R.id.updateTime, updateDate);
            remoteViews.setTextViewText(R.id.today_tem, realTime.now.temp + "°");

            String otherInfo = String.format("%s %s 湿度 %s%% 气压 %shPa 云量 %s%%",
                    realTime.now.text, realTime.now.windDir, realTime.now.humidity,
                    realTime.now.pressure, realTime.now.cloud);
            remoteViews.setTextViewText(R.id.today_other, otherInfo);

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
                            + "°");
            if (tomorrow.textDay.equals(tomorrow.textNight))
                remoteViews.setTextViewText(R.id.tomorrowDescription, tomorrow.textDay);
            else
                remoteViews.setTextViewText(R.id.tomorrowDescription, tomorrow.textDay
                        + " 转 " + tomorrow.textNight);

            // 后天预报
            Daily overmorrow = threeDay.daily.get(2);
            remoteViews.setImageViewResource(R.id.overmorrowImg, ImageUtils.getWeatherIcon(
                    Integer.parseInt(overmorrow.iconDay)));
            remoteViews.setTextViewText(R.id.overmorrow,
                    (Integer.parseInt(overmorrow.tempMin) + Integer.parseInt(overmorrow.tempMax)) / 2
                            + "°");
            if (overmorrow.textDay.equals(overmorrow.textNight))
                remoteViews.setTextViewText(R.id.overmorrowDescription, overmorrow.textDay);
            else
                remoteViews.setTextViewText(R.id.overmorrowDescription, overmorrow.textDay
                        + " 转 " + overmorrow.textNight);
        }
        if (tips.length() > 0)
            remoteViews.setTextViewText(R.id.today_other, tips);

        // 刷新小部件UI
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    }
}