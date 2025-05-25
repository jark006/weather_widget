package io.github.jark006.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import io.github.jark006.weather.utils.LocationStruct;
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
    final int MSG_OK = 1;
    final int MSG_NULL = -1;

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
        findViewById(R.id.settingArea).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
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

        LocationStruct locationStruct = (LocationStruct) Utils.readObj(getApplicationContext(), "locationStruct");
        if (locationStruct == null || locationStruct.updateTime <= 0) {
            locationInfo.setText("暂无位置信息，请更新");
        } else {
            locationInfo.setText(String.format(Locale.CHINA,
                    "更新时间: %s\n经度: %.5f 纬度: %.5f\n%s",
                    DateUtils.getFormatDate(locationStruct.updateTime, DateUtils.yyyyMMddHHmm),
                    locationStruct.longitude, locationStruct.latitude, locationStruct.address));
        }
        btUpdateLocation.setText("更新位置");
        btUpdateLocation.setOnClickListener(v -> {
            getLocationAmap(getApplicationContext());
            Utils.createNotificationChannel(MainActivity.this, this);
        });
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if (msg.what == MSG_NULL) {
                locationInfo.setText((String) msg.obj);
                return;
            }

            if (msg.what != MSG_OK) {
                locationInfo.setText("无效数据");
                return;
            }

            LocationStruct locationStruct = (LocationStruct) msg.obj;
            if (locationStruct == null || locationStruct.updateTime <= 0) {
                locationInfo.setText("无效数据");
                return;
            }

            locationInfo.setText(String.format(Locale.CHINA,
                    "更新时间: %s\n经度: %.5f 纬度: %.5f\n%s",
                    DateUtils.getFormatDate(locationStruct.updateTime, DateUtils.yyyyMMddHHmm),
                    locationStruct.longitude, locationStruct.latitude, locationStruct.address));

            Utils.saveObj(getApplicationContext(), "locationStruct", locationStruct);

        }
    };

    void getLocationAmap(Context context) {
        if (Utils.isNoPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                Utils.isNoPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,}, 0);

            if (Utils.isNoPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    Utils.isNoPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
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

        AMapLocationClient.updatePrivacyShow(context, true, true);
        AMapLocationClient.updatePrivacyAgree(context, true);

        // 请到高德平台申请你的apiKey https://lbs.amap.com/api/android-location-sdk/guide/create-project/get-key
        AMapLocationClient.setApiKey(Utils.getMetaValue(this, "com.amap.api.v2.apikey"));

        AMapLocationListener mLocationListener = aMapLocation -> {
            Message msg = Message.obtain();
            if (aMapLocation.getErrorCode() == 0) {
                LocationStruct locationStruct = new LocationStruct();

                locationStruct.longitude = aMapLocation.getLongitude();
                locationStruct.latitude = aMapLocation.getLatitude();
                locationStruct.address = aMapLocation.getAddress();
                locationStruct.cityName = aMapLocation.getCity();
                locationStruct.districtName = aMapLocation.getDistrict();
                locationStruct.adCode = aMapLocation.getAdCode();
                locationStruct.updateTime = aMapLocation.getTime();

                msg.obj = locationStruct;
                msg.what = MSG_OK;
            } else {
                // 定位失败，详见错误码表。 https://lbs.amap.com/api/android-location-sdk/guide/utilities/errorcode
                msg.obj = String.format(Locale.CHINA, "%s\n错误码:[%d] 错误信息:[%s]",
                        getString(R.string.location_failed), aMapLocation.getErrorCode(),
                        aMapLocation.getErrorInfo());
                msg.what = MSG_NULL;
            }

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
        option.setMockEnable(true);

        mLocationClient.setLocationListener(mLocationListener);//设置定位回调监听
        mLocationClient.setLocationOption(option);//给定位客户端对象设置定位参数
        mLocationClient.startLocation();//启动定位
    }

    public static class CustomAdapter extends BaseAdapter {
        private final Context context;
        private List<Alert.Content> items;

        public CustomAdapter(Context context) {
            this.context = context;
            this.items = new ArrayList<>();
        }

        public void setList(List<Alert.Content> items) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                items.sort(Comparator.comparingLong(o -> o.pubtimestamp));
            }
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
