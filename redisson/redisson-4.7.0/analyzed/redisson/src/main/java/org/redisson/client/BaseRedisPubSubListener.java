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

import org.redisson.client.protocol.pubsub.PubSubType;

/**
 * Redis 发布/订阅监听器的空实现基类。
 * <p>
 * 子类可按需覆盖 {@link #onStatus}、{@link #onMessage} 或 {@link #onPatternMessage}。
 *
 * @author Nikita Koksharov
 *
 */
public class BaseRedisPubSubListener implements RedisPubSubListener<Object> {

    /** 订阅状态变更回调，默认空实现。 */
    @Override
    public void onStatus(PubSubType type, CharSequence channel) {
    }

    /** 频道消息回调，默认空实现。 */
    @Override
    public void onMessage(CharSequence channel, Object message) {
    }

    /** 模式订阅消息回调，默认空实现。 */
    @Override
    public void onPatternMessage(CharSequence pattern, CharSequence channel, Object message) {
    }

}
