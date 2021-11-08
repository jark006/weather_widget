package com.jark006.weather.utils;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by JARK006 on 2017/2/4/0004.
 */

public class NetworkUtils {

    public static String getData(String link) {
        try {
            URL url = new URL(link);
            Log.i(TAG, "NetworkUtils 1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);

            Log.i(TAG, "NetworkUtils 2");
            if (conn.getResponseCode() == 200) {  //返回码是200，网络正常
                InputStream is = conn.getInputStream();
                Log.i(TAG, "NetworkUtils 3");
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[204800];  //200kb
                Log.i(TAG, "NetworkUtils 4");
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                is.close();
                os.close();
                return os.toString();
            } else {
                //返回码不是200，网络异常
                Log.i(TAG, "返回码不是200，网络异常");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "NetworkUtils IOException IO异常");
        }
        Log.i(TAG, "NetworkUtils getData IO返回null");
        return null;
    }

}
