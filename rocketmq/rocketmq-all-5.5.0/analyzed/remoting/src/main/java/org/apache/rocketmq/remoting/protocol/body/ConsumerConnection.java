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

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import org.apache.rocketmq.remoting.protocol.heartbeat.ConsumeType;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

/**
 * 消费组连接快照：在线客户端、订阅表及消费模式/起始位点策略。
 */
public class ConsumerConnection extends RemotingSerializable {
    /** 当前在线客户端连接集合。 */
    private HashSet<Connection> connectionSet = new HashSet<>();
    /** Topic → 订阅数据并发映射。 */
    private ConcurrentMap<String/* Topic */, SubscriptionData> subscriptionTable =
        new ConcurrentHashMap<>();
    /** 消费类型（Push/Pull/Pop 等）。 */
    private ConsumeType consumeType;
    /** 消息模型（集群/广播）。 */
    private MessageModel messageModel;
    /** 首次启动消费起始策略。 */
    private ConsumeFromWhere consumeFromWhere;

    /** 计算连接集合中最低的 Remoting 协议版本。 */
    public int computeMinVersion() {
        int minVersion = Integer.MAX_VALUE;
        for (Connection c : this.connectionSet) {
            if (c.getVersion() < minVersion) {
                minVersion = c.getVersion();
            }
        }

        return minVersion;
    }

    /** 返回连接集合。 */
    public HashSet<Connection> getConnectionSet() {
        return connectionSet;
    }

    /** 设置连接集合。 */
    public void setConnectionSet(HashSet<Connection> connectionSet) {
        this.connectionSet = connectionSet;
    }

    /** 返回订阅表。 */
    public ConcurrentMap<String, SubscriptionData> getSubscriptionTable() {
        return subscriptionTable;
    }

    /** 设置订阅表。 */
    public void setSubscriptionTable(ConcurrentHashMap<String, SubscriptionData> subscriptionTable) {
        this.subscriptionTable = subscriptionTable;
    }

    /** 返回消费类型。 */
    public ConsumeType getConsumeType() {
        return consumeType;
    }

    /** 设置消费类型。 */
    public void setConsumeType(ConsumeType consumeType) {
        this.consumeType = consumeType;
    }

    /** 返回消息模型。 */
    public MessageModel getMessageModel() {
        return messageModel;
    }

    /** 设置消息模型。 */
    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    /** 返回起始消费策略。 */
    public ConsumeFromWhere getConsumeFromWhere() {
        return consumeFromWhere;
    }

    /** 设置起始消费策略。 */
    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }
}
