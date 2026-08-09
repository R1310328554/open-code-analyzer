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
package org.apache.rocketmq.client.producer;

import java.util.Collection;
import java.util.List;
import org.apache.rocketmq.client.MQAdmin;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.exception.RequestTimeoutException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * 消息生产者接口：提供同步/异步/单向发送、顺序/批量/事务消息及 Request-Reply 模式。
 * 继承 {@link MQAdmin} 的管理能力。
 */
public interface MQProducer extends MQAdmin {
    /** 启动 Producer，注册路由并初始化内部组件。 */
    void start() throws MQClientException;

    /** 关闭 Producer，释放资源。 */
    void shutdown();

    /** 获取 Topic 的可发布队列列表。 */
    List<MessageQueue> fetchPublishMessageQueues(final String topic) throws MQClientException;

    /** 同步发送单条消息（默认超时）。 */
    SendResult send(final Message msg) throws MQClientException, RemotingException, MQBrokerException,
        InterruptedException;

    SendResult send(final Message msg, final long timeout) throws MQClientException,
        RemotingException, MQBrokerException, InterruptedException;

    /** 异步发送单条消息，结果通过 SendCallback 回调。 */
    void send(final Message msg, final SendCallback sendCallback) throws MQClientException,
        RemotingException, InterruptedException, MQBrokerException;

    void send(final Message msg, final SendCallback sendCallback, final long timeout)
        throws MQClientException, RemotingException, InterruptedException;

    /** 单向发送，不等待 Broker 响应。 */
    void sendOneway(final Message msg) throws MQClientException, RemotingException,
        InterruptedException;

    /** 同步发送到指定队列。 */
    SendResult send(final Message msg, final MessageQueue mq) throws MQClientException,
        RemotingException, MQBrokerException, InterruptedException;

    SendResult send(final Message msg, final MessageQueue mq, final long timeout)
        throws MQClientException, RemotingException, MQBrokerException, InterruptedException;

    void send(final Message msg, final MessageQueue mq, final SendCallback sendCallback)
        throws MQClientException, RemotingException, InterruptedException;

    void send(final Message msg, final MessageQueue mq, final SendCallback sendCallback, long timeout)
        throws MQClientException, RemotingException, InterruptedException;

    void sendOneway(final Message msg, final MessageQueue mq) throws MQClientException,
        RemotingException, InterruptedException;

    /** 通过自定义选择器选取队列并同步发送。 */
    SendResult send(final Message msg, final MessageQueueSelector selector, final Object arg)
        throws MQClientException, RemotingException, MQBrokerException, InterruptedException;

    SendResult send(final Message msg, final MessageQueueSelector selector, final Object arg,
        final long timeout) throws MQClientException, RemotingException, MQBrokerException,
        InterruptedException;

    void send(final Message msg, final MessageQueueSelector selector, final Object arg,
        final SendCallback sendCallback) throws MQClientException, RemotingException,
        InterruptedException;

    void send(final Message msg, final MessageQueueSelector selector, final Object arg,
        final SendCallback sendCallback, final long timeout) throws MQClientException, RemotingException,
        InterruptedException;

    void sendOneway(final Message msg, final MessageQueueSelector selector, final Object arg)
        throws MQClientException, RemotingException, InterruptedException;

    /** 发送事务消息，本地事务由 TransactionListener 执行。 */
    TransactionSendResult sendMessageInTransaction(final Message msg,
        final Object arg) throws MQClientException;

    /** 批量发送相关 API */
    /** 同步批量发送多条消息。 */
    SendResult send(final Collection<Message> msgs) throws MQClientException, RemotingException, MQBrokerException,
        InterruptedException;

    SendResult send(final Collection<Message> msgs, final long timeout) throws MQClientException,
        RemotingException, MQBrokerException, InterruptedException;

    SendResult send(final Collection<Message> msgs, final MessageQueue mq) throws MQClientException,
        RemotingException, MQBrokerException, InterruptedException;

    SendResult send(final Collection<Message> msgs, final MessageQueue mq, final long timeout)
        throws MQClientException, RemotingException, MQBrokerException, InterruptedException;

    void send(final Collection<Message> msgs,
        final SendCallback sendCallback) throws MQClientException, RemotingException, MQBrokerException,
        InterruptedException;

    void send(final Collection<Message> msgs, final SendCallback sendCallback,
        final long timeout) throws MQClientException, RemotingException,
        MQBrokerException, InterruptedException;

    void send(final Collection<Message> msgs, final MessageQueue mq,
        final SendCallback sendCallback) throws MQClientException, RemotingException,
        MQBrokerException, InterruptedException;

    void send(final Collection<Message> msgs, final MessageQueue mq, final SendCallback sendCallback,
        final long timeout) throws MQClientException,
        RemotingException, MQBrokerException, InterruptedException;

    /** 撤回已发送消息（需 Broker 支持）。 */
    String recallMessage(String topic, String recallHandle)
        throws MQClientException, RemotingException, MQBrokerException, InterruptedException;

    /** Request-Reply 模式相关 API */
    /** 同步 Request-Reply：发送请求并阻塞等待响应消息。 */
    Message request(final Message msg, final long timeout) throws RequestTimeoutException, MQClientException,
        RemotingException, MQBrokerException, InterruptedException;

    /** 异步 Request-Reply：响应通过 RequestCallback 回调。 */
    void request(final Message msg, final RequestCallback requestCallback, final long timeout)
        throws MQClientException, RemotingException, InterruptedException, MQBrokerException;

    Message request(final Message msg, final MessageQueueSelector selector, final Object arg,
        final long timeout) throws RequestTimeoutException, MQClientException, RemotingException, MQBrokerException,
        InterruptedException;

    void request(final Message msg, final MessageQueueSelector selector, final Object arg,
        final RequestCallback requestCallback,
        final long timeout) throws MQClientException, RemotingException,
        InterruptedException, MQBrokerException;

    Message request(final Message msg, final MessageQueue mq, final long timeout)
        throws RequestTimeoutException, MQClientException, RemotingException, MQBrokerException, InterruptedException;

    void request(final Message msg, final MessageQueue mq, final RequestCallback requestCallback, long timeout)
        throws MQClientException, RemotingException, MQBrokerException, InterruptedException;
}
