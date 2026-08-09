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
package org.apache.rocketmq.broker.pop;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * POP 投递记录：序列化后写入 {@link PopConsumerKVStore}，用于可见性超时检查与重试调度。
 * Key 由可见性超时时间戳 + groupId + topicId + queueId + offset 组成。
 */
public class PopConsumerRecord {

    /** POP 重试 topic 版本标识。 */
    public enum RetryType {

        /** 普通 topic，非重试。 */
        NORMAL_TOPIC(0),

        /** 重试 topic V1 格式。 */
        RETRY_TOPIC_V1(1),

        /** 重试 topic V2 格式。 */
        RETRY_TOPIC_V2(2);

        private final int code;

        RetryType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    @JSONField()
    private long popTime;

    @JSONField(ordinal = 1)
    private String groupId;

    @JSONField(ordinal = 2)
    private String topicId;

    @JSONField(ordinal = 3)
    private int queueId;

    @JSONField(ordinal = 4)
    private int retryFlag;

    @JSONField(ordinal = 5)
    private long invisibleTime;

    @JSONField(ordinal = 6)
    private long offset;

    @JSONField(ordinal = 7)
    private int attemptTimes;

    @JSONField(ordinal = 8)
    private String attemptId;

    @JSONField(ordinal = 9)
    private boolean suspend;

    // 供测试与 fastjson 反序列化使用
    public PopConsumerRecord() {
    }

    public PopConsumerRecord(long popTime, String groupId, String topicId, int queueId,
        int retryFlag, long invisibleTime, long offset, String attemptId) {
        this(popTime, groupId, topicId, queueId, retryFlag, invisibleTime, offset, attemptId, false);
    }

    public PopConsumerRecord(long popTime, String groupId, String topicId, int queueId, int retryFlag,
                             long invisibleTime, long offset, String attemptId, boolean suspend) {

        this.popTime = popTime;
        this.groupId = groupId;
        this.topicId = topicId;
        this.queueId = queueId;
        this.retryFlag = retryFlag;
        this.invisibleTime = invisibleTime;
        this.offset = offset;
        this.attemptId = attemptId;
        this.suspend = suspend;
    }

    @JSONField(serialize = false)
    public long getVisibilityTimeout() {
        return popTime + invisibleTime;
    }

    /** Key 布局：可见性超时时间戳(8B) + groupId + topicId + queueId + offset */
    @JSONField(serialize = false)
    /** 生成 RocksDB 存储键：以可见性超时时间为前缀便于范围扫描。 */
    public byte[] getKeyBytes() {
        int length = Long.BYTES + groupId.length() + 1 + topicId.length() + 1 + Integer.BYTES + 1 + Long.BYTES;
        byte[] bytes = new byte[length];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.putLong(this.getVisibilityTimeout());
        buffer.put(groupId.getBytes(StandardCharsets.UTF_8)).put((byte) '@');
        buffer.put(topicId.getBytes(StandardCharsets.UTF_8)).put((byte) '@');
        buffer.putInt(queueId).put((byte) '@');
        buffer.putLong(offset);
        return bytes;
    }

    @JSONField(serialize = false)
    /** 是否为重试 topic 上的 POP 记录。 */
    public boolean isRetry() {
        return retryFlag != 0;
    }

    @JSONField(serialize = false)
    /** 将记录序列化为 JSON 字节数组作为 value。 */
    public byte[] getValueBytes() {
        return JSON.toJSONBytes(this);
    }

    /** 从 JSON 字节反序列化 POP 记录。 */
    public static PopConsumerRecord decode(byte[] body) {
        return JSON.parseObject(body, PopConsumerRecord.class);
    }

    public long getPopTime() {
        return popTime;
    }

    public void setPopTime(long popTime) {
        this.popTime = popTime;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public int getRetryFlag() {
        return retryFlag;
    }

    public void setRetryFlag(int retryFlag) {
        this.retryFlag = retryFlag;
    }

    public long getInvisibleTime() {
        return invisibleTime;
    }

    public void setInvisibleTime(long invisibleTime) {
        this.invisibleTime = invisibleTime;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getAttemptTimes() {
        return attemptTimes;
    }

    public void setAttemptTimes(int attemptTimes) {
        this.attemptTimes = attemptTimes;
    }

    public String getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }

    public boolean isSuspend() {
        return suspend;
    }

    public void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }

    @Override
    public String toString() {
        return "PopDeliveryRecord{" +
            "popTime=" + popTime +
            ", groupId='" + groupId + '\'' +
            ", topicId='" + topicId + '\'' +
            ", queueId=" + queueId +
            ", retryFlag=" + retryFlag +
            ", invisibleTime=" + invisibleTime +
            ", offset=" + offset +
            ", attemptTimes=" + attemptTimes +
            ", attemptId='" + attemptId + '\'' +
            ", suspend=" + suspend +
            '}';
    }
}
