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

package org.apache.rocketmq.remoting.protocol.header;

import com.google.common.base.MoreObjects;
import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.annotation.CFNullable;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.TopicRequestHeader;

/**
 * 直接消费消息结果查询请求头：按消费组、客户端与 MsgId 定位消费结果。
 */
@RocketMQAction(value = RequestCode.CONSUME_MESSAGE_DIRECTLY, action = Action.SUB)
public class ConsumeMessageDirectlyResultRequestHeader extends TopicRequestHeader {
    /** 消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String consumerGroup;
    /** 消费者客户端 ID（可选）。 */
    @CFNullable
    private String clientId;
    /** 目标消息 ID（可选）。 */
    @CFNullable
    private String msgId;
    /** 承载消息的 Broker 名称（可选）。 */
    @CFNullable
    private String brokerName;
    /** 消息所属 Topic（可选）。 */
    @CFNullable
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** Topic 系统标志位（可选）。 */
    @CFNullable
    private Integer topicSysFlag;
    /** 消费组系统标志位（可选）。 */
    @CFNullable
    private Integer groupSysFlag;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回消费组名称。 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /** 返回 Broker 名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** 返回消息 ID。 */
    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getTopicSysFlag() {
        return topicSysFlag;
    }

    public void setTopicSysFlag(Integer topicSysFlag) {
        this.topicSysFlag = topicSysFlag;
    }

    public Integer getGroupSysFlag() {
        return groupSysFlag;
    }

    public void setGroupSysFlag(Integer groupSysFlag) {
        this.groupSysFlag = groupSysFlag;
    }

    /** 返回便于诊断的可读字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("consumerGroup", consumerGroup)
            .add("clientId", clientId)
            .add("msgId", msgId)
            .add("brokerName", brokerName)
            .add("topic", topic)
            .add("topicSysFlag", topicSysFlag)
            .add("groupSysFlag", groupSysFlag)
            .toString();
    }
}
