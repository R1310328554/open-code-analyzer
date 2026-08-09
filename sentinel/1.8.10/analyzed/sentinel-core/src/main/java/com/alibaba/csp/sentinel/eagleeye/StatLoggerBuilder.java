/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.eagleeye;

import java.util.concurrent.TimeUnit;

/**
 * {@link StatLogger} 的构建器，用于配置统计日志的滚动间隔、条目上限、分隔符与输出 Appender。
 *
 * @author jifeng
 */
public final class StatLoggerBuilder extends BaseLoggerBuilder<StatLoggerBuilder> {

    private int intervalSeconds = 60;

    private int maxEntryCount = 20000;

    private char keyDelimiter = ',';

    private char valueDelimiter = ',';

    private EagleEyeAppender appender = null;

    StatLoggerBuilder(String loggerName) {
        super(loggerName);
    }

    public StatLoggerBuilder intervalSeconds(int intervalSeconds) {
        validateInterval(intervalSeconds);
        this.intervalSeconds = intervalSeconds;
        return this;
    }

    public StatLoggerBuilder maxEntryCount(int maxEntryCount) {
        if (maxEntryCount < 1) {
            throw new IllegalArgumentException("Max entry count should be at least 1: " + maxEntryCount);
        }
        this.maxEntryCount = maxEntryCount;
        return this;
    }

    public StatLoggerBuilder keyDelimiter(char keyDelimiter) {
        this.keyDelimiter = keyDelimiter;
        return this;
    }

    public StatLoggerBuilder valueDelimiter(char valueDelimiter) {
        this.valueDelimiter = valueDelimiter;
        return this;
    }

    StatLoggerBuilder appender(EagleEyeAppender appender) {
        this.appender = appender;
        return this;
    }

    StatLogger create() {
        long intervalMillis = TimeUnit.SECONDS.toMillis(this.intervalSeconds);

        String filePath;
        if (this.filePath == null) {
            filePath = EagleEye.EAGLEEYE_LOG_DIR + "stat-" + loggerName + ".log";
        } else if (this.filePath.endsWith("/") || this.filePath.endsWith("\\")) {
            filePath = this.filePath + "stat-" + loggerName + ".log";
        } else {
            filePath = this.filePath;
        }

        EagleEyeAppender appender = this.appender;
        if (appender == null) {
            EagleEyeRollingFileAppender rfAppender = new EagleEyeRollingFileAppender(filePath, maxFileSize);
            appender = new SyncAppender(rfAppender);
        }

        EagleEyeLogDaemon.watch(appender);
        return new StatLogger(loggerName, appender, intervalMillis, maxEntryCount,
            entryDelimiter, keyDelimiter, valueDelimiter);
    }

    /**
     * 构建单例 {@link StatLogger}，若同名 Logger 已存在则复用。
     *
     * @return 单例 StatLogger
     */
    public StatLogger buildSingleton() {
        return StatLogController.createLoggerIfNotExists(this);
    }

    /**
     * 校验统计滚动间隔（秒）。须能被 60 整除且不超过 5 分钟。
     *
     * @param intervalSeconds 间隔秒数
     * @throws IllegalArgumentException 间隔非法时抛出
     */
    static void validateInterval(final long intervalSeconds) throws IllegalArgumentException {
        if (intervalSeconds < 1) {
            throw new IllegalArgumentException("Interval cannot be less than 1" + intervalSeconds);
        } else if (intervalSeconds < 60) {
            if (60 % intervalSeconds != 0) {
                throw new IllegalArgumentException("Invalid second interval (cannot divide by 60): " + intervalSeconds);
            }
        } else if (intervalSeconds <= 5 * 60) {
            if (intervalSeconds % 60 != 0) {
                throw new IllegalArgumentException("Invalid second interval (cannot divide by 60): " + intervalSeconds);
            }
            if (60 % intervalSeconds != 0) {
                throw new IllegalArgumentException("Invalid second interval (cannot divide by 60): " + intervalSeconds);
            }
        } else if (intervalSeconds > 5 * 60) {
            throw new IllegalArgumentException("Interval should be less than 5 min: " + intervalSeconds);
        }
    }
}
