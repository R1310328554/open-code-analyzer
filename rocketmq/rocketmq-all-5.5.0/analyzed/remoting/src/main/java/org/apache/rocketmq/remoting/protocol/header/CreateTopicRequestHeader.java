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
 * $Id: CreateTopicRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.header;

import com.google.common.base.MoreObjects;
import org.apache.rocketmq.common.TopicFilterType;
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
 * 创建或更新 Topic 请求头：配置读写队列数、权限、过滤类型及顺序属性。
 */
@RocketMQAction(value = RequestCode.UPDATE_AND_CREATE_TOPIC, action = Action.CREATE)
public class CreateTopicRequestHeader extends TopicRequestHeader {
    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 默认 Topic（用于路由占位）。 */
    @CFNotNull
    private String defaultTopic;
    /** 读队列数量。 */
    @CFNotNull
    private Integer readQueueNums;
    /** 写队列数量。 */
    @CFNotNull
    private Integer writeQueueNums;
    /** Topic 权限位（读/写/继承）。 */
    @CFNotNull
    private Integer perm;
    /** 消息过滤类型（SINGLE_TAG / MULTI_TAG）。 */
    @CFNotNull
    private String topicFilterType;
    /** Topic 系统标志位。 */
    private Integer topicSysFlag;
    /** 是否顺序 Topic。 */
    @CFNotNull
    private Boolean order = false;
    /** Topic 扩展属性（JSON 字符串）。 */
    private String attributes;

    /** 是否强制覆盖已有配置。 */
    @CFNullable
    private Boolean force = false;

    @Override
    /** 校验 topicFilterType 是否为合法枚举值。 */
    public void checkFields() throws RemotingCommandException {
        try {
            TopicFilterType.valueOf(this.topicFilterType);
        } catch (Exception e) {
            throw new RemotingCommandException("topicFilterType = [" + topicFilterType + "] value invalid", e);
        }
    }

    /** 返回过滤类型枚举。 */
    public TopicFilterType getTopicFilterTypeEnum() {
        return TopicFilterType.valueOf(this.topicFilterType);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDefaultTopic() {
        return defaultTopic;
    }

    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }

    /** 返回读队列数。 */
    public Integer getReadQueueNums() {
        return readQueueNums;
    }

    public void setReadQueueNums(Integer readQueueNums) {
        this.readQueueNums = readQueueNums;
    }

    public Integer getWriteQueueNums() {
        return writeQueueNums;
    }

    public void setWriteQueueNums(Integer writeQueueNums) {
        this.writeQueueNums = writeQueueNums;
    }

    public Integer getPerm() {
        return perm;
    }

    public void setPerm(Integer perm) {
        this.perm = perm;
    }

    public String getTopicFilterType() {
        return topicFilterType;
    }

    public void setTopicFilterType(String topicFilterType) {
        this.topicFilterType = topicFilterType;
    }

    public Integer getTopicSysFlag() {
        return topicSysFlag;
    }

    public void setTopicSysFlag(Integer topicSysFlag) {
        this.topicSysFlag = topicSysFlag;
    }

    /** 返回是否顺序 Topic。 */
    public Boolean getOrder() {
        return order;
    }

    public void setOrder(Boolean order) {
        this.order = order;
    }

    /** 返回是否强制覆盖。 */
    public Boolean getForce() {
        return force;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    /** 返回便于诊断的可读字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("topic", topic)
            .add("defaultTopic", defaultTopic)
            .add("readQueueNums", readQueueNums)
            .add("writeQueueNums", writeQueueNums)
            .add("perm", perm)
            .add("topicFilterType", topicFilterType)
            .add("topicSysFlag", topicSysFlag)
            .add("order", order)
            .add("attributes", attributes)
            .add("force", force)
            .toString();
    }
}
