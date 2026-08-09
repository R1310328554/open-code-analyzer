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

import org.redisson.api.ObjectListener;

/**
 * 监听 Valkey 或 Redis 发布的<b>客户端追踪</b>（client tracking）失效事件。
 * <p>
 * 当服务端检测到被追踪的键发生变更并通知客户端时触发；需要 Redis 6.0+ 或任意版本 Valkey。
 *
 * @author Nikita Koksharov
 *
 */
@FunctionalInterface
public interface TrackingListener extends ObjectListener {

    /**
     * 当被追踪的 Redisson 对象在服务端发生变更时触发。
     *
     * @param name 对象名称（键名）
     */
    void onChange(String name);

}
