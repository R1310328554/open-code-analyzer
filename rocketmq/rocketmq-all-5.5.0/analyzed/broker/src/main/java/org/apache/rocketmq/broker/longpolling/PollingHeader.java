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

package org.apache.rocketmq.broker.longpolling;

import org.apache.rocketmq.remoting.protocol.header.NotificationRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.PopMessageRequestHeader;

/**
 * 长轮询请求头快照：从 POP 或通知请求中提取 consumerGroup、topic、queueId 及轮询时间参数。
 */
public class PollingHeader {
    private final String consumerGroup;
    private final String topic;
    private final int queueId;
    private final long bornTime;
    private final long pollTime;

    /** 从 {@link PopMessageRequestHeader} 构造轮询头。 */
    public PollingHeader(PopMessageRequestHeader requestHeader) {
        this.consumerGroup = requestHeader.getConsumerGroup();
        this.topic = requestHeader.getTopic();
        this.queueId = requestHeader.getQueueId();
        this.bornTime = requestHeader.getBornTime();
        this.pollTime = requestHeader.getPollTime();
    }

    /** 从 {@link NotificationRequestHeader} 构造轮询头。 */
    public PollingHeader(NotificationRequestHeader requestHeader) {
        this.consumerGroup = requestHeader.getConsumerGroup();
        this.topic = requestHeader.getTopic();
        this.queueId = requestHeader.getQueueId();
        this.bornTime = requestHeader.getBornTime();
        this.pollTime = requestHeader.getPollTime();
    }

    /** 返回消费组名。 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /** 返回 topic 名。 */
    public String getTopic() {
        return topic;
    }

    /** 返回队列 ID。 */
    public int getQueueId() {
        return queueId;
    }

    /** 返回请求创建时间戳（毫秒）。 */
    public long getBornTime() {
        return bornTime;
    }

    /** 返回长轮询最长等待时长（毫秒）。 */
    public long getPollTime() {
        return pollTime;
    }
}
