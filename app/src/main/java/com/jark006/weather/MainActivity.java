package com.jark006.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.jark006.weather.utils.PermissionUtil;

import static android.content.ContentValues.TAG;

//debug key sha1
//C:\Program Files\Android\Android Studio\jre\bin> ./keytool -list -v -keystore "C:\Users\JARK006\.android\debug.keystore"
//密码 android

//release key sha1
//C:\Program Files\Android\Android Studio\jre\bin> ./keytool -list -v -keystore "D:\AndroidWSP\jarkstudio_xxxx_jark006Key.jks"
//获取两个SHA1后到高德平台设置获取key

public class MainActivity extends AppCompatActivity {
    LocationManager locationManager;
    TextView mainText;
    Button bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        mainText = findViewById(R.id.mainText);
        bt = findViewById(R.id.button);
        if (PermissionUtil.isOwnPermisson(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                PermissionUtil.isOwnPermisson(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
//            mainText.setText("定位权限已获取\n请到桌面添加小部件");
////            bt.setVisibility(View.GONE);
            mainText.setText(" ");
            bt.setText("更新此时位置");
            bt.setOnClickListener(v -> {
                try {
                    getLocationAmap();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            mainText.setText("没有永久定位权限\n请到设置里开启应用的定位权限\n位置权限设为 [ 始终允许 ]");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 2);

            bt.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", this.getPackageName(), null));
                startActivity(intent);
            });
        }
    }

    void getLocation() {
//        Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_COARSE);//低精度，如果设置为高精度，依然获取不了location。
//        criteria.setAltitudeRequired(false);//不要求海拔
//        criteria.setBearingRequired(false);//不要求方位
//        criteria.setCostAllowed(true);//允许有花费
//        criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗
//
//        //从可用的位置提供器中，匹配以上标准的最佳提供器
//        String locationProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: 没有权限 ");
            return;
        }
        //监视地理位置变化
        locationManager.requestLocationUpdates("fused", 1000, 10, locationListener);
        Log.d(TAG, "getLocation: 开始监听");
    }

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider + ".." + Thread.currentThread().getName());
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: " + provider + ".." + Thread.currentThread().getName());
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: " + ".." + Thread.currentThread().getName());
            //如果位置发生变化,重新显示
            //从可用的位置提供器中，匹配以上标准的最佳提供器

            Log.d(TAG, "onCreate: " + (location == null) + "..");
            if (location != null) {
                Log.d(TAG, "onCreate: location");
                //不为空,显示地理位置经纬度
                @SuppressLint("DefaultLocale")
                String locationStr = String.format("定位成功 \n%f \n%f", location.getLatitude(), location.getLongitude());
                Log.d(TAG, locationStr);
                mainText.setText(locationStr);
            } else {
                Log.d(TAG, "onCreate: location Null");
            }
            locationManager.removeUpdates(locationListener);
        }
    };


    void getLocationAmap() throws Exception {
        ApplicationInfo appInfo = super.getPackageManager().getApplicationInfo(getPackageName(),
                PackageManager.GET_META_DATA);

        AMapLocationClient.updatePrivacyShow(getApplicationContext(),true,true);
        AMapLocationClient.updatePrivacyAgree(getApplicationContext(),true);
        AMapLocationClient.setApiKey(appInfo.metaData.getString("com.amap.api.v2.apikey"));
        //声明AMapLocationClient类对象
        AMapLocationClient mLocationClient = null;
        //声明定位回调监听器
        AMapLocationListener mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        //可在其中解析amapLocation获取相应内容。
                        @SuppressLint("DefaultLocale")
                        String locationStr = String.format("定位成功 \n%f \n%f", aMapLocation.getLatitude(), aMapLocation.getLongitude());
                        Log.d(TAG, locationStr);
                        mainText.setText(locationStr);
                    } else {
                        //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError", "location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:"
                                + aMapLocation.getErrorInfo());
                    }
                }
            }
        };
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        //声明AMapLocationClientOption对象
        AMapLocationClientOption mLocationOption = null;
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();

        AMapLocationClientOption option = new AMapLocationClientOption();
        /**
         * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
         */
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        if (null != mLocationClient) {
            mLocationClient.setLocationOption(option);
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient.stopLocation();
            mLocationClient.startLocation();
        }

        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);

        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(true);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);


        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

    }

}
