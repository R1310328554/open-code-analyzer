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
package org.apache.rocketmq.client.producer.selector;

import java.util.List;
import java.util.Set;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 按机房（IDC）选择 MessageQueue 的策略占位实现；
 * 当前 {@link #select} 恒返回 null，需业务侧自行扩展。
 */
public class SelectMessageQueueByMachineRoom implements MessageQueueSelector {
    /** 消费者所在机房 ID 集合。 */
    private Set<String> consumeridcs;

    @Override
    /** 按机房筛选队列（当前未实现，返回 null）。 */
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        return null;
    }

    /** 返回消费者机房集合。 */
    public Set<String> getConsumeridcs() {
        return consumeridcs;
    }

    /** 设置消费者机房集合。 */
    public void setConsumeridcs(Set<String> consumeridcs) {
        this.consumeridcs = consumeridcs;
    }
}
