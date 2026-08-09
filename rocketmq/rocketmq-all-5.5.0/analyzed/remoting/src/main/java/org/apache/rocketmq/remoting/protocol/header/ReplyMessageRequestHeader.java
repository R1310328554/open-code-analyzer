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

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.annotation.CFNullable;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.rpc.TopicQueueRequestHeader;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 推送应答消息到客户端的请求头：Request-Reply 模式下 Broker 将应答消息推送给 Producer。
 * 携带完整消息元数据（Topic、队列、时间戳、属性等）。
 */
@RocketMQAction(value = RequestCode.PUSH_REPLY_MESSAGE_TO_CLIENT, action = Action.SUB)
public class ReplyMessageRequestHeader extends TopicQueueRequestHeader {
    /** 生产者组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String producerGroup;
    /** 应答消息 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 默认 Topic 名称（路由占位）。 */
    @CFNotNull
    private String defaultTopic;
    /** 默认 Topic 队列数量。 */
    @CFNotNull
    private Integer defaultTopicQueueNums;
    /** 消息队列 ID。 */
    @CFNotNull
    private Integer queueId;
    /** 消息系统标志位。 */
    @CFNotNull
    private Integer sysFlag;
    /** 消息创建时间戳（毫秒）。 */
    @CFNotNull
    private Long bornTimestamp;
    /** 消息类型标志。 */
    @CFNotNull
    private Integer flag;
    /** 消息用户属性（键值对字符串），可为空。 */
    @CFNullable
    private String properties;
    /** 重试消费次数，可为空。 */
    @CFNullable
    private Integer reconsumeTimes;
    /** 是否单元化模式，默认 false。 */
    @CFNullable
    private boolean unitMode = false;

    /** 消息创建主机地址。 */
    @CFNotNull
    private String bornHost;
    /** 消息存储主机地址。 */
    @CFNotNull
    private String storeHost;
    /** 消息存储时间戳（毫秒）。 */
    @CFNotNull
    private long storeTimestamp;

    /** 校验请求头字段（本类无额外约束，空实现）。 */
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
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic 名称。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回默认 Topic 名称。 */
    public String getDefaultTopic() {
        return defaultTopic;
    }

    /** 设置默认 Topic 名称。 */
    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }

    /** 返回默认 Topic 队列数。 */
    public Integer getDefaultTopicQueueNums() {
        return defaultTopicQueueNums;
    }

    /** 设置默认 Topic 队列数。 */
    public void setDefaultTopicQueueNums(Integer defaultTopicQueueNums) {
        this.defaultTopicQueueNums = defaultTopicQueueNums;
    }

    /** 返回队列 ID。 */
    public Integer getQueueId() {
        return queueId;
    }

    /** 设置队列 ID。 */
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

    /** 返回消息创建时间戳。 */
    public Long getBornTimestamp() {
        return bornTimestamp;
    }

    /** 设置消息创建时间戳。 */
    public void setBornTimestamp(Long bornTimestamp) {
        this.bornTimestamp = bornTimestamp;
    }

    /** 返回消息类型标志。 */
    public Integer getFlag() {
        return flag;
    }

    /** 设置消息类型标志。 */
    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    /** 返回消息用户属性。 */
    public String getProperties() {
        return properties;
    }

    /** 设置消息用户属性。 */
    public void setProperties(String properties) {
        this.properties = properties;
    }

    /** 返回重试消费次数。 */
    public Integer getReconsumeTimes() {
        return reconsumeTimes;
    }

    /** 设置重试消费次数。 */
    public void setReconsumeTimes(Integer reconsumeTimes) {
        this.reconsumeTimes = reconsumeTimes;
    }

    /** 返回是否单元化模式。 */
    public boolean isUnitMode() {
        return unitMode;
    }

    /** 设置是否单元化模式。 */
    public void setUnitMode(boolean unitMode) {
        this.unitMode = unitMode;
    }

    /** 返回消息创建主机。 */
    public String getBornHost() {
        return bornHost;
    }

    /** 设置消息创建主机。 */
    public void setBornHost(String bornHost) {
        this.bornHost = bornHost;
    }

    /** 返回消息存储主机。 */
    public String getStoreHost() {
        return storeHost;
    }

    /** 设置消息存储主机。 */
    public void setStoreHost(String storeHost) {
        this.storeHost = storeHost;
    }

    /** 返回消息存储时间戳。 */
    public long getStoreTimestamp() {
        return storeTimestamp;
    }

    /** 设置消息存储时间戳。 */
    public void setStoreTimestamp(long storeTimestamp) {
        this.storeTimestamp = storeTimestamp;
    }
}
