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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * EagleEye 统计日志器。
 * <p>按固定时间窗口聚合 {@link StatEntry} 指标，滚动后异步写入日志文件。</p>
 *
 * @author jifeng
 */
public final class StatLogger {

    private final String loggerName;

    private final EagleEyeAppender appender;

    private final AtomicReference<StatRollingData> ref;

    private final long intervalMillis;

    private final int maxEntryCount;

    private final char entryDelimiter;
    private final char keyDelimiter;
    private final char valueDelimiter;

    StatLogger(String loggerName, EagleEyeAppender appender, long intervalMillis, int maxEntryCount,
               char entryDelimiter, char keyDelimiter, char valueDelimiter) {
        this.loggerName = loggerName;
        this.appender = appender;
        this.intervalMillis = intervalMillis;
        this.maxEntryCount = maxEntryCount;
        this.entryDelimiter = entryDelimiter;
        this.keyDelimiter = keyDelimiter;
        this.valueDelimiter = valueDelimiter;
        this.ref = new AtomicReference<StatRollingData>();
        rolling();
    }

    public String getLoggerName() {
        return loggerName;
    }

    EagleEyeAppender getAppender() {
        return appender;
    }

    StatRollingData getRollingData() {
        return ref.get();
    }

    long getIntervalMillis() {
        return intervalMillis;
    }

    int getMaxEntryCount() {
        return maxEntryCount;
    }

    char getEntryDelimiter() {
        return entryDelimiter;
    }

    char getKeyDelimiter() {
        return keyDelimiter;
    }

    char getValueDelimiter() {
        return valueDelimiter;
    }

    /**
     * 滚动到下一时间窗口，返回上一窗口的聚合数据供写入。
     */
    StatRollingData rolling() {
        do {
            long now = System.currentTimeMillis();
            long timeSlot = now - now % intervalMillis;

            StatRollingData prevData = ref.get();
            long rollingTimeMillis = timeSlot + intervalMillis;
            int initialCapacity = prevData != null ? prevData.getStatCount() : 16;
            StatRollingData nextData = new StatRollingData(
                this, initialCapacity, timeSlot, rollingTimeMillis);
            if (ref.compareAndSet(prevData, nextData)) {
                return prevData;
            }
        } while (true);
    }

    /**
     * 创建单 key 统计项。
     */
    public StatEntry stat(String key) {
        return new StatEntry(this, key);
    }

    public StatEntry stat(String key1, String key2) {
        return new StatEntry(this, key1, key2);
    }

    public StatEntry stat(String key1, String key2, String key3) {
        return new StatEntry(this, key1, key2, key3);
    }

    public StatEntry stat(String key1, String key2, String key3, String key4) {
        return new StatEntry(this, key1, key2, key3, key4);
    }

    public StatEntry stat(String key1, String key2, String key3, String key4, String key5) {
        return new StatEntry(this, key1, key2, key3, key4, key5);
    }

    public StatEntry stat(String key1, String key2, String key3, String key4, String key5, String key6) {
        return new StatEntry(this, key1, key2, key3, key4, key5, key6);
    }

    public StatEntry stat(String key1, String key2, String key3, String key4, String key5, String key6, String key7) {
        return new StatEntry(this, key1, key2, key3, key4, key5, key6, key7);
    }

    public StatEntry stat(String key1, String key2, String key3, String key4, String key5, String key6, String key7,
                          String key8) {
        return new StatEntry(this, key1, key2, key3, key4, key5, key6, key7, key8);
    }

    public StatEntry stat(String key1, String... moreKeys) {
        return new StatEntry(this, key1, moreKeys);
    }

    public StatEntry stat(List<String> keys) {
        return new StatEntry(this, keys);
    }

    public StatEntry stat(String[] keys) {
        return new StatEntry(this, keys);
    }
}
