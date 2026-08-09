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
package com.alibaba.csp.sentinel.slots.statistic.base;

/**
 * 时间窗口段的包装实体类。
 *
 * @param <T> 统计数据类型
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class WindowWrap<T> {

    /** 单个窗口桶的时间长度（毫秒）。 */
    private final long windowLengthInMs;

    /** 窗口起始时间戳（毫秒）。 */
    private long windowStart;

    /** 统计数据。 */
    private T value;

    /**
     * @param windowLengthInMs 单个窗口桶的时间长度（毫秒）
     * @param windowStart      窗口起始时间戳
     * @param value            统计数据
     */
    public WindowWrap(long windowLengthInMs, long windowStart, T value) {
        this.windowLengthInMs = windowLengthInMs;
        this.windowStart = windowStart;
        this.value = value;
    }

    public long windowLength() {
        return windowLengthInMs;
    }

    public long windowStart() {
        return windowStart;
    }

    public T value() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    /**
     * 将当前桶的起始时间重置为给定值。
     *
     * @param startTime 有效的起始时间戳
     * @return 重置后的桶
     */
    public WindowWrap<T> resetTo(long startTime) {
        this.windowStart = startTime;
        return this;
    }

    /**
     * 判断给定时间戳是否落在当前桶内。
     *
     * @param timeMillis 有效时间戳（毫秒）
     * @return 在桶内返回 true，否则 false
     * @since 1.5.0
     */
    public boolean isTimeInWindow(long timeMillis) {
        return windowStart <= timeMillis && timeMillis < windowStart + windowLengthInMs;
    }

    @Override
    public String toString() {
        return "WindowWrap{" +
            "windowLengthInMs=" + windowLengthInMs +
            ", windowStart=" + windowStart +
            ", value=" + value +
            '}';
    }
}
