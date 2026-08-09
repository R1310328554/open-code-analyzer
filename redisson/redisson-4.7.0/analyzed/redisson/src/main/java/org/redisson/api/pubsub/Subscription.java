/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api.pubsub;

import java.util.Set;

/**
 * 可靠 Pub/Sub 主题的订阅。
 * <p>
 * 每个订阅维护独立的消费偏移，与同主题上其他订阅互不影响。
 * 一个订阅可挂载多个拉模式或推模式消费者，共同分担消息处理负载。
 *
 * @param <V> 消息值类型
 *
 * @author Nikita Koksharov
 *
 */
public interface Subscription<V> extends SubscriptionAsync<V> {

    /**
     * 创建使用自动生成名称的拉模式消费者。
     * <p>
     * 拉模式消费者按需拉取消息，由应用手动控制消费速率与时机。
     *
     * @return 拉模式消费者
     */
    PullConsumer<V> createPullConsumer();

    /**
     * 使用指定配置创建拉模式消费者。
     *
     * @param config 消费者配置
     * @return 拉模式消费者
     */
    PullConsumer<V> createPullConsumer(ConsumerConfig config);

    /**
     * 创建使用自动生成名称的推模式消费者。
     * <p>
     * 推模式消费者通过已注册监听器自动接收消息，实现事件驱动处理。
     *
     * @return 推模式消费者
     */
    PushConsumer<V> createPushConsumer();

    /**
     * 使用指定配置创建推模式消费者。
     *
     * @param config 消费者配置
     * @return 推模式消费者
     */
    PushConsumer<V> createPushConsumer(ConsumerConfig config);

    /**
     * 返回此订阅下所有已注册消费者的名称。
     *
     * @return 消费者名称集合
     */
    Set<String> getConsumerNames();

    /**
     * 检查指定名称的消费者是否存在于本订阅中。
     *
     * @param name 待检查的消费者名称
     * @return 存在返回 {@code true}，否则 {@code false}
     */
    boolean hasConsumer(String name);

    /**
     * 从本订阅移除指定名称的消费者。
     *
     * @param name 待移除的消费者名称
     * @return 成功移除返回 {@code true}，不存在则 {@code false}
     */
    boolean removeConsumer(String name);

    /**
     * 返回此订阅的名称。
     *
     * @return 订阅名称
     */
    String getName();

    /**
     * 将订阅偏移移动到指定位置。
     * <p>
     * 可用于从某点重放消息或跳转到较新位置；影响本订阅内所有消费者。
     *
     * @param value 目标位置
     */
    void seek(Position value);

    /**
     * 返回此订阅的统计信息。
     *
     * @return 统计对象
     */
    SubscriptionStatistics getStatistics();

}
