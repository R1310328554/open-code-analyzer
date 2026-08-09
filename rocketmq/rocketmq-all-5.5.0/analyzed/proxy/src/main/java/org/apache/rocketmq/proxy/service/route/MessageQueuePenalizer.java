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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rocketmq.common.message.MessageQueue;

@FunctionalInterface
public interface MessageQueuePenalizer<Q extends MessageQueue> {

    /**
     * 返回给定 {@link MessageQueue} 的惩罚值；数值越小越优。
     */
    int penaltyOf(Q messageQueue);

    /**
     * 对同一队列聚合多个惩罚器的得分（求和）。
     */
    static <Q extends MessageQueue> int evaluatePenalty(Q messageQueue, List<MessageQueuePenalizer<Q>> penalizers) {
        Objects.requireNonNull(messageQueue, "messageQueue");
        if (penalizers == null || penalizers.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (MessageQueuePenalizer<Q> p : penalizers) {
            sum += p.penaltyOf(messageQueue);
        }
        return sum;
    }

    /**
     * 从候选队列中选取综合惩罚值最小者。
     *
     * <p>遍历全部队列一次，起始下标由 {@code startIndex} 轮询决定，避免总从 0 扫描。</p>
     *
     * <p>各队列通过 {@link #evaluatePenalty} 与 {@code penalizers} 计算惩罚，取最小者。</p>
     *
     * <p>短路规则：若某队列 {@code penalty <= 0} 则立即返回（无法更优）。</p>
     *
     * @param queues 候选队列列表
     * @param penalizers 应用于各队列的惩罚评估器
     * @param startIndex 轮询起始位置的原子计数器
     * @param <Q> 队列类型
     * @return 选中队列与惩罚值的 {@code Pair}；{@code queues} 为空时返回 {@code null}
     */
    static <Q extends MessageQueue> Pair<Q, Integer> selectLeastPenalty(List<Q> queues,
        List<MessageQueuePenalizer<Q>> penalizers, AtomicInteger startIndex) {
        if (queues == null || queues.isEmpty()) {
            return null;
        }
        Q bestQueue = null;
        int bestPenalty = Integer.MAX_VALUE;

        for (int i = 0; i < queues.size(); i++) {
            int index = Math.floorMod(startIndex.getAndIncrement(), queues.size());
            Q messageQueue = queues.get(index);
            int penalty = evaluatePenalty(messageQueue, penalizers);

            // 短路：惩罚值已无法优于 0
            if (penalty <= 0) {
                return Pair.of(messageQueue, penalty);
            }

            if (penalty < bestPenalty) {
                bestPenalty = penalty;
                bestQueue = messageQueue;
            }
        }
        return Pair.of(bestQueue,  bestPenalty);
    }

    /**
     * 从多优先级队列组中选取综合惩罚最小者。
     *
     * <p>{@code queuesWithPriority} 按优先级排序，每组委托 {@link #selectLeastPenalty} 选最优队列。</p>
     *
     * <p>短路规则：任一组出现 {@code penalty <= 0} 的队列则立即返回。</p>
     *
     * <p>否则返回各组最小正惩罚队列；同分取先遇到者。</p>
     *
     * @param queuesWithPriority 按优先级分组的队列列表
     * @param penalizers 供 {@code selectLeastPenalty} 使用的惩罚计算器
     * @param startIndex 转发给 {@code selectLeastPenalty} 的轮询起始下标
     * @param <Q> 队列类型
     * @return 选中队列与惩罚值的 {@code Pair}；输入为空时返回 {@code null}
     */
    static <Q extends MessageQueue> Pair<Q, Integer> selectLeastPenaltyWithPriority(List<List<Q>> queuesWithPriority,
        List<MessageQueuePenalizer<Q>> penalizers, AtomicInteger startIndex) {
        if (queuesWithPriority == null || queuesWithPriority.isEmpty()) {
            return null;
        }
        if (queuesWithPriority.size() == 1) {
            return selectLeastPenalty(queuesWithPriority.get(0), penalizers, startIndex);
        }
        Q bestQueue = null;
        int bestPenalty = Integer.MAX_VALUE;
        for (List<Q> queues : queuesWithPriority) {
            Pair<Q, Integer> queueAndPenalty = selectLeastPenalty(queues, penalizers, startIndex);
            int penalty =  queueAndPenalty.getRight();
            if (queueAndPenalty.getRight() <= 0) {
                return queueAndPenalty;
            }
            if (penalty < bestPenalty) {
                bestPenalty = penalty;
                bestQueue = queueAndPenalty.getLeft();
            }
        }
        return Pair.of(bestQueue,  bestPenalty);
    }
}