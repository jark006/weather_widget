package io.github.jark006.weather.bean;

import java.util.List;

public class Hourly {
    String status;
    public String description;
    List<StringValue> skycon;
    List<DoubleValue> cloudrate;
    List<IntValue> aqi;
    List<DoubleValue> dswrf;
    List<DoubleValue> visibility;
    List<DoubleValue> humidity;
    List<DoubleValue> pres;
    List<IntValue> pm25;
    List<DoubleValue> precipitation;
    List<WindHourly> wind;
    public List<DoubleValue> temperature;
}

