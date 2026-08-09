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
package com.alibaba.csp.sentinel.slots.statistic.data;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.slots.block.flow.param.RollingParamEvent;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * 滑动窗口内热点参数指标桶：按 {@link RollingParamEvent} 维度
 * 维护参数值到 {@link AtomicInteger} 计数的 LRU 映射。
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamMapBucket {

    private final CacheMap<Object, AtomicInteger>[] data;

    /** 使用默认容量 {@link #DEFAULT_MAX_CAPACITY} 构造。 */
    public ParamMapBucket() {
        this(DEFAULT_MAX_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    /**
     * @param capacity 每种事件类型的 LRU 缓存容量
     */
    public ParamMapBucket(int capacity) {
        AssertUtil.isTrue(capacity > 0, "capacity should be positive");
        RollingParamEvent[] events = RollingParamEvent.values();
        this.data = new CacheMap[events.length];
        for (RollingParamEvent event : events) {
            data[event.ordinal()] = new ConcurrentLinkedHashMapWrapper<Object, AtomicInteger>(capacity);
        }
    }

    /** 清空全部事件维度的计数缓存。 */
    public void reset() {
        for (RollingParamEvent event : RollingParamEvent.values()) {
            data[event.ordinal()].clear();
        }
    }

    /** 获取指定事件与参数值的当前计数，不存在时返回 0。 */
    public int get(RollingParamEvent event, Object value) {
        AtomicInteger counter = data[event.ordinal()].get(value);
        return counter == null ? 0 : counter.intValue();
    }

    /** 累加指定事件与参数值的计数，支持链式调用。 */
    public ParamMapBucket add(RollingParamEvent event, int count, Object value) {
        AtomicInteger counter = data[event.ordinal()].get(value);
        // 非严格原子：并发下可能短暂重复 putIfAbsent
        if (counter == null) {
            AtomicInteger old = data[event.ordinal()].putIfAbsent(value, new AtomicInteger(count));
            if (old != null) {
                old.addAndGet(count);
            }
        } else {
            counter.addAndGet(count);
        }
        return this;
    }

    /** 按访问升序返回参数值键集合。 */
    public Set<Object> ascendingKeySet(RollingParamEvent type) {
        return data[type.ordinal()].keySet(true);
    }

    /** 按访问降序返回参数值键集合。 */
    public Set<Object> descendingKeySet(RollingParamEvent type) {
        return data[type.ordinal()].keySet(false);
    }

    /** 默认 LRU 缓存容量。 */
    public static final int DEFAULT_MAX_CAPACITY = 200;
}