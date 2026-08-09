/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.proxy.service.route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 消息队列优先级提供函数式接口：支持自定义优先级以辅助路由与选队。
 * <p>
 * 数值越小优先级越高，例如 0 高于 1。
 * </p>
 *
 * @param <Q> 消息队列类型，须继承 {@link MessageQueue}
 */
@FunctionalInterface
public interface MessageQueuePriorityProvider<Q extends MessageQueue> {

    /**
     * 计算给定消息队列的优先级。
     * <p>
     * 数值越小优先级越高，例如：
     * <ul>
     *   <li>优先级 0：最高</li>
     *   <li>优先级 1：中等</li>
     *   <li>优先级 2：较低</li>
     * </ul>
     * </p>
     *
     * @param q 待评估的消息队列
     * @return 优先级数值，越小越高
     */
    int priorityOf(Q q);

    /**
     * 按优先级将消息队列分组并返回有序列表。
     * <p>
     * 根据 {@code provider} 计算各队列优先级，按从高到低排列各组。
     * </p>
     *
     * @param <Q> 消息队列类型，须继承 {@link MessageQueue}
     * @param queues 待分组的队列列表，可为 null 或空
     * @param provider 优先级计算提供者
     * @return 按优先级排序的分组列表；输入为空时返回空列表
     */
    static <Q extends MessageQueue> List<List<Q>> buildPriorityGroups(List<Q> queues, MessageQueuePriorityProvider<Q> provider) {
        if (queues == null || queues.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, List<Q>> buckets = new TreeMap<>();
        for (Q q : queues) {
            int p = provider.priorityOf(q);
            buckets.computeIfAbsent(p, k -> new ArrayList<>()).add(q);
        }
        return new ArrayList<>(buckets.values());
    }
}
