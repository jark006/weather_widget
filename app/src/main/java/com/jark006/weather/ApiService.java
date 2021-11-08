package com.jark006.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.jark006.weather.bean.WeatherBean;
import com.jark006.weather.district.district;

public interface ApiService {

    @GET("{longitude},{latitude}/weather.json?dailysteps=6")
    Call<WeatherBean> weather(@Path("longitude") double longitude, @Path("latitude") double latitude);

//    @GET("{longitude},{latitude}/weather.json?dailysteps=6")
//    Call<district> getD(@Path("longitude") double longitude, @Path("latitude") double latitude);

//    @GET("{longitude},{latitude}/weather.json?dailysteps=6")
    @GET("regeo")//https://restapi.amap.com/v3/geocode/regeo?key=c2844d38363cae2a8a52eb9fa18a2ebc&location=113.381268,23.0390333
    Call<district> getD(@Query("key") String key, @Query("location") String location);

}