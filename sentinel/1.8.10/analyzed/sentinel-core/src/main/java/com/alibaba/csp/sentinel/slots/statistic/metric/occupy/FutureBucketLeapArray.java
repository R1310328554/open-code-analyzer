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
package com.alibaba.csp.sentinel.slots.statistic.metric.occupy;

import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.statistic.data.MetricBucket;

/**
 * 仅用于未来窗口桶的 {@code BucketLeapArray} 变体。
 *
 * @author jialiang.linjl
 * @since 1.5.0
 */
public class FutureBucketLeapArray extends LeapArray<MetricBucket> {

    public FutureBucketLeapArray(int sampleCount, int intervalInMs) {
        // 本类即原 BorrowBucketArray。
        super(sampleCount, intervalInMs);
    }

    @Override
    public MetricBucket newEmptyBucket(long time) {
        return new MetricBucket();
    }

    @Override
    protected WindowWrap<MetricBucket> resetWindowTo(WindowWrap<MetricBucket> w, long startTime) {
        // 更新起始时间并重置统计值。
        w.resetTo(startTime);
        w.value().reset();
        return w;
    }

    @Override
    public boolean isWindowDeprecated(long time, WindowWrap<MetricBucket> windowWrap) {
        // 技巧：仅对未来窗口进行计算。
        return time >= windowWrap.windowStart();
    }
}
