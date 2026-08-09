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
package org.apache.rocketmq.store;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageConst;

/**
 * CommitLog 分发请求：携带单条（或批量）消息在存储层的元数据。
 * 供 ReputMessageService 与各 Dispatcher 构建 ConsumeQueue/索引。
 */
public class DispatchRequest {
    /** Topic 名称。 */
    private final String topic;
    /** 队列 ID。 */
    private final int queueId;
    /** 消息在 CommitLog 中的物理偏移。 */
    private final long commitLogOffset;
    /** 消息体大小（字节）。 */
    private int msgSize;
    /** Tag 哈希码，用于 ConsumeQueue 过滤。 */
    private final long tagsCode;
    /** 存储时间戳。 */
    private final long storeTimestamp;
    /** 对应 ConsumeQueue 逻辑偏移。 */
    private final long consumeQueueOffset;
    /** 消息 Keys（可为空）。 */
    private final String keys;
    /** 解析/构造是否成功。 */
    private final boolean success;
    /** 消息唯一键（如 UNIQ_KEY 属性）。 */
    private final String uniqKey;

    /** 系统标志位。 */
    private final int sysFlag;
    /** 事务消息 prepared 偏移（非事务为 0）。 */
    private final long preparedTransactionOffset;
    /** 消息用户属性映射。 */
    private final Map<String, String> propertiesMap;
    /** 可选位图（如 SQL92 过滤）。 */
    private byte[] bitMap;

    /** 缓冲区大小（可能大于 msgSize，例如外层包装）。 */
    private int bufferSize = -1;

    /** 批量 ConsumeQueue 的起始逻辑偏移。 */
    private long  msgBaseOffset = -1;
    /** 批量消息条数。 */
    private short batchSize = 1;

    /** 下次 Reput 起始物理偏移（-1 表示默认）。 */
    private long nextReputFromOffset = -1;

    /** 偏移标识（扩展用途）。 */
    private String offsetId;

    public DispatchRequest(
        final String topic,
        final int queueId,
        final long commitLogOffset,
        final int msgSize,
        final long tagsCode,
        final long storeTimestamp,
        final long consumeQueueOffset,
        final String keys,
        final String uniqKey,
        final int sysFlag,
        final long preparedTransactionOffset,
        final Map<String, String> propertiesMap
    ) {
        this.topic = topic;
        this.queueId = queueId;
        this.commitLogOffset = commitLogOffset;
        this.msgSize = msgSize;
        this.tagsCode = tagsCode;
        this.storeTimestamp = storeTimestamp;
        this.consumeQueueOffset = consumeQueueOffset;
        this.msgBaseOffset = consumeQueueOffset;
        this.keys = keys;
        this.uniqKey = uniqKey;

        this.sysFlag = sysFlag;
        this.preparedTransactionOffset = preparedTransactionOffset;
        this.success = true;
        this.propertiesMap = propertiesMap;
    }

    public DispatchRequest(String topic, int queueId, long consumeQueueOffset, long commitLogOffset, int size, long tagsCode) {
        this.topic = topic;
        this.queueId = queueId;
        this.commitLogOffset = commitLogOffset;
        this.msgSize = size;
        this.tagsCode = tagsCode;
        this.storeTimestamp = 0;
        this.consumeQueueOffset = consumeQueueOffset;
        this.keys = "";
        this.uniqKey = null;
        this.sysFlag = 0;
        this.preparedTransactionOffset = 0;
        this.success = false;
        this.propertiesMap = null;
    }

    public DispatchRequest(int size) {
        this.topic = "";
        this.queueId = 0;
        this.commitLogOffset = 0;
        this.msgSize = size;
        this.tagsCode = 0;
        this.storeTimestamp = 0;
        this.consumeQueueOffset = 0;
        this.keys = "";
        this.uniqKey = null;
        this.sysFlag = 0;
        this.preparedTransactionOffset = 0;
        this.success = false;
        this.propertiesMap = null;
    }

    public DispatchRequest(int size, boolean success) {
        this.topic = "";
        this.queueId = 0;
        this.commitLogOffset = 0;
        this.msgSize = size;
        this.tagsCode = 0;
        this.storeTimestamp = 0;
        this.consumeQueueOffset = 0;
        this.keys = "";
        this.uniqKey = null;
        this.sysFlag = 0;
        this.preparedTransactionOffset = 0;
        this.success = success;
        this.propertiesMap = null;
    }

    public String getTopic() {
        return topic;
    }

    public int getQueueId() {
        return queueId;
    }

    public long getCommitLogOffset() {
        return commitLogOffset;
    }

    public int getMsgSize() {
        return msgSize;
    }

    public long getStoreTimestamp() {
        return storeTimestamp;
    }

    public long getConsumeQueueOffset() {
        return consumeQueueOffset;
    }

    public String getKeys() {
        return keys;
    }

    public long getTagsCode() {
        return tagsCode;
    }

    public int getSysFlag() {
        return sysFlag;
    }

    public long getPreparedTransactionOffset() {
        return preparedTransactionOffset;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getUniqKey() {
        return uniqKey;
    }

    public Map<String, String> getPropertiesMap() {
        return propertiesMap;
    }

    public byte[] getBitMap() {
        return bitMap;
    }

    public void setBitMap(byte[] bitMap) {
        this.bitMap = bitMap;
    }

    public short getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(short batchSize) {
        this.batchSize = batchSize;
    }

    public void setMsgSize(int msgSize) {
        this.msgSize = msgSize;
    }

    public long getMsgBaseOffset() {
        return msgBaseOffset;
    }

    public void setMsgBaseOffset(long msgBaseOffset) {
        this.msgBaseOffset = msgBaseOffset;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public long getNextReputFromOffset() {
        return nextReputFromOffset;
    }

    public void setNextReputFromOffset(long nextReputFromOffset) {
        this.nextReputFromOffset = nextReputFromOffset;
    }

    public String getOffsetId() {
        return offsetId;
    }

    public void setOffsetId(String offsetId) {
        this.offsetId = offsetId;
    }

    /** 是否包含轻量级消息队列（LMQ）多路分发属性。 */
    public boolean containsLMQ() {
        if (!MixAll.topicAllowsLMQ(topic)) {
            return false;
        }
        if (null == propertiesMap || propertiesMap.isEmpty()) {
            return false;
        }
        String lmqNames = propertiesMap.get(MessageConst.PROPERTY_INNER_MULTI_DISPATCH);
        String lmqOffsets = propertiesMap.get(MessageConst.PROPERTY_INNER_MULTI_QUEUE_OFFSET);
        return !StringUtils.isBlank(lmqNames) && !StringUtils.isBlank(lmqOffsets);
    }

    @Override
    public String toString() {
        return "DispatchRequest{" +
                "topic='" + topic + '\'' +
                ", queueId=" + queueId +
                ", commitLogOffset=" + commitLogOffset +
                ", msgSize=" + msgSize +
                ", success=" + success +
                ", msgBaseOffset=" + msgBaseOffset +
                ", batchSize=" + batchSize +
                ", nextReputFromOffset=" + nextReputFromOffset +
            '}';
    }
}
