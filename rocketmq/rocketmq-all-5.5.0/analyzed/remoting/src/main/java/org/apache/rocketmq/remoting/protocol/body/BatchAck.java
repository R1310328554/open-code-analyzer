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
package org.apache.rocketmq.remoting.protocol.body;

import com.alibaba.fastjson2.annotation.JSONField;
import org.apache.rocketmq.remoting.protocol.BitSetSerializerDeserializer;

import java.io.Serializable;
import java.util.BitSet;

/**
 * Pop 消费批量 Ack 单元：以 BitSet 标记相对 startOffset 的已确认消息。
 */
public class BatchAck implements Serializable {
    /** 消费组名。 */
    @JSONField(name = "c", alternateNames = {"consumerGroup"})
    private String consumerGroup;
    /** Topic 名称。 */
    @JSONField(name = "t", alternateNames = {"topic"})
    private String topic;
    @JSONField(name = "r", alternateNames = {"retry"})
    /** 是否为重试 Topic（"1" 表示是）。 */
    private String retry; // "1" if is retry topic
    /** BitSet 基准起始偏移。 */
    @JSONField(name = "so", alternateNames = {"startOffset"})
    private long startOffset;
    /** 消息队列 ID。 */
    @JSONField(name = "q", alternateNames = {"queueId"})
    private int queueId;
    /** 复活队列 ID（Pop 超时重投）。 */
    @JSONField(name = "rq", alternateNames = {"reviveQueueId"})
    private int reviveQueueId;
    /** Pop 请求时间戳。 */
    @JSONField(name = "pt", alternateNames = {"popTime"})
    private long popTime;
    /** 消息不可见时长（毫秒）。 */
    @JSONField(name = "it", alternateNames = {"invisibleTime"})
    private long invisibleTime;
    @JSONField(name = "b", alternateNames = {"bitSet"}, serializeUsing = BitSetSerializerDeserializer.class, deserializeUsing = BitSetSerializerDeserializer.class)
    /** 相对 startOffset 的已 Ack 偏移位图。 */
    private BitSet bitSet; // ack offsets bitSet

    /** 返回消费组。 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getRetry() {
        return retry;
    }

    public void setRetry(String retry) {
        this.retry = retry;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public int getReviveQueueId() {
        return reviveQueueId;
    }

    public void setReviveQueueId(int reviveQueueId) {
        this.reviveQueueId = reviveQueueId;
    }

    public long getPopTime() {
        return popTime;
    }

    public void setPopTime(long popTime) {
        this.popTime = popTime;
    }

    public long getInvisibleTime() {
        return invisibleTime;
    }

    public void setInvisibleTime(long invisibleTime) {
        this.invisibleTime = invisibleTime;
    }

    /** 返回 Ack 位图。 */
    public BitSet getBitSet() {
        return bitSet;
    }

    public void setBitSet(BitSet bitSet) {
        this.bitSet = bitSet;
    }

    /** 返回 BatchAck 调试字符串。 */
    @Override
    public String toString() {
        return "BatchAck{" +
                "consumerGroup='" + consumerGroup + '\'' +
                ", topic='" + topic + '\'' +
                ", retry='" + retry + '\'' +
                ", startOffset=" + startOffset +
                ", queueId=" + queueId +
                ", reviveQueueId=" + reviveQueueId +
                ", popTime=" + popTime +
                ", invisibleTime=" + invisibleTime +
                ", bitSet=" + bitSet +
                '}';
    }
}
