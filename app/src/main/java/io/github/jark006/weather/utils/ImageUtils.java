package io.github.jark006.weather.utils;

import androidx.annotation.DrawableRes;

import io.github.jark006.weather.R;

public class ImageUtils {

    // 和风天气 icon https://dev.qweather.com/docs/resource/icons
    public static @DrawableRes int getWeatherIcon(int id) {
        return switch (id) {
            case 100 -> R.drawable.icon_100;
            case 101 -> R.drawable.icon_101;
            case 102 -> R.drawable.icon_102;
            case 103 -> R.drawable.icon_103;
            case 104 -> R.drawable.icon_104;
            case 150 -> R.drawable.icon_150;
            case 151 -> R.drawable.icon_151;
            case 152 -> R.drawable.icon_152;
            case 153 -> R.drawable.icon_153;
            case 300 -> R.drawable.icon_300;
            case 301 -> R.drawable.icon_301;
            case 302 -> R.drawable.icon_302;
            case 303 -> R.drawable.icon_303;
            case 304 -> R.drawable.icon_304;
            case 305 -> R.drawable.icon_305;
            case 306 -> R.drawable.icon_306;
            case 307 -> R.drawable.icon_307;
            case 308 -> R.drawable.icon_308;
            case 309 -> R.drawable.icon_309;
            case 310 -> R.drawable.icon_310;
            case 311 -> R.drawable.icon_311;
            case 312 -> R.drawable.icon_312;
            case 313 -> R.drawable.icon_313;
            case 314 -> R.drawable.icon_314;
            case 315 -> R.drawable.icon_315;
            case 316 -> R.drawable.icon_316;
            case 317 -> R.drawable.icon_317;
            case 318 -> R.drawable.icon_318;
            case 350 -> R.drawable.icon_350;
            case 351 -> R.drawable.icon_351;
            case 399 -> R.drawable.icon_399;
            case 400 -> R.drawable.icon_400;
            case 401 -> R.drawable.icon_401;
            case 402 -> R.drawable.icon_402;
            case 403 -> R.drawable.icon_403;
            case 404 -> R.drawable.icon_404;
            case 405 -> R.drawable.icon_405;
            case 406 -> R.drawable.icon_406;
            case 407 -> R.drawable.icon_407;
            case 408 -> R.drawable.icon_408;
            case 409 -> R.drawable.icon_409;
            case 410 -> R.drawable.icon_410;
            case 456 -> R.drawable.icon_456;
            case 457 -> R.drawable.icon_457;
            case 499 -> R.drawable.icon_499;
            case 500 -> R.drawable.icon_500;
            case 501 -> R.drawable.icon_501;
            case 502 -> R.drawable.icon_502;
            case 503 -> R.drawable.icon_503;
            case 504 -> R.drawable.icon_504;
            case 507 -> R.drawable.icon_507;
            case 508 -> R.drawable.icon_508;
            case 509 -> R.drawable.icon_509;
            case 510 -> R.drawable.icon_510;
            case 511 -> R.drawable.icon_511;
            case 512 -> R.drawable.icon_512;
            case 513 -> R.drawable.icon_513;
            case 514 -> R.drawable.icon_514;
            case 515 -> R.drawable.icon_515;
            case 800 -> R.drawable.icon_800;
            case 801 -> R.drawable.icon_801;
            case 802 -> R.drawable.icon_802;
            case 803 -> R.drawable.icon_803;
            case 804 -> R.drawable.icon_804;
            case 805 -> R.drawable.icon_805;
            case 806 -> R.drawable.icon_806;
            case 807 -> R.drawable.icon_807;
            case 900 -> R.drawable.icon_900;
            case 901 -> R.drawable.icon_901;
            case 999 -> R.drawable.icon_999;
            default -> R.drawable.ic_sunny;
        };
    }

    // 和风天气 icon https://dev.qweather.com/docs/resource/icons
    public static @DrawableRes int getBgResourceId(int icon, boolean isDay) {
        return switch (icon) {
            case 150 -> R.drawable.bg_widget_sunny_night; //晴
            case 101 -> R.drawable.bg_widget_overcast;//多云
            case 151 -> R.drawable.bg_widget_overcast_night;//少云

            //晴间多云
            //阴
            //少云
            case 102, 103, 104 ->
                    R.drawable.bg_widget_cloudy;

            //晴间多云
            // 小雨
            case 152, 153 ->
                    R.drawable.bg_widget_cloudy_night;

            // 毛毛雨/细雨
            // 小到中雨
            // 阵雨
            case 305, 309, 314 ->
                    isDay ? R.drawable.bg_widget_drizzle : R.drawable.bg_widget_drizzle_night;

            // 强阵雨
            // 雷阵雨
            // 阵雨
            // 中雨
            case 300, 301, 302, 350 ->
                    isDay ? R.drawable.bg_widget_shower : R.drawable.bg_widget_shower_night;
            // 冻雨
            // 中到大雨
            // 雨
            // 强雷阵雨
            case 306, 313, 315, 399 ->
                    isDay ? R.drawable.bg_widget_rain : R.drawable.bg_widget_rain_night;

            // 雷阵雨伴有冰雹
            // 大到暴雨
            // 强阵雨
            // 大雨
            case 303, 304, 316, 351 ->
                    isDay ? R.drawable.bg_widget_downpour : R.drawable.bg_widget_downpour_night;

            // 极端降雨
            // 暴雨
            // 大暴雨
            // 特大暴雨
            // 暴雨到大暴雨
            // 大暴雨到特大暴雨
            // 雨夹雪
            case 307, 308, 310, 311, 312, 317, 318 ->
                    isDay ? R.drawable.bg_widget_rainstorm : R.drawable.bg_widget_rainstorm_night;

            // 雨雪天气
            // 阵雨夹雪
            // 阵雨夹雪
            // 小雪
            case 404, 405, 406, 456 ->
                    isDay ? R.drawable.bg_widget_sleet : R.drawable.bg_widget_sleet_night;

            // 中雪
            // 大雪
            // 暴雪
            // 阵雪
            // 小到中雪
            // 中到大雪
            // 大到暴雪
            // 阵雪
            // 雪
            // 冷
            // 薄雾
            case 400, 401, 402, 403, 407, 408, 409, 410, 457, 499, 901 ->
                    isDay ? R.drawable.bg_widget_snow : R.drawable.bg_widget_snow_night;
            // 雾
            // 浓雾
            // 强浓雾
            // 大雾
            // 特强浓雾
            // 扬沙
            case 500, 501, 509, 510, 514, 515 ->
                    isDay ? R.drawable.bg_widget_fog : R.drawable.bg_widget_fog_night;
            // 浮尘
            // 沙尘暴
            // 强沙尘暴
            // 霾
            case 503, 504, 507, 508 ->
                    isDay ? R.drawable.bg_widget_sandstorm : R.drawable.bg_widget_sandstorm_night;
            // 中度霾
            // 重度霾
            // 严重霾
            case 502, 511, 512, 513 ->
                    isDay ? R.drawable.bg_widget_haze : R.drawable.bg_widget_haze_night;
            default -> R.drawable.bg_widget_sunny;
        };
    }

    public static int getSkyconIconCaiyun(String weather) {
        return switch (weather) {
            // 多云天气
            case "PARTLY_CLOUDY_DAY", "PARTLY_CLOUDY_NIGHT" -> R.drawable.ic_cloud;

            // 阴天
            case "CLOUDY" -> R.drawable.ic_nosun;

            // 雾霾和雾
            case "HAZE", "LIGHT_HAZE", "MODERATE_HAZE", "HEAVY_HAZE", "FOG" -> R.drawable.ic_haze;

            // 各类降雨
            case "RAIN", "LIGHT_RAIN", "MODERATE_RAIN", "HEAVY_RAIN", "STORM_RAIN" ->
                    R.drawable.ic_rain;

            // 各类降雪
            case "SNOW", "LIGHT_SNOW", "MODERATE_SNOW", "HEAVY_SNOW", "STORM_SNOW" ->
                    R.drawable.ic_snow;

            // 沙尘天气
            case "DUST", "SAND" -> R.drawable.ic_sand;

            // 大风
            case "WIND" -> R.drawable.ic_wind;

            // 默认晴天（包括CLEAR_DAY/CLEAR_NIGHT）
            default -> R.drawable.ic_sunny;
        };
    }

    public static int getBgResourceIdCaiyun(String weather, boolean isDay) {
        return switch (weather) {
            case "CLEAR_DAY" -> R.drawable.bg_widget_sunny;
            case "CLEAR_NIGHT" -> R.drawable.bg_widget_sunny_night;
            case "PARTLY_CLOUDY_DAY" -> R.drawable.bg_widget_cloudy;
            case "PARTLY_CLOUDY_NIGHT" -> R.drawable.bg_widget_cloudy_night;
            case "CLOUDY" ->
                    isDay ? R.drawable.bg_widget_overcast : R.drawable.bg_widget_overcast_night;
            case "LIGHT_HAZE", "MODERATE_HAZE", "HEAVY_HAZE" ->
                    isDay ? R.drawable.bg_widget_haze : R.drawable.bg_widget_haze_night;
            case "LIGHT_RAIN" ->
                    isDay ? R.drawable.bg_widget_drizzle : R.drawable.bg_widget_drizzle_night;
            case "MODERATE_RAIN" ->
                    isDay ? R.drawable.bg_widget_rain : R.drawable.bg_widget_rain_night;
            case "HEAVY_RAIN" ->
                    isDay ? R.drawable.bg_widget_downpour : R.drawable.bg_widget_downpour_night;
            case "STORM_RAIN" ->
                    isDay ? R.drawable.bg_widget_rainstorm : R.drawable.bg_widget_rainstorm_night;
            case "FOG" -> isDay ? R.drawable.bg_widget_fog : R.drawable.bg_widget_fog_night;
            case "LIGHT_SNOW", "MODERATE_SNOW", "HEAVY_SNOW", "STORM_SNOW" ->
                    isDay ? R.drawable.bg_widget_snow : R.drawable.bg_widget_snow_night;
            case "DUST", "WIND" ->
                    isDay ? R.drawable.bg_widget_sandstorm : R.drawable.bg_widget_sandstorm_night;
            default -> isDay ? R.drawable.bg_widget_sunny : R.drawable.bg_widget_sunny_night;
        };
    }
}
