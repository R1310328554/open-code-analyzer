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

import org.redisson.api.Message;

import java.util.EventListener;

/**
 * 从 {@link PushConsumer} 接收消息的监听器接口。
 *
 * @param <V> 消息值的类型
 *
 * @author Nikita Koksharov
 *
 */
public interface MessageListener<V> extends EventListener {

    /**
     * 当从订阅收到消息时调用。
     * <p>
     * 消息逐条投递，不使用批量模式。
     * <p>
     * 实现方应处理消息后调用 {@link Acknowledgment#acknowledge(MessageAckArgs)}
     * 确认成功，或调用 {@link Acknowledgment#negativeAcknowledge(MessageNegativeAckArgs)}
     * 触发重新投递或死信处理。
     * <p>
     * 未确认的消息在可见性超时到期后将自动重新投递。
     *
     * @param message 收到的消息，包含负载与元数据
     * @param acknowledgment 该消息的确认处理器
     */
    void onMessage(Message<V> message, Acknowledgment acknowledgment);

}
