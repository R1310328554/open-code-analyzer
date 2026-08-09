/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api;

import org.redisson.executor.CronExpression;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * 用于 {@link RScheduledExecutorService} 的 Cron 表达式对象。
 * <p>与 Quartz Cron 表达式完全兼容。
 *
 * @see RScheduledExecutorService#schedule(Runnable, CronSchedule)
 * @author Nikita Koksharov
 */
public final class CronSchedule {

    private final CronExpression expression;
    private final ZoneId zoneId;

    CronSchedule(CronExpression expression, ZoneId zoneId) {
        super();
        this.expression = expression;
        this.zoneId = zoneId;
    }

    /**
     * 根据 Cron 表达式字符串创建实例（使用系统默认时区）。
     *
     * @param expression Cron 表达式
     * @return CronSchedule 实例
     * @throws IllegalArgumentException 表达式无效时包装 {@code ParseException}
     */
    public static CronSchedule of(String expression) {
        return of(expression, ZoneId.systemDefault());
    }

    /**
     * 根据 Cron 表达式与时区 ID 创建实例。
     *
     * @param expression Cron 表达式
     * @param zoneId 时区 ID
     * @return CronSchedule 实例
     * @throws IllegalArgumentException 表达式无效时包装 {@code ParseException}
     */
    public static CronSchedule of(String expression, ZoneId zoneId) {
        CronExpression ce = new CronExpression(expression);
        ce.setTimeZone(TimeZone.getTimeZone(zoneId));
        return new CronSchedule(ce, zoneId);
    }

    /**
     * 创建每天在指定时刻执行任务的 Cron 表达式（系统默认时区）。
     *
     * @param hour 小时（0–23）
     * @param minute 分钟（0–59）
     * @return CronSchedule 实例
     * @throws IllegalArgumentException 表达式无效时包装 {@code ParseException}
     */
    public static CronSchedule dailyAtHourAndMinute(int hour, int minute) {
        String expression = String.format("0 %d %d ? * *", minute, hour);
        return of(expression);
    }

    /**
     * 创建每天在指定时刻、指定时区执行任务的 Cron 表达式。
     *
     * @param hour 小时（0–23）
     * @param minute 分钟（0–59）
     * @param zoneId 时区 ID
     * @return CronSchedule 实例
     * @throws IllegalArgumentException 表达式无效时包装 {@code ParseException}
     */
    public static CronSchedule dailyAtHourAndMinute(int hour, int minute, ZoneId zoneId) {
        String expression = String.format("0 %d %d ? * *", minute, hour);
        return of(expression, zoneId);
    }

    /**
     * 创建在指定星期几、指定时刻执行任务的 Cron 表达式（系统默认时区）。
     * <p>使用 {@link java.util.Calendar} 常量表示星期。
     *
     * @param hour 小时（0–23）
     * @param minute 分钟（0–59）
     * @param daysOfWeek {@link java.util.Calendar} 星期常量
     * @return CronSchedule 实例
     */
    public static CronSchedule weeklyOnDayAndHourAndMinute(int hour, int minute, Integer... daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.length == 0) {
            throw new IllegalArgumentException("You must specify at least one day of week.");
        }

        String expression = String.format("0 %d %d ? * %d", minute, hour, daysOfWeek[0]);
        for (int i = 1; i < daysOfWeek.length; i++) {
            expression = expression + "," + daysOfWeek[i];
        }

        return of(expression);
    }

    /**
     * 创建在指定星期几、指定时刻与指定时区执行任务的 Cron 表达式。
     * <p>使用 {@link java.util.Calendar} 常量表示星期。
     *
     * @param hour 小时（0–23）
     * @param minute 分钟（0–59）
     * @param zoneId 时区 ID
     * @param daysOfWeek {@link java.util.Calendar} 星期常量
     * @return CronSchedule 实例
     */
    public static CronSchedule weeklyOnDayAndHourAndMinute(int hour, int minute, ZoneId zoneId, Integer... daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.length == 0) {
            throw new IllegalArgumentException("You must specify at least one day of week.");
        }

        String expression = String.format("0 %d %d ? * %d", minute, hour, daysOfWeek[0]);
        for (int i = 1; i < daysOfWeek.length; i++) {
            expression = expression + "," + daysOfWeek[i];
        }

        return of(expression, zoneId);
    }

    /**
     * 创建在每月指定日期、指定时刻执行任务的 Cron 表达式（系统默认时区）。
     *
     * @param dayOfMonth 月中日期（1–31）
     * @param hour 小时（0–23）
     * @param minute 分钟（0–59）
     * @return CronSchedule 实例
     */
    public static CronSchedule monthlyOnDayAndHourAndMinute(int dayOfMonth, int hour, int minute) {
        String expression = String.format("0 %d %d %d * ?", minute, hour, dayOfMonth);
        return of(expression);
    }

    /**
     * 创建在每月指定日期、指定时刻与指定时区执行任务的 Cron 表达式。
     *
     * @param dayOfMonth 月中日期（1–31）
     * @param hour 小时（0–23）
     * @param minute 分钟（0–59）
     * @param zoneId 时区 ID
     * @return CronSchedule 实例
     */
    public static CronSchedule monthlyOnDayAndHourAndMinute(int dayOfMonth, int hour, int minute, ZoneId zoneId) {
        String expression = String.format("0 %d %d %d * ?", minute, hour, dayOfMonth);
        return of(expression, zoneId);
    }

    public CronExpression getExpression() {
        return expression;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }
}

