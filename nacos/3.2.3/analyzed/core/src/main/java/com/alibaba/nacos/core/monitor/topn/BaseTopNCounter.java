/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.monitor.topn;

import com.alibaba.nacos.common.utils.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TopN 计数器抽象基类，按 key 累加计数并支持取 TopN 快照。
 * <p>通过 {@link TopNConfig} 开关控制是否采集；取 TopN 时使用 {@link FixedSizePriorityQueue} 维护前 N 名。</p>
 * Nacos base topN counter.
 *
 * @author xiweng.yy
 */
public abstract class BaseTopNCounter<T> {
    
    /** 按计数值升序比较（小顶堆取 TopN 最大项）。 */
    private final Comparator<Pair<String, AtomicInteger>> comparator;
    
    /** key → 累计计数 的并发 Map，取 TopN 时会替换为新 Map 以实现快照隔离。 */
    protected ConcurrentMap<T, AtomicInteger> dataCount;
    
    /** 初始化并发计数 Map 与比较器。 */
    protected BaseTopNCounter() {
        dataCount = new ConcurrentHashMap<>();
        this.comparator = Comparator.comparingInt(value -> value.getSecond().get());
    }
    
    /**
     * 交换当前计数快照并返回 TopN 列表（按计数降序语义由小顶堆维护）。
     *
     * @param topN topN
     * @return topN counter
     */
    public List<Pair<String, AtomicInteger>> getCounterOfTopN(int topN) {
        if (!checkEnabled()) {
            reset();
            return Collections.emptyList();
        }
        ConcurrentMap<T, AtomicInteger> snapshot = dataCount;
        dataCount = new ConcurrentHashMap<>(1);
        FixedSizePriorityQueue<Pair<String, AtomicInteger>> queue =
            new FixedSizePriorityQueue<>(topN, comparator);
        for (T t : snapshot.keySet()) {
            queue.offer(Pair.with(keyToString(t), snapshot.get(t)));
        }
        return queue.toList();
    }
    
    /**
     * 将泛型 key 转为字符串，供 TopN 结果展示。
     *
     * @param t key
     * @return String
     */
    protected abstract String keyToString(T t);
    
    /**
     * 为目标 key 计数加 1（TopN 未启用时忽略）。
     *
     * @param t key
     */
    public void increment(T t) {
        if (checkEnabled()) {
            increment(t, 1);
        }
    }
    
    /**
     * 为目标 key 增加指定计数值。
     *
     * @param t     key
     * @param count count
     */
    public void increment(T t, int count) {
        if (checkEnabled()) {
            dataCount.computeIfAbsent(t, k -> new AtomicInteger(0)).addAndGet(count);
        }
    }
    
    /**
     * 直接设置目标 key 的计数值。
     *
     * @param t     key
     * @param count new count
     */
    public void set(T t, int count) {
        if (checkEnabled()) {
            dataCount.computeIfAbsent(t, k -> new AtomicInteger(0)).set(count);
        }
    }
    
    /** 清空所有计数。 */
    public void reset() {
        dataCount.clear();
    }
    
    /** 检查 TopN 功能是否启用（委托 {@link TopNConfig}）。 */
    protected boolean checkEnabled() {
        return TopNConfig.getInstance().isEnabled();
    }
}
