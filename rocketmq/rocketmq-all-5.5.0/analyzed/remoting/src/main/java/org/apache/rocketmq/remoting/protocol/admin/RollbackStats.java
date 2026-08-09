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

package org.apache.rocketmq.remoting.protocol.admin;

/**
 * 按时间戳回退消费位点的统计项：记录 Broker/Consumer 偏移与目标回退位点。
 */
public class RollbackStats {
    /** Broker 名称。 */
    private String brokerName;
    /** 队列 ID。 */
    private long queueId;
    /** 回退前 Broker 最大偏移。 */
    private long brokerOffset;
    /** 回退前消费组偏移。 */
    private long consumerOffset;
    /** 按时间戳匹配到的目标偏移。 */
    private long timestampOffset;
    /** 实际回退到的偏移。 */
    private long rollbackOffset;

    /** 返回 Broker 名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    /** 返回队列 ID。 */
    public long getQueueId() {
        return queueId;
    }

    /** 设置队列 ID。 */
    public void setQueueId(long queueId) {
        this.queueId = queueId;
    }

    public long getBrokerOffset() {
        return brokerOffset;
    }

    public void setBrokerOffset(long brokerOffset) {
        this.brokerOffset = brokerOffset;
    }

    public long getConsumerOffset() {
        return consumerOffset;
    }

    public void setConsumerOffset(long consumerOffset) {
        this.consumerOffset = consumerOffset;
    }

    public long getTimestampOffset() {
        return timestampOffset;
    }

    public void setTimestampOffset(long timestampOffset) {
        this.timestampOffset = timestampOffset;
    }

    public long getRollbackOffset() {
        return rollbackOffset;
    }

    public void setRollbackOffset(long rollbackOffset) {
        this.rollbackOffset = rollbackOffset;
    }
}
