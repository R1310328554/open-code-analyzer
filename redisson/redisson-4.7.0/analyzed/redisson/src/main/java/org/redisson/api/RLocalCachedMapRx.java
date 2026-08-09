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

import io.reactivex.rxjava3.core.Completable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 带本地条目缓存的 Map RxJava API。
 * <p>
 * 各方法返回 {@link Completable} 或 {@link Single}；本地缓存视图方法同步返回。
 *
 * @author Nikita Koksharov
 * @param <K> Map 键类型
 * @param <V> Map 值类型
 */
public interface RLocalCachedMapRx<K, V> extends RMapRx<K, V> {

    /**
     * 清除所有实例上的本地缓存。
     *
     * @return 无返回值
     */
    Completable clearLocalCache();

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
    Set<Map.Entry<K, V>> cachedEntrySet();

    /**
     * 返回本地缓存的当前快照（键值映射）。
     *
     * @return 本地缓存快照
     */
    Map<K, V> getCachedMap();

}
