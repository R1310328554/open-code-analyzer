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
 * $Id: GetEarliestMsgStoretimeRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.header;

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.TopicQueueRequestHeader;

/**
 * 查询队列最早消息存储时间的请求头：指定 Topic 与队列 ID。
 */
@RocketMQAction(value = RequestCode.GET_EARLIEST_MSG_STORETIME, action = Action.GET)
public class GetEarliestMsgStoretimeRequestHeader extends TopicQueueRequestHeader {
    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 目标队列 ID。 */
    @CFNotNull
    private Integer queueId;

    /** 校验请求头字段（空实现）。 */
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
}
