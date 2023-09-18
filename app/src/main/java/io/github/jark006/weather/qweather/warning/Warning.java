package io.github.jark006.weather.qweather.warning;

import java.util.List;

public class Warning {
    public String code;
    public String updateTime;
    public String fxLink;
    public List<WarningItem> warning;

    static public class WarningItem {
        public String id;
        public String sender;
        public String pubTime;
        public String title;
        public String startTime;
        public String endTime;
        public String status;

        @Deprecated
        public String level; // 已弃用

        public String severity;
        public String severityColor;
        public String type;
        public String typeName;
        public String urgency;
        public String certainty;
        public String text;
        public String related;
    }
}
