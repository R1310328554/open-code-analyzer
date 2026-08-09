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
package org.redisson.api;

/**
 * Redis 发布<b>过期</b>键空间事件时触发的 Redisson 对象监听器。
 * <p>
 * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code x} 字母。
 *
 * @author Nikita Koksharov
 */
@FunctionalInterface
public interface ExpiredObjectListener extends ObjectListener {

    /**
     * 对象 TTL 到期时回调。
     *
     * @param name 过期对象的 Redis 键名
     */
    void onExpired(String name);
    
}
