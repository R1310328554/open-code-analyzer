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
package org.apache.rocketmq.broker.pop.orderly;

import com.alibaba.fastjson2.annotation.JSONField;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.BrokerPathConfigHelper;
import org.apache.rocketmq.common.ConfigManager;
import org.apache.rocketmq.common.OrderedConsumptionLevel;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import org.apache.rocketmq.remoting.protocol.header.ExtraInfoUtil;
import org.apache.rocketmq.store.GetMessageResult;

/**
 * 队列级顺序 POP 消费状态管理器：维护 topic@group → queueId → {@link OrderInfo} 映射，
 * 支持 POP 阻塞判定、ACK 位图提交、可见性时间更新及磁盘持久化。
 * 实现 {@link ConsumerOrderInfoManager} 的 QUEUE 粒度策略。
 */
public class QueueLevelConsumerManager extends ConfigManager implements ConsumerOrderInfoManager {

    protected static final Logger log = LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);
    /** topic 与 group 复合键分隔符。 */
    private static final String TOPIC_GROUP_SEPARATOR = "@";
    /** 超过该毫秒数未消费的 orderInfo 可被 autoClean 清理。 */
    private static final long CLEAN_SPAN_FROM_LAST = 24 * 3600 * 1000;

    /** topic@group → (queueId → 顺序消费快照)。 */
    private ConcurrentHashMap<String/* topic@group*/, ConcurrentHashMap<Integer/*queueId*/, OrderInfo>> table =
        new ConcurrentHashMap<>(128);

    private transient QueueLevelConsumerOrderInfoLockManager queueLevelConsumerOrderInfoLockManager;
    private transient BrokerController brokerController;

    public QueueLevelConsumerManager() {
    }

    public QueueLevelConsumerManager(BrokerController brokerController) {
        this.brokerController = brokerController;
        this.queueLevelConsumerOrderInfoLockManager = new QueueLevelConsumerOrderInfoLockManager(brokerController);
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<Integer, OrderInfo>> getTable() {
        return table;
    }

    public void setTable(ConcurrentHashMap<String, ConcurrentHashMap<Integer, OrderInfo>> table) {
        this.table = table;
    }

    protected static String buildKey(String topic, String group) {
        return topic + TOPIC_GROUP_SEPARATOR + group;
    }

    protected static String[] decodeKey(String key) {
        return key.split(TOPIC_GROUP_SEPARATOR);
    }

    protected void updateLockFreeTimestamp(String topic, String group, int queueId, OrderInfo orderInfo) {
        if (queueLevelConsumerOrderInfoLockManager != null) {
            queueLevelConsumerOrderInfoLockManager.updateLockFreeTimestamp(topic, group, queueId, orderInfo);
        }
    }

    /**
     * POP 成功后更新本批消息的接收状态，合并 offset 位图并写入 orderInfoBuilder。
     *
     * @param isRetry 是否为重试 topic
     * @param topic topic
     * @param group group
     * @param queueId 队列 ID
     * @param popTime POP 时刻
     * @param invisibleTime 不可见时长（毫秒）
     * @param msgQueueOffsetList 消息队列 offset 列表
     * @param orderInfoBuilder 追加 orderInfo 的 StringBuilder
     */
    public void update(String attemptId, boolean isRetry, String topic, String group, int queueId, long popTime,
        long invisibleTime,
        List<Long> msgQueueOffsetList, StringBuilder orderInfoBuilder) {
        String key = buildKey(topic, group);
        ConcurrentHashMap<Integer/*queueId*/, OrderInfo> qs = table.get(key);
        if (qs == null) {
            qs = new ConcurrentHashMap<>(16);
            ConcurrentHashMap<Integer/*queueId*/, OrderInfo> old = table.putIfAbsent(key, qs);
            if (old != null) {
                qs = old;
            }
        }

        OrderInfo orderInfo = qs.get(queueId);

        if (orderInfo != null) {
            OrderInfo newOrderInfo = new OrderInfo(attemptId, popTime, invisibleTime, msgQueueOffsetList, System.currentTimeMillis(), 0);
            newOrderInfo.mergeOffsetConsumedCount(orderInfo.attemptId, orderInfo.offsetList, orderInfo.offsetConsumedCount);

            orderInfo = newOrderInfo;
        } else {
            orderInfo = new OrderInfo(attemptId, popTime, invisibleTime, msgQueueOffsetList, System.currentTimeMillis(), 0);
        }
        qs.put(queueId, orderInfo);

        Map<Long, Integer> offsetConsumedCount = orderInfo.offsetConsumedCount;
        int minConsumedTimes = Integer.MAX_VALUE;
        if (offsetConsumedCount != null) {
            Set<Long> offsetSet = offsetConsumedCount.keySet();
            for (Long offset : offsetSet) {
                Integer consumedTimes = offsetConsumedCount.getOrDefault(offset, 0);
                ExtraInfoUtil.buildQueueOffsetOrderCountInfo(orderInfoBuilder, topic, queueId, offset, consumedTimes);
                minConsumedTimes = Math.min(minConsumedTimes, consumedTimes);
            }

            if (offsetConsumedCount.size() != orderInfo.offsetList.size()) {
                // offsetConsumedCount 仅保存消费次数>0 的 offset；size 不等说明有新消息
                minConsumedTimes = 0;
            }
        } else {
            minConsumedTimes = 0;
        }

        // 兼容旧 SDK：通过 queueId 从 orderCountInfo 读取 consumedTimes
        ExtraInfoUtil.buildQueueIdOrderCountInfo(orderInfoBuilder, topic, queueId, minConsumedTimes);
        updateLockFreeTimestamp(topic, group, queueId, orderInfo);
    }

    @Override
    public void update(String attemptId, boolean isRetry, String topic, String group, int queueId, long popTime,
        long invisibleTime,
        List<Long> msgQueueOffsetList, StringBuilder orderInfoBuilder, GetMessageResult getMessageResult) {
        update(attemptId, isRetry, topic, group, queueId, popTime, invisibleTime, msgQueueOffsetList, orderInfoBuilder);
    }

    @Override
    public boolean checkBlock(String attemptId, String topic, String group, int queueId, long invisibleTime) {
        String key = buildKey(topic, group);
        ConcurrentHashMap<Integer/*queueId*/, OrderInfo> qs = table.get(key);
        if (qs == null) {
            qs = new ConcurrentHashMap<>(16);
            ConcurrentHashMap<Integer/*queueId*/, OrderInfo> old = table.putIfAbsent(key, qs);
            if (old != null) {
                qs = old;
            }
        }

        OrderInfo orderInfo = qs.get(queueId);

        if (orderInfo == null) {
            return false;
        }
        return orderInfo.needBlock(attemptId, invisibleTime);
    }

    @Override
    public void clearBlock(String topic, String group, int queueId) {
        table.computeIfPresent(buildKey(topic, group), (key, val) -> {
            val.remove(queueId);
            return val;
        });
    }

    @Override
    public void remove(String topic, String group) {
        table.remove(buildKey(topic, group));
    }

    @Override
    public int getOrderInfoCount() {
        return table.size();
    }

    @Override
    public OrderedConsumptionLevel getOrderedConsumptionLevel() {
        return OrderedConsumptionLevel.QUEUE;
    }

    @Override
    public void start() {
    }

    /**
     * ACK 时标记消息已消费并返回下一可提交 offset。
     *
     * @param topic topic
     * @param group group
     * @param queueId 队列 ID
     * @param queueOffset 消息队列 offset
     * @return -1 非法；-2 无需提交；>=0 应提交的 offset
     */
    @Override
    public long commitAndNext(String topic, String group, int queueId, long queueOffset, long popTime) {
        String key = buildKey(topic, group);
        ConcurrentHashMap<Integer/*queueId*/, OrderInfo> qs = table.get(key);

        if (qs == null) {
            return queueOffset + 1;
        }
        OrderInfo orderInfo = qs.get(queueId);
        if (orderInfo == null) {
            log.warn("OrderInfo is null, {}, {}, {}", key, queueOffset, orderInfo);
            return queueOffset + 1;
        }

        List<Long> o = orderInfo.offsetList;
        if (o == null || o.isEmpty()) {
            log.warn("OrderInfo is empty, {}, {}, {}", key, queueOffset, orderInfo);
            return -1;
        }

        if (popTime != orderInfo.popTime) {
            log.warn("popTime is not equal to orderInfo saved. key: {}, offset: {}, orderInfo: {}, popTime: {}", key, queueOffset, orderInfo, popTime);
            return -2;
        }

        Long first = o.get(0);
        int i = 0, size = o.size();
        for (; i < size; i++) {
            long temp;
            if (i == 0) {
                temp = first;
            } else {
                temp = first + o.get(i);
            }
            if (queueOffset == temp) {
                break;
            }
        }
        // not found
        if (i >= size) {
            log.warn("OrderInfo not found commit offset, {}, {}, {}", key, queueOffset, orderInfo);
            return -1;
        }
        //set bit
        orderInfo.setCommitOffsetBit(orderInfo.commitOffsetBit | (1L << i));
        long nextOffset = orderInfo.getNextOffset();

        updateLockFreeTimestamp(topic, group, queueId, orderInfo);
        return nextOffset;
    }

    /**
     * 更新指定消息的下次可见时间（延迟重消费 / 修改不可见时间）。
     *
     * @param topic topic
     * @param group group
     * @param queueId 队列 ID
     * @param queueOffset 消息 offset
     * @param nextVisibleTime 下次可见时间戳
     */
    @Override
    public void updateNextVisibleTime(String topic, String group, int queueId, long queueOffset, long popTime,
        long nextVisibleTime) {
        String key = buildKey(topic, group);
        ConcurrentHashMap<Integer/*queueId*/, OrderInfo> qs = table.get(key);

        if (qs == null) {
            log.warn("orderInfo of queueId is null. key: {}, queueOffset: {}, queueId: {}", key, queueOffset, queueId);
            return;
        }
        OrderInfo orderInfo = qs.get(queueId);
        if (orderInfo == null) {
            log.warn("orderInfo is null, key: {}, queueOffset: {}, queueId: {}", key, queueOffset, queueId);
            return;
        }
        if (popTime != orderInfo.popTime) {
            log.warn("popTime is not equal to orderInfo saved. key: {}, queueOffset: {}, orderInfo: {}, popTime: {}", key, queueOffset, orderInfo, popTime);
            return;
        }

        orderInfo.updateOffsetNextVisibleTime(queueOffset, nextVisibleTime);
        updateLockFreeTimestamp(topic, group, queueId, orderInfo);
    }

    /** 定时清理：移除 topic/group 不存在或长期无消费的 orderInfo 条目。 */
    public void autoClean() {
        if (brokerController == null) {
            return;
        }
        Iterator<Map.Entry<String/* topic@group*/, ConcurrentHashMap<Integer/*queueId*/, OrderInfo>>> iterator =
            this.table.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String/* topic@group*/, ConcurrentHashMap<Integer/*queueId*/, OrderInfo>> entry =
                iterator.next();
            String topicAtGroup = entry.getKey();
            ConcurrentHashMap<Integer/*queueId*/, OrderInfo> qs = entry.getValue();
            String[] arrays = decodeKey(topicAtGroup);
            if (arrays.length != 2) {
                continue;
            }
            String topic = arrays[0];
            String group = arrays[1];

            TopicConfig topicConfig = this.brokerController.getTopicConfigManager().selectTopicConfig(topic);
            if (topicConfig == null) {
                iterator.remove();
                log.info("Topic not exist, Clean order info, {}:{}", topicAtGroup, qs);
                continue;
            }

            if (!this.brokerController.getSubscriptionGroupManager().containsSubscriptionGroup(group)) {
                iterator.remove();
                log.info("Group not exist, Clean order info, {}:{}", topicAtGroup, qs);
                continue;
            }

            if (qs.isEmpty()) {
                iterator.remove();
                log.info("Order table is empty, Clean order info, {}:{}", topicAtGroup, qs);
                continue;
            }

            Iterator<Map.Entry<Integer/*queueId*/, OrderInfo>> qsIterator = qs.entrySet().iterator();
            while (qsIterator.hasNext()) {
                Map.Entry<Integer/*queueId*/, OrderInfo> qsEntry = qsIterator.next();

                if (qsEntry.getKey() >= topicConfig.getReadQueueNums()) {
                    qsIterator.remove();
                    log.info("Queue not exist, Clean order info, {}:{}, {}", topicAtGroup, entry.getValue(), topicConfig);
                    continue;
                }

                if (System.currentTimeMillis() - qsEntry.getValue().getLastConsumeTimestamp() > CLEAN_SPAN_FROM_LAST) {
                    qsIterator.remove();
                    log.info("Not consume long time, Clean order info, {}:{}, {}", topicAtGroup, entry.getValue(), topicConfig);
                }
            }
        }
    }

    @Override
    public String encode() {
        return this.encode(false);
    }

    @Override
    public String configFilePath() {
        if (brokerController != null) {
            return BrokerPathConfigHelper.getConsumerOrderInfoPath(this.brokerController.getMessageStoreConfig().getStorePathRootDir());
        } else {
            return BrokerPathConfigHelper.getConsumerOrderInfoPath("~");
        }
    }

    @Override
    public void decode(String jsonString) {
        if (jsonString != null) {
            QueueLevelConsumerManager obj = RemotingSerializable.fromJson(jsonString, QueueLevelConsumerManager.class);
            if (obj != null) {
                this.table = obj.table;
                if (this.queueLevelConsumerOrderInfoLockManager != null) {
                    this.queueLevelConsumerOrderInfoLockManager.recover(this.table);
                }
            }
        }
    }

    @Override
    public String encode(boolean prettyFormat) {
        this.autoClean();
        return RemotingSerializable.toJson(this, prettyFormat);
    }

    public void shutdown() {
        if (this.queueLevelConsumerOrderInfoLockManager != null) {
            this.queueLevelConsumerOrderInfoLockManager.shutdown();
        }
    }

    @Override
    public CompletableFuture<GetMessageResult> getAvailableMessageResult(String attemptId, long popTime, long invisibleTime,
        String groupId, String topicId, int queueId, int batchSize, StringBuilder orderCountInfoBuilder) {
        return CompletableFuture.completedFuture(null);
    }

    @VisibleForTesting
    protected QueueLevelConsumerOrderInfoLockManager getConsumerOrderInfoLockManager() {
        return queueLevelConsumerOrderInfoLockManager;
    }

    /** 单次 POP 批次的顺序消费快照，可 JSON 持久化到磁盘。 */
    public static class OrderInfo {
        private long popTime;
        /** POP 时的不可见时长（毫秒）。 */
        @JSONField(name = "i")
        private Long invisibleTime;
        /**
         * 压缩存储的 offset 列表：
         * offsetList[0] 为首条消息的 queue offset；
         * offsetList[i]（i>0）为相对首条的差值。
         */
        @JSONField(name = "o")
        private List<Long> offsetList;
        /** offset → 下次可见时间戳（修改不可见时间时使用）。 */
        @JSONField(name = "ot")
        private Map<Long, Long> offsetNextVisibleTime;
        /** offset → 该消息已被 POP 的次数（重试计数）。 */
        @JSONField(name = "oc")
        private Map<Long, Integer> offsetConsumedCount;
        /**
         * last consume timestamp
         */
        @JSONField(name = "l")
        private long lastConsumeTimestamp;
        /** ACK 位图：第 i 位为 1 表示 offsetList[i] 已确认。 */
        @JSONField(name = "cm")
        private long commitOffsetBit;
        @JSONField(name = "a")
        private String attemptId;

        public OrderInfo() {
        }

        public OrderInfo(String attemptId, long popTime, long invisibleTime, List<Long> queueOffsetList,
            long lastConsumeTimestamp,
            long commitOffsetBit) {
            this.popTime = popTime;
            this.invisibleTime = invisibleTime;
            this.offsetList = buildOffsetList(queueOffsetList);
            this.lastConsumeTimestamp = lastConsumeTimestamp;
            this.commitOffsetBit = commitOffsetBit;
            this.attemptId = attemptId;
        }

        public List<Long> getOffsetList() {
            return offsetList;
        }

        public void setOffsetList(List<Long> offsetList) {
            this.offsetList = offsetList;
        }

        public long getLastConsumeTimestamp() {
            return lastConsumeTimestamp;
        }

        public void setLastConsumeTimestamp(long lastConsumeTimestamp) {
            this.lastConsumeTimestamp = lastConsumeTimestamp;
        }

        public long getCommitOffsetBit() {
            return commitOffsetBit;
        }

        public void setCommitOffsetBit(long commitOffsetBit) {
            this.commitOffsetBit = commitOffsetBit;
        }

        public long getPopTime() {
            return popTime;
        }

        public void setPopTime(long popTime) {
            this.popTime = popTime;
        }

        public Long getInvisibleTime() {
            return invisibleTime;
        }

        public void setInvisibleTime(Long invisibleTime) {
            this.invisibleTime = invisibleTime;
        }

        public Map<Long, Long> getOffsetNextVisibleTime() {
            return offsetNextVisibleTime;
        }

        public void setOffsetNextVisibleTime(Map<Long, Long> offsetNextVisibleTime) {
            this.offsetNextVisibleTime = offsetNextVisibleTime;
        }

        public Map<Long, Integer> getOffsetConsumedCount() {
            return offsetConsumedCount;
        }

        public void setOffsetConsumedCount(Map<Long, Integer> offsetConsumedCount) {
            this.offsetConsumedCount = offsetConsumedCount;
        }

        public String getAttemptId() {
            return attemptId;
        }

        public void setAttemptId(String attemptId) {
            this.attemptId = attemptId;
        }

        public static List<Long> buildOffsetList(List<Long> queueOffsetList) {
            List<Long> simple = new ArrayList<>();
            if (queueOffsetList.size() == 1) {
                simple.addAll(queueOffsetList);
                return simple;
            }
            Long first = queueOffsetList.get(0);
            simple.add(first);
            for (int i = 1; i < queueOffsetList.size(); i++) {
                simple.add(queueOffsetList.get(i) - first);
            }
            return simple;
        }

        @JSONField(serialize = false, deserialize = false)
        public boolean needBlock(String attemptId, long currentInvisibleTime) {
            if (offsetList == null || offsetList.isEmpty()) {
                return false;
            }
            if (this.attemptId != null && this.attemptId.equals(attemptId)) {
                return false;
            }
            int num = offsetList.size();
            int i = 0;
            if (this.invisibleTime == null || this.invisibleTime <= 0) {
                this.invisibleTime = currentInvisibleTime;
            }
            long currentTime = System.currentTimeMillis();
            for (; i < num; i++) {
                if (isNotAck(i)) {
                    long nextVisibleTime = popTime + invisibleTime;
                    if (offsetNextVisibleTime != null) {
                        Long time = offsetNextVisibleTime.get(this.getQueueOffset(i));
                        if (time != null) {
                            nextVisibleTime = time;
                        }
                    }
                    if (currentTime < nextVisibleTime) {
                        return true;
                    }
                }
            }
            return false;
        }

        @JSONField(serialize = false, deserialize = false)
        public Long getLockFreeTimestamp() {
            if (offsetList == null || offsetList.isEmpty()) {
                return null;
            }
            int num = offsetList.size();
            int i = 0;
            long currentTime = System.currentTimeMillis();
            for (; i < num; i++) {
                if (isNotAck(i)) {
                    if (invisibleTime == null || invisibleTime <= 0) {
                        return null;
                    }
                    long nextVisibleTime = popTime + invisibleTime;
                    if (offsetNextVisibleTime != null) {
                        Long time = offsetNextVisibleTime.get(this.getQueueOffset(i));
                        if (time != null) {
                            nextVisibleTime = time;
                        }
                    }
                    if (currentTime < nextVisibleTime) {
                        return nextVisibleTime;
                    }
                }
            }
            return currentTime;
        }

        @JSONField(serialize = false, deserialize = false)
        public Long getMaxLockFreeTimestamp() {
            if (offsetList == null || offsetList.isEmpty()) {
                return null;
            }
            int num = offsetList.size();
            long maxTime = System.currentTimeMillis();
            for (int i = 0; i < num; i++) {
                if (isNotAck(i)) {
                    if (invisibleTime == null || invisibleTime <= 0) {
                        return null;
                    }
                    long nextVisibleTime = popTime + invisibleTime;
                    if (offsetNextVisibleTime != null) {
                        Long time = offsetNextVisibleTime.get(this.getQueueOffset(i));
                        if (time != null) {
                            nextVisibleTime = time;
                        }
                    }
                    if (maxTime < nextVisibleTime) {
                        maxTime = nextVisibleTime;
                    }
                }
            }
            return maxTime;
        }

        @JSONField(serialize = false, deserialize = false)
        public void updateOffsetNextVisibleTime(long queueOffset, long nextVisibleTime) {
            if (this.offsetNextVisibleTime == null) {
                this.offsetNextVisibleTime = new HashMap<>();
            }
            this.offsetNextVisibleTime.put(queueOffset, nextVisibleTime);
        }

        @JSONField(serialize = false, deserialize = false)
        public long getNextOffset() {
            if (offsetList == null || offsetList.isEmpty()) {
                return -2;
            }
            int num = offsetList.size();
            int i = 0;
            for (; i < num; i++) {
                if (isNotAck(i)) {
                    break;
                }
            }
            if (i == num) {
                // 全部 ACK 完毕
                return getQueueOffset(num - 1) + 1;
            }
            return getQueueOffset(i);
        }

        /**
         * 将 offsetList 下标转换为真实 queue offset。
         *
         * @param offsetIndex offsetList 下标
         * @return 消息 queue offset
         */
        @JSONField(serialize = false, deserialize = false)
        public long getQueueOffset(int offsetIndex) {
            return getQueueOffset(this.offsetList, offsetIndex);
        }

        protected static long getQueueOffset(List<Long> offsetList, int offsetIndex) {
            if (offsetIndex == 0) {
                return offsetList.get(0);
            }
            return offsetList.get(0) + offsetList.get(offsetIndex);
        }

        @JSONField(serialize = false, deserialize = false)
        public boolean isNotAck(int offsetIndex) {
            return (commitOffsetBit & (1L << offsetIndex)) == 0;
        }

        /**
         * 合并上一批 POP 的消费次数，写入 offsetConsumedCount（仅保留非零值）。
         *
         * @param prevOffsetConsumedCount 上一批 offset 消费计数表
         */
        @JSONField(serialize = false, deserialize = false)
        public void mergeOffsetConsumedCount(String preAttemptId, List<Long> preOffsetList,
            Map<Long, Integer> prevOffsetConsumedCount) {
            Map<Long, Integer> offsetConsumedCount = new HashMap<>();
            if (prevOffsetConsumedCount == null) {
                prevOffsetConsumedCount = new HashMap<>();
            }
            if (preAttemptId != null && preAttemptId.equals(this.attemptId)) {
                this.offsetConsumedCount = prevOffsetConsumedCount;
                return;
            }
            Set<Long> preQueueOffsetSet = new HashSet<>();
            for (int i = 0; i < preOffsetList.size(); i++) {
                preQueueOffsetSet.add(getQueueOffset(preOffsetList, i));
            }
            for (int i = 0; i < offsetList.size(); i++) {
                long queueOffset = this.getQueueOffset(i);
                if (preQueueOffsetSet.contains(queueOffset)) {
                    int count = 1;
                    Integer preCount = prevOffsetConsumedCount.get(queueOffset);
                    if (preCount != null) {
                        count = preCount + 1;
                    }
                    offsetConsumedCount.put(queueOffset, count);
                }
            }
            this.offsetConsumedCount = offsetConsumedCount;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("popTime", popTime)
                .add("invisibleTime", invisibleTime)
                .add("offsetList", offsetList)
                .add("offsetNextVisibleTime", offsetNextVisibleTime)
                .add("offsetConsumedCount", offsetConsumedCount)
                .add("lastConsumeTimestamp", lastConsumeTimestamp)
                .add("commitOffsetBit", commitOffsetBit)
                .add("attemptId", attemptId)
                .toString();
        }
    }
}