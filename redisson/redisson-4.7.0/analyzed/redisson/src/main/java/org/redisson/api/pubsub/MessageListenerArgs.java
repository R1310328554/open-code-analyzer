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

import org.redisson.api.SyncArgs;
import org.redisson.client.codec.Codec;

import java.time.Duration;

/**
 * 向 {@link PushConsumer} 注册 {@link MessageListener} 时的配置参数。
 *
 * @param <V> 消息值的类型
 * @author Nikita Koksharov
 */
public interface MessageListenerArgs<V> extends SyncArgs<MessageListenerArgs<V>> {

    /**
     * 使用指定监听器创建参数实例。
     *
     * @param <V> 消息值的类型
     * @param listener 要注册的消息监听器
     * @return 新的参数实例
     */
    static <V> MessageListenerArgs<V> listener(MessageListener<V> listener) {
        return new MessageListenerParams<>(listener);
    }

    /**
     * 设置消息处理的确认模式。
     *
     * <p>确认模式决定消息拉取后如何确认：
     * <ul>
     *   <li>{@code AcknowledgeMode.AUTO} - 投递后由系统自动确认</li>
     *   <li>{@code AcknowledgeMode.MANUAL} - 须由消费者显式确认</li>
     * </ul></p>
     * 默认值为 {@link AcknowledgeMode#MANUAL}。
     *
     * @param mode 确认模式
     * @return 参数对象
     * @see AcknowledgeMode
     */
    MessageListenerArgs<V> acknowledgeMode(AcknowledgeMode mode);

    /**
     * 指定用于解码消息头部的编解码器。
     *
     * @param codec 头部反序列化所用的编解码器
     * @return 参数对象
     */
    MessageListenerArgs<V> headersCodec(Codec codec);

    /**
     * 设置已拉取消息的可见性超时。
     * <p>
     * 可见性超时指定消息被拉取后、在确认或负向确认之前对其他消费者隐藏的时间，
     * 防止多个消费者同时处理同一条消息。
     * <p>
     * 若在此时间内未确认，消息将重新在订阅中可见，可能被其他消费者投递。
     * <p>
     * 若未设置，则使用订阅级别的可见性配置；
     * 若订阅也未设置，默认值为 <code>30 秒</code>。
     *
     * @param value 已拉取消息对其他消费者不可见的时长
     * @return 参数对象
     */
    MessageListenerArgs<V> visibility(Duration value);

}
