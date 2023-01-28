package io.github.jark006.weather.bean;

import java.util.List;

public class Minutely {
    String status;
    public String description;
    List<Double> probability;
    String datasource;
    List<Double> precipitation_2h;
    List<Double> precipitation;
}