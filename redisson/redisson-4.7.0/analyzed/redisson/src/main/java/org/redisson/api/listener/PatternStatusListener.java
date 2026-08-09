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
 * 监听 Valkey 或 Redis Pub/Sub <b>模式订阅</b>状态变更。
 * <p>
 * 在客户端成功订阅或取消模式（pattern）时回调，
 * 可配合 {@link org.redisson.api.RTopic} 跟踪连接状态。
 *
 * @author Nikita Koksharov
 *
 * @see org.redisson.api.RTopic
 */
public interface PatternStatusListener extends EventListener {

    /**
     * 模式订阅成功时调用。
     *
     * @param pattern 已订阅的模式表达式
     */
    void onPSubscribe(String pattern);

    /**
     * 取消模式订阅时调用。
     *
     * @param pattern 已取消订阅的模式表达式
     */
    void onPUnsubscribe(String pattern);

}
