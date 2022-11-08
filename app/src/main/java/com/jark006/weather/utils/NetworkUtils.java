package com.jark006.weather.utils;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by JARK006 on 2022-5-22 01:59:13
 */

public class NetworkUtils {

    @Nullable
    public static String getDataxx(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        String res = null;
        try {
            Response response = okHttpClient.newCall(request).execute();
            assert response.body() != null;
            res =  response.body().string();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static String getData(String link) {
        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);

            int resCode = conn.getResponseCode();
            if (resCode == 200) {
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[204800];  //200kb
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                is.close();
                os.close();
                return os.toString();
            } else {
                Log.e(TAG,  String.format("返回码:%d，网络异常", resCode));
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "NetworkUtils IOException IO异常");
        }
        Log.e(TAG, "NetworkUtils getData IO返回null");
        return null;
    }

}
