package io.github.jark006.weather;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.github.jark006.weather.utils.LocationStruct;
import io.github.jark006.weather.utils.Utils;

public class SettingsActivity extends AppCompatActivity {
    EditText longitudeEdit, latitudeEdit, areaEdit;
    TextView tipsTextViewer;
    Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        longitudeEdit = findViewById(R.id.longitudeEdit);
        latitudeEdit = findViewById(R.id.latitudeEdit);
        areaEdit = findViewById(R.id.areaEdit);
        saveButton = findViewById(R.id.saveButton);
        tipsTextViewer = findViewById(R.id.tipsTextViewer);

        saveButton.setOnClickListener(v -> {
            try {
                double longitude = Double.parseDouble(longitudeEdit.getText().toString());
                double latitude = Double.parseDouble(latitudeEdit.getText().toString());
                String area = areaEdit.getText().toString();

                if (longitude < -180 || longitude > 180) {
                    Toast.makeText(this, "数值错误，经度数值需在[-180 ~ 180]", Toast.LENGTH_LONG).show();
                    return;
                }
                if (latitude < -90 || latitude > 90) {
                    Toast.makeText(this, "数值错误，纬度数值需在[-90 ~ 90]", Toast.LENGTH_LONG).show();
                    return;
                }
                if (area.isEmpty()) {
                    area = "本地";
                    Toast.makeText(this, "地区名默认为：本地", Toast.LENGTH_LONG).show();
                }

                var locationStruct = new LocationStruct();
                locationStruct.longitude = longitude;
                locationStruct.latitude = latitude;
                locationStruct.cityName = area;
                locationStruct.districtName = area;
                locationStruct.address = area;
                locationStruct.updateTime = System.currentTimeMillis();

                Utils.saveObj(getApplicationContext(), "locationStruct", locationStruct);
                Toast.makeText(this, "保存成功", Toast.LENGTH_LONG).show();

                Utils.createNotificationChannel(SettingsActivity.this, this);
            } catch (Exception e) {
                Toast.makeText(this, "处理错误: " + e, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        LocationStruct locationStruct = (LocationStruct) Utils.readObj(getApplicationContext(), "locationStruct");
        if (locationStruct == null || locationStruct.updateTime <= 0) {
            tipsTextViewer.setText("暂无位置信息，请编辑并保存");
        } else {
            longitudeEdit.setText(String.valueOf(locationStruct.longitude));
            latitudeEdit.setText(String.valueOf(locationStruct.latitude));
            areaEdit.setText((!locationStruct.districtName.isEmpty()) ?
                    locationStruct.districtName : locationStruct.cityName);
        }
    }


}