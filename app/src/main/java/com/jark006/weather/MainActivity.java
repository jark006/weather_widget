package com.jark006.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.jark006.weather.utils.DateUtils;
import com.jark006.weather.utils.Utils;

//debug key sha1
//C:\Program Files\Android\Android Studio\jre\bin> ./keytool -list -v -keystore "C:\Users\JARK006\.android\debug.keystore"
//密码 android

//release key sha1
//C:\Program Files\Android\Android Studio\jre\bin> ./keytool -list -v -keystore "D:\AndroidWSP\xxx.jks"
//获取两个SHA1后到高德平台设置获取key

public class MainActivity extends AppCompatActivity {
    final String tips = "\n\n小部件将会一直使用以上地址，若平时移动范围小于10公里，则不需要频繁更新。";
    TextView mainText;
    Button btJumpToQQ, btUpdateLocation, btJumpToGithub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainText = findViewById(R.id.mainText);
        btJumpToQQ = findViewById(R.id.btJumpToQQ);
        btJumpToGithub = findViewById(R.id.btJumpToGithub);
        btUpdateLocation = findViewById(R.id.btUpdateLocation);

        btJumpToQQ.setOnClickListener(v -> {
            try {
                //【冻它模块 freezeit】(781222669) 的 key 为： ntLAwm7WxB0hVcetV7DsxfNTVN16cGUD
                String key = "ntLAwm7WxB0hVcetV7DsxfNTVN16cGUD";
                Intent intent = new Intent();
                intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D" + key));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.qq_group_link))));
            }
        });

        btJumpToGithub.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jark006/weather_widget"))));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sf = this.getSharedPreferences("locationInfo", Context.MODE_PRIVATE);
        long updateTime = sf.getLong("updateTime", 0);
        if (updateTime > 0) {
            double longitude = sf.getFloat("longitude", 0);
            double latitude = sf.getFloat("latitude", 0);
            String address = sf.getString("address", "");
            mainText.setText("更新时间: " + DateUtils.getFormatDate(updateTime, DateUtils.yyyyMMddHHmm) +
                    "\n经度: " + longitude + "\n纬度: " + latitude + "\n" + address + tips);
        } else {
            mainText.setText("暂无位置信息，请更新");
        }
        btUpdateLocation.setText("更新当前位置");
        btUpdateLocation.setOnClickListener(v -> getLocationAmap(getApplicationContext()));
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            boolean status = msg.getData().getBoolean("status");

            if (!status) {
                String errorTips = msg.getData().getString("errorTips");
                mainText.setText(errorTips);
                return;
            }

            double longitude = msg.getData().getDouble("longitude");
            double latitude = msg.getData().getDouble("latitude");
            String address = msg.getData().getString("address");
            long updateTime = msg.getData().getLong("updateTime");
            mainText.setText("更新时间: " + DateUtils.getFormatDate(updateTime, DateUtils.yyyyMMddHHmm) +
                    "\n经度: " + longitude + "\n纬度: " + latitude + "\n" + address + tips);

            SharedPreferences.Editor editor = getBaseContext().getSharedPreferences("locationInfo", Context.MODE_PRIVATE).edit();
            editor.putFloat("longitude", (float) longitude);
            editor.putFloat("latitude", (float) latitude);
            editor.putString("address", address);
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

            final String[] requestList = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
            ActivityCompat.requestPermissions(MainActivity.this, requestList, 2);

            try {
                Thread.sleep(200);
            } catch (Exception ignored) {
            }

            if (isNoPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    isNoPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                mainText.setText(R.string.location_tips);
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
        AMapLocationClient.setApiKey("50407ea2ca3f8f53e63b5524f791f0fe"); // AndroidManifest.xml  com.amap.api.v2.apikey

        AMapLocationListener mLocationListener = aMapLocation -> {
            Message msg = new Message();
            Bundle data = new Bundle();
            if (aMapLocation.getErrorCode() == 0) {
                double longitude = aMapLocation.getLongitude();
                double latitude = aMapLocation.getLatitude();
                String address = aMapLocation.getAddress();
                long updateTime = aMapLocation.getTime();

                data.putBoolean("status", true);
                data.putDouble("longitude", longitude);
                data.putDouble("latitude", latitude);
                data.putString("address", address);
                data.putLong("updateTime", updateTime);
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
            e.printStackTrace();
            return;
        }

        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);//低功耗模式。
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        option.setOnceLocationLatest(true);

        mLocationClient.setLocationListener(mLocationListener);//设置定位回调监听
        mLocationClient.setLocationOption(option);//给定位客户端对象设置定位参数
        mLocationClient.startLocation();//启动定位

        final String[] warnLevelStr = Utils.warnLevelStr;//00白色 ... 04红色
        final String[] warnLevelDescription = Utils.warnLevelDescription;
        final int[] IMPORTANT_INT = Utils.IMPORTANT_INT;

        // 创建预警信息通知通道
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        for (int warnLevel = 0; warnLevel < 5; warnLevel++) {
            String channelId = warnLevelStr[warnLevel];
            NotificationChannel channel = new NotificationChannel(channelId, channelId, IMPORTANT_INT[warnLevel + 1]);
            channel.setDescription(warnLevelDescription[warnLevel]);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
