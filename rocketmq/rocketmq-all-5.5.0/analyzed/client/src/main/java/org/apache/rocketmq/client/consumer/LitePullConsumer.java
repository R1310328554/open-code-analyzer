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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 轻量级主动拉取消费者接口：支持 assign 模式手动分配队列、poll 拉取与偏移量管理。
 */
public interface LitePullConsumer {

    /** 启动消费者。 */
    void start() throws MQClientException;

    /** 关闭消费者。 */
    void shutdown();

    /**
     * 判断消费者是否仍在运行。
     *
     * @return 仍在运行返回 true
     */
    boolean isRunning();

    /**
     * 订阅 Topic（全部 Tag）。
     * @throws MQClientException 客户端错误时抛出
     */
    void subscribe(final String topic) throws MQClientException;

    /**
     * 按 Tag 子表达式订阅 Topic。
     *
     * @param subExpression 订阅表达式，仅支持或运算如 "tag1 || tag2 || tag3"；null 或 * 表示订阅全部
     * @throws MQClientException 客户端错误时抛出
     */
    void subscribe(final String topic, final String subExpression) throws MQClientException;

    /**
     * 订阅 Topic 并注册队列变更监听器。
     * @param topic Topic 名称
     * @param subExpression Tag 子表达式
     * @param messageQueueListener 队列变更监听器
     */
    void subscribe(final String topic, final String subExpression, final MessageQueueListener messageQueueListener) throws MQClientException;

    /**
     * 使用 {@link MessageSelector} 订阅 Topic。
     *
     * @param selector 消息选择器（{@link MessageSelector}），可为 null
     * @throws MQClientException 客户端错误时抛出
     */
    void subscribe(final String topic, final MessageSelector selector) throws MQClientException;

    /**
     * 取消订阅指定 Topic。
     *
     * @param topic 待取消订阅的 Topic
     */
    void unsubscribe(final String topic);


    /**
     * 订阅模式下获取已分配的消息队列集合。
     * @return 已分配队列
     * @throws MQClientException 客户端错误时抛出
     */
    Set<MessageQueue> assignment() throws MQClientException;

    /**
     * 手动分配消息队列（全量替换，不支持增量追加）。
     *
     * @param messageQueues 待分配的队列列表
     */
    void assign(Collection<MessageQueue> messageQueues);

    /**
     * 为 assign 模式设置 Topic 的 Tag 子表达式（start 后不可调用，默认 *）。
     *
     * @param subExpression Tag 子表达式，仅支持或运算；null 或 * 表示全部
     */
    void setSubExpressionForAssign(final String topic, final String subExpression);

    /** 为心跳构建订阅信息。 */
    void buildSubscriptionsForHeartbeat(Map<String, MessageSelector> subExpressionMap) throws Exception;

    /**
     * 拉取 assign 模式下已分配队列的消息（非阻塞）。
     *
     * @return 消息列表，可能为 null
     */
    List<MessageExt> poll();

    /**
     * 带超时的 poll 拉取。
     *
     * @param timeout 无数据时等待毫秒数，不可为负
     * @return 消息列表，可能为 null
     */
    List<MessageExt> poll(long timeout);

    /**
     * 设置下次 poll 使用的拉取偏移量；同一队列多次调用以最后一次为准。
     * 消费中途随意 seek 可能导致数据丢失。
     *
     * @param messageQueue 目标队列
     * @param offset 目标偏移量
     */
    void seek(MessageQueue messageQueue, long offset) throws MQClientException;

    /**
     * 暂停指定队列的拉取；因预拉取机制，{@link #poll()} 可能直到缓冲耗尽才停止。
     * 不影响订阅关系，不会触发 rebalance。
     *
     * @param messageQueues 待暂停的队列
     */
    void pause(Collection<MessageQueue> messageQueues);

    /**
     * 恢复此前 {@link #pause(Collection)} 暂停的队列。
     *
     * @param messageQueues 待恢复的队列
     */
    void resume(Collection<MessageQueue> messageQueues);

    /**
     * 是否启用消费偏移量自动提交。
     *
     * @return 启用返回 true
     */
    boolean isAutoCommit();

    /**
     * 设置是否自动提交消费偏移量。
     *
     * @param autoCommit 是否自动提交
     */
    void setAutoCommit(boolean autoCommit);

    /**
     * 获取指定 Topic 的消息队列元数据。
     *
     * @param topic Topic 名称
     * @return 队列集合
     * @throws MQClientException 客户端错误时抛出
     */
    Collection<MessageQueue> fetchMessageQueues(String topic) throws MQClientException;

    /**
     * 按时间戳查找队列偏移量：返回不小于给定时间戳的最早偏移。
     *
     * @param messageQueue 目标队列
     * @param timestamp 时间戳
     * @return 偏移量
     * @throws MQClientException 客户端错误时抛出
     */
    Long offsetForTimestamp(MessageQueue messageQueue, Long timestamp) throws MQClientException;

    @Deprecated
    /**
     * 已废弃：名称易误解，实际由后台线程提交偏移量而非同步提交。
     * 5.1.0 后移除，请改用 {@link #commit()}。
     *
     * 手动提交系统保存的消费偏移量。
     */
    void commitSync();

    @Deprecated
    /**
     * 已废弃：名称易误解，实际由后台线程提交。
     * 5.1.0 后移除，请改用 {@link #commit(java.util.Map, boolean)}。
     *
     * @param offsetMap 批量提交的偏移量映射
     */
    void commitSync(Map<MessageQueue, Long> offsetMap, boolean persist);

    /** 非阻塞方式手动提交系统保存的消费偏移量。 */
    void commit();

    /**
     * 按指定偏移量映射批量提交。
     *
     * @param offsetMap 队列到偏移量的映射
     * @param persist 是否持久化到 Broker
     */
    void commit(Map<MessageQueue, Long> offsetMap, boolean persist);

    /**
     * 提交指定队列的消费偏移量。
     *
     * @param messageQueues 待提交偏移量的队列
     * @param persist 是否持久化到 Broker
     */
    void commit(final Set<MessageQueue> messageQueues, boolean persist);

    /**
     * 获取队列上次已提交的偏移量。
     *
     * @param messageQueue 目标队列
     * @return 偏移量；-1 表示 Broker 无记录
     * @throws MQClientException 客户端错误时抛出
     */
    Long committed(MessageQueue messageQueue) throws MQClientException;

    /**
     * 注册 Topic 元数据（队列集合）变更回调。
     *
     * @param topic 待监听的 Topic
     * @param topicMessageQueueChangeListener 变更回调，参见 {@link TopicMessageQueueChangeListener}
     * @throws MQClientException 客户端错误时抛出
     */
    void registerTopicMessageQueueChangeListener(String topic,
        TopicMessageQueueChangeListener topicMessageQueueChangeListener) throws MQClientException;

    /** 更新 NameServer 地址。 */
    void updateNameServerAddress(String nameServerAddress);

    /**
     * 将下次 poll 偏移设为队列起始位置；多次调用以最后一次为准。
     *
     * @param messageQueue 目标队列
     */
    void seekToBegin(MessageQueue messageQueue)throws MQClientException;

    /**
     * 将下次 poll 偏移设为队列末尾；多次调用以最后一次为准。
     *
     * @param messageQueue 目标队列
     */
    void seekToEnd(MessageQueue messageQueue)throws MQClientException;
}
