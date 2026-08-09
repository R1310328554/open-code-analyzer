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

/**
 * 基于 Project Reactor 的推模式消费者，通过监听器自动接收消息。
 * <p>
 * 消息一旦可用即事件驱动地投递到应用，监听器负责处理与确认。
 * <p>
 * 投递的消息须通过
 * {@link Acknowledgment#acknowledge(MessageAckArgs)} 或
 * {@link Acknowledgment#negativeAcknowledge(MessageNegativeAckArgs)} 显式确认；
 * 未确认的消息在可见性超时后将自动重新投递。
 *
 * @param <V> 消息值类型
 *
 * @author Nikita Koksharov
 *
 */
public interface PushConsumerReactive<V> extends ConsumerReactive {

    /**
     * 注册监听器以接收此消费者的消息。
     * <p>
     * 注册后，订阅中有新消息可用时将自动回调监听器。
     * <p>
     * 每个消费者只能注册一个监听器；更换监听器需创建新的消费者实例。
     *
     * @param listenerArgs 包含消息处理器的监听器配置
     * @throws IllegalStateException 若已注册过监听器
     */
    void registerListener(MessageListenerArgs<V> listenerArgs);

}
