package io.github.jark006.weather;

import static io.github.jark006.weather.utils.DateUtils.getFormatDate;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import io.github.jark006.weather.caiyun.Caiyun;
import io.github.jark006.weather.utils.DateUtils;
import io.github.jark006.weather.utils.ImageUtils;

public class WidgetCaiyun2 extends WidgetCaiyunBase {

    @Override
    public void showTips(Context context, String tips) {
        ComponentName componentName = new ComponentName(context, this.getClass());
        RemoteViews remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.widget_caiyun2);
        remoteViews.setTextViewText(R.id.description, tips);
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void updateAppWidget(Context context, Caiyun caiyun, String districtName) {
        ComponentName componentName = new ComponentName(context, this.getClass());
        RemoteViews remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.widget_caiyun2);

        // 点击手动刷新的Intent
        remoteViews.setOnClickPendingIntent(R.id.widget_rl, createUpdatePendingIntent(context));

        String updateTime = getFormatDate(System.currentTimeMillis(), DateUtils.HHmm);
        var tips = new StringBuilder();
        if (caiyun == null) {
            tips.append("caiyun == null ");
        } else if (caiyun.result == null) {
            tips.append("caiyun.result == null ");
        } else {
            var realtime = caiyun.result.realtime;
            if (realtime != null) {
                remoteViews.setTextViewText(R.id.updateTime, updateTime +
                        context.getString(R.string.widget_update_time) + "\n" + districtName);
                remoteViews.setTextViewText(R.id.today_tem, (int) Math.ceil(realtime.temperature) + "°");

                // 是否白天
                long hours = (System.currentTimeMillis() / 3600000 + 8) % 24;
                boolean isDay = hours >= 6 && hours < 18;
                remoteViews.setInt(R.id.widget_rl, "setBackgroundResource",
                        ImageUtils.getBgResourceIdCaiyun(realtime.skycon, isDay));
            }
            var minutely = caiyun.result.minutely;
            if (minutely != null) {
                remoteViews.setTextViewText(R.id.description, minutely.description);
            }

            var daily = caiyun.result.daily;
            if (daily != null && daily.temperature != null && daily.temperature.size() > 2) {
                var tomorrow = daily.temperature.get(1);
                remoteViews.setTextViewText(R.id.tomorrowRange,
                        (int) Math.ceil(tomorrow.min) + "~" + (int) Math.ceil(tomorrow.max) + "°");
            }
            if (daily != null && daily.skycon != null && daily.skycon.size() > 2) {
                var tomorrow = daily.skycon.get(1);
                remoteViews.setInt(R.id.tomorrowImg, "setBackgroundResource",
                        ImageUtils.getSkyconIconCaiyun(tomorrow.value));
            }
        }

        if (tips.length() > 0)
            remoteViews.setTextViewText(R.id.description, updateTime + "更新失败：" + tips);

        // 刷新小部件UI
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    }
}
