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
 * Pop 消息确认（ACK）请求头：标识消费组、Topic、队列、位点及 Pop 附加信息。
 */
@RocketMQAction(value = RequestCode.ACK_MESSAGE, action = Action.SUB)
public class AckMessageRequestHeader extends TopicQueueRequestHeader {
    /** 消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String consumerGroup;
    /** 目标 Topic。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 队列 ID。 */
    @CFNotNull
    private Integer queueId;
    /** Pop 附加信息（含 startOffset、popTime 等）。 */
    @CFNotNull
    private String extraInfo;

    /** 消息消费位点。 */
    @CFNotNull
    private Long offset;

    /** Lite Topic 名（Lite 消费场景可选）。 */
    private String liteTopic;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    /** 返回消费位点。 */
    public Long getOffset() {
        return offset;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    /** 返回 Pop 附加信息。 */
    public String getExtraInfo() {
        return extraInfo;
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

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public String getLiteTopic() {
        return liteTopic;
    }

    public void setLiteTopic(String liteTopic) {
        this.liteTopic = liteTopic;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("consumerGroup", consumerGroup)
            .add("topic", topic)
            .add("queueId", queueId)
            .add("extraInfo", extraInfo)
            .add("offset", offset)
            .add("liteTopic", liteTopic)
            .omitNullValues()
            .toString();
    }
}
