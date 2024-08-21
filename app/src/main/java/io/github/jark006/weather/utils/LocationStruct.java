package io.github.jark006.weather.utils;

import java.io.Serializable;

public class LocationStruct implements Serializable {
    public String address = "";
    public String cityName = "";
    public String districtName = "";
    public String adCode = "";
    public double latitude = 0;
    public double longitude = 0;
    public long updateTime = 0;
}
