package com.jark006.weather;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.jark006.weather.bean.Daily;
import com.jark006.weather.bean.Realtime;
import com.jark006.weather.bean.Skycon;
import com.jark006.weather.bean.Temperature;
import com.jark006.weather.bean.WeatherBean;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Response;

import com.jark006.weather.utils.DateUtils;
import com.jark006.weather.utils.ImageUtils;
import com.jark006.weather.district.district;

/**
 * Implementation of App Widget functionality.
 */
public class Widget1 extends AppWidgetProvider {
    double myLongitude = 113.381429; //默认在广州大学城
    double myLatitude  =  23.039126;
    String myDistrict  = "广州大学城";

    double lastLongitude = myLongitude;
    double lastLatitude  =  myLatitude;
    String lastDistrict  = myDistrict;

//    static long lastUpdateTime = System.currentTimeMillis();
    /**
     * 更新
     */
    public static final String ACTION_UPDATE = "action_update";
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

//    int layoutID = R.layout.widget1;

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        // 手动刷新
        if (ACTION_UPDATE.equals(action)) {
            Log.d(TAG, "onReceive: 手动刷新1");
            getLocation(context);
        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate: 刷新1");
        getLocation(context);
    }


    private void getLocation(final Context context) {


        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "getLocation: UPDATE_FAILED");
//            ActivityCompat.requestPermissions(, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 0);

            // 打开APP首页的Intent
//        PendingIntent launchPendingIntent = createLaunchPendingIntent(context);
//        remoteViews.setOnClickPendingIntent(R.id.location, launchPendingIntent);
//            Intent in = new Intent(context, MainActivity.class);
//            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(in);

            String ss = DateUtils.getFormatDate(System.currentTimeMillis(), DateUtils.HHmm)+"定位错误";
            updateAppWidget(context, UPDATE_FAILED, ss, null);
            return ;
        }

        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }

        if(bestLocation != null){
            myLongitude = bestLocation.getLongitude();
            myLatitude = bestLocation.getLatitude();
            myDistrict = "本地";
            Log.e(TAG, "bestLocation: "+myLongitude+","+myLatitude );
        }else{
            Log.w(TAG, "LocationFail,Using default:"+myLongitude+","+myLatitude );
        }

        //这个纬度下， 大约半径[5km]以内不用重新获取地名
        if(Math.sqrt(Math.pow(myLongitude-lastLongitude,2)+Math.pow(myLatitude-lastLatitude, 2)) < 0.05){
            myDistrict = lastDistrict;
            getWeatherData(context);
            return;
        }else{
            lastLongitude = myLongitude;
            lastLatitude  = myLatitude;
        }


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://restapi.amap.com/v3/geocode/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        Call<district> call = apiService.getLocationName("c2844d38363cae2a8a52eb9fa18a2ebc", myLongitude+","+myLatitude);
        call.enqueue(new Callback<district>() {
            @Override
            public void onResponse(@NonNull Call<district> call, @NonNull Response<district> response) {
                district district = response.body();
                if (district != null ){
                    if(district.regeocode.addressComponent.district.length()>1){
                        myDistrict = district.regeocode.addressComponent.district;
                    }else if(district.regeocode.addressComponent.city.length()>1){
                        myDistrict = district.regeocode.addressComponent.city;
                    }else{
                        myDistrict = district.regeocode.addressComponent.province;
                    }
                    lastDistrict = myDistrict;
                    Log.w(TAG, "getLocation onResponse: 定位 "+myDistrict);
                }else{
                    Log.w(TAG, "getLocation onResponse: myDistrict == null");
                }
                getWeatherData(context);
            }

            @Override
            public void onFailure(@NonNull Call<district> call, @NonNull Throwable t) {
                Log.w(TAG, "getLocation onFailure: 使用默认："+myDistrict);
                getWeatherData(context);
            }
        });



    }
    /**
     * 获取天气数据
     */
    private void getWeatherData(final Context context) {
        Log.d(TAG, "getWeatherData enter");
        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://api.caiyunapp.com/v2/TwsDo9aQUYewFhV8/")//原作者
                .baseUrl("https://api.caiyunapp.com/v2.5/wh9aWLYieE1akfGi/")//jark
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        Call<WeatherBean> call = apiService.weather(myLongitude,myLatitude);
        call.enqueue(new Callback<WeatherBean>() {
            @Override
            public void onResponse(@NonNull Call<WeatherBean> call, @NonNull Response<WeatherBean> response) {
                Log.d(TAG, "getWeatherData onResponse: ");
                WeatherBean weatherBean = response.body();
                updateAppWidget(context, UPDATE_SUCCESS, myDistrict, weatherBean);
            }

            @Override
            public void onFailure(@NonNull Call<WeatherBean> call, @NonNull Throwable t) {
                Log.d(TAG, "getWeatherData onFailure: ");
                String ss = DateUtils.getFormatDate(System.currentTimeMillis(), DateUtils.HHmm)+"获取天气数据失败";
                updateAppWidget(context, UPDATE_FAILED, ss, null);
            }
        });
    }

//    /**
//     * 创建跳转首界面 PendingIntent
//     */
//    protected PendingIntent createLaunchPendingIntent(Context context) {
//        Intent launchIntent = new Intent(context, MainActivity.class);
//        return PendingIntent.getActivity(context, 0, launchIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//    }

    /**
     * 创建更新数据 PendingIntent
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    protected PendingIntent createUpdatePendingIntent(Context context) {
        Intent updateIntent = new Intent(ACTION_UPDATE);
        updateIntent.setClass(context, this.getClass());
        return PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 根据状态更新天气小部件
     *
     * @param status      更新状态
     * @param context     上下文
     * @param district    定位数据
     * @param weatherBean 天气数据
     */
    private void updateAppWidget(Context context, int status, String district, WeatherBean weatherBean) {
        ComponentName componentName = new ComponentName(context, this.getClass());
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget1);

        // 打开APP首页的Intent
//        PendingIntent launchPendingIntent = createLaunchPendingIntent(context);
//        remoteViews.setOnClickPendingIntent(R.id.location, launchPendingIntent);
        // 刷新的Intent
        PendingIntent updatePendingIntent = createUpdatePendingIntent(context);
        remoteViews.setOnClickPendingIntent(R.id.widget_rl, updatePendingIntent);

        if (status == UPDATE_SUCCESS) {
            showAppWidgetData(context, remoteViews, district, weatherBean);
//            Log.d(TAG, "updateAppWidget: UPDATE_SUCCESS");
        } else {
            remoteViews.setTextViewText(R.id.descriptionTomorrow, district);
//            Log.d(TAG, "updateAppWidget: Fail");

        }

        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    }


    private void showAppWidgetData(Context context, RemoteViews remoteViews, String district, WeatherBean weatherBean) {
        Log.w(TAG, "showAppWidgetData: enter" );
        Realtime realtime = weatherBean.result.realtime;
        Daily daily = weatherBean.result.daily;

        String forecast = weatherBean.result.forecast_keypoint;
        String description = weatherBean.result.minutely.description;

        remoteViews.setTextViewText(R.id.location, district);
        remoteViews.setTextViewText(R.id.descriptionTomorrow, forecast.equals(description)?"":forecast);
//        String updateDate = DateUtils.getFormatDate(weatherBean.server_time * 1000, DateUtils.HHmm);
        String updateDate = DateUtils.getFormatDate(System.currentTimeMillis(), DateUtils.HHmm);
        remoteViews.setTextViewText(R.id.updateTime, context.getString(R.string.widget_update_time, updateDate));
        remoteViews.setTextViewText(R.id.today, (int) realtime.temperature + "°");

        remoteViews.setTextViewText(R.id.description, description);

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
//        Result result = weatherBean.result;
//        String currentDate = DateUtils.getFormatDate(new Date(), DateUtils.yyyyMMdd) + " ";
//        Date sunriseDate = DateUtils.getDate(currentDate + result.daily.astro.get(0).sunrise.time, DateUtils.yyyyMMddHHmm);
//        Date sunsetDate = DateUtils.getDate(currentDate + result.daily.astro.get(0).sunset.time, DateUtils.yyyyMMddHHmm);
//        Date date = new Date();
//        boolean isDay = date.compareTo(sunriseDate) >= 0 && date.compareTo(sunsetDate) < 0;

        long hours = System.currentTimeMillis()/3600000 +8;
        hours %= 24;
        Log.w(TAG, "showAppWidgetData: hours:"+hours );
        boolean isDay  = hours > 6 && hours < 18;


        // 设置背景
        remoteViews.setInt(R.id.widget_rl, "setBackgroundResource", ImageUtils.getBgResourceId(weather, intensity, isDay));
    }
}