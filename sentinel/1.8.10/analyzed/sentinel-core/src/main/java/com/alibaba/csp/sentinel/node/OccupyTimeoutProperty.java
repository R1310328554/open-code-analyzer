/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.node;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.property.SimplePropertyListener;

/**
 * 优先级占用未来统计窗口令牌的最大等待超时配置。
 *
 * @author jialiang.linjl
 * @author Carpenter Lee
 * @since 1.5.0
 */
public class OccupyTimeoutProperty {

    /**
     * <p>
     * 最大占用超时（毫秒）。带优先级的请求可占用未来统计窗口的令牌，
     * {@code occupyTimeout} 限制可占用的最大时长。
     * </p>
     * <p>
     * 注意：超时值不应大于 {@link IntervalProperty#INTERVAL}。
     * </p>
     * 请勿直接修改此值，应使用 {@link #updateTimeout(int)}，
     * 否则修改不会生效。
     */
    private static volatile int occupyTimeout = 500;

    public static void register2Property(SentinelProperty<Integer> property) {
        property.addListener(new SimplePropertyListener<Integer>() {
            @Override
            public void configUpdate(Integer value) {
                if (value != null) {
                    updateTimeout(value);
                }
            }
        });
    }

    public static int getOccupyTimeout() {
        return occupyTimeout;
    }

    /**
     * 更新超时值。</br>
     * 注意：超时值不应大于 {@link IntervalProperty#INTERVAL}，
     * 否则将被忽略。
     *
     * @param newInterval 新超时值
     */
    public static void updateTimeout(int newInterval) {
        if (newInterval < 0) {
            RecordLog.warn("[OccupyTimeoutProperty] Illegal timeout value will be ignored: " + occupyTimeout);
            return;
        }
        if (newInterval > IntervalProperty.INTERVAL) {
            RecordLog.warn("[OccupyTimeoutProperty] Illegal timeout value will be ignored: {}, should <= {}",
                occupyTimeout, IntervalProperty.INTERVAL);
            return;
        }
        if (newInterval != occupyTimeout) {
            occupyTimeout = newInterval;
        }
        RecordLog.info("[OccupyTimeoutProperty] occupyTimeout updated to: {}", occupyTimeout);
    }
}
