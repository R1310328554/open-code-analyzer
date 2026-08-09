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
 * $Id: UpdateConsumerOffsetRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
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
 * 更新消费位点的请求头：消费端主动提交某队列的消费进度至 Broker。
 */
@RocketMQAction(value = RequestCode.UPDATE_CONSUMER_OFFSET, action = Action.SUB)
public class UpdateConsumerOffsetRequestHeader extends TopicQueueRequestHeader {
    /** 目标消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String consumerGroup;
    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 消息队列 ID。 */
    @CFNotNull
    private Integer queueId;
    /** 待提交的消费位点（逻辑 offset）。 */
    @CFNotNull
    private Long commitOffset;

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回消费组名称。 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /** 设置消费组名称。 */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /** 返回 Topic 名称。 */
    @Override
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic 名称。 */
    @Override
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回队列 ID。 */
    @Override
    public Integer getQueueId() {
        return queueId;
    }

    /** 设置队列 ID。 */
    @Override
    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    /** 返回待提交位点。 */
    public Long getCommitOffset() {
        return commitOffset;
    }

    /** 设置待提交位点。 */
    public void setCommitOffset(Long commitOffset) {
        this.commitOffset = commitOffset;
    }

    /** 返回含消费组、Topic、队列及位点的调试字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("consumerGroup", consumerGroup)
            .add("topic", topic)
            .add("queueId", queueId)
            .add("commitOffset", commitOffset)
            .toString();
    }
}
