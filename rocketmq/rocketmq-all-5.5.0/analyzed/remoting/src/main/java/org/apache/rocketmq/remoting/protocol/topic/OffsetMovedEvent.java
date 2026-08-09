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

package org.apache.rocketmq.remoting.protocol.topic;

import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * Offset 被 Broker 强制迁移事件：记录消费组、队列及请求/新 offset。
 */
public class OffsetMovedEvent extends RemotingSerializable {
    /** 发生 offset 迁移的消费组。 */
    private String consumerGroup;
    /** 受影响的 MessageQueue。 */
    private MessageQueue messageQueue;
    /** 客户端请求的 offset。 */
    private long offsetRequest;
    /** Broker 指定的新 offset。 */
    private long offsetNew;

    /** 返回消费组名称。 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /** 设置消费组名称。 */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /** 返回消息队列。 */
    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    /** 设置消息队列。 */
    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    /** 返回请求的 offset。 */
    public long getOffsetRequest() {
        return offsetRequest;
    }

    /** 设置请求的 offset。 */
    public void setOffsetRequest(long offsetRequest) {
        this.offsetRequest = offsetRequest;
    }

    /** 返回新 offset。 */
    public long getOffsetNew() {
        return offsetNew;
    }

    /** 设置新 offset。 */
    public void setOffsetNew(long offsetNew) {
        this.offsetNew = offsetNew;
    }

    /** 返回事件摘要字符串。 */
    @Override
    public String toString() {
        return "OffsetMovedEvent [consumerGroup=" + consumerGroup + ", messageQueue=" + messageQueue
            + ", offsetRequest=" + offsetRequest + ", offsetNew=" + offsetNew + "]";
    }
}
