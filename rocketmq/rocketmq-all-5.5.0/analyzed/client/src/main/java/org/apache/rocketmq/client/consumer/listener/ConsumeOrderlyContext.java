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
 * 顺序消费上下文：控制 offset 自动提交与队列暂停时长。
 */
public class ConsumeOrderlyContext {
    /** 当前顺序消费所在队列。 */
    private final MessageQueue messageQueue;
    /** 消费成功后是否自动提交 offset，默认 true。 */
    private boolean autoCommit = true;
    /** 暂停当前队列的毫秒数，-1 表示不暂停。 */
    private long suspendCurrentQueueTimeMillis = -1;

    /** 绑定当前消费队列。 */
    public ConsumeOrderlyContext(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    /** 是否自动提交 offset。 */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /** 设置是否自动提交 offset。 */
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    /** 返回当前消息队列。 */
    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    /** 返回队列暂停时长（毫秒）。 */
    public long getSuspendCurrentQueueTimeMillis() {
        return suspendCurrentQueueTimeMillis;
    }

    /** 设置队列暂停时长（毫秒）。 */
    public void setSuspendCurrentQueueTimeMillis(long suspendCurrentQueueTimeMillis) {
        this.suspendCurrentQueueTimeMillis = suspendCurrentQueueTimeMillis;
    }
}
