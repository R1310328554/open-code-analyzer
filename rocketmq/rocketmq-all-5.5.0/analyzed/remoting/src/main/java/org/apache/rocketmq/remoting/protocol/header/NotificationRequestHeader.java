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
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.TopicQueueRequestHeader;

/**
 * Pop 长轮询通知请求头：Consumer 向 Broker 注册对指定 Topic/Queue 的消息到达通知。
 * Broker 在有新消息或超时时通过 {@link NotificationResponseHeader} 响应。
 */
@RocketMQAction(value = RequestCode.NOTIFICATION, action = Action.SUB)
public class NotificationRequestHeader extends TopicQueueRequestHeader {
    /** 消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String consumerGroup;
    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 消息队列 ID。 */
    @CFNotNull
    private int queueId;
    /** 长轮询超时时间（毫秒）。 */
    @CFNotNull
    private long pollTime;
    /** 请求创建时间戳（毫秒）。 */
    @CFNotNull
    private long bornTime;

    /** 是否顺序消费，默认 false。 */
    private Boolean order = Boolean.FALSE;
    /** Pop 消费尝试 ID，用于幂等与追踪。 */
    private String attemptId;

    /** 消息过滤表达式类型（如 TAG、SQL92）。 */
    private String expType;
    /** 消息过滤表达式内容。 */
    private String exp;

    /** 校验请求头必填字段（由框架注解驱动，空实现）。 */
    @CFNotNull
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回长轮询超时时间。 */
    public long getPollTime() {
        return pollTime;
    }

    /** 设置长轮询超时时间。 */
    public void setPollTime(long pollTime) {
        this.pollTime = pollTime;
    }

    /** 返回消费组名称。 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /** 设置消费组名称。 */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /** 返回请求创建时间戳。 */
    public long getBornTime() {
        return bornTime;
    }

    /** 设置请求创建时间戳。 */
    public void setBornTime(long bornTime) {
        this.bornTime = bornTime;
    }

    /** 返回 Topic 名称。 */
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic 名称。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回队列 ID；负值归一化为 -1。 */
    public Integer getQueueId() {
        if (queueId < 0) {
            return -1;
        }
        return queueId;
    }

    /** 设置队列 ID。 */
    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    /** 返回是否顺序消费。 */
    public Boolean getOrder() {
        return order;
    }

    /** 设置是否顺序消费。 */
    public void setOrder(Boolean order) {
        this.order = order;
    }

    /** 返回 Pop 尝试 ID。 */
    public String getAttemptId() {
        return attemptId;
    }

    /** 设置 Pop 尝试 ID。 */
    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }

    /** 返回过滤表达式类型。 */
    public String getExpType() {
        return expType;
    }

    /** 设置过滤表达式类型。 */
    public void setExpType(String expType) {
        this.expType = expType;
    }

    /** 返回过滤表达式内容。 */
    public String getExp() {
        return exp;
    }

    /** 设置过滤表达式内容。 */
    public void setExp(String exp) {
        this.exp = exp;
    }

    /** 返回含消费组、Topic、队列及轮询参数的调试字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("consumerGroup", consumerGroup)
            .add("topic", topic)
            .add("queueId", queueId)
            .add("pollTime", pollTime)
            .add("bornTime", bornTime)
            .add("order", order)
            .add("attemptId", attemptId)
            .toString();
    }
}
