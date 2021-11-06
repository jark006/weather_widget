package com.jark006.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import com.jark006.weather.bean.WeatherBean;

public interface ApiService {

    @GET("{longitude},{latitude}/weather.json?dailysteps=6")
    Call<WeatherBean> weather(@Path("longitude") double longitude, @Path("latitude") double latitude);
}