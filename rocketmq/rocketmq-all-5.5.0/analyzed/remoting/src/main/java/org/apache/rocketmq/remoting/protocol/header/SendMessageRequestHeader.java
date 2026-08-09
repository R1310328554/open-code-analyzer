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
 * $Id: SendMessageRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
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
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.TopicQueueRequestHeader;

/**
 * 发送单条/批量消息的请求头：携带生产者组、Topic、队列及消息属性等元数据。
 * parseRequestHeader 可兼容 V1/V2 及批量发送请求码。
 */
@RocketMQAction(value = RequestCode.SEND_MESSAGE, action = Action.PUB)
public class SendMessageRequestHeader extends TopicQueueRequestHeader {
    /** 生产者组名称。 */
    @CFNotNull
    private String producerGroup;
    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 自动创建 Topic 时使用的默认 Topic 名。 */
    @CFNotNull
    private String defaultTopic;
    /** 自动创建 Topic 时的默认队列数。 */
    @CFNotNull
    private Integer defaultTopicQueueNums;
    /** 目标消息队列 ID。 */
    @CFNotNull
    private Integer queueId;
    /** 系统标志位，编码消息压缩、事务等特性。 */
    @CFNotNull
    private Integer sysFlag;
    /** 消息 born 时间戳（毫秒）。 */
    @CFNotNull
    private Long bornTimestamp;
    /** 消息 flag，用于过滤表达式匹配。 */
    @CFNotNull
    private Integer flag;
    /** 用户自定义属性键值对字符串，可为空。 */
    @CFNullable
    private String properties;
    /** 当前重试消费次数，可为空（默认 0）。 */
    @CFNullable
    private Integer reconsumeTimes;
    /** 是否单元化模式，可为空（默认 false）。 */
    @CFNullable
    private Boolean unitMode;
    /** 是否为批量发送，可为空（默认 false）。 */
    @CFNullable
    private Boolean batch;
    /** 最大允许重试消费次数，可为空。 */
    private Integer maxReconsumeTimes;

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回生产者组名称。 */
    public String getProducerGroup() {
        return producerGroup;
    }

    /** 设置生产者组名称。 */
    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
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

    /** 返回默认 Topic 名。 */
    public String getDefaultTopic() {
        return defaultTopic;
    }

    /** 设置默认 Topic 名。 */
    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }

    /** 返回默认队列数。 */
    public Integer getDefaultTopicQueueNums() {
        return defaultTopicQueueNums;
    }

    /** 设置默认队列数。 */
    public void setDefaultTopicQueueNums(Integer defaultTopicQueueNums) {
        this.defaultTopicQueueNums = defaultTopicQueueNums;
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

    /** 返回系统标志位。 */
    public Integer getSysFlag() {
        return sysFlag;
    }

    /** 设置系统标志位。 */
    public void setSysFlag(Integer sysFlag) {
        this.sysFlag = sysFlag;
    }

    /** 返回 born 时间戳。 */
    public Long getBornTimestamp() {
        return bornTimestamp;
    }

    /** 设置 born 时间戳。 */
    public void setBornTimestamp(Long bornTimestamp) {
        this.bornTimestamp = bornTimestamp;
    }

    /** 返回消息 flag。 */
    public Integer getFlag() {
        return flag;
    }

    /** 设置消息 flag。 */
    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    /** 返回用户属性字符串。 */
    public String getProperties() {
        return properties;
    }

    /** 设置用户属性字符串。 */
    public void setProperties(String properties) {
        this.properties = properties;
    }

    /** 返回重试消费次数，空时返回 0。 */
    public Integer getReconsumeTimes() {
        if (null == reconsumeTimes) {
            return 0;
        }
        return reconsumeTimes;
    }

    /** 设置重试消费次数。 */
    public void setReconsumeTimes(Integer reconsumeTimes) {
        this.reconsumeTimes = reconsumeTimes;
    }

    /** 是否单元化模式，空时返回 false。 */
    public boolean isUnitMode() {
        if (null == unitMode) {
            return false;
        }
        return unitMode;
    }

    /** 设置是否单元化模式。 */
    public void setUnitMode(Boolean isUnitMode) {
        this.unitMode = isUnitMode;
    }

    /** 返回最大重试消费次数。 */
    public Integer getMaxReconsumeTimes() {
        return maxReconsumeTimes;
    }

    /** 设置最大重试消费次数。 */
    public void setMaxReconsumeTimes(final Integer maxReconsumeTimes) {
        this.maxReconsumeTimes = maxReconsumeTimes;
    }

    /** 是否批量发送，空时返回 false。 */
    public boolean isBatch() {
        if (null == batch) {
            return false;
        }
        return batch;
    }

    /** 设置是否批量发送。 */
    public void setBatch(Boolean batch) {
        this.batch = batch;
    }

    /** 从 RemotingCommand 解析发送消息请求头，兼容 V1/V2 及批量码。 */
    public static SendMessageRequestHeader parseRequestHeader(RemotingCommand request) throws RemotingCommandException {
        SendMessageRequestHeaderV2 requestHeaderV2 = null;
        SendMessageRequestHeader requestHeader = null;
        switch (request.getCode()) {
            case RequestCode.SEND_BATCH_MESSAGE:
            case RequestCode.SEND_MESSAGE_V2:
                requestHeaderV2 = request.decodeCommandCustomHeader(SendMessageRequestHeaderV2.class);
            case RequestCode.SEND_MESSAGE:
                if (null == requestHeaderV2) {
                    requestHeader = request.decodeCommandCustomHeader(SendMessageRequestHeader.class);
                } else {
                    requestHeader = SendMessageRequestHeaderV2.createSendMessageRequestHeaderV1(requestHeaderV2);
                }
            default:
                break;
        }
        return requestHeader;
    }

    /** 返回含生产者、Topic、队列及消息属性的调试字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("producerGroup", producerGroup)
            .add("topic", topic)
            .add("defaultTopic", defaultTopic)
            .add("defaultTopicQueueNums", defaultTopicQueueNums)
            .add("queueId", queueId)
            .add("sysFlag", sysFlag)
            .add("bornTimestamp", bornTimestamp)
            .add("flag", flag)
            .add("properties", properties)
            .add("reconsumeTimes", reconsumeTimes)
            .add("unitMode", unitMode)
            .add("batch", batch)
            .add("maxReconsumeTimes", maxReconsumeTimes)
            .toString();
    }
}
