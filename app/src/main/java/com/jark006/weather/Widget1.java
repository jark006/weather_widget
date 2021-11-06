package com.jark006.weather;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import com.jark006.weather.bean.Daily;
import com.jark006.weather.bean.Realtime;
import com.jark006.weather.bean.Skycon;
import com.jark006.weather.bean.Temperature;
import com.jark006.weather.bean.WeatherBean;
import java.util.Date;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Response;
import com.jark006.weather.bean.Result;
import com.jark006.weather.utils.DateUtils;
import com.jark006.weather.utils.ImageUtils;
/**
 * Implementation of App Widget functionality.
 */
public class Widget1 extends AppWidgetProvider {

    /**
     * 更新
     */
//    public static final String ACTION_UPDATE = "action_update";
//    /**
//     * 无定位
//     */
//    public final int NO_LOCATION = 0x01;
//    /**
//     * 正在更新
//     */
//    public final int UPDATING = 0x02;
    /**
     * 更新成功
     */
    public final int UPDATE_SUCCESS = 0x03;
    /**
     * 更新失败
     */
    public final int UPDATE_FAILED = 0x04;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        getWeatherData(context, "番禺区", 113.27324,23.15792, appWidgetIds[0]);
    }


    /**
     * 获取天气数据
     */
    private void getWeatherData(final Context context, String district, double longitude, double latitude, int id) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.caiyunapp.com/v2/TwsDo9aQUYewFhV8/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        Call<WeatherBean> call = apiService.weather(longitude,latitude);
        call.enqueue(new Callback<WeatherBean>() {
            @Override
            public void onResponse(@NonNull Call<WeatherBean> call, @NonNull Response<WeatherBean> response) {
                WeatherBean weatherBean = response.body();
                updateAppWidget(context, UPDATE_SUCCESS, district, weatherBean, id);
            }

            @Override
            public void onFailure(@NonNull Call<WeatherBean> call, @NonNull Throwable t) {
                updateAppWidget(context, UPDATE_FAILED, district, null,id);
            }
        });
    }

    /**
     * 根据状态更新天气小部件
     *
     * @param status      更新状态
     * @param context     上下文
     * @param district    定位数据
     * @param weatherBean 天气数据
     */
    private void updateAppWidget(Context context, int status, String district, WeatherBean weatherBean, int id) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget1);

        if (status == UPDATE_SUCCESS) {
            showAppWidgetData(context, remoteViews, district, weatherBean);
        } else {
            remoteViews.setTextViewText(R.id.updateTime, "定位错误");
        }

        AppWidgetManager.getInstance(context).updateAppWidget(id, remoteViews);
    }


    private void showAppWidgetData(Context context, RemoteViews remoteViews, String district, WeatherBean weatherBean) {
        Realtime realtime = weatherBean.result.realtime;
        Daily daily = weatherBean.result.daily;

        remoteViews.setTextViewText(R.id.location, district);
        remoteViews.setTextViewText(R.id.descriptionTomorrow, weatherBean.result.forecast_keypoint);
        String updateDate = DateUtils.getFormatDate(weatherBean.server_time * 1000, DateUtils.HHmm);
        remoteViews.setTextViewText(R.id.updateTime, context.getString(R.string.widget_update_time, updateDate));
        remoteViews.setTextViewText(R.id.today, (int) realtime.temperature + "°");

        remoteViews.setTextViewText(R.id.description, weatherBean.result.minutely.description);


        // 明天预报
        Skycon skycon1 = daily.skycon.get(1);
        Temperature temperature1 = daily.temperature.get(1);
        remoteViews.setImageViewResource(R.id.tomorrowImg, ImageUtils.getWeatherIcon(skycon1.value));
        remoteViews.setTextViewText(R.id.tomorrow, (int) temperature1.avg+"°");
        remoteViews.setTextViewText(R.id.tomorrowRange, (int) temperature1.min
                + " ~ " + (int) temperature1.max + "°");


        // 后天预报
        Skycon skycon2 = daily.skycon.get(2);
        remoteViews.setImageViewResource(R.id.bigTomorrowImg, ImageUtils.getWeatherIcon(skycon2.value));
        Temperature temperature2 = daily.temperature.get(2);
        remoteViews.setTextViewText(R.id.bigTomorrow, (int) temperature2.avg+"°");
        remoteViews.setTextViewText(R.id.bigTomorrowRange, (int) temperature2.min
                + " ~ " + (int) temperature2.max + "°");


        // 天气描述
        String weather = weatherBean.result.realtime.skycon;
        // 降雨（雪）强度
        double intensity = weatherBean.result.realtime.precipitation.local.intensity;
        // 是否是白天
        Result result = weatherBean.result;
        String currentDate = DateUtils.getFormatDate(new Date(), DateUtils.yyyyMMdd) + " ";
        Date sunriseDate = DateUtils.getDate(currentDate + result.daily.astro.get(0).sunrise.time, DateUtils.yyyyMMddHHmm);
        Date sunsetDate = DateUtils.getDate(currentDate + result.daily.astro.get(0).sunset.time, DateUtils.yyyyMMddHHmm);
        Date date = new Date();
        boolean isDay = date.compareTo(sunriseDate) >= 0 && date.compareTo(sunsetDate) < 0;

        // 设置背景
        remoteViews.setInt(R.id.widget_rl, "setBackgroundResource", ImageUtils.getBgResourceId(weather, intensity, isDay));
    }




}