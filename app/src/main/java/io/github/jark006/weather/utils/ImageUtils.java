package io.github.jark006.weather.utils;

import androidx.annotation.DrawableRes;

import io.github.jark006.weather.R;

public class ImageUtils {

    // 和风天气 icon https://dev.qweather.com/docs/resource/icons
    public static @DrawableRes int getWeatherIcon(int id) {
        switch (id) {
            case 100: return R.drawable.icon_100;
            case 101: return R.drawable.icon_101;
            case 102: return R.drawable.icon_102;
            case 103: return R.drawable.icon_103;
            case 104: return R.drawable.icon_104;
            case 150: return R.drawable.icon_150;
            case 151: return R.drawable.icon_151;
            case 152: return R.drawable.icon_152;
            case 153: return R.drawable.icon_153;
            case 300: return R.drawable.icon_300;
            case 301: return R.drawable.icon_301;
            case 302: return R.drawable.icon_302;
            case 303: return R.drawable.icon_303;
            case 304: return R.drawable.icon_304;
            case 305: return R.drawable.icon_305;
            case 306: return R.drawable.icon_306;
            case 307: return R.drawable.icon_307;
            case 308: return R.drawable.icon_308;
            case 309: return R.drawable.icon_309;
            case 310: return R.drawable.icon_310;
            case 311: return R.drawable.icon_311;
            case 312: return R.drawable.icon_312;
            case 313: return R.drawable.icon_313;
            case 314: return R.drawable.icon_314;
            case 315: return R.drawable.icon_315;
            case 316: return R.drawable.icon_316;
            case 317: return R.drawable.icon_317;
            case 318: return R.drawable.icon_318;
            case 350: return R.drawable.icon_350;
            case 351: return R.drawable.icon_351;
            case 399: return R.drawable.icon_399;
            case 400: return R.drawable.icon_400;
            case 401: return R.drawable.icon_401;
            case 402: return R.drawable.icon_402;
            case 403: return R.drawable.icon_403;
            case 404: return R.drawable.icon_404;
            case 405: return R.drawable.icon_405;
            case 406: return R.drawable.icon_406;
            case 407: return R.drawable.icon_407;
            case 408: return R.drawable.icon_408;
            case 409: return R.drawable.icon_409;
            case 410: return R.drawable.icon_410;
            case 456: return R.drawable.icon_456;
            case 457: return R.drawable.icon_457;
            case 499: return R.drawable.icon_499;
            case 500: return R.drawable.icon_500;
            case 501: return R.drawable.icon_501;
            case 502: return R.drawable.icon_502;
            case 503: return R.drawable.icon_503;
            case 504: return R.drawable.icon_504;
            case 507: return R.drawable.icon_507;
            case 508: return R.drawable.icon_508;
            case 509: return R.drawable.icon_509;
            case 510: return R.drawable.icon_510;
            case 511: return R.drawable.icon_511;
            case 512: return R.drawable.icon_512;
            case 513: return R.drawable.icon_513;
            case 514: return R.drawable.icon_514;
            case 515: return R.drawable.icon_515;
            case 800: return R.drawable.icon_800;
            case 801: return R.drawable.icon_801;
            case 802: return R.drawable.icon_802;
            case 803: return R.drawable.icon_803;
            case 804: return R.drawable.icon_804;
            case 805: return R.drawable.icon_805;
            case 806: return R.drawable.icon_806;
            case 807: return R.drawable.icon_807;
            case 900: return R.drawable.icon_900;
            case 901: return R.drawable.icon_901;
            case 999: return R.drawable.icon_999;
        }
        return R.drawable.ic_sunny;
    }

    // 和风天气 icon https://dev.qweather.com/docs/resource/icons
    public static @DrawableRes int getBgResourceId(int icon, boolean isDay) {
        switch (icon) {
            case 900: // 热
            case 100:
                return R.drawable.bg_widget_sunny; //晴
            case 150:
                return R.drawable.bg_widget_sunny_night;

            case 101:
                return R.drawable.bg_widget_overcast;//多云
            case 151:
                return R.drawable.bg_widget_overcast_night;

            case 102://少云
            case 103://晴间多云
            case 104://阴
                return R.drawable.bg_widget_cloudy;

            case 152://少云
            case 153://晴间多云
                return R.drawable.bg_widget_cloudy_night;

            case 305: // 小雨
            case 309: // 毛毛雨/细雨
            case 314: // 小到中雨
                return isDay ? R.drawable.bg_widget_drizzle : R.drawable.bg_widget_drizzle_night;

            case 300: // 阵雨
            case 301: // 强阵雨
            case 302: // 雷阵雨
            case 350: // 阵雨
                return isDay ? R.drawable.bg_widget_shower : R.drawable.bg_widget_shower_night;

            case 306: // 中雨
            case 313: // 冻雨
            case 315: // 中到大雨
            case 399: // 雨
                return isDay ? R.drawable.bg_widget_rain : R.drawable.bg_widget_rain_night;

            case 303: // 强雷阵雨
            case 304: // 雷阵雨伴有冰雹
            case 316: // 大到暴雨
            case 351: // 强阵雨
                return isDay ? R.drawable.bg_widget_downpour : R.drawable.bg_widget_downpour_night;

            case 307: // 大雨
            case 308: // 极端降雨
            case 310: // 暴雨
            case 311: // 大暴雨
            case 312: // 特大暴雨
            case 317: // 暴雨到大暴雨
            case 318: // 大暴雨到特大暴雨
                return isDay ? R.drawable.bg_widget_rainstorm : R.drawable.bg_widget_rainstorm_night;

            case 404: // 雨夹雪
            case 405: // 雨雪天气
            case 406: // 阵雨夹雪
            case 456: // 阵雨夹雪
                return isDay ? R.drawable.bg_widget_sleet : R.drawable.bg_widget_sleet_night;

            case 400: // 小雪
            case 401: // 中雪
            case 402: // 大雪
            case 403: // 暴雪
            case 407: // 阵雪
            case 408: // 小到中雪
            case 409: // 中到大雪
            case 410: // 大到暴雪
            case 457: // 阵雪
            case 499: // 雪
            case 901: // 冷
                return isDay ? R.drawable.bg_widget_snow : R.drawable.bg_widget_snow_night;

            case 500: // 薄雾
            case 501: // 雾
            case 509: // 浓雾
            case 510: // 强浓雾
            case 514: // 大雾
            case 515: // 特强浓雾
                return isDay ? R.drawable.bg_widget_fog : R.drawable.bg_widget_fog_night;

            case 503: // 扬沙
            case 504: // 浮尘
            case 507: // 沙尘暴
            case 508: // 强沙尘暴
                return isDay ? R.drawable.bg_widget_sandstorm : R.drawable.bg_widget_sandstorm_night;

            case 502: // 霾
            case 511: // 中度霾
            case 512: // 重度霾
            case 513: // 严重霾
                return isDay ? R.drawable.bg_widget_haze : R.drawable.bg_widget_haze_night;
        }
        return R.drawable.bg_widget_sunny;
    }

    public static int getSkyconIconCaiyun(String weather) {
        switch (weather) {
            case "PARTLY_CLOUDY_DAY":
            case "PARTLY_CLOUDY_NIGHT":
                return R.drawable.ic_cloud;
            case "CLOUDY":
                return R.drawable.ic_nosun;
            case "WIND":
                return R.drawable.ic_wind;
            case "HAZE":
                return R.drawable.ic_haze;
            case "RAIN":
                return R.drawable.ic_rain;
            case "SNOW":
                return R.drawable.ic_snow;
            default: // "CLEAR_DAY" "CLEAR_NIGHT":
                return R.drawable.ic_sunny;
        }
    }

    public static int getBgResourceIdCaiyun(String weather, boolean isDay) {
        switch (weather) {
            case "CLEAR_DAY":
                return R.drawable.bg_widget_sunny;
            case "CLEAR_NIGHT":
                return R.drawable.bg_widget_sunny_night;
            case "PARTLY_CLOUDY_DAY":
                return R.drawable.bg_widget_cloudy;
            case "PARTLY_CLOUDY_NIGHT":
                return R.drawable.bg_widget_cloudy_night;
            case "CLOUDY":
                return isDay ? R.drawable.bg_widget_overcast : R.drawable.bg_widget_overcast_night;
            case "LIGHT_HAZE":
            case "MODERATE_HAZE":
            case "HEAVY_HAZE":
                return isDay ? R.drawable.bg_widget_haze : R.drawable.bg_widget_haze_night;
            case "LIGHT_RAIN":
                return isDay ? R.drawable.bg_widget_drizzle : R.drawable.bg_widget_drizzle_night;
            case "MODERATE_RAIN":
                return isDay ? R.drawable.bg_widget_rain : R.drawable.bg_widget_rain_night;
            case "HEAVY_RAIN":
                return isDay ? R.drawable.bg_widget_downpour : R.drawable.bg_widget_downpour_night;
            case "STORM_RAIN":
                return isDay ? R.drawable.bg_widget_rainstorm : R.drawable.bg_widget_rainstorm_night;
            case "FOG":
                return isDay ? R.drawable.bg_widget_fog : R.drawable.bg_widget_fog_night;
            case "LIGHT_SNOW":
            case "MODERATE_SNOW":
            case "HEAVY_SNOW":
            case "STORM_SNOW":
                return isDay ? R.drawable.bg_widget_snow : R.drawable.bg_widget_snow_night;
            case "DUST":
            case "WIND":
                return isDay ? R.drawable.bg_widget_sandstorm : R.drawable.bg_widget_sandstorm_night;
            default:
                return isDay ? R.drawable.bg_widget_sunny : R.drawable.bg_widget_sunny_night;
        }
    }
}
