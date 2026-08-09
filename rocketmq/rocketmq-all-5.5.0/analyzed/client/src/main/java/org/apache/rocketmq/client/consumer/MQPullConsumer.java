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
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * 主动拉取型消费者接口：支持同步/异步 pull 与偏移量管理。
 */
public interface MQPullConsumer extends MQConsumer {
    /** 启动消费者。 */
    void start() throws MQClientException;

    /** 关闭消费者。 */
    void shutdown();

    /** 注册消息队列变更监听器。 */
    void registerMessageQueueListener(final String topic, final MessageQueueListener listener);

    /**
     * 非阻塞拉取消息（Tag 表达式）。
     *
     * @param mq 目标消息队列
     * @param subExpression Tag 子表达式，null 或 * 表示全部
     * @param offset 起始偏移量
     * @param maxNums 最大拉取条数
     * @return {@code PullResult}
     */
    PullResult pull(final MessageQueue mq, final String subExpression, final long offset,
        final int maxNums) throws MQClientException, RemotingException, MQBrokerException,
        InterruptedException;

    /**
     * 带超时的拉取（Tag 表达式）。
     *
     * @return {@code PullResult}
     */
    PullResult pull(final MessageQueue mq, final String subExpression, final long offset,
        final int maxNums, final long timeout) throws MQClientException, RemotingException,
        MQBrokerException, InterruptedException;

    /**
     * 非阻塞拉取（支持 {@link MessageSelector}，含 SQL92 等）。
     *
     * @param mq 目标消息队列
     * @param selector 消息选择器（{@link MessageSelector}），可为 null
     * @param offset 起始偏移量
     * @param maxNums 最大拉取条数
     * @return {@code PullResult}
     */
    PullResult pull(final MessageQueue mq, final MessageSelector selector, final long offset,
        final int maxNums) throws MQClientException, RemotingException, MQBrokerException,
        InterruptedException;

    /**
     * 带超时的拉取（支持 {@link MessageSelector}）。
     *
     * @param mq 目标消息队列
     * @param selector 消息选择器
     * @param offset 起始偏移量
     * @param maxNums 最大拉取条数
     * @param timeout 超时毫秒数
     * @return {@code PullResult}
     */
    PullResult pull(final MessageQueue mq, final MessageSelector selector, final long offset,
        final int maxNums, final long timeout) throws MQClientException, RemotingException, MQBrokerException,
        InterruptedException;

    /** 异步拉取（Tag 表达式，无超时参数）。 */
    void pull(final MessageQueue mq, final String subExpression, final long offset, final int maxNums,
        final PullCallback pullCallback) throws MQClientException, RemotingException,
        InterruptedException;

    /** 异步拉取（Tag 表达式，带超时）。 */
    void pull(final MessageQueue mq, final String subExpression, final long offset, final int maxNums,
        final PullCallback pullCallback, long timeout) throws MQClientException, RemotingException,
        InterruptedException;

    /** 异步拉取（Tag 表达式，带 maxSize 与超时）。 */
    void pull(final MessageQueue mq, final String subExpression, final long offset, final int maxNums, final int maxSize,
        final PullCallback pullCallback, long timeout) throws MQClientException, RemotingException,
        InterruptedException;

    /** 异步拉取（{@link MessageSelector}，无超时）。 */
    void pull(final MessageQueue mq, final MessageSelector selector, final long offset, final int maxNums,
        final PullCallback pullCallback) throws MQClientException, RemotingException,
        InterruptedException;

    /** 异步拉取（{@link MessageSelector}，带超时）。 */
    void pull(final MessageQueue mq, final MessageSelector selector, final long offset, final int maxNums,
        final PullCallback pullCallback, long timeout) throws MQClientException, RemotingException,
        InterruptedException;

    /**
     * 无消息时阻塞等待的拉取（Tag 表达式）。
     *
     * @return {@code PullResult}
     */
    PullResult pullBlockIfNotFound(final MessageQueue mq, final String subExpression,
        final long offset, final int maxNums) throws MQClientException, RemotingException,
        MQBrokerException, InterruptedException;

    /** 无消息时阻塞的异步拉取（Tag 表达式）。 */
    void pullBlockIfNotFound(final MessageQueue mq, final String subExpression, final long offset,
        final int maxNums, final PullCallback pullCallback) throws MQClientException, RemotingException,
        InterruptedException;

    /** 无消息时阻塞的异步拉取（{@link MessageSelector}）。 */
    void pullBlockIfNotFoundWithMessageSelector(final MessageQueue mq, final MessageSelector selector,
        final long offset, final int maxNums,
        final PullCallback pullCallback) throws MQClientException, RemotingException,
        InterruptedException;

    /**
     * 无消息时阻塞等待的拉取（{@link MessageSelector}）。
     *
     * @return {@code PullResult}
     */
    PullResult pullBlockIfNotFoundWithMessageSelector(final MessageQueue mq, final MessageSelector selector,
        final long offset, final int maxNums) throws MQClientException, RemotingException,
        MQBrokerException, InterruptedException;

    /** 更新本地消费偏移量。 */
    void updateConsumeOffset(final MessageQueue mq, final long offset) throws MQClientException;

    /**
     * 获取消费偏移量。
     *
     * @return 指定队列的偏移量
     */
    long fetchConsumeOffset(final MessageQueue mq, final boolean fromStore) throws MQClientException;

    /**
     * 获取负载均衡后分配给本消费者的 Topic 队列。
     *
     * @param topic 消息 Topic
     * @return 队列集合
     */
    Set<MessageQueue> fetchMessageQueuesInBalance(final String topic) throws MQClientException;

    /**
     * 消费失败时将消息发回 Broker 并延迟重投；仅能在同一消费组内消费。
     */
    void sendMessageBack(MessageExt msg, int delayLevel, String brokerName, String consumerGroup)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException;

}
