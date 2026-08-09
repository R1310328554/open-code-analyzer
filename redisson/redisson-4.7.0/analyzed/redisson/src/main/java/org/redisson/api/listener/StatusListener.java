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
 * 监听 Valkey 或 Redis Pub/Sub 频道订阅状态变更。
 * <p>
 * 在订阅成功、取消订阅以及重连或故障转移过程中触发回调。
 *
 * @author Nikita Koksharov
 *
 * @see org.redisson.api.RTopic
 */
public interface StatusListener extends EventListener {

    /**
     * Redisson 成功订阅频道时调用。
     * <p>
     * 在重连或故障转移过程中也会触发。
     * 
     * @param channel 已订阅的频道名称
     */
    void onSubscribe(String channel);

    /**
     * Redisson 成功取消频道订阅时调用。
     * 
     * @param channel 已取消订阅的频道名称
     */
    void onUnsubscribe(String channel);

}
