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
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.rpc.TopicQueueRequestHeader;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 查询 ConsumeQueue 索引的请求头：按 Topic、队列及起始索引分页读取。
 */
@RocketMQAction(value = RequestCode.QUERY_CONSUME_QUEUE, action = Action.GET)
public class QueryConsumeQueueRequestHeader extends TopicQueueRequestHeader {

    /** 目标 Topic 名称。 */
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 目标队列 ID。 */
    private int queueId;
    /** ConsumeQueue 起始索引位置。 */
    private long index;
    /** 读取条目数量。 */
    private int count;
    /** 消费组名称。 */
    @RocketMQResource(ResourceType.GROUP)
    private String consumerGroup;

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

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /** 校验请求头字段（空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }
}
