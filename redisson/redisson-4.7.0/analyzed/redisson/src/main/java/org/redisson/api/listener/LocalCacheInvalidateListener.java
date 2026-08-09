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
 * 监听 Valkey 或 Redis 发布的<b>本地缓存失效</b>事件。
 * <p>
 * 当远程 Map 条目变更导致本地缓存条目需失效时回调，
 * 适用于带本地缓存的 {@link org.redisson.api.RLocalCachedMap} 等结构。
 *
 * @author Nikita Koksharov
 *
 */
@FunctionalInterface
public interface LocalCacheInvalidateListener<K, V> extends ObjectListener {

    /**
     * 当 Map 条目从本地缓存中失效时触发。
     *
     * @param key 待移除的键
     * @param value 待移除的值
     */
    void onInvalidate(K key, V value);

}
