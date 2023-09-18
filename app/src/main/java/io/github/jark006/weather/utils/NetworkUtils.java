package io.github.jark006.weather.utils;

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
    public static String getDataOkHttp(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder().url(url).build();
        String res = null;
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.body() != null)
                res = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static String getData(String link) {
        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2 * 1000);

            int resCode = conn.getResponseCode();
            if (resCode == 200) {
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[1024 * 200];  // 200 KiB
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                is.close();
                os.close();
                return os.toString();
            } else {
                Log.e(TAG, String.format("NetworkUtils 返回码:%d 异常", resCode));
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "NetworkUtils IOException IO异常");
        }
        return null;
    }

}
