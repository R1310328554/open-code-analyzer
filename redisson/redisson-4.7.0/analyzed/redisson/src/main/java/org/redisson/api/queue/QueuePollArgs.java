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

import org.redisson.api.SyncArgs;
import org.redisson.client.codec.Codec;

import java.time.Duration;

/**
 * 队列拉取（poll）操作的参数。
 *
 * <p>使用 {@code defaults()} 工厂方法创建带默认设置的实例。</p>
 *
 * <p>用法示例：</p>
 * <pre>
 * QueuePollArgs args = QueuePollArgs.defaults()
 *     .acknowledgeMode(AcknowledgeMode.MANUAL)
 *     .timeout(Duration.ofSeconds(5))
 *     .count(10);
 * </pre>
 *
 * @author Nikita Koksharov
 *
 */
public interface QueuePollArgs extends SyncArgs<QueuePollArgs> {

    /**
     * 创建带默认设置的 {@link QueuePollArgs} 实例。
     *
     * @return 参数对象
     */
    static QueuePollArgs defaults() {
        return new QueuePollParams();
    }

    /**
     * 设置消息确认模式。
     *
     * <p>确认模式决定消息被拉取后如何确认：
     * <ul>
     *   <li>{@code AcknowledgeMode.AUTO} — 投递后自动确认</li>
     *   <li>{@code AcknowledgeMode.MANUAL} — 需消费者显式确认</li>
     * </ul></p>
     * 默认值为 {@code AcknowledgeMode.MANUAL}。
     *
     * @param mode 确认模式
     * @return 参数对象
     * @see AcknowledgeMode
     */
    QueuePollArgs acknowledgeMode(AcknowledgeMode mode);

    /**
     * 指定用于解码消息头字段的 Codec。
     *
     * @param codec 消息头反序列化编解码器
     * @return 参数对象
     */
    QueuePollArgs headersCodec(Codec codec);

    /**
     * 设置等待消息可用的最长时长。
     *
     * <p>若队列为空，拉取操作将阻塞，直到：
     * <ul>
     *   <li>至少有一条消息可用，或</li>
     *   <li>达到指定的超时时长</li>
     * </ul>
     *
     * <p>超时仍无消息时，拉取操作返回空集合。
     * <p>
     * {@code 0} 表示无限期等待消息。
     * <p>
     * 默认值为未定义。
     *
     * @param value 等待消息的最长时长
     * @return 参数对象
     */
    QueuePollArgs timeout(Duration value);

    /**
     * 设置已拉取消息的可见性超时。
     * <p>
     * 可见性超时指定消息被拉取后、在确认或负确认之前，
     * 对其他消费者不可见的时长，避免同一条消息被多个消费者同时处理。
     * <p>
     * 若在此时间内未确认，消息将重新在队列中可见，可能被投递给其他消费者。
     * <p>
     * 若未设置，则使用队列级可见性配置；
     * 若队列也未配置，默认值为 {@code 30 秒}。
     *
     * @param value 已拉取消息对其他消费者不可见的时长
     * @return 参数对象
     */
    QueuePollArgs visibility(Duration value);

    /**
     * 设置单次拉取操作最多返回的消息条数。
     *
     * <p>启用批量拉取可提升吞吐；实际返回条数可能少于请求值（队列中可用消息不足时）。</p>
     *
     * @param value 最多拉取的消息条数
     * @return 参数对象
     */
    QueuePollArgs count(int value);

}
