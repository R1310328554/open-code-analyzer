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

package org.apache.rocketmq.proxy.common;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.rocketmq.common.consumer.ReceiptHandle;

/**
 * 消息回执句柄：封装 POP 消费场景下的 receipt handle、队列位置与续期计数。
 */
public class MessageReceiptHandle {
    /** 消费者组名。 */
    private final String group;
    /** 消息所属 Topic。 */
    private final String topic;
    /** 消息所在队列 ID。 */
    private final int queueId;
    /** 消息唯一标识。 */
    private final String messageId;
    private final long queueOffset;
    private final String originalReceiptHandleStr;
    private final ReceiptHandle originalReceiptHandle;
    private final int reconsumeTimes;

    /** 续期失败重试次数。 */
    private final AtomicInteger renewRetryTimes = new AtomicInteger(0);
    private final AtomicInteger renewTimes = new AtomicInteger(0);
    private final long consumeTimestamp;
    private String liteTopic;
    private volatile String receiptHandleStr;

    /** 构造回执句柄（不含 Lite Topic）。 */
    public MessageReceiptHandle(String group, String topic, int queueId, String receiptHandleStr, String messageId,
        long queueOffset, int reconsumeTimes) {
        this(group, topic, queueId, receiptHandleStr, messageId, queueOffset, reconsumeTimes, null);
    }

    public MessageReceiptHandle(String group, String topic, int queueId, String receiptHandleStr, String messageId,
        long queueOffset, int reconsumeTimes, String liteTopic) {
        // 解码原始 receipt handle 并记录首次消费时间
        this.originalReceiptHandle = ReceiptHandle.decode(receiptHandleStr);
        this.group = group;
        this.topic = topic;
        this.queueId = queueId;
        this.receiptHandleStr = receiptHandleStr;
        this.originalReceiptHandleStr = receiptHandleStr;
        this.messageId = messageId;
        this.queueOffset = queueOffset;
        this.reconsumeTimes = reconsumeTimes;
        this.consumeTimestamp = originalReceiptHandle.getRetrieveTime();
        this.liteTopic = liteTopic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MessageReceiptHandle handle = (MessageReceiptHandle) o;
        return queueId == handle.queueId && queueOffset == handle.queueOffset && consumeTimestamp == handle.consumeTimestamp
            && reconsumeTimes == handle.reconsumeTimes
            && Objects.equal(group, handle.group) && Objects.equal(topic, handle.topic)
            && Objects.equal(messageId, handle.messageId) && Objects.equal(originalReceiptHandleStr, handle.originalReceiptHandleStr)
            && Objects.equal(receiptHandleStr, handle.receiptHandleStr);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(group, topic, queueId, messageId, queueOffset, originalReceiptHandleStr, consumeTimestamp,
            reconsumeTimes, receiptHandleStr);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("group", group)
            .add("topic", topic)
            .add("queueId", queueId)
            .add("messageId", messageId)
            .add("queueOffset", queueOffset)
            .add("originalReceiptHandleStr", originalReceiptHandleStr)
            .add("reconsumeTimes", reconsumeTimes)
            .add("renewRetryTimes", renewRetryTimes)
            .add("firstConsumeTimestamp", consumeTimestamp)
            .add("receiptHandleStr", receiptHandleStr)
            .add("liteTopic", liteTopic)
            .omitNullValues()
            .toString();
    }

    public String getGroup() {
        return group;
    }

    public String getTopic() {
        return topic;
    }

    public int getQueueId() {
        return queueId;
    }

    public String getReceiptHandleStr() {
        return receiptHandleStr;
    }

    public String getOriginalReceiptHandleStr() {
        return originalReceiptHandleStr;
    }

    public String getMessageId() {
        return messageId;
    }

    public long getQueueOffset() {
        return queueOffset;
    }

    public int getReconsumeTimes() {
        return reconsumeTimes;
    }

    public long getConsumeTimestamp() {
        return consumeTimestamp;
    }

    /** 续期成功后更新当前 receipt handle 字符串。 */
    public void updateReceiptHandle(String receiptHandleStr) {
        this.receiptHandleStr = receiptHandleStr;
    }

    /** 续期失败时递增重试计数并返回新值。 */
    public int incrementAndGetRenewRetryTimes() {
        return this.renewRetryTimes.incrementAndGet();
    }

    public int incrementRenewTimes() {
        return this.renewTimes.incrementAndGet();
    }

    public int getRenewTimes() {
        return this.renewTimes.get();
    }

    public void resetRenewRetryTimes() {
        this.renewRetryTimes.set(0);
    }

    public int getRenewRetryTimes() {
        return this.renewRetryTimes.get();
    }

    public ReceiptHandle getOriginalReceiptHandle() {
        return originalReceiptHandle;
    }

    public String getLiteTopic() {
        return liteTopic;
    }

    public void setLiteTopic(String liteTopic) {
        this.liteTopic = liteTopic;
    }
}
