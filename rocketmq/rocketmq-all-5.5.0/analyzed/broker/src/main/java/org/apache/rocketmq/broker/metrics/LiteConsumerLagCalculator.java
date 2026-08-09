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
package org.apache.rocketmq.broker.metrics;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.offset.ConsumerOffsetManager;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.entity.TopicGroup;
import org.apache.rocketmq.common.lite.LiteLagInfo;
import org.apache.rocketmq.common.lite.LiteUtil;

/**
 * Lite 消费滞后计算器：按 LMQ 维度统计消息条数滞后与最早未消费时间戳。
 */
public class LiteConsumerLagCalculator {

    /** 尚无滞后数据时的占位时间戳。 */
    protected static final long INIT_CONSUME_TIMESTAMP = -1L;

    @VisibleForTesting
    protected final ConcurrentHashMap<TopicGroup, PriorityBlockingQueue<LagTimeInfo>> topicGroupLagTimeMap =
        new ConcurrentHashMap<>();

    private final BrokerController brokerController;

    /** 绑定 Broker 控制器以访问 offset 表与 MessageStore。 */
    public LiteConsumerLagCalculator(BrokerController brokerController) {
        this.brokerController = brokerController;
    }

    /** 移除指定 LMQ 的滞后时间堆条目。 */
    public void removeLagInfo(String group, String bindTopic, String lmqName) {
        PriorityBlockingQueue<LagTimeInfo> lagHeap = topicGroupLagTimeMap.get(new TopicGroup(bindTopic, group));
        if (lagHeap != null) {
            lagHeap.removeIf(info -> info.getLmqName().equals(lmqName));
        }
    }

    /** 更新 LMQ 最早未消费时间戳，堆大小受 {@code liteLagLatencyTopK} 限制。 */
    public void updateLagInfo(String group, String bindTopic, String lmqName, long storeTimestamp) {
        PriorityBlockingQueue<LagTimeInfo> lagHeap = topicGroupLagTimeMap.computeIfAbsent(
            new TopicGroup(bindTopic, group),
            k -> new PriorityBlockingQueue<>(8, Comparator.comparingLong(LagTimeInfo::getLagTimestamp).reversed()));
        lagHeap.removeIf(info -> info.getLmqName().equals(lmqName));
        lagHeap.offer(new LagTimeInfo(lmqName, storeTimestamp));
        int topK = brokerController.getBrokerConfig().getLiteLagLatencyTopK();
        if (lagHeap.size() > topK) {
            lagHeap.remove();
        }
    }

    @VisibleForTesting
    protected long getStoreTimestamp(String lmqName, long offset) {
        return this.brokerController.getMessageStore().getMessageStoreTimeStamp(lmqName, 0, offset);
    }

    @VisibleForTesting
    protected long getOffset(String group, String topic) {
        return brokerController.getConsumerOffsetManager().queryOffset(group, topic, 0);
    }

    @VisibleForTesting
    protected long getMaxOffset(String lmqName) {
        return brokerController.getLiteLifecycleManager().getMaxOffsetInQueue(lmqName);
    }

    private long offsetDiff(Long offset, String lmqName) {
        long consumerOffset = offset == null ? -1L : offset;
        if (consumerOffset < 0) {
            return 0L;
        }
        long maxOffset = getMaxOffset(lmqName);
        return Math.max(0L, maxOffset - consumerOffset);
    }

    /** 遍历 lite offset 表，按父 topic 聚合消息条数滞后并回调记录。 */
    public void calculateLiteLagCount(Consumer<ConsumerLagCalculator.CalculateLagResult> lagRecorder) {
        if (!brokerController.getBrokerConfig().isLiteLagCountMetricsEnable()) {
            return;
        }

        Map<TopicGroup, Long> counter = new HashMap<>();

        offsetTableForEachByGroup(null, (topicGroup, consumerOffset) -> {
            String lmqName = topicGroup.topic;
            String group = topicGroup.group;
            String parentTopic = LiteUtil.getParentTopic(lmqName);
            long diff = offsetDiff(consumerOffset, lmqName);
            if (diff > 0) {
                TopicGroup key = new TopicGroup(parentTopic, group);
                counter.merge(key, diff, Long::sum);
            }
        });

        counter.forEach((topicGroup, totalCount) -> {
            ConsumerLagCalculator.CalculateLagResult lagResult =
                new ConsumerLagCalculator.CalculateLagResult(topicGroup.group, topicGroup.topic, false);
            lagResult.lag = totalCount;
            lagRecorder.accept(lagResult);
        });
    }

    /** 从滞后时间堆取最小 storeTimestamp 作为 topic 组滞后延迟指标。 */
    public void calculateLiteLagLatency(Consumer<ConsumerLagCalculator.CalculateLagResult> lagRecorder) {
        if (!brokerController.getBrokerConfig().isLiteLagLatencyMetricsEnable()) {
            return;
        }

        topicGroupLagTimeMap.forEach((topicGroup, lagHeap) -> {
            if (CollectionUtils.isEmpty(lagHeap)) {
                return;
            }

            // Find the minimum storeTimestamp in the heap
            long minTimestamp = lagHeap.stream()
                .mapToLong(LagTimeInfo::getLagTimestamp)
                .min()
                .orElse(0L);

            ConsumerLagCalculator.CalculateLagResult lagResult =
                new ConsumerLagCalculator.CalculateLagResult(topicGroup.group, topicGroup.topic, false);
            lagResult.earliestUnconsumedTimestamp = minTimestamp;
            lagRecorder.accept(lagResult);
        });
    }

    /**
     * 获取指定 topic 组滞后时间最小的 topK {@link LiteLagInfo} 条目。
     *
     * @param group       consumer group name
     * @param parentTopic parent topic name
     * @param topK        max number of entries to retrieve
     * @return Pair containing:
     * - Left: list of at most topK LiteLagInfo entries sorted by timestamp
     * - Right: minimum lag timestamp (or initial consume timestamp if no data)
     */
    public Pair<List<LiteLagInfo>/*topK*/, Long/*timestamp*/> getLagTimestampTopK(
        String group,
        String parentTopic,
        int topK
    ) {
        TopicGroup key = new TopicGroup(parentTopic, group);
        PriorityBlockingQueue<LagTimeInfo> lagHeap = topicGroupLagTimeMap.get(key);
        if (CollectionUtils.isEmpty(lagHeap)) {
            return Pair.of(Collections.emptyList(), INIT_CONSUME_TIMESTAMP);
        }

        // Evict the largest timestamp when heap is full, keeping smallest topK timestamps
        PriorityQueue<LagTimeInfo> maxHeap = new PriorityQueue<>(topK, Comparator.comparingLong(LagTimeInfo::getLagTimestamp).reversed());
        for (LagTimeInfo lagInfo : lagHeap) {
            if (maxHeap.size() < topK) {
                maxHeap.offer(lagInfo);
            } else if (maxHeap.peek() != null && lagInfo.getLagTimestamp() < maxHeap.peek().getLagTimestamp()) {
                maxHeap.poll();
                maxHeap.offer(lagInfo);
            }
        }

        // Convert results to LiteLagInfo list and sort by timestamp
        List<LiteLagInfo> topList = new ArrayList<>(maxHeap.size());
        for (LagTimeInfo lagInfo : maxHeap) {
            String lmqName = lagInfo.getLmqName();
            LiteLagInfo liteLagInfo = new LiteLagInfo();
            liteLagInfo.setLiteTopic(LiteUtil.getLiteTopic(lmqName));
            liteLagInfo.setEarliestUnconsumedTimestamp(lagInfo.getLagTimestamp());
            liteLagInfo.setLagCount(offsetDiff(getOffset(group, lmqName), lmqName));
            topList.add(liteLagInfo);
        }

        // Sort by timestamp in ascending order
        topList.sort(Comparator.comparingLong(LiteLagInfo::getEarliestUnconsumedTimestamp));
        long minLagTimestamp = topList.isEmpty() ? INIT_CONSUME_TIMESTAMP :
            topList.get(0).getEarliestUnconsumedTimestamp();

        return Pair.of(topList, minLagTimestamp);
    }

    /**
     * 获取指定消费组滞后条数最大的 topK {@link LiteLagInfo} 条目。
     *
     * @param group consumer group name
     * @param topK  max number of entries to retrieve
     * @return Pair containing:
     * - Left: list of at most topK LiteLagInfo entries sorted by lag count
     * - Right: total lag count
     */
    public Pair<List<LiteLagInfo>, Long> getLagCountTopK(
        String group,
        int topK
    ) {
        // Use a min heap to maintain the largest topK lag counts
        PriorityQueue<LiteLagInfo> minHeap = new PriorityQueue<>(topK, Comparator.comparingLong(LiteLagInfo::getLagCount));
        AtomicLong totalLagCount = new AtomicLong(0L);

        offsetTableForEachByGroup(group, (topicGroup, consumerOffset) -> {
            String topic = topicGroup.topic;

            long diff = offsetDiff(consumerOffset, topic);
            if (diff > 0) {
                totalLagCount.addAndGet(diff);
                LiteLagInfo liteLagInfo = new LiteLagInfo();
                liteLagInfo.setLiteTopic(LiteUtil.getLiteTopic(topic));
                liteLagInfo.setLagCount(diff);
                liteLagInfo.setEarliestUnconsumedTimestamp(getStoreTimestamp(topic, consumerOffset));

                if (minHeap.size() < topK) {
                    minHeap.offer(liteLagInfo);
                } else if (minHeap.peek() != null && liteLagInfo.getLagCount() > minHeap.peek().getLagCount()) {
                    minHeap.poll();
                    minHeap.offer(liteLagInfo);
                }
            }
        });

        // Convert heap elements to list and sort by lag count in descending order
        List<LiteLagInfo> topList = new ArrayList<>(minHeap);
        topList.sort(Comparator.comparingLong(LiteLagInfo::getLagCount).reversed());

        return Pair.of(topList, totalLagCount.get());
    }

    /**
     * 遍历 lite 消费 offset 表，{@code group} 为 null 时处理全部组。
     *
     * @param group    The specified consumer group. If null, all offset information is processed.
     * @param consumer The BiConsumer used to process each entry.
     */
    protected void offsetTableForEachByGroup(
        String group,
        BiConsumer<TopicGroup, Long> consumer
    ) {
        ConcurrentMap<String, ConcurrentMap<Integer, Long>> offsetTable =
            brokerController.getConsumerOffsetManager().getOffsetTable();
        offsetTable.forEach((topicAtGroup, queueOffset) -> {
            String[] topicGroup = topicAtGroup.split(ConsumerOffsetManager.TOPIC_GROUP_SEPARATOR);
            if (topicGroup.length == 2) {
                if (!LiteUtil.isLiteTopicQueue(topicGroup[0])) {
                    return;
                }
                // If group specified, only process the matching group
                if (StringUtils.isEmpty(group) || group.equals(topicGroup[1])) {
                    TopicGroup tg = new TopicGroup(topicGroup[0], topicGroup[1]);
                    Long consumerOffset = queueOffset.get(0);
                    if (consumerOffset == null) {
                        return;
                    }
                    consumer.accept(tg, consumerOffset);
                }
            }
        });
    }

    /** LMQ 最早未消费消息的时间戳条目，按 lmqName 去重。 */
    protected static class LagTimeInfo {
        private final String lmqName;
        // earliest unconsumed timestamp
        private final long lagTimestamp;

        public LagTimeInfo(String lmqName, long lagTimestamp) {
            this.lmqName = lmqName;
            this.lagTimestamp = lagTimestamp;
        }

        public String getLmqName() {
            return lmqName;
        }

        public long getLagTimestamp() {
            return lagTimestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LagTimeInfo lagInfo = (LagTimeInfo) o;
            return Objects.equals(lmqName, lagInfo.lmqName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(lmqName);
        }
    }
}
