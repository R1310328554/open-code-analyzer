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
 * 监听通过 {@link org.redisson.api.RTopic} 以<b>模式订阅</b>接收的 Valkey 或 Redis 消息。
 * <p>
 * 与 {@link MessageListener} 不同，回调中包含匹配的模式与具体频道名称。
 *
 * @author Nikita Koksharov
 *
 * @param <M> 消息体类型
 *
 * @see org.redisson.api.RTopic
 */
@FunctionalInterface
public interface PatternMessageListener<M> extends EventListener {

    /**
     * 收到模式匹配的主题消息时调用。
     *
     * @param pattern 订阅的模式表达式
     * @param channel 实际消息来源频道
     * @param msg 主题消息内容
     */
    void onMessage(CharSequence pattern, CharSequence channel, M msg);

}
