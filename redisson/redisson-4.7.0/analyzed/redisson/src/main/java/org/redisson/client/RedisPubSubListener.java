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
package org.redisson.client;

import org.redisson.api.listener.MessageListener;
import org.redisson.client.protocol.pubsub.PubSubType;

/**
 * Redis 发布/订阅消息监听器，扩展 {@link MessageListener}。
 * <p>
 * 提供订阅状态变更与模式消息的默认空实现回调。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 消息值类型
 */
public interface RedisPubSubListener<V> extends MessageListener<V> {

    /** 订阅/取消订阅状态变更回调，默认空实现。 */
    default void onStatus(PubSubType type, CharSequence channel) {
    }

    /** 模式订阅消息回调，默认空实现。 */
    default void onPatternMessage(CharSequence pattern, CharSequence channel, V message) {
    }

}
