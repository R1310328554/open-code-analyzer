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

import java.time.Duration;

/**
 * 定义可靠主题的各项参数。
 *
 * @author Nikita Koksharov
 *
 */
public interface TopicConfig {

    /**
     * 创建使用默认设置的 {@link TopicConfig} 实例。
     *
     * @return 配置对象
     */
    static TopicConfig defaults() {
        return new TopicConfigParams();
    }

    /**
     * 设置消息被拉取后对其它消费者不可见的时长。
     * 用于防止同一条消息被多个消费者并发处理。
     * 可在订阅、拉取消息或定义推送监听器时覆盖此值。
     * <p>
     * 默认值为 30 秒。
     *
     * @param value 可见性超时时长
     * @return config object
     */
    TopicConfig visibility(Duration value);

    /**
     * 设置主题中消息的存活时间（TTL）。
     * 超过该时长后消息将从主题中自动移除。
     * {@code 0} 表示不启用过期。
     * 可在发布消息时覆盖此值。
     * <p>
     * 默认值为 {@code 0}。
     *
     * @param value 存活时长
     * @return config object
     */
    TopicConfig timeToLive(Duration value);

    /**
     * 设置单条消息允许的最大字节数。
     * 超出限制的消息将被拒绝。
     * {@code 0} 表示不限制大小。
     * <p>
     * 默认值为 {@code 0}。
     *
     * @param value 最大消息大小（字节）
     * @return config object
     */
    TopicConfig maxMessageSize(int value);

    /**
     * 设置消息写入主题后、可供消费前的延迟时长。
     * {@code 0} 表示不启用延迟。
     * 可在发布消息时覆盖此值。
     * <p>
     * 默认值为 {@code 0}。
     *
     * @param delay 延迟时长
     * @return config object
     */
    TopicConfig delay(Duration delay);

    /**
     * 设置主题可存储的最大消息条数。
     * 达到上限后，添加消息的操作可能被阻塞和/或返回空结果。
     * {@code 0} 表示不限制主题大小。
     * <p>
     * 默认值为 {@code 0}。
     *
     * @param value 最大主题容量
     * @return config object
     */
    TopicConfig maxSize(int value);

    /**
     * 定义单条消息的最大投递次数。
     * 达到上限后，若已配置死信主题则消息转入死信，否则被删除。
     * 可在订阅或发布消息时覆盖此值。
     * <p>
     * 默认值为 10 次。
     *
     * @param value 最大投递次数
     * @return config object
     */
    TopicConfig deliveryLimit(int value);

    /**
     * 定义主题中消息的保留策略，控制消息何时存储以及
     * 在何种订阅与处理状态下可被丢弃。
     * <p>
     * Default value is {@link RetentionMode#SUBSCRIPTION_OPTIONAL_RETAIN_ALL}
     *
     * @param mode 保留模式
     * @return config object
     */
    TopicConfig retentionMode(RetentionMode mode);

}
