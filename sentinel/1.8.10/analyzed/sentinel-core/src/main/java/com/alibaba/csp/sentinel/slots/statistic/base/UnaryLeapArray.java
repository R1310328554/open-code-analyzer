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

import java.util.concurrent.atomic.LongAdder;

/**
 * 基于 {@link LongAdder} 的一元滑动窗口数组，用于简单计数统计。
 *
 * @author Eric Zhao
 */
public class UnaryLeapArray extends LeapArray<LongAdder> {

    /** 指定采样数与窗口间隔（毫秒）构造滑动数组。 */
    public UnaryLeapArray(int sampleCount, int intervalInMs) {
        super(sampleCount, intervalInMs);
    }

    /** 创建空的 {@link LongAdder} 桶。 */
    @Override
    public LongAdder newEmptyBucket(long time) {
        return new LongAdder();
    }

    /** 重置窗口起始时间并清空计数。 */
    @Override
    protected WindowWrap<LongAdder> resetWindowTo(WindowWrap<LongAdder> windowWrap, long startTime) {
        windowWrap.resetTo(startTime);
        windowWrap.value().reset();
        return windowWrap;
    }
}
