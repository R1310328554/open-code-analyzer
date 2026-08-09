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

package org.apache.rocketmq.common.consumer;

import java.util.Arrays;
import java.util.List;
import org.apache.rocketmq.common.KeyBuilder;
import org.apache.rocketmq.common.message.MessageConst;

/**
 * POP 消费回执句柄：编码/解码消息拉取凭证，含不可见时间、Broker 位点及重试 Topic 类型等信息。
 */
public class ReceiptHandle {
    /** 回执串字段分隔符。 */
    private static final String SEPARATOR = MessageConst.KEY_SEPARATOR;
    /** 普通 Topic 类型标识。 */
    public static final String NORMAL_TOPIC = "0";
    /** 重试 Topic V1 类型标识。 */
    public static final String RETRY_TOPIC = "1";

    /** 重试 Topic V2 类型标识。 */
    public static final String RETRY_TOPIC_V2 = "2";
    /** POP 起始消费位点。 */
    private final long startOffset;
    /** 消息拉取时间戳（毫秒）。 */
    private final long retrieveTime;
    /** 不可见时长（毫秒）。 */
    private final long invisibleTime;
    /** 下次可见时间戳（retrieveTime + invisibleTime）。 */
    private final long nextVisibleTime;
    /** 复活队列 ID（超时重投用）。 */
    private final int reviveQueueId;
    /** Topic 类型（普通/重试 V1/V2）。 */
    private final String topicType;
    /** 所属 Broker 名称。 */
    private final String brokerName;
    /** 消息队列 ID。 */
    private final int queueId;
    /** 消费队列 offset。 */
    private final long offset;
    /** CommitLog 物理 offset。 */
    private final long commitLogOffset;
    /** 原始回执字符串。 */
    private final String receiptHandle;

    /** 将回执字段编码为分隔符拼接的字符串（不含原始 receiptHandle 字段）。 */
    public String encode() {
        return startOffset + SEPARATOR + retrieveTime + SEPARATOR + invisibleTime + SEPARATOR + reviveQueueId
            + SEPARATOR + topicType + SEPARATOR + brokerName + SEPARATOR + queueId + SEPARATOR + offset + SEPARATOR
            + commitLogOffset;
    }

    /** 判断不可见期是否已过期（当前时间 >= nextVisibleTime）。 */
    public boolean isExpired() {
        return nextVisibleTime <= System.currentTimeMillis();
    }

    /**
     * 从回执字符串解析 {@link ReceiptHandle}。
     *
     * @param receiptHandle 编码后的回执串
     * @return 解析得到的回执对象
     * @throws IllegalArgumentException 字段数不足时抛出
     */
    public static ReceiptHandle decode(String receiptHandle) {
        List<String> dataList = Arrays.asList(receiptHandle.split(SEPARATOR));
        if (dataList.size() < 8) {
            throw new IllegalArgumentException("Parse failed, dataList size " + dataList.size());
        }
        long startOffset = Long.parseLong(dataList.get(0));
        long retrieveTime = Long.parseLong(dataList.get(1));
        long invisibleTime = Long.parseLong(dataList.get(2));
        int reviveQueueId = Integer.parseInt(dataList.get(3));
        String topicType = dataList.get(4);
        String brokerName = dataList.get(5);
        int queueId = Integer.parseInt(dataList.get(6));
        long offset = Long.parseLong(dataList.get(7));
        // 兼容旧版回执（无 commitLogOffset 字段）
        long commitLogOffset = -1L;
        if (dataList.size() >= 9) {
            commitLogOffset = Long.parseLong(dataList.get(8));
        }

        return new ReceiptHandleBuilder()
            .startOffset(startOffset)
            .retrieveTime(retrieveTime)
            .invisibleTime(invisibleTime)
            .reviveQueueId(reviveQueueId)
            .topicType(topicType)
            .brokerName(brokerName)
            .queueId(queueId)
            .offset(offset)
            .commitLogOffset(commitLogOffset)
            .receiptHandle(receiptHandle).build();
    }

    /** 包内构造：由 {@link ReceiptHandleBuilder} 调用。 */
    ReceiptHandle(final long startOffset, final long retrieveTime, final long invisibleTime, final long nextVisibleTime,
        final int reviveQueueId, final String topicType, final String brokerName, final int queueId, final long offset,
        final long commitLogOffset, final String receiptHandle) {
        this.startOffset = startOffset;
        this.retrieveTime = retrieveTime;
        this.invisibleTime = invisibleTime;
        this.nextVisibleTime = nextVisibleTime;
        this.reviveQueueId = reviveQueueId;
        this.topicType = topicType;
        this.brokerName = brokerName;
        this.queueId = queueId;
        this.offset = offset;
        this.commitLogOffset = commitLogOffset;
        this.receiptHandle = receiptHandle;
    }

    /** {@link ReceiptHandle} 建造者。 */
    public static class ReceiptHandleBuilder {
        private long startOffset;
        private long retrieveTime;
        private long invisibleTime;
        private int reviveQueueId;
        private String topicType;
        private String brokerName;
        private int queueId;
        private long offset;
        private long commitLogOffset;
        private String receiptHandle;

        /** 包内可见的无参构造。 */
        ReceiptHandleBuilder() {
        }

        /** 设置 POP 起始位点。 */
        public ReceiptHandle.ReceiptHandleBuilder startOffset(final long startOffset) {
            this.startOffset = startOffset;
            return this;
        }

        /** 设置拉取时间戳。 */
        public ReceiptHandle.ReceiptHandleBuilder retrieveTime(final long retrieveTime) {
            this.retrieveTime = retrieveTime;
            return this;
        }

        /** 设置不可见时长。 */
        public ReceiptHandle.ReceiptHandleBuilder invisibleTime(final long invisibleTime) {
            this.invisibleTime = invisibleTime;
            return this;
        }

        /** 设置复活队列 ID。 */
        public ReceiptHandle.ReceiptHandleBuilder reviveQueueId(final int reviveQueueId) {
            this.reviveQueueId = reviveQueueId;
            return this;
        }

        /** 设置 Topic 类型标识。 */
        public ReceiptHandle.ReceiptHandleBuilder topicType(final String topicType) {
            this.topicType = topicType;
            return this;
        }

        /** 设置 Broker 名称。 */
        public ReceiptHandle.ReceiptHandleBuilder brokerName(final String brokerName) {
            this.brokerName = brokerName;
            return this;
        }

        /** 设置队列 ID。 */
        public ReceiptHandle.ReceiptHandleBuilder queueId(final int queueId) {
            this.queueId = queueId;
            return this;
        }

        /** 设置消费队列 offset。 */
        public ReceiptHandle.ReceiptHandleBuilder offset(final long offset) {
            this.offset = offset;
            return this;
        }

        /** 设置 CommitLog 物理 offset。 */
        public ReceiptHandle.ReceiptHandleBuilder commitLogOffset(final long commitLogOffset) {
            this.commitLogOffset = commitLogOffset;
            return this;
        }

        /** 设置原始回执字符串。 */
        public ReceiptHandle.ReceiptHandleBuilder receiptHandle(final String receiptHandle) {
            this.receiptHandle = receiptHandle;
            return this;
        }

        /** 构建不可变 {@link ReceiptHandle} 实例。 */
        public ReceiptHandle build() {
            return new ReceiptHandle(this.startOffset, this.retrieveTime, this.invisibleTime, this.retrieveTime + this.invisibleTime,
                this.reviveQueueId, this.topicType, this.brokerName, this.queueId, this.offset, this.commitLogOffset, this.receiptHandle);
        }

        @Override
        public String toString() {
            return "ReceiptHandle.ReceiptHandleBuilder(startOffset=" + this.startOffset + ", retrieveTime=" + this.retrieveTime + ", invisibleTime=" + this.invisibleTime + ", reviveQueueId=" + this.reviveQueueId + ", topic=" + this.topicType + ", brokerName=" + this.brokerName + ", queueId=" + this.queueId + ", offset=" + this.offset + ", commitLogOffset=" + this.commitLogOffset + ", receiptHandle=" + this.receiptHandle + ")";
        }
    }

    /** 创建 {@link ReceiptHandleBuilder}。 */
    public static ReceiptHandle.ReceiptHandleBuilder builder() {
        return new ReceiptHandle.ReceiptHandleBuilder();
    }

    /** 返回 POP 起始位点。 */
    public long getStartOffset() {
        return this.startOffset;
    }

    /** 返回拉取时间戳。 */
    public long getRetrieveTime() {
        return this.retrieveTime;
    }

    /** 返回不可见时长。 */
    public long getInvisibleTime() {
        return this.invisibleTime;
    }

    /** 返回下次可见时间戳。 */
    public long getNextVisibleTime() {
        return this.nextVisibleTime;
    }

    /** 返回复活队列 ID。 */
    public int getReviveQueueId() {
        return this.reviveQueueId;
    }

    /** 返回 Topic 类型标识。 */
    public String getTopicType() {
        return this.topicType;
    }

    /** 返回 Broker 名称。 */
    public String getBrokerName() {
        return this.brokerName;
    }

    /** 返回队列 ID。 */
    public int getQueueId() {
        return this.queueId;
    }

    /** 返回消费队列 offset。 */
    public long getOffset() {
        return this.offset;
    }

    /** 返回 CommitLog 物理 offset。 */
    public long getCommitLogOffset() {
        return commitLogOffset;
    }

    /** 返回原始回执字符串。 */
    public String getReceiptHandle() {
        return this.receiptHandle;
    }

    /** 判断是否为重试 Topic（V1 或 V2）。 */
    public boolean isRetryTopic() {
        return RETRY_TOPIC.equals(topicType) || RETRY_TOPIC_V2.equals(topicType);
    }

    /**
     * 根据 Topic 类型解析实际消费 Topic（重试 Topic 需经 {@link KeyBuilder} 构建）。
     *
     * @param topic 原始 Topic
     * @param groupName 消费组名
     * @return 实际 Topic 名
     */
    public String getRealTopic(String topic, String groupName) {
        if (RETRY_TOPIC.equals(topicType)) {
            return KeyBuilder.buildPopRetryTopicV1(topic, groupName);
        }
        if (RETRY_TOPIC_V2.equals(topicType)) {
            return KeyBuilder.buildPopRetryTopicV2(topic, groupName);
        }
        return topic;
    }
}
