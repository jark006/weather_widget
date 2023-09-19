package io.github.jark006.weather.caiyun;

import java.util.List;

public class Caiyun {
    public String status;
    public String api_version;
    public String api_status;
    public String lang;
    public String unit;
    public int tzshift;
    public String timezone;
    public long server_time;
    public Result result;

    static public class Result {
        public Alert alert;
        public Realtime realtime;
        public Minutely minutely;
        public Hourly hourly;
        public Daily daily;

        static public class Alert {
            public String status;
            public List<Content> content;

            static public class Content {
                public String province;
                public String status;
                public String code;
                public String description;
                public String regionId;
                public String county;
                public long pubtimestamp;
                public String city;
                public String alertId;
                public String title;
                public String adcode;
                public String source;
                public String location;
                public String request_status;
            }
        }

        static public class Realtime {
            public String status;
            public float temperature;
            public float humidity;
            public float cloudrate;
            public String skycon;

            public float visibility;
            public float dswrf;
            public float pressure;
            public Air_quality air_quality;

            static public class Air_quality {
                public float pm25;
                public float pm10;
                public float o3;
                public float so2;
                public float no2;
                public float co;
                public Aqi aqi;
                public Description description;

                static public class Aqi {
                    public String chn;
                    public String usa;
                }

                static public class Description {
                    public String chn;
                    public String usa;
                }

            }

        }

        static public class Minutely {
            public String status;
            public String description;
        }

        static public class Hourly {
            public String status;
            public String description;
            public List<Temperature> temperature;

            static public class Temperature {
                public String datetime;
                public float value;
            }
        }

        static public class Daily {
            public String status;
            public List<Temperature> temperature;
            public List<Skycon> skycon;

            static public class Temperature {
                public String date;
                public float max;
                public float min;
                public float avg;
            }

            static public class Skycon {
                public String date;
                public String value;

            }
        }
    }
}
