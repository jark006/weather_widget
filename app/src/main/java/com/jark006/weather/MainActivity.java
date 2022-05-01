package com.jark006.weather;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.jark006.weather.utils.PermissionUtil;

import java.util.List;


public class MainActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView mainText = findViewById(R.id.mainText);
        Button bt = findViewById(R.id.button);
        if(PermissionUtil.isOwnPermisson(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                PermissionUtil.isOwnPermisson(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
            mainText.setText("定位权限已获取\n请到桌面添加小部件");
            bt.setVisibility(View.GONE);
        }else {
            mainText.setText("没有永久定位权限，请到设置里开启应用的定位权限");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 2);

            bt.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", this.getPackageName(), null));
                startActivity(intent);
            });
        }
    }
}
