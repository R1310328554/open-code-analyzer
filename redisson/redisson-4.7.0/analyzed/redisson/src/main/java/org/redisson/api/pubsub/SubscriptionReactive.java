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

import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * 可靠 PubSub 主题的订阅（Reactive API）。
 * <p>
 * 每个订阅维护独立的偏移量，与同主题上的其他订阅互不影响地跟踪消息消费进度。
 * 单个订阅可挂载多个拉取或推送消费者，共同分担消息处理负载。
 *
 * @param <V> 消息值的类型
 *
 * @author Nikita Koksharov
 *
 */
public interface SubscriptionReactive<V> {

    /**
     * 创建使用自动生成名称的拉取消费者.
     * <p>
     * 拉取消费者按需获取消息，可手动控制消费速率与时机。
     *
     * @return 拉取消费者对象
     */
    Mono<PullConsumerReactive<V>> createPullConsumer();

    /**
     * 使用指定配置创建拉取消费者。
     * <p>
     * Pull consumers retrieve messages on-demand, providing manual control
     * over message consumption rate and timing.
     *
     * @param config 消费者配置
     * @return pull consumer object
     */
    Mono<PullConsumerReactive<V>> createPullConsumer(ConsumerConfig config);

    /**
     * 创建使用自动生成名称的推送消费者。
     * <p>
     * 推送消费者通过已注册的监听器自动接收消息，适合事件驱动式处理。
     *
     * @return pull consumer object
     */
    Mono<PushConsumerReactive<V>> createPushConsumer();

    /**
     * Creates a new push consumer with the specified configuration.
     * <p>
     * 推送消费者通过已注册的监听器自动接收消息，适合事件驱动式处理。
     *
     * @param config the consumer configuration
     * @return pull consumer object
     */
    Mono<PushConsumerReactive<V>> createPushConsumer(ConsumerConfig config);

    /**
     * 返回注册到本订阅的所有消费者名称。
     *
     * @return 消费者名称集合
     */
    Mono<Set<String>> getConsumerNames();

    /**
     * 检查指定名称的消费者是否存在于本订阅中。
     *
     * @param name 待检查的消费者名称
     * @return 存在返回 {@code true}，否则 {@code false}
     */
    Mono<Boolean> hasConsumer(String name);

    /**
     * 从本订阅中移除指定名称的消费者。
     *
     * @param name 待移除的消费者名称
     * @return 移除成功返回 {@code true}，不存在则 {@code false}
     */
    Mono<Boolean> removeConsumer(String name);

    /**
     * 返回本订阅的名称。
     *
     * @return 订阅名称
     */
    String getName();

    /**
     * 将订阅偏移量移动到指定位置。
     * <p>
     * 可用于从某一点重放消息，或跳转到较新的消息；会影响本订阅内的所有消费者。
     *
     * @param value 目标位置
     */
    Mono<Void> seek(Position value);

    /**
     * 返回本订阅的统计信息。
     *
     * @return 统计对象
     */
    Mono<SubscriptionStatistics> getStatistics();

}
