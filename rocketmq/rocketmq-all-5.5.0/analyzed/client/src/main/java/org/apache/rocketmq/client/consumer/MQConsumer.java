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
package org.apache.rocketmq.client.consumer;

import java.util.Set;
import org.apache.rocketmq.client.MQAdmin;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * 消息队列消费者基础接口，继承 {@link org.apache.rocketmq.client.MQAdmin} 管理能力。
 */
public interface MQConsumer extends MQAdmin {
    /**
     * 消费失败时将消息发回 Broker，按 delayLevel 延迟后重投（已废弃）。
     */
    @Deprecated
    void sendMessageBack(final MessageExt msg, final int delayLevel) throws RemotingException,
        MQBrokerException, InterruptedException, MQClientException;

    /**
     * 消费失败时将消息发回指定 Broker，按 delayLevel 延迟后重投。
     */
    void sendMessageBack(final MessageExt msg, final int delayLevel, final String brokerName)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException;

    /**
     * 从消费者缓存获取已订阅 Topic 的消息队列集合。
     *
     * @param topic 消息 Topic
     * @return 队列集合
     */
    Set<MessageQueue> fetchSubscribeMessageQueues(final String topic) throws MQClientException;
}
