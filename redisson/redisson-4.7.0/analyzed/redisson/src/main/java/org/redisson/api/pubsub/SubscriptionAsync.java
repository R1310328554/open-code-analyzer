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

import org.redisson.api.RFuture;

import java.util.Set;

/**
 * 可靠 Pub/Sub 主题订阅的异步 API。
 * <p>
 * 每个订阅维护独立偏移；可挂载多个拉模式或推模式消费者共同处理消息。
 *
 * @param <V> 消息值类型
 *
 * @author Nikita Koksharov
 *
 */
public interface SubscriptionAsync<V> {

    /**
     * 异步创建使用自动生成名称的拉模式消费者。
     *
     * @return 拉模式消费者的异步结果
     */
    RFuture<PullConsumer<V>> createPullConsumerAsync();

    /**
     * 异步创建使用指定配置的拉模式消费者。
     *
     * @param config 消费者配置
     * @return 拉模式消费者的异步结果
     */
    RFuture<PullConsumer<V>> createPullConsumerAsync(ConsumerConfig config);

    /**
     * 异步创建使用自动生成名称的推模式消费者。
     *
     * @return 推模式消费者的异步结果
     */
    RFuture<PushConsumer<V>> createPushConsumerAsync();

    /**
     * 异步创建使用指定配置的推模式消费者。
     *
     * @param config 消费者配置
     * @return 推模式消费者的异步结果
     */
    RFuture<PushConsumer<V>> createPushConsumerAsync(ConsumerConfig config);

    /**
     * 异步返回此订阅下所有已注册消费者的名称。
     *
     * @return 消费者名称集合的异步结果
     */
    RFuture<Set<String>> getConsumerNamesAsync();

    /**
     * 异步检查指定名称的消费者是否存在于本订阅中。
     *
     * @param name 待检查的消费者名称
     * @return 是否存在
     */
    RFuture<Boolean> hasConsumerAsync(String name);

    /**
     * 异步从本订阅移除指定名称的消费者。
     *
     * @param name 待移除的消费者名称
     * @return 是否成功移除
     */
    RFuture<Boolean> removeConsumerAsync(String name);

    /**
     * 异步将订阅偏移移动到指定位置。
     * <p>
     * 可用于重放或跳过消息；影响本订阅内所有消费者。
     *
     * @param value 目标位置
     * @return 完成时的异步结果
     */
    RFuture<Void> seekAsync(Position value);

    /**
     * 异步返回此订阅的统计信息。
     *
     * @return 统计对象的异步结果
     */
    RFuture<SubscriptionStatistics> getStatisticsAsync();

}
