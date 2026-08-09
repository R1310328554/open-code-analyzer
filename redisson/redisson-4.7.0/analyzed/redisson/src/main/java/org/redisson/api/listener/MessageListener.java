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
package org.redisson.api.listener;

import java.util.EventListener;

/**
 * 监听通过 {@link org.redisson.api.RTopic} 发布的 Valkey 或 Redis 消息。
 * <p>
 * 订阅指定频道后，每条 Pub/Sub 消息都会回调 {@link #onMessage(CharSequence, Object)}。
 *
 * @author Nikita Koksharov
 *
 * @param <M> 消息体类型
 *
 * @see org.redisson.api.RTopic
 */
@FunctionalInterface
public interface MessageListener<M> extends EventListener {

    /**
     * 收到主题消息时调用。
     *
     * @param channel 主题频道名称
     * @param msg 主题消息内容
     */
    void onMessage(CharSequence channel, M msg);

}
