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
package org.apache.rocketmq.store.pop;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * Pop 消费确认消息：记录 ack 偏移、主题、队列及 pop 时间等元数据。
 */
public class AckMsg {

    /** 已确认的队列逻辑偏移。 */
    @JSONField(name = "ao", alternateNames = {"ackOffset"})
    private long ackOffset;

    /** 本次 pop 批次的起始偏移。 */
    @JSONField(name = "so", alternateNames = {"startOffset"})
    private long startOffset;

    /** 消费者组名。 */
    @JSONField(name = "c", alternateNames = {"consumerGroup"})
    private String consumerGroup;

    /** 主题名。 */
    @JSONField(name = "t", alternateNames = {"topic"})
    private String topic;

    /** 队列 ID。 */
    @JSONField(name = "q", alternateNames = {"queueId"})
    private int queueId;

    /** Pop 操作发生的时间戳。 */
    @JSONField(name = "pt", alternateNames = {"popTime"})
    private long popTime;

    /** 处理 pop 的 Broker 名称。 */
    @JSONField(name = "bn", alternateNames = {"brokerName"})
    private String brokerName;

    /** 返回 pop 时间戳。 */
    public long getPopTime() {
        return popTime;
    }

    public void setPopTime(long popTime) {
        this.popTime = popTime;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    /** 返回 ack 偏移。 */
    public long getAckOffset() {
        return ackOffset;
    }

    /** 返回消费者组名。 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public void setAckOffset(long ackOffset) {
        this.ackOffset = ackOffset;
    }

    /** 返回起始偏移。 */
    public long getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }

    /** 返回 Broker 名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    /** 返回包含各字段的可读字符串。 */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AckMsg{");
        sb.append("ackOffset=").append(ackOffset);
        sb.append(", startOffset=").append(startOffset);
        sb.append(", consumerGroup='").append(consumerGroup).append('\'');
        sb.append(", topic='").append(topic).append('\'');
        sb.append(", queueId=").append(queueId);
        sb.append(", popTime=").append(popTime);
        sb.append(", brokerName=").append(brokerName);
        sb.append('}');
        return sb.toString();
    }
}
