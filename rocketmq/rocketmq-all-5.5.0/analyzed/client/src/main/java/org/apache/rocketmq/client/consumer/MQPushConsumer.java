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

import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;

/**
 * 服务端推送型消费者接口：注册监听器后由 Broker 长轮询推送消息。
 */
public interface MQPushConsumer extends MQConsumer {
    /** 启动消费者。 */
    void start() throws MQClientException;

    /** 关闭消费者。 */
    void shutdown();

    /** 注册消息监听器（已废弃的通用接口）。 */
    @Deprecated
    void registerMessageListener(MessageListener messageListener);

    /** 注册并发消费监听器。 */
    void registerMessageListener(final MessageListenerConcurrently messageListener);

    /** 注册顺序消费监听器。 */
    void registerMessageListener(final MessageListenerOrderly messageListener);

    /**
     * 按 Tag 子表达式订阅 Topic。
     *
     * @param subExpression Tag 子表达式，null 或 * 表示全部
     */
    void subscribe(final String topic, final String subExpression) throws MQClientException;

    /**
     * 已废弃：FilterServer 移除后将在 5.0.0 删除，请改用 {@code subscribe(topic, MessageSelector)}。
     *
     * @param fullClassName 过滤器全类名，须继承 MessageFilter
     * @param filterClassSource 过滤器源码（UTF-8），需自行保证安全
     */
    @Deprecated
    void subscribe(final String topic, final String fullClassName,
        final String filterClassSource) throws MQClientException;

    /**
     * 使用 {@link MessageSelector} 订阅 Topic，支持 Tag 与 SQL92。
     * <p>Tag：{@link MessageSelector#byTag(java.lang.String)}</p>
     * <p>SQL92：{@link MessageSelector#bySql(java.lang.String)}</p>
     *
     * @param selector 消息选择器（{@link MessageSelector}），可为 null
     */
    void subscribe(final String topic, final MessageSelector selector) throws MQClientException;

    /**
     * 取消订阅指定 Topic。
     *
     * @param topic 消息 Topic
     */
    void unsubscribe(final String topic);

    /** 动态调整消费线程池核心线程数。 */
    void updateCorePoolSize(int corePoolSize);

    /** 暂停消费。 */
    void suspend();

    /** 恢复消费。 */
    void resume();
}
