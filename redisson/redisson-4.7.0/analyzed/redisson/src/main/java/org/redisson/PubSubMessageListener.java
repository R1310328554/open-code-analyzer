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
package org.redisson;

import org.redisson.api.listener.MessageListener;
import org.redisson.client.RedisPubSubListener;
import org.redisson.client.protocol.pubsub.PubSubType;

import java.util.Set;

/**
 * 将 Redis 频道/模式消息转发给 {@link MessageListener} 的适配器。
 * <p>仅当频道/模式在 {@code names} 中且消息类型匹配时才回调。
 *
 * @author Nikita Koksharov
 * @param <V> 消息体类型
 */
public class PubSubMessageListener<V> implements RedisPubSubListener<Object> {

    private final MessageListener<V> listener;
    private final Set<String> names;
    private final Class<V> type;
    private Runnable callback;

    /** @param type 期望的消息类型
     *  @param listener 用户消息监听器
     *  @param names 监听的频道或模式名集合
     */
    public PubSubMessageListener(Class<V> type, MessageListener<V> listener, Set<String> names) {
        super();
        this.type = type;
        this.listener = listener;
        this.names = names;
    }

    /** 同上；消息匹配后额外执行 {@code callback}（如释放信号量）。 */
    public PubSubMessageListener(Class<V> type, MessageListener<V> listener, Set<String> names, Runnable callback) {
        super();
        this.type = type;
        this.listener = listener;
        this.names = names;
        this.callback = callback;
    }

    public MessageListener<V> getListener() {
        return listener;
    }

    /** SUBSCRIBE 模式：频道名在 names 中且类型匹配时回调。 */
    @Override
    public void onMessage(CharSequence channel, Object message) {
        // could be subscribed to multiple channels
        if (names.contains(channel.toString()) && type.isInstance(message)) {
            listener.onMessage(channel, (V) message);
            if (callback != null) {
                callback.run();
            }
        }
    }

    /** PSUBSCRIBE 模式：模式名在 names 中且类型匹配时回调。 */
    @Override
    public void onPatternMessage(CharSequence pattern, CharSequence channel, Object message) {
        // could be subscribed to multiple channels
        if (names.contains(pattern.toString()) && type.isInstance(message)) {
            listener.onMessage(channel, (V) message);
            if (callback != null) {
                callback.run();
            }
        }
    }

    @Override
    public void onStatus(PubSubType type, CharSequence channel) {
    }

}
