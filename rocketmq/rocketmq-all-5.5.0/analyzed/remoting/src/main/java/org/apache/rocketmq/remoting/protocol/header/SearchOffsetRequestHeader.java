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
 * $Id: SearchOffsetRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.header;

import com.google.common.base.MoreObjects;
import org.apache.rocketmq.common.BoundaryType;
import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.TopicQueueRequestHeader;

/**
 * 按时间戳搜索队列 offset 的请求头：在指定 Topic/队列上查找最接近给定时间的消费位点。
 * boundaryType 控制取不大于（LOWER）或不小于（UPPER）目标时间的 offset。
 */
@RocketMQAction(value = RequestCode.SEARCH_OFFSET_BY_TIMESTAMP, action = Action.GET)
public class SearchOffsetRequestHeader extends TopicQueueRequestHeader {
    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** Lite Topic 名称，可为空。 */
    private String liteTopic;
    /** 消息队列 ID。 */
    @CFNotNull
    private Integer queueId;
    /** 目标时间戳（毫秒）。 */
    @CFNotNull
    private Long timestamp;

    /** 边界类型：LOWER 取不大于目标时间的 offset，UPPER 取不小于的 offset。 */
    private BoundaryType boundaryType;

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

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

    /** 返回 Lite Topic 名称。 */
    public String getLiteTopic() {
        return liteTopic;
    }

    /** 设置 Lite Topic 名称。 */
    public void setLiteTopic(String liteTopic) {
        this.liteTopic = liteTopic;
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

    /** 返回目标时间戳。 */
    public Long getTimestamp() {
        return timestamp;
    }

    /** 设置目标时间戳。 */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /** 返回边界类型，未设置时默认 LOWER。 */
    public BoundaryType getBoundaryType() {
        // 默认返回 LOWER
        return boundaryType == null ? BoundaryType.LOWER : boundaryType;
    }

    /** 设置边界类型。 */
    public void setBoundaryType(BoundaryType boundaryType) {
        this.boundaryType = boundaryType;
    }

    /** 返回含 Topic、队列、时间戳及边界类型的调试字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("topic", topic)
            .add("liteTopic", liteTopic)
            .add("queueId", queueId)
            .add("timestamp", timestamp)
            .add("boundaryType", boundaryType.getName())
            .toString();
    }
}
