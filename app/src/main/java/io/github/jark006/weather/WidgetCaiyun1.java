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

public class WidgetCaiyun1 extends WidgetCaiyunBase {

    @Override
    public void showTips(Context context, String tips) {
        ComponentName componentName = new ComponentName(context, this.getClass());
        RemoteViews remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.widget_caiyun1);
        remoteViews.setTextViewText(R.id.today_other, tips);
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void updateAppWidget(Context context, Caiyun caiyun, String districtName) {

        ComponentName componentName = new ComponentName(context, this.getClass());
        RemoteViews remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.widget_caiyun1);

        // 点击手动刷新的Intent
        remoteViews.setOnClickPendingIntent(R.id.widget_rl, createUpdatePendingIntent(context));

        if (!districtName.isEmpty())
            remoteViews.setTextViewText(R.id.districtName, districtName);

        String updateTime = getFormatDate(System.currentTimeMillis(), DateUtils.HHmm);
        var tips = new StringBuilder();
        if (caiyun == null) {
            tips.append("caiyun == null ");
        } else if (caiyun.result == null) {
            tips.append("caiyun.result == null ");
        } else {
            var realtime = caiyun.result.realtime;
            if (realtime != null) {
                remoteViews.setTextViewText(R.id.updateTime, updateTime + context.getString(R.string.widget_update_time));
                remoteViews.setTextViewText(R.id.today_tem, (int) Math.ceil(realtime.temperature) + "°");

                String otherInfo = String.format("体感: %d℃  湿度: %d%%  空气质量: %s",
                        Math.round(realtime.apparent_temperature), (int) (realtime.humidity * 100), realtime.air_quality.description.chn);
                remoteViews.setTextViewText(R.id.today_other, otherInfo);

                // 是否白天
                long hours = (System.currentTimeMillis() / 3600000 + 8) % 24;
                boolean isDay = hours >= 6 && hours < 18;
                remoteViews.setInt(R.id.widget_rl, "setBackgroundResource",
                        ImageUtils.getBgResourceIdCaiyun(realtime.skycon, isDay));
            }

            var minutely = caiyun.result.minutely;
            if (minutely != null) {
                var description = minutely.description;
                var hourly = caiyun.result.hourly;
                if (hourly != null) {
                    description += "。 " + hourly.description;
                    var tempList = hourly.temperature;
                    if (tempList != null && tempList.size() > 10) {
                        var tempIn10hours = new StringBuilder(); // 未来10小时的温度
                        tempIn10hours.append(tempList.get(1).datetime.substring(11, 13)).append("时[");
                        double gap = (tempList.get(1).value - tempList.get(0).value); // 误差校准
                        for (int i = 1; i <= 10; i++)
                            tempIn10hours.append((int) Math.ceil(tempList.get(i).value - gap)).append("° ");
                        tempIn10hours.setCharAt(tempIn10hours.length() - 1, ']'); // 把最后的空格换成 ']'
                        tempIn10hours.append(tempList.get(10).datetime.substring(11, 13)).append("时");
                        remoteViews.setTextViewText(R.id.hour_temp, tempIn10hours.toString());
                    }
                }
                remoteViews.setTextViewText(R.id.description, description);
            }

            var daily = caiyun.result.daily;
            if (daily != null && daily.temperature != null && daily.temperature.size() > 2) {
                var tomorrow = daily.temperature.get(1);
                remoteViews.setTextViewText(R.id.tomorrow, (int) Math.ceil(tomorrow.avg) + "°");
                remoteViews.setTextViewText(R.id.tomorrowRange,
                        (int) Math.ceil(tomorrow.min) + " ~ " + (int) Math.ceil(tomorrow.max) + "°");

                var overmorrow = daily.temperature.get(2);
                remoteViews.setTextViewText(R.id.overmorrow, (int) Math.ceil(overmorrow.avg) + "°");
                remoteViews.setTextViewText(R.id.overmorrowRange,
                        (int) Math.ceil(overmorrow.min) + " ~ " + (int) Math.ceil(overmorrow.max) + "°");
            }
            if (daily != null && daily.skycon != null && daily.skycon.size() > 2) {
                var tomorrow = daily.skycon.get(1);
                remoteViews.setInt(R.id.tomorrowImg, "setBackgroundResource",
                        ImageUtils.getSkyconIconCaiyun(tomorrow.value));
                var overmorrow = daily.skycon.get(2);
                remoteViews.setInt(R.id.overmorrowImg, "setBackgroundResource",
                        ImageUtils.getSkyconIconCaiyun(overmorrow.value));
            }
        }

        if (tips.length() > 0)
            remoteViews.setTextViewText(R.id.today_other, updateTime + "更新失败：" + tips);

        // 刷新小部件UI
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    }
}
