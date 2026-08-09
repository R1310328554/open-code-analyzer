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
package org.apache.rocketmq.client.impl.consumer;

import org.apache.rocketmq.common.constant.ConsumeInitMode;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.message.MessageRequestMode;

/**
 * POP 拉取请求：携带 topic、消费组、队列及 {@link PopProcessQueue} 等上下文。
 */
public class PopRequest implements MessageRequest {
    /** Topic 名称。 */
    private String topic;
    /** 消费组名。 */
    private String consumerGroup;
    /** 目标消息队列。 */
    private MessageQueue messageQueue;
    /** 关联的 POP 处理队列。 */
    private PopProcessQueue popProcessQueue;
    /** 是否已优先加锁（顺序消费场景）。 */
    private boolean lockedFirst = false;
    /** POP 初始 offset 模式。 */
    private int initMode = ConsumeInitMode.MAX;

    /** 是否已优先加锁。 */
    public boolean isLockedFirst() {
        return lockedFirst;
    }

    /** 设置优先加锁标志。 */
    public void setLockedFirst(boolean lockedFirst) {
        this.lockedFirst = lockedFirst;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回 POP 处理队列。 */
    public PopProcessQueue getPopProcessQueue() {
        return popProcessQueue;
    }

    /** 设置 POP 处理队列。 */
    public void setPopProcessQueue(PopProcessQueue popProcessQueue) {
        this.popProcessQueue = popProcessQueue;
    }

    /** 返回 POP 初始模式。 */
    public int getInitMode() {
        return initMode;
    }

    /** 设置 POP 初始模式。 */
    public void setInitMode(int initMode) {
        this.initMode = initMode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        result = prime * result + ((consumerGroup == null) ? 0 : consumerGroup.hashCode());
        result = prime * result + ((messageQueue == null) ? 0 : messageQueue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        PopRequest other = (PopRequest) obj;

        if (topic == null) {
            if (other.topic != null)
                return false;
        } else if (!topic.equals(other.topic)) {
            return false;
        }

        if (consumerGroup == null) {
            if (other.consumerGroup != null)
                return false;
        } else if (!consumerGroup.equals(other.consumerGroup))
            return false;

        if (messageQueue == null) {
            if (other.messageQueue != null)
                return false;
        } else if (!messageQueue.equals(other.messageQueue)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PopRequest [topic=" + topic + ", consumerGroup=" + consumerGroup + ", messageQueue=" + messageQueue + "]";
    }

    @Override
    /** 返回 {@link MessageRequestMode#POP}。 */
    public MessageRequestMode getMessageRequestMode() {
        return MessageRequestMode.POP;
    }
}
