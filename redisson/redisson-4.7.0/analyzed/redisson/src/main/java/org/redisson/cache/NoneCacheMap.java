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
package org.redisson.cache;

/**
 * 无容量上限的本地缓存映射。
 * <p>
 * 当淘汰策略为 {@link org.redisson.api.LocalCachedMapOptions.EvictionPolicy#NONE} 时使用，
 * 仍支持 TTL 与最大空闲时间。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class NoneCacheMap<K, V> extends AbstractCacheMap<K, V> {

    /**
     * 创建无容量限制的缓存映射。
     *
     * @param timeToLiveInMillis 条目存活时间（毫秒）
     * @param maxIdleInMillis 最大空闲时间（毫秒）
     */
    public NoneCacheMap(long timeToLiveInMillis, long maxIdleInMillis) {
        super(0, timeToLiveInMillis, maxIdleInMillis);
    }

    /** 无容量限制，映射满时无需淘汰。 */
    @Override
    protected void onMapFull() {
    }
    
}
