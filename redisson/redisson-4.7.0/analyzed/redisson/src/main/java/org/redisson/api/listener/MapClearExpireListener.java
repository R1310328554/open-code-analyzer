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
 * 监听 Valkey 或 Redis 发布的 Hash<b>清除过期时间</b>（hpersist）键空间事件。
 * <p>
 * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code h} 或 {@code T} 字母。
 *
 * @author Nikita Koksharov
 */
@FunctionalInterface
public interface MapClearExpireListener extends ObjectListener {

    /**
     * 当 Hash 条目的过期时间被清除时触发。
     *
     * @param name 对象名称（键名）
     * @param fieldName Map 字段名；键事件通知时可为 null
     */
    void onClearExpire(String name, String fieldName);

}
