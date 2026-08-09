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
package org.apache.rocketmq.client.consumer.listener;

import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 并发消费上下文：携带当前队列、重试策略及批量 ack 游标。
 */
public class ConsumeConcurrentlyContext {
    /** 当前消费所在消息队列。 */
    private final MessageQueue messageQueue;
    /**
     * 消费失败后的重试策略：<br>
     * -1 — 不重试，直接进入死信队列；<br>
     * 0 — 由 Broker 控制重试间隔；<br>
     * &gt;0 — 由客户端指定延迟级别。
     */
    /** 下次消费延迟级别，默认 0（Broker 控制）。 */
    private int delayLevelWhenNextConsume = 0;
    /** 批量消费时已确认的消息下标，默认全部成功。 */
    private int ackIndex = Integer.MAX_VALUE;

    /** 绑定当前消费队列。 */
    public ConsumeConcurrentlyContext(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    /** 返回下次消费延迟级别。 */
    public int getDelayLevelWhenNextConsume() {
        return delayLevelWhenNextConsume;
    }

    /** 设置下次消费延迟级别。 */
    public void setDelayLevelWhenNextConsume(int delayLevelWhenNextConsume) {
        this.delayLevelWhenNextConsume = delayLevelWhenNextConsume;
    }

    /** 返回当前消息队列。 */
    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    /** 返回批量 ack 下标。 */
    public int getAckIndex() {
        return ackIndex;
    }

    /** 设置批量 ack 下标（部分成功场景）。 */
    public void setAckIndex(int ackIndex) {
        this.ackIndex = ackIndex;
    }
}
