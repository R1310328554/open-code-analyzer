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
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.rpc.TopicQueueRequestHeader;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 重置消费位点的请求头：按时间戳或指定 offset 将消费组进度回拨。
 * queueId 为 -1 表示重置该 Topic 下所有队列；isForce 强制跳过消费端确认。
 */
@RocketMQAction(value = RequestCode.INVOKE_BROKER_TO_RESET_OFFSET, action = Action.UPDATE)
public class ResetOffsetRequestHeader extends TopicQueueRequestHeader {

    /** 目标消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String group;

    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;

    /** 队列 ID，-1 表示全部队列。 */
    private int queueId = -1;

    /** 目标消费位点，可为空（按 timestamp 定位）。 */
    private Long offset;

    /** 重置基准时间戳（毫秒）。 */
    @CFNotNull
    private long timestamp;

    /** 是否强制重置（跳过消费端确认）。 */
    @CFNotNull
    private boolean isForce;

    /** 返回 Topic 名称。 */
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic 名称。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回消费组名称。 */
    public String getGroup() {
        return group;
    }

    /** 设置消费组名称。 */
    public void setGroup(String group) {
        this.group = group;
    }

    /** 返回重置基准时间戳。 */
    public long getTimestamp() {
        return timestamp;
    }

    /** 设置重置基准时间戳。 */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** 返回是否强制重置。 */
    public boolean isForce() {
        return isForce;
    }

    /** 设置是否强制重置。 */
    public void setForce(boolean isForce) {
        this.isForce = isForce;
    }

    /** 返回队列 ID。 */
    public Integer getQueueId() {
        return queueId;
    }

    /** 设置队列 ID。 */
    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    /** 返回目标消费位点。 */
    public Long getOffset() {
        return offset;
    }

    /** 设置目标消费位点。 */
    public void setOffset(Long offset) {
        this.offset = offset;
    }

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }
}
