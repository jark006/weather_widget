package io.github.jark006.weather.utils;

import io.github.jark006.weather.R;

public class ImageUtils {
    public static int getWeatherIcon(String weather) {
        int iconId;
        switch (weather) {
            case "PARTLY_CLOUDY_DAY":
            case "PARTLY_CLOUDY_NIGHT":
                iconId = R.drawable.ic_cloud;
                break;
            case "CLOUDY":
                iconId = R.drawable.ic_nosun;
                break;
            case "WIND":
                iconId = R.drawable.ic_wind;
                break;
            case "HAZE":
                iconId = R.drawable.ic_haze;
                break;
            case "RAIN":
                iconId = R.drawable.ic_rain;
                break;
            case "SNOW":
                iconId = R.drawable.ic_snow;
                break;
            default: // "CLEAR_DAY" "CLEAR_NIGHT":
                iconId = R.drawable.ic_sunny;
                break;
        }
        return iconId;
    }


    public static int getBgResourceId(String weather, double intensity, boolean isDay) {
        int bgResId;
        switch (weather) {
            case "CLEAR_DAY":
                bgResId = R.drawable.bg_widget_sunny;
                break;
            case "CLEAR_NIGHT":
                bgResId = R.drawable.bg_widget_sunny_night;
                break;
            case "PARTLY_CLOUDY_DAY":
                bgResId = R.drawable.bg_widget_cloudy;
                break;
            case "PARTLY_CLOUDY_NIGHT":
                bgResId = R.drawable.bg_widget_cloudy_night;
                break;
            case "CLOUDY":
                bgResId = isDay ? R.drawable.bg_widget_overcast : R.drawable.bg_widget_overcast_night;
                break;
            case "LIGHT_HAZE":
            case "MODERATE_HAZE":
            case "HEAVY_HAZE":
                bgResId = isDay ? R.drawable.bg_widget_haze : R.drawable.bg_widget_haze_night;
                break;
            case "LIGHT_RAIN":
                bgResId = isDay ? R.drawable.bg_widget_drizzle : R.drawable.bg_widget_drizzle_night;
                break;
            case "MODERATE_RAIN":
                bgResId = isDay ? R.drawable.bg_widget_rain : R.drawable.bg_widget_rain_night;
                break;
            case "HEAVY_RAIN":
                bgResId = isDay ? R.drawable.bg_widget_downpour : R.drawable.bg_widget_downpour_night;
                break;
            case "STORM_RAIN":
                bgResId = isDay ? R.drawable.bg_widget_rainstorm : R.drawable.bg_widget_rainstorm_night;
                break;
            case "FOG":
                bgResId = isDay ? R.drawable.bg_widget_fog:R.drawable.bg_widget_fog_night;
                break;
            case "LIGHT_SNOW":
            case "MODERATE_SNOW":
            case "HEAVY_SNOW":
            case "STORM_SNOW":
                bgResId = isDay ? R.drawable.bg_widget_snow:R.drawable.bg_widget_snow_night;
                break;
            case "DUST":
            case "WIND":
                bgResId = isDay ? R.drawable.bg_widget_sandstorm : R.drawable.bg_widget_sandstorm_night;
                break;
            default:
                bgResId = isDay ? R.drawable.bg_widget_sunny:R.drawable.bg_widget_sunny_night;
                break;
        }
        return bgResId;
    }
}
