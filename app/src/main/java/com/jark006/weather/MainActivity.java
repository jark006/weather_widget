package com.jark006.weather;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.jark006.weather.utils.PermissionUtil;


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
            mainText.setText("没有永久定位权限\n请到设置里开启应用的定位权限\n位置权限设为 [ 始终允许 ]");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 2);

            bt.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", this.getPackageName(), null));
                startActivity(intent);
            });
        }
    }
}
