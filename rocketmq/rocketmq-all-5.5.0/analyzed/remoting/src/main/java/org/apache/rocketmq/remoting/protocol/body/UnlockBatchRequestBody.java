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

import com.google.common.base.MoreObjects;
import java.util.HashSet;
import java.util.Set;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 批量解锁 MessageQueue 请求体：Pop 消费模式下释放已锁定的队列。
 */
public class UnlockBatchRequestBody extends RemotingSerializable {
    /** 消费组名称。 */
    private String consumerGroup;
    /** 发起解锁的客户端 ID。 */
    private String clientId;
    /** 是否仅在本 Broker 解锁，默认 false。 */
    private boolean onlyThisBroker = false;
    /** 待解锁的 MessageQueue 集合。 */
    private Set<MessageQueue> mqSet = new HashSet<>();

    /** 返回消费组。 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isOnlyThisBroker() {
        return onlyThisBroker;
    }

    public void setOnlyThisBroker(boolean onlyThisBroker) {
        this.onlyThisBroker = onlyThisBroker;
    }

    /** 返回待解锁队列集合。 */
    public Set<MessageQueue> getMqSet() {
        return mqSet;
    }

    public void setMqSet(Set<MessageQueue> mqSet) {
        this.mqSet = mqSet;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("consumerGroup", consumerGroup)
            .add("clientId", clientId)
            .add("onlyThisBroker", onlyThisBroker)
            .add("mqSet", mqSet)
            .toString();
    }
}
