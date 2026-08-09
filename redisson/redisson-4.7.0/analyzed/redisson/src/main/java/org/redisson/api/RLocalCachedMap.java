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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 带本地条目缓存的 Map 同步 API。
 * <p>
 * 每个实例维护本地缓存以加速读操作，适合读多写少、需减少网络往返的场景。
 *
 * @author Nikita Koksharov
 * @param <K> Map 键类型
 * @param <V> Map 值类型
 */
public interface RLocalCachedMap<K, V> extends RMap<K, V> {
    
    /**
     * 预热本地缓存条目；不保证加载全部值，但在无并发写入时通常可加载几乎全部。
     * 统计上可预热几乎全部条目（无并发写入时可加载全部）。
     * 每批加载 10 个条目。
     */
    void preloadCache();

    /**
     * 预热本地缓存条目；不保证加载全部值，但在无并发写入时通常可加载几乎全部。
     * will preload approximately all (all if no concurrent mutating activity)
     * Entries are loaded in a batch. Batch size is defined by <code>count</code> param.
     *
     * @param count 每次 SCAN 加载的键数量
     */
    void preloadCache(int count);

    /**
     * 清除所有实例上的本地缓存。
     * 
     * @return 无返回值
     */
    RFuture<Void> clearLocalCacheAsync();
    
    /**
     * 清除所有实例上的本地缓存。
     */
    void clearLocalCache();
    
    /**
     * 返回本地缓存中的全部键。
     *
     * @return 键集合
     */
    Set<K> cachedKeySet();

    /**
     * 返回本地缓存中的全部值。
     *
     * @return 值集合
     */
    Collection<V> cachedValues();

    /**
     * 返回本地缓存中的全部 Map 条目。
     *
     * @return 条目集合
     */
    Set<Entry<K, V>> cachedEntrySet();

    /**
     * 返回本地缓存的当前快照（键值映射）。
     *
     * @return 本地缓存快照
     */
    Map<K, V> getCachedMap();

    /**
     * 注册本地缓存事件监听器。
     *
     * @see org.redisson.api.listener.LocalCacheUpdateListener
     * @see org.redisson.api.listener.LocalCacheInvalidateListener
     *
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    @Override
    int addListener(ObjectListener listener);

}