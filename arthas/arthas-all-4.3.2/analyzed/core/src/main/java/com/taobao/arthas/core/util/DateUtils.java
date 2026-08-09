package com.taobao.arthas.core.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间格式化工具，供 dashboard 等命令展示当前时间与 JVM 启动时间。
 *
 * @author diecui1202 on 2017/10/25.
 */
public final class DateUtils {

    private DateUtils() {
        throw new AssertionError();
    }

    /** 统一时间格式：{@code yyyy-MM-dd HH:mm:ss.SSS} */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /** @return 当前本地时间的格式化字符串 */
    public static String getCurrentDateTime() {
        return DATE_TIME_FORMATTER.format(LocalDateTime.now());
    }

    /**
     * 将指定 {@link LocalDateTime} 格式化为标准字符串。
     *
     * @param dateTime 待格式化时间
     * @return 格式化结果
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return DATE_TIME_FORMATTER.format(dateTime);
    }

    /**
     * 获取 JVM 启动时刻的本地时间字符串。
     * <p>
     * 通过 {@link RuntimeMXBean#getStartTime()} 读取；异常时返回 {@code "unknown"}。
     *
     * @return JVM 启动时间或 unknown
     */
    public static String getStartDateTime() {
        try {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            long startTime = runtimeMXBean.getStartTime();
            Instant startInstant = Instant.ofEpochMilli(startTime);
            LocalDateTime startDateTime = LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault());
            return DATE_TIME_FORMATTER.format(startDateTime);
        } catch (Throwable e) {
            return "unknown";
        }
    }
}
