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

/**
 * $Id: ConsumerData.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.heartbeat;

import java.util.HashSet;
import java.util.Set;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;

/**
 * 心跳中的消费者数据：描述消费组、消费类型、消息模型、订阅集合与单元化标志。
 */
public class ConsumerData {
    /** 消费组名称。 */
    private String groupName;
    /** 消费类型（Push/Pull/Pop）。 */
    private ConsumeType consumeType;
    /** 消息模型（集群/广播）。 */
    private MessageModel messageModel;
    /** 初次启动时的消费位点策略。 */
    private ConsumeFromWhere consumeFromWhere;
    /** 当前订阅 Topic 集合。 */
    private Set<SubscriptionData> subscriptionDataSet = new HashSet<>();
    /** 是否启用单元化模式。 */
    private boolean unitMode;

    /** 返回消费组名称。 */
    public String getGroupName() {
        return groupName;
    }

    /** 设置消费组名称。 */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    /** 返回消费位点策略。 */
    public ConsumeFromWhere getConsumeFromWhere() {
        return consumeFromWhere;
    }

    /** 设置消费位点策略。 */
    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }

    /** 返回订阅数据集合。 */
    public Set<SubscriptionData> getSubscriptionDataSet() {
        return subscriptionDataSet;
    }

    /** 设置订阅数据集合。 */
    public void setSubscriptionDataSet(Set<SubscriptionData> subscriptionDataSet) {
        this.subscriptionDataSet = subscriptionDataSet;
    }

    /** 返回是否单元化模式。 */
    public boolean isUnitMode() {
        return unitMode;
    }

    /** 设置单元化模式标志。 */
    public void setUnitMode(boolean isUnitMode) {
        this.unitMode = isUnitMode;
    }

    /** 返回含消费组、类型与订阅集合的调试字符串。 */
    @Override
    public String toString() {
        return "ConsumerData [groupName=" + groupName + ", consumeType=" + consumeType + ", messageModel="
            + messageModel + ", consumeFromWhere=" + consumeFromWhere + ", unitMode=" + unitMode
            + ", subscriptionDataSet=" + subscriptionDataSet + "]";
    }
}
