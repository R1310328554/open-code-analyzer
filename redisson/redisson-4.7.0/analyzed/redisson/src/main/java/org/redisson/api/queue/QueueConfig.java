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
package org.redisson.api.queue;

import java.time.Duration;

/**
 * 定义可靠队列的参数配置。
 *
 * @author Nikita Koksharov
 *
 */
public interface QueueConfig {

    /**
     * 创建带默认设置的 {@link QueueConfig} 实例。
     *
     * @return 配置对象
     */
    static QueueConfig defaults() {
        return new QueueConfigParams();
    }

    /**
     * 设置单条消息的最大投递尝试次数。
     * 达到上限后，若已配置死信队列（DLQ）则消息转入 DLQ，否则将被删除。
     * 入队时可单独覆盖此值。
     * <p>
     * 默认值为 10 次。
     *
     * @param value 最大投递次数
     * @return 配置对象
     */
    QueueConfig deliveryLimit(int value);

    /**
     * 设置消息被拉取后对其它消费者不可见的时长（可见性超时）。
     * 用于避免同一条消息被多个消费者同时处理。
     * 拉取时可单独覆盖此值。
     * <p>
     * 默认值为 30 秒。
     *
     * @param value 可见性超时时长
     * @return 配置对象
     */
    QueueConfig visibility(Duration value);

    /**
     * 设置队列中消息的存活时间（TTL）。
     * 超过该时长后消息将自动从队列中移除。
     * 值为 0 表示不启用过期。
     * 入队时可单独覆盖此值。
     * <p>
     * 默认值为 0。
     *
     * @param value 存活时长
     * @return 配置对象
     */
    QueueConfig timeToLive(Duration value);

    /**
     * 设置死信队列（Dead Letter Queue，DLQ）名称。
     * 达到投递上限或被拒绝的消息将发往该队列。
     * <p>
     * 传入 {@code null} 可移除死信队列配置。
     *
     * @param value 死信队列名称
     * @return 配置对象
     */
    QueueConfig deadLetterQueueName(String value);

    /**
     * 设置单条消息允许的最大字节数。
     * 超出限制的消息将被拒绝。
     * 值为 0 表示不限制大小。
     * <p>
     * 默认值为 0。
     *
     * @param value 最大消息字节数
     * @return 配置对象
     */
    QueueConfig maxMessageSize(int value);

    /**
     * 设置消息入队后、可被消费前的延迟时长。
     * 值为 0 表示不延迟。
     * 入队时可单独覆盖此值。
     * <p>
     * 默认值为 0。
     *
     * @param delay 延迟时长
     * @return 配置对象
     */
    QueueConfig delay(Duration delay);

    /**
     * 设置队列可存储的最大消息条数。
     * 达到上限后，入队操作可能被阻塞和/或返回空结果。
     * 值为 0 表示不限制队列大小。
     * <p>
     * 默认值为 0。
     *
     * @param value 最大队列容量
     * @return 配置对象
     */
    QueueConfig maxSize(int value);

    /**
     * 设置队列的消息处理模式，决定消费者如何消费消息。
     *
     * @param mode 处理模式
     * @return 配置对象
     */
    QueueConfig processingMode(ProcessingMode mode);

}
