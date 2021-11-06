package com.jark006.weather;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
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
//import com.jark006.weather.utils.TranslationUtils;
/**
 * Implementation of App Widget functionality.
 */
public class Widget1 extends AppWidgetProvider {
//    private int[] temperature = new int[3];
//    private String location = "unknow", discript = "unkown";
//    private boolean isSuccess = false;

//    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
//                                int appWidgetId) {
//
////        new Thread(networkTask).start();
//
//        CharSequence widgetText = context.getString(R.string.appwidget_text);
//        // Construct the RemoteViews object
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget1);
//        views.setTextViewText(R.id.discription, widgetText);
//
//        // Instruct the widget manager to update the widget
//        appWidgetManager.updateAppWidget(appWidgetId, views);
//    }
//
//    @Override
//    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//        // There may be multiple widgets active, so update all of them
//        for (int appWidgetId : appWidgetIds) {
//            updateAppWidget(context, appWidgetManager, appWidgetId);
//        }
//    }
//
//    @Override
//    public void onEnabled(Context context) {
//        // Enter relevant functionality for when the first widget is created
//    }
//
//    @Override
//    public void onDisabled(Context context) {
//        // Enter relevant functionality for when the last widget is disabled
//    }
//
//    private void parseDate(String str){
//
//    }
//    private final Handler handler = new Handler(Looper.getMainLooper()) {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            Bundle b = msg.getData();
//            String rawStr = b.getString("rawDate");
//            if(rawStr == null || rawStr.length() < 10){
////                Toast.makeText(, )
//                Log.e(TAG, "handleMessage: 网络错误", null);
//            }else
//                parseDate(rawStr);
//        }
//    };
//
//    Runnable networkTask = () -> {
////        String link = "https://webapi.sporttery.cn/gateway/lottery/getHistoryPageListV1.qry?gameNo=04&provinceId=0&isVerify=1&termLimits=100";
//        String link = "https://api.caiyunapp.com/v2/TwsDo9aQUYewFhV8/113.27324,23.15792/weather.json?dailysteps=6";
//        String Response = utils.getData(link);
//        Message msg = new Message();
//        Bundle data = new Bundle();
//        Log.i(TAG, "ResponseLength: "+Response);
//        data.putString("rawDate", Response);
//        msg.setData(data);
//        handler.sendMessage(msg);
//    };




    /**
     * 更新
     */
    public static final String ACTION_UPDATE = "action_update";
    /**
     * 无定位
     */
    public final int NO_LOCATION = 0x01;
    /**
     * 正在更新
     */
    public final int UPDATING = 0x02;
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
//        ComponentName componentName = new ComponentName(context, this.getClass());
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget1);

        if (status == NO_LOCATION) {
            // 无定位
//            noLocation(context, remoteViews, district, weatherBean);
            remoteViews.setTextViewText(R.id.updateTime, "定位错误");
        } else if (status == UPDATING) {
            // 正在更新
//            updating(context, remoteViews, district, weatherBean);
        } else if (status == UPDATE_SUCCESS) {
            // 更新成功
//            updateSuccess(context, remoteViews, district, weatherBean);
            showAppWidgetData(context, remoteViews, district, weatherBean);
        } else if (status == UPDATE_FAILED) {
            // 正在失败
//            updateFail(context, remoteViews, district, weatherBean);
        }

        AppWidgetManager.getInstance(context).updateAppWidget(id, remoteViews);
    }

    /**
     * 获取 RemoteViews 布局 id
     */
//    abstract protected int layoutId();
//
//    abstract protected void noLocation(Context context, RemoteViews remoteViews, String district, WeatherBean weatherBean);
//
//    abstract protected void updating(Context context, RemoteViews remoteViews, String district, WeatherBean weatherBean);
//
    protected void updateSuccess(Context context, RemoteViews remoteViews, String district, WeatherBean weatherBean) {
//        // 打开APP首页的Intent
//        PendingIntent launchPendingIntent = createLaunchPendingIntent(context);
//        // 刷新的Intent
//        PendingIntent updatePendingIntent = createUpdatePendingIntent(context);

        showAppWidgetData(context, remoteViews, district, weatherBean);
    }
//    abstract protected void updateFail(Context context, RemoteViews remoteViews, String district, WeatherBean weatherBean);
//


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