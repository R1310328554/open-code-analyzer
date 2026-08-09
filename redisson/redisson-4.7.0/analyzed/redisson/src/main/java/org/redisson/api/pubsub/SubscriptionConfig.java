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
 * 可靠主题订阅的配置参数接口。
 *
 * @author Nikita Koksharov
 *
 */
public interface SubscriptionConfig {

    /**
     * 创建使用自动生成名称的订阅配置。
     *
     * @return 订阅配置
     */
    static SubscriptionConfig generatedName() {
        return new SubscriptionConfigParams(null);
    }

    /**
     * 创建使用指定名称的订阅配置。
     *
     * @param value 订阅名称
     * @return 订阅配置
     */
    static SubscriptionConfig name(String value) {
        return new SubscriptionConfigParams(value);
    }

    /**
     * 设置死信主题（DLT）名称，用于接收达到投递上限或被拒绝的消息。
     * <p>
     * 传入 {@code null} 可移除死信主题配置。
     *
     * @param value 死信主题名称
     * @return 配置对象
     */
    SubscriptionConfig deadLetterTopicName(String value);

    /**
     * 定义单条消息的最大投递尝试次数。
     * <p>
     * 达到上限后，若已配置死信主题则移入其中，否则删除消息。
     * 发布消息时可覆盖此值。
     * <p>
     * 默认值为 10 次。
     *
     * @param value 最大投递次数
     * @return 配置对象
     */
    SubscriptionConfig deliveryLimit(int value);

    /**
     * 设置消息被拉取后对其它消费者不可见的时长（可见性超时）。
     * <p>
     * 防止多条消息被并发处理；拉取或推模式监听器可单独覆盖。
     * <p>
     * 默认值为 30 秒。
     *
     * @param value 可见性超时时长
     * @return 配置对象
     */
    SubscriptionConfig visibility(Duration value);

    /**
     * 设置订阅开始消费消息的初始位置。
     * <p>
     * 可选位置：
     * <ul>
     *   <li>{@link Position#latest()} — 从最新消息开始（默认）</li>
     *   <li>{@link Position#earliest()} — 从最早可用消息开始</li>
     *   <li>{@link Position#messageId(String)} — 从指定消息 ID 起（含）</li>
     *   <li>{@link Position#messageIdExclusive(String)} — 从指定消息 ID 之后（不含）</li>
     *   <li>{@link Position#timestamp(java.time.Instant)} — 从指定时间戳起（含）</li>
     *   <li>{@link Position#timestampExclusive(java.time.Instant)} — 从指定时间戳之后（不含）</li>
     * </ul>
     * <p>
     * 默认值为 {@link Position#latest()}。
     *
     * @param value 起始消费位置
     * @return 配置对象
     */
    SubscriptionConfig position(Position value);

    /**
     * 启用确认后仍保留消息。
     * <p>
     * 启用后，已确认的消息仍留在主题中，便于重放或审计；
     * 默认情况下确认后即删除。
     *
     * @return 配置对象
     */
    SubscriptionConfig retainAfterAck();

}
