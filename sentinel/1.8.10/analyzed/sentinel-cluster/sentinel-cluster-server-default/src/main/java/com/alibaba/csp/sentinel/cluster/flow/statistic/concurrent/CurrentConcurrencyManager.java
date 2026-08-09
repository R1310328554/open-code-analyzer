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
package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 使用 ConcurrentHashMap&lt;Long, AtomicInteger&gt; 存储各规则的当前并发数（nowCalls），
 * key 为 flowId，value 为并发计数。因多线程并发访问与修改，值设计为 {@link AtomicInteger}。
 * 新建规则时会向 map 添加计数器；并发阈值变更时实时更新对应计数。
 * 获取令牌时递增 nowCalls，释放令牌时递减。
 *
 * @author yunfeiyanggzq
 */
public final class CurrentConcurrencyManager {
    /**
     * 使用 ConcurrentHashMap 存储各规则的 nowCalls。
     */
    private static final ConcurrentHashMap<Long, AtomicInteger> NOW_CALLS_MAP = new ConcurrentHashMap<Long, AtomicInteger>();

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("sentinel-cluster-concurrency-record-task", true));

    static {
        ClusterConcurrentCheckerLogListener logTask = new ClusterConcurrentCheckerLogListener();
        SCHEDULER.scheduleAtFixedRate(logTask, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * 增加当前并发数。
     */
    public static void addConcurrency(Long flowId, Integer acquireCount) {

        AtomicInteger nowCalls = NOW_CALLS_MAP.get(flowId);
        if (nowCalls == null) {
            return;
        }
        nowCalls.getAndAdd(acquireCount);
    }

    /**
     * 获取指定 flowId 的当前并发计数器。
     */
    public static AtomicInteger get(Long flowId) {
        return NOW_CALLS_MAP.get(flowId);
    }

    /**
     * 删除指定 flowId 的并发计数器。
     */
    public static void remove(Long flowId) {
        NOW_CALLS_MAP.remove(flowId);
    }

    /**
     * 设置指定 flowId 的初始并发数。
     */
    public static void put(Long flowId, Integer nowCalls) {
        NOW_CALLS_MAP.put(flowId, new AtomicInteger(nowCalls));
    }

    /**
     * 检查 flowId 是否已注册。
     */
    public static boolean containsFlowId(Long flowId) {
        return NOW_CALLS_MAP.containsKey(flowId);
    }

    /**
     * 获取并发 map 的全部 flowId 键集合。
     */
    public static Set<Long> getConcurrencyMapKeySet() {
        return NOW_CALLS_MAP.keySet();
    }
}
