package io.github.jark006.weather;

import static io.github.jark006.weather.utils.DateUtils.getFormatDate;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import io.github.jark006.weather.bean.AirQuality;
import io.github.jark006.weather.bean.CodeName;
import io.github.jark006.weather.bean.Daily;
import io.github.jark006.weather.bean.DoubleValue;
import io.github.jark006.weather.bean.Realtime;
import io.github.jark006.weather.bean.Skycon;
import io.github.jark006.weather.bean.Temperature;
import io.github.jark006.weather.bean.WeatherBean;
import io.github.jark006.weather.utils.DateUtils;
import io.github.jark006.weather.utils.ImageUtils;

import java.util.List;

/**
 * 天气小部件
 */
public class Widget1 extends WidgetBase {

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
        RemoteViews remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.widget1);

        // 点击手动刷新的Intent
        remoteViews.setOnClickPendingIntent(R.id.widget_rl, createUpdatePendingIntent(context));

        if (status != UPDATE_SUCCESS) {
            remoteViews.setTextViewText(R.id.today_other, weatherJsonOrTips);
            AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
            return;
        }

        // UPDATE_SUCCESS
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
            remoteViews.setTextViewText(R.id.today_other, "请打开APP更新你的位置信息");
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