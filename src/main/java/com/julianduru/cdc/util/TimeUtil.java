package com.julianduru.cdc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class TimeUtil {
    private static final Logger log = LoggerFactory.getLogger(TimeUtil.class);
    private static final String[] DATE_FORMATS = new String[]{"yyyy-MM-dd"};
    private static final String[] DATE_TIME_FORMATS = new String[]{"yyyy-MM-dd hh:mma"};
    private static final String[] ZONE_DATE_TIME_FORMATS = new String[]{"yyyy-MM-dd HH:mm:ss.SSSS Z", "yyyy-MM-dd HH:mm Z", "yyyy-MM-dd hh:mma Z", "yyyy-MM-dd HH:mmZ", "yyyy-MM-dd HH:mmX", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSSSSS"};
    public static final String DEFAULT_DATE_FORMAT;
    public static final String DEFAULT_DATE_TIME_FORMAT;
    public static final String DEFAULT_ZONE_DATE_TIME_FORMAT;
    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER;
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER;
    public static final DateTimeFormatter DEFAULT_ZONE_DATE_TIME_FORMATTER;

    public TimeUtil() {
    }

    public static Date ldtToDate(LocalDateTime ldt) {
        Assert.notNull(ldt, "LocalDateTime argument cannot be null");
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static ZonedDateTime zdtFromTimeStamp(Long timeStamp) {
        Assert.notNull(timeStamp, "TimeStamp argument cannot be null");
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), ZoneId.systemDefault());
    }

    public static ZonedDateTime parseZonedDateTime(String str) {
        String[] var1 = ZONE_DATE_TIME_FORMATS;
        int var2 = var1.length;
        int var3 = 0;

        while(var3 < var2) {
            String format = var1[var3];

            try {
                return ZonedDateTime.parse(str, DateTimeFormatter.ofPattern(format));
            } catch (DateTimeParseException var6) {
                log.warn(String.format("Unable to parse %s with format %s", str, format));
                ++var3;
            }
        }

        log.warn(String.format("Unable to parse ZoneDateTime string %s", str));
        return ZonedDateTime.of(parseLocalDateTime(str), ZoneId.systemDefault());
    }

    public static LocalDateTime parseLocalDateTime(String str) {
        String[] var1 = ZONE_DATE_TIME_FORMATS;
        int var2 = var1.length;
        int var3 = 0;

        while(var3 < var2) {
            String format = var1[var3];

            try {
                return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(format));
            } catch (DateTimeParseException var6) {
                log.warn(String.format("Unable to parse %s with format %s", str, format));
                ++var3;
            }
        }

        throw new IllegalArgumentException(String.format("Unable to parse date time string %s", str));
    }

    static {
        DEFAULT_DATE_FORMAT = DATE_FORMATS[0];
        DEFAULT_DATE_TIME_FORMAT = DATE_TIME_FORMATS[0];
        DEFAULT_ZONE_DATE_TIME_FORMAT = ZONE_DATE_TIME_FORMATS[0];
        DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
        DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
        DEFAULT_ZONE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_ZONE_DATE_TIME_FORMAT);
    }
}
