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
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.core.app.ActivityCompat;
import java.util.List;
import com.google.gson.*;

import com.jark006.weather.bean.AirQuality;
import com.jark006.weather.bean.Daily;
import com.jark006.weather.bean.DoubleValue;
import com.jark006.weather.bean.Realtime;
import com.jark006.weather.bean.Skycon;
import com.jark006.weather.bean.Temperature;
import com.jark006.weather.bean.WeatherBean;
import com.jark006.weather.district.district;
import com.jark006.weather.utils.DateUtils;
import com.jark006.weather.utils.ImageUtils;
import com.jark006.weather.utils.NetworkUtils;

/**
 * Implementation of App Widget functionality.
 */
public class Widget1 extends AppWidgetProvider {
    double myLongitude = 113.381429; //默认在广州大学城
    double myLatitude  =  23.039126;
    String myDistrict  = "广州大学城";

    double lastLongitude = myLongitude;
    double lastLatitude  = myLatitude;
    String lastDistrict  = myDistrict;

    public static final String ACTION_UPDATE = "action_update";

    public final int UPDATE_SUCCESS = 0x03;
    public final int UPDATE_FAILED = 0x04;
    public final int UPDATE_ONGOING = 0x05;


    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        // 手动刷新
        if (ACTION_UPDATE.equals(action)) {
            Log.d(TAG, "onReceive: 手动刷新");
            updateAppWidget(context, UPDATE_ONGOING, "更新中...", null);
            getLocation(context);
        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate: 周期刷新");
        getLocation(context);
    }


    static final double EARTH_RADIUS = 6378.137;
    static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }
    // Unit : Km
    public static double GetDistance(double lat1, double lng1, double lat2, double lng2)
    {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        return s;
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

            String errorInfo = DateUtils.getFormatDate(System.currentTimeMillis(), DateUtils.HHmm)+"定位错误，或无定位权限";
            updateAppWidget(context, UPDATE_FAILED, errorInfo, null);
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
            Log.w(TAG, "bestLocation: "+myLongitude+","+myLatitude );
        }else{
            Log.w(TAG, "LocationFail,Using default:"+myLongitude+","+myLatitude );
        }

        //这个纬度下， 大约半径[10km]以内不用重新获取地名
        if(GetDistance(myLatitude, myLongitude, lastLatitude, lastLongitude) < 10){
            Log.w(TAG, "getLocation: 地点变化不大");
            if(!lastDistrict.equals("本地")){
                Log.w(TAG, "getLocation: 地名正确");
                myDistrict = lastDistrict;
                getWeatherData(context);
                return;
            }
            Log.w(TAG, "getLocation: 地名错了");
        }else{
            Log.w(TAG, "getLocation: 地点变化了");
            lastLongitude = myLongitude;
            lastLatitude  = myLatitude;
        }

        //https://restapi.amap.com/v3/geocode/regeo?key=c2844d38363cae2a8a52eb9fa18a2ebc&location=
        new Thread(() -> {
            try {
                @SuppressLint("DefaultLocale")
                String link = String.format("https://restapi.amap.com/v3/geocode/regeo?key=c2844d38363cae2a8a52eb9fa18a2ebc&location=%f,%f",
                        myLongitude,myLatitude);
                String res = NetworkUtils.getData(link);
                try {
                    district district = new Gson().fromJson(res, district.class);
                    if(district.regeocode.addressComponent.district.length()>1){
                        myDistrict = district.regeocode.addressComponent.district;
                    }else if(district.regeocode.addressComponent.city.length()>1){
                        myDistrict = district.regeocode.addressComponent.city;
                    }else{
                        myDistrict = district.regeocode.addressComponent.province;
                    }
                    lastDistrict = myDistrict;
                    Log.w(TAG, "getLocation onResponse: 定位 "+myDistrict);

                } catch (Exception e) {
                    Log.w(TAG, "getLocation: 解析数据失败 " + e);
                    Log.w(TAG, "getLocation: 使用默认："+myDistrict);
                    getWeatherData(context);
                }
            } catch (Exception e) {
                Log.w(TAG, "getLocation: 网络错误 " + e);
                Log.w(TAG, "getLocation: 使用默认："+myDistrict);
                getWeatherData(context);
            }
        }).start();

    }
    /**
     * 获取天气数据
     * https://api.caiyunapp.com/v2.5/wh9aWLYieE1akfGi/113.381429,23.039126/weather.json?dailysteps=6
     */
    private void getWeatherData(final Context context) {
        Log.d(TAG, "getWeatherData enter");

        new Thread(() -> {
            try {
                @SuppressLint("DefaultLocale")
                String link = String.format("https://api.caiyunapp.com/v2.5/wh9aWLYieE1akfGi/%f,%f/weather.json?dailysteps=6",
                        myLongitude,myLatitude);
                String res = NetworkUtils.getData(link);
                try {
                    updateAppWidget(context, UPDATE_SUCCESS, myDistrict, res);
                } catch (Exception e) {
                    Log.d(TAG, "getWeatherData: 解析数据失败 " + e);
                    String errorTips = DateUtils.getFormatDate(System.currentTimeMillis(), DateUtils.HHmm) + "解析数据失败";
                    updateAppWidget(context, UPDATE_FAILED, errorTips, null);
                }
            } catch (Exception e) {
                Log.d(TAG, "getWeatherData: 网络错误 " + e);
                String errorTips = DateUtils.getFormatDate(System.currentTimeMillis(), DateUtils.HHmm) + "获取天气数据失败";
                updateAppWidget(context, UPDATE_FAILED, errorTips, null);
            }
        }).start();
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
        Log.d(TAG, "createUpdatePendingIntent: ");
        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            return PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_MUTABLE);
        else
            return PendingIntent.getBroadcast(context, 0, updateIntent,PendingIntent.FLAG_UPDATE_CURRENT);

    }

    /**
     * 根据状态更新天气小部件
     *
     * @param status      更新状态
     * @param context     上下文
     * @param district    定位数据
     * @param weatherJson 天气数据
     */
    private void updateAppWidget(Context context, int status, String district, String weatherJson) {
        ComponentName componentName = new ComponentName(context, this.getClass());
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget1);

        // 打开APP首页的Intent
//        PendingIntent launchPendingIntent = createLaunchPendingIntent(context);
//        remoteViews.setOnClickPendingIntent(R.id.location, launchPendingIntent);
        // 刷新的Intent
        PendingIntent updatePendingIntent = createUpdatePendingIntent(context);
        remoteViews.setOnClickPendingIntent(R.id.widget_rl, updatePendingIntent);

        if (status == UPDATE_SUCCESS) {
            showAppWidgetData(context, remoteViews, district, weatherJson);
        } else if (status == UPDATE_ONGOING) {
            remoteViews.setTextViewText(R.id.updateTime, district);
        } else {
            remoteViews.setTextViewText(R.id.today_other, district);
        }
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    }


    @SuppressLint("DefaultLocale")
    private void showAppWidgetData(Context context, RemoteViews remoteViews, String district, String weatherJson) {
        WeatherBean weatherBean = new Gson().fromJson(weatherJson, WeatherBean.class);
        StringBuilder hourTemp = new StringBuilder();
        List<DoubleValue> list = weatherBean.result.hourly.temperature;
        double gap = (list.get(1).value-list.get(0).value);
        hourTemp.append(list.get(1).datetime.substring(11,13)).append("时[");
        for (int i = 1; i <= 10; i++) {
            hourTemp.append(String.format("%2d° ", (int) (list.get(i).value-gap)));
        }
        hourTemp.setCharAt(hourTemp.length()-1, ']');
        hourTemp.append(list.get(10).datetime.substring(11,13)).append("时");
        remoteViews.setTextViewText(R.id.hour_temp, hourTemp.toString().trim());

        Realtime realtime = weatherBean.result.realtime;
        Daily daily = weatherBean.result.daily;
        AirQuality air = realtime.air_quality;

        String forecast = weatherBean.result.forecast_keypoint;
        String description = weatherBean.result.minutely.description;
        String otherInfo = String.format("%d%% PM2.5:%.0f PM10:%.0f O₃:%.0f SO₂:%.0f NO₂:%.0f CO:%.1f %s",
                (int)(realtime.humidity*100), air.pm25, air.pm10, air.o3, air.so2, air.no2,air.co, air.description.chn);
        remoteViews.setTextViewText(R.id.location, district);
        remoteViews.setTextViewText(R.id.today_other, forecast.equals(description)? otherInfo:forecast);
        String updateDate = DateUtils.getFormatDate(System.currentTimeMillis(), DateUtils.HHmm);
        remoteViews.setTextViewText(R.id.updateTime, context.getString(R.string.widget_update_time, updateDate));
        remoteViews.setTextViewText(R.id.today_tem, (int) realtime.temperature + "°");
        remoteViews.setTextViewText(R.id.description, description);

        // 明天预报
        Skycon skycon = daily.skycon.get(1);
        Temperature temperature1 = daily.temperature.get(1);
        remoteViews.setImageViewResource(R.id.tomorrowImg, ImageUtils.getWeatherIcon(skycon.value));
        remoteViews.setTextViewText(R.id.tomorrow, (int) temperature1.avg+"°");
        remoteViews.setTextViewText(R.id.tomorrowRange, (int) temperature1.min
                + " ~ " + (int) temperature1.max + "°");

        // 后天预报
        skycon = daily.skycon.get(2);
        remoteViews.setImageViewResource(R.id.bigTomorrowImg, ImageUtils.getWeatherIcon(skycon.value));
        Temperature temperature2 = daily.temperature.get(2);
        remoteViews.setTextViewText(R.id.bigTomorrow, (int) temperature2.avg+"°");
        remoteViews.setTextViewText(R.id.bigTomorrowRange, (int) temperature2.min
                + " ~ " + (int) temperature2.max + "°");

        // 天气描述
        String weather = weatherBean.result.realtime.skycon;
        // 降雨（雪）强度
        double intensity = weatherBean.result.realtime.precipitation.local.intensity;

        // 是否是白天
        long hours = System.currentTimeMillis()/3600000 +8;
        hours %= 24;
        boolean isDay  = hours > 6 && hours < 18;

        // 设置背景
        remoteViews.setInt(R.id.widget_rl, "setBackgroundResource", ImageUtils.getBgResourceId(weather, intensity, isDay));
    }
}