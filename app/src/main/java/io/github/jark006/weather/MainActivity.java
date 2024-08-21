package io.github.jark006.weather;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.util.Locale;

import io.github.jark006.weather.utils.DateUtils;
import io.github.jark006.weather.utils.Utils;
import io.github.jark006.weather.caiyun.Caiyun.Result.Alert;

//debug key sha1
//C:\Program Files\Android\Android Studio\jre\bin> ./keytool -list -v -keystore "C:\Users\JARK006\.android\debug.keystore"
//密码 android

//release key sha1
//C:\Program Files\Android\Android Studio\jre\bin> ./keytool -list -v -keystore "D:\AndroidWSP\xxx.jks"
//获取两个SHA1后到高德平台设置获取key

public class MainActivity extends AppCompatActivity {
    TextView locationInfo;
    Button btUpdateLocation;
    LinearLayout recentAlert;
    ListView alertListVew;
    CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationInfo = findViewById(R.id.locationInfo);
        btUpdateLocation = findViewById(R.id.btUpdateLocation);
        recentAlert = findViewById(R.id.recentAlert);
        alertListVew = findViewById(R.id.alertList);

        customAdapter = new CustomAdapter(this);
        alertListVew.setAdapter(customAdapter);

        findViewById(R.id.btJumpToGithub).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jark006/weather_widget"))));
    }

    @Override
    public void onResume() {
        super.onResume();

        @SuppressWarnings("unchecked")
        var alertMap = (HashMap<String, Alert.Content>) Utils.readObj(getApplicationContext(), "alertMap");

        if (alertMap == null || alertMap.isEmpty()) {
            recentAlert.setVisibility(View.GONE);
        } else {
            recentAlert.setVisibility(View.VISIBLE);
            customAdapter.setList(new ArrayList<>(alertMap.values()));
        }

        SharedPreferences sf = this.getSharedPreferences("locationInfo", Context.MODE_PRIVATE);
        long updateTime = sf.getLong("updateTime", 0);
        if (updateTime > 0) {
            double longitude = sf.getFloat("longitude", 0);
            double latitude = sf.getFloat("latitude", 0);
            String address = sf.getString("address", "未知");

            locationInfo.setText(String.format(Locale.CHINA,
                    "更新时间: %s\n经度: %.5f 纬度: %.5f\n%s",
                    DateUtils.getFormatDate(updateTime, DateUtils.yyyyMMddHHmm),
                    longitude, latitude, address));
        } else {
            locationInfo.setText("暂无位置信息，请更新");
        }
        btUpdateLocation.setText("更新当前位置");
        btUpdateLocation.setOnClickListener(v -> getLocationAmap(getApplicationContext()));
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            boolean status = msg.getData().getBoolean("status");

            if (!status) {
                String errorTips = msg.getData().getString("errorTips");
                locationInfo.setText(errorTips);
                return;
            }

            double longitude = msg.getData().getDouble("longitude");
            double latitude = msg.getData().getDouble("latitude");
            String address = msg.getData().getString("address");
            String cityName = msg.getData().getString("cityName");
            String districtName = msg.getData().getString("districtName");
            String adCode = msg.getData().getString("adCode");
            long updateTime = msg.getData().getLong("updateTime");

            locationInfo.setText(String.format(Locale.CHINA,
                    "更新时间: %s\n经度: %.5f 纬度: %.5f\n%s",
                    DateUtils.getFormatDate(updateTime, DateUtils.yyyyMMddHHmm),
                    longitude, latitude, address));

            SharedPreferences.Editor editor = getBaseContext().getSharedPreferences("locationInfo", Context.MODE_PRIVATE).edit();
            editor.putFloat("longitude", (float) longitude);
            editor.putFloat("latitude", (float) latitude);
            editor.putString("address", address);
            editor.putString("cityName", cityName);
            editor.putString("districtName", districtName);
            editor.putString("adCode", adCode);
            editor.putLong("updateTime", updateTime);
            editor.apply();
        }
    };

    boolean isNoPermission(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED;
    }

    void getLocationAmap(Context context) {
        if (isNoPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                isNoPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,}, 0);

            if (isNoPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    isNoPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                locationInfo.setText(R.string.location_tips);
                btUpdateLocation.setText(R.string.setting_permission);
                btUpdateLocation.setOnClickListener(v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", this.getPackageName(), null));
                    startActivity(intent);
                });
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                isNoPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS,}, 1);

            if (isNoPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
                locationInfo.setText(R.string.notification_tips);
                btUpdateLocation.setText(R.string.setting_permission);
                btUpdateLocation.setOnClickListener(v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", this.getPackageName(), null));
                    startActivity(intent);
                });
                return;
            }
        }

        AMapLocationClient.updatePrivacyShow(context, true, true);
        AMapLocationClient.updatePrivacyAgree(context, true);

        // 请到高德平台申请你的apiKey https://lbs.amap.com/api/android-location-sdk/guide/create-project/get-key
        AMapLocationClient.setApiKey(Utils.getMetaValue(this, "com.amap.api.v2.apikey"));

        AMapLocationListener mLocationListener = aMapLocation -> {
            Message msg = new Message();
            Bundle data = new Bundle();
            if (aMapLocation.getErrorCode() == 0) {
                data.putBoolean("status", true);
                data.putDouble("longitude", aMapLocation.getLongitude());
                data.putDouble("latitude", aMapLocation.getLatitude());
                data.putString("address", aMapLocation.getAddress());
                data.putString("cityName", aMapLocation.getCity());
                data.putString("districtName", aMapLocation.getDistrict());
                data.putString("adCode", aMapLocation.getAdCode());
                data.putLong("updateTime", aMapLocation.getTime());
            } else {
                // 定位失败，详见错误码表。 https://lbs.amap.com/api/android-location-sdk/guide/utilities/errorcode
                String errorTips = getString(R.string.location_failed) + "\n"
                        + aMapLocation.getErrorInfo();
                data.putBoolean("status", false);
                data.putString("errorTips", errorTips);
            }
            msg.setData(data);
            handler.sendMessage(msg);
        };

        AMapLocationClient mLocationClient;
        try {
            mLocationClient = new AMapLocationClient(context);
        } catch (Exception e) {
            Utils.saveLog(context, "定位错误" + e);
            return;
        }

        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);//低功耗模式
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        option.setOnceLocationLatest(true);

        mLocationClient.setLocationListener(mLocationListener);//设置定位回调监听
        mLocationClient.setLocationOption(option);//给定位客户端对象设置定位参数
        mLocationClient.startLocation();//启动定位

        // 创建预警信息通知通道
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        for (int warnLevel = 0; warnLevel < 5; warnLevel++) {
            String channelId = Utils.warnLevelStr[warnLevel];//00白色 ... 04红色
            NotificationChannel channel = new NotificationChannel(channelId, channelId, Utils.IMPORTANT_INT[warnLevel + 1]);
            channel.setDescription(Utils.warnLevelDescription[warnLevel]);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static class CustomAdapter extends BaseAdapter {
        private final Context context;
        private List<Alert.Content> items;

        public CustomAdapter(Context context) {
            this.context = context;
            this.items = new ArrayList<>();
        }

        public void setList(List<Alert.Content> items) {
            items.sort(Comparator.comparingLong(o -> o.pubtimestamp));
            this.items = items;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.alert_item, parent, false);
            }

            var item = items.get(items.size() - 1 - position); //逆序

            String titleStr;
            long deltaSec = System.currentTimeMillis() / 1000 - item.pubtimestamp;
            if (deltaSec < 60) {
                titleStr = "刚刚";
            } else if (deltaSec < 3600) {
                titleStr = (deltaSec / 60) + "分钟前";
            } else if (deltaSec < 86400) {
                titleStr = (deltaSec / 3600) + "小时前";
            } else {
                titleStr = (deltaSec / 86400) + "天前";
            }
            titleStr += " " + item.title;

            ((TextView) convertView.findViewById(R.id.title)).setText(titleStr);
            ((TextView) convertView.findViewById(R.id.content)).setText(item.description);

            int warnLevel = item.code == null ? -1 : Integer.parseInt(item.code) % 100; // 0(白色预警) ~ 4(红色预警)
            ((ImageView) convertView.findViewById(R.id.icon)).setImageResource(Utils.warnIconIndex[warnLevel < 0 ? 2 : warnLevel]);

            return convertView;
        }
    }
}
