package org.keycloak.services.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

/**
 * 日期/时间戳解析工具类。
 * <p>支持 ISO-8601 本地日期与毫秒 epoch 时间戳互转。</p>
 */
public class DateUtil {

    /**
     * 将日期字符串解析为 UTC 当天起始时刻的 epoch 毫秒数。
     * <p>含 '-' 则按 ISO-8601 本地日期解析，否则按 long 毫秒戳解析。</p>
     *
     * @param date ISO-8601 扩展本地日期或毫秒时间戳字符串
     * @return 当天 00:00:00 UTC 的 epoch 毫秒
     */
    public static long toStartOfDay(String date) {
        if (date.indexOf('-') != -1) {
            return LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        } else {
            return Long.parseLong(date);
        }
    }

    /**
     * 将日期字符串解析为 UTC 当天结束时刻的 epoch 毫秒数。
     * <p>含 '-' 则按 ISO-8601 本地日期解析，否则按 long 毫秒戳解析。</p>
     *
     * @param date ISO-8601 扩展本地日期或毫秒时间戳字符串
     * @return 当天 23:59:59.999 UTC 的 epoch 毫秒
     */
    public static long toEndOfDay(String date) {
        if (date.indexOf('-') != -1) {
            return LocalDate.parse(date).atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC).toEpochMilli();
        } else {
            return Long.parseLong(date);
        }
    }

}
