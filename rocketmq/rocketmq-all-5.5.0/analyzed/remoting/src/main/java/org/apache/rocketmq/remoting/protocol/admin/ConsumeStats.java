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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 消费组消费统计：各 {@link MessageQueue} 的偏移量快照与消费 TPS。
 */
public class ConsumeStats extends RemotingSerializable {
    /** 队列 → 偏移量包装（Broker/Consumer/Pull 进度）。 */
    private Map<MessageQueue, OffsetWrapper> offsetTable = new ConcurrentHashMap<>();
    /** 消费 TPS（条/秒）。 */
    private double consumeTps = 0;

    /** 汇总 brokerOffset − consumerOffset，衡量消息堆积量。 */
    public long computeTotalDiff() {
        long diffTotal = 0L;
        for (Entry<MessageQueue, OffsetWrapper> entry : this.offsetTable.entrySet()) {
            diffTotal += entry.getValue().getBrokerOffset() - entry.getValue().getConsumerOffset();
        }
        return diffTotal;
    }

    /** 汇总 pullOffset − consumerOffset，衡量在途未 ack 消息量。 */
    public long computeInflightTotalDiff() {
        long diffTotal = 0L;
        for (Entry<MessageQueue, OffsetWrapper> entry : this.offsetTable.entrySet()) {
            diffTotal += entry.getValue().getPullOffset() - entry.getValue().getConsumerOffset();
        }
        return diffTotal;
    }

    /** 返回偏移量表。 */
    public Map<MessageQueue, OffsetWrapper> getOffsetTable() {
        return offsetTable;
    }

    /** 设置偏移量表。 */
    public void setOffsetTable(Map<MessageQueue, OffsetWrapper> offsetTable) {
        this.offsetTable = offsetTable;
    }

    /** 返回消费 TPS。 */
    public double getConsumeTps() {
        return consumeTps;
    }

    /** 设置消费 TPS。 */
    public void setConsumeTps(double consumeTps) {
        this.consumeTps = consumeTps;
    }
}
