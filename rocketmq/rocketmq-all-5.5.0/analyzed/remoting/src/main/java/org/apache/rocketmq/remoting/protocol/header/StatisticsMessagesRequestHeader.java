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

import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.rpc.TopicQueueRequestHeader;

/**
 * 统计消息数量的请求头：按消费组、Topic、队列及时间区间统计消息条数。
 * queueId 小于 0 时在 getter 中归一化为 -1（表示全部队列）。
 */
public class StatisticsMessagesRequestHeader extends TopicQueueRequestHeader {
    /** 目标消费组名称。 */
    @CFNotNull
    private String consumerGroup;
    /** 目标 Topic 名称。 */
    @CFNotNull
    private String topic;
    /** 队列 ID，小于 0 表示全部队列。 */
    @CFNotNull
    private int queueId;

    /** 统计起始时间（毫秒）。 */
    private long fromTime;
    /** 统计结束时间（毫秒）。 */
    private long toTime;

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
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic 名称。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回队列 ID，内部值小于 0 时返回 -1。 */
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

    /** 返回统计起始时间。 */
    public long getFromTime() {
        return fromTime;
    }

    /** 设置统计起始时间。 */
    public void setFromTime(long fromTime) {
        this.fromTime = fromTime;
    }

    /** 返回统计结束时间。 */
    public long getToTime() {
        return toTime;
    }

    /** 设置统计结束时间。 */
    public void setToTime(long toTime) {
        this.toTime = toTime;
    }
}
