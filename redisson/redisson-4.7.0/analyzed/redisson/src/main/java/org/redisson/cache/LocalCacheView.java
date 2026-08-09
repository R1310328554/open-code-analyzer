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

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.redisson.RedissonObject;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.misc.Hash;

import io.netty.buffer.ByteBuf;

/**
 * 本地缓存只读视图，提供键集、值集、条目集及 Map 视图。
 * <p>
 * 根据 {@link org.redisson.api.LocalCachedMapOptions} 创建底层缓存实现。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class LocalCacheView<K, V> {

    private final RedissonObject object;
    private final ConcurrentMap<CacheKey, CacheValue> cache;
    private final ConcurrentMap<Object, CacheKey> cacheKeyMap;
    private final boolean useObjectAsCacheKey;

    public LocalCacheView(LocalCachedMapOptions<?, ?> options, RedissonObject object) {
        this.cache = createCache(options);
        this.object = object;
        this.cacheKeyMap = createCache(options);
        this.useObjectAsCacheKey = options.isUseObjectAsCacheKey();
    }

    /** 返回本地缓存中所有键的集合视图。 */
    public Set<K> cachedKeySet() {
        return new LocalKeySet();
    }

    class LocalKeySet extends AbstractSet<K> {

        @Override
        public Iterator<K> iterator() {
            return new Iterator<K>() {

                private Iterator<CacheValue> iter = cache.values().iterator();
                
                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public K next() {
                    return (K) iter.next().getKey();
                }
                
                @Override
                public void remove() {
                    if (useObjectAsCacheKey) {
                        cacheKeyMap.remove(((AbstractCacheMap.MapIterator) iter).cursorValue().getKey());
                    }
                    iter.remove();
                }
            };
        }

        @Override
        public boolean contains(Object o) {
            CacheKey cacheKey = toCacheKey(o);
            return cache.containsKey(cacheKey);
        }

        @Override
        public boolean remove(Object o) {
            CacheKey cacheKey = toCacheKey(o);
            if (useObjectAsCacheKey) {
                cacheKeyMap.remove(o);
            }
            return cache.remove(cacheKey) != null;
        }

        @Override
        public int size() {
            return cache.size();
        }

        @Override
        public void clear() {
            if (useObjectAsCacheKey) {
                cacheKeyMap.clear();
            }
            cache.clear();
        }

    }
    
    /** 返回本地缓存中所有值的集合视图。 */
    public Collection<V> cachedValues() {
        return new LocalValues();
    }
    
    final class LocalValues extends AbstractCollection<V> {

        @Override
        public Iterator<V> iterator() {
            return new Iterator<V>() {
                
                private Iterator<CacheValue> iter = cache.values().iterator();
                
                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public V next() {
                    return (V) iter.next().getValue();
                }
                
                @Override
                public void remove() {
                    if (useObjectAsCacheKey) {
                        cacheKeyMap.remove(((AbstractCacheMap.MapIterator) iter).cursorValue().getKey());
                    }
                    iter.remove();
                }
            };
        }

        @Override
        public boolean contains(Object o) {
            CacheValue cacheValue = new CacheValue(null, o);
            return cache.containsValue(cacheValue);
        }

        @Override
        public int size() {
            return cache.size();
        }

        @Override
        public void clear() {
            cache.clear();
            if (useObjectAsCacheKey) {
                cacheKeyMap.clear();
            }
        }

    }

    /** 返回本地缓存中所有键值条目的集合视图。 */
    public Set<Entry<K, V>> cachedEntrySet() {
        return new LocalEntrySet();
    }

    final class LocalEntrySet extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new Iterator<Map.Entry<K, V>>() {
                
                private Iterator<CacheValue> iter = cache.values().iterator();
                
                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public Map.Entry<K, V> next() {
                    CacheValue e = iter.next();
                    V val = toValue(e);
                    return new AbstractMap.SimpleEntry<K, V>((K) e.getKey(), val);
                }
                
                @Override
                public void remove() {
                    if (useObjectAsCacheKey) {
                        cacheKeyMap.remove(((AbstractCacheMap.MapIterator) iter).cursorValue().getKey());
                    }
                    iter.remove();
                }
            };
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            CacheKey cacheKey = toCacheKey(e.getKey());
            CacheValue entry = cache.get(cacheKey);
            return entry != null && entry.getValue().equals(e.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                CacheKey cacheKey = toCacheKey(e.getKey());
                if (useObjectAsCacheKey) {
                    cacheKeyMap.remove(e.getKey());
                }
                return cache.remove(cacheKey) != null;
            }
            return false;
        }

        @Override
        public int size() {
            return cache.size();
        }

        @Override
        public void clear() {
            cache.clear();
        }

    }
    
    /** 返回本地缓存的 Map 视图。 */
    public Map<K, V> getCachedMap() {
        return new LocalMap();
    }
    
    final class LocalMap extends AbstractMap<K, V> {

        @Override
        public V get(Object key) {
            CacheKey cacheKey = toCacheKey(key);
            CacheValue e = cache.get(cacheKey);
            if (e != null) {
                return (V) e.getValue();
            }
            return null;
        }
        
        @Override
        public boolean containsKey(Object key) {
            CacheKey cacheKey = toCacheKey(key);
            return cache.containsKey(cacheKey);
        }
        
        @Override
        public boolean containsValue(Object value) {
            CacheValue cacheValue = new CacheValue(null, value);
            return cache.containsValue(cacheValue);
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return cachedEntrySet();
        }

    }

    /** 将内部 {@link CacheValue} 转换为值对象。 */
    protected V toValue(CacheValue cv) {
        return (V) cv.getValue();
    }

    /** 将业务键转换为内部 {@link CacheKey}（含编码与哈希）。 */
    public CacheKey toCacheKey(Object key) {
        CacheKey cacheKey;
        if (useObjectAsCacheKey) {
            cacheKey = cacheKeyMap.get(key);
            if (cacheKey != null) {
                return cacheKey;
            }
        }
        ByteBuf encoded = encodeMapKey(key);
        try {
            return toCacheKey(encoded);
        } finally {
            encoded.release();
        }
    }

    /** 编码 Map 键为 Netty 字节缓冲区。 */
    protected ByteBuf encodeMapKey(Object key) {
        return object.encodeMapKey(key);
    }

    /** 在启用对象作为缓存键时，建立业务键与 {@link CacheKey} 的映射。 */
    public void putCacheKey(Object key, CacheKey cacheKey) {
        if (useObjectAsCacheKey) {
            cacheKeyMap.put(key, cacheKey);
        }
    }

    /** 由已编码键缓冲区计算 128 位哈希并构造 {@link CacheKey}。 */
    public CacheKey toCacheKey(ByteBuf encodedKey) {
        return new CacheKey(Hash.hash128toArray(encodedKey));
    }

    /** 返回底层并发缓存映射。 */
    public <K1, V1> ConcurrentMap<K1, V1> getCache() {
        return (ConcurrentMap<K1, V1>) cache;
    }

    /** 返回业务键到 {@link CacheKey} 的反向映射。 */
    public ConcurrentMap<Object, CacheKey> getCacheKeyMap() {
        return cacheKeyMap;
    }

    /**
     * 根据配置选项创建本地缓存实现。
     * <p>
     * 支持 Caffeine、LRU、LFU、软/弱引用及无操作等多种策略。
     *
     * @param options 本地缓存配置
     * @return 并发缓存映射实例
     */
    public <K1, V1> ConcurrentMap<K1, V1> createCache(LocalCachedMapOptions<?, ?> options) {
        if (options.getCacheSize() == -1) {
            return new NoOpCacheMap<>();
        }

        if (options.getCacheProvider() == LocalCachedMapOptions.CacheProvider.CAFFEINE) {
            Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder();
            if (options.getTimeToLiveInMillis() > 0) {
                caffeineBuilder.expireAfterWrite(options.getTimeToLiveInMillis(), TimeUnit.MILLISECONDS);
            }
            if (options.getMaxIdleInMillis() > 0) {
                caffeineBuilder.expireAfterAccess(options.getMaxIdleInMillis(), TimeUnit.MILLISECONDS);
            }
            if (options.getCacheSize() > 0) {
                caffeineBuilder.maximumSize(options.getCacheSize());
            }
            if (options.getEvictionPolicy() == LocalCachedMapOptions.EvictionPolicy.SOFT) {
                caffeineBuilder.softValues();
            }
            if (options.getEvictionPolicy() == LocalCachedMapOptions.EvictionPolicy.WEAK) {
                caffeineBuilder.weakValues();
            }
            return caffeineBuilder.<K1, V1>build().asMap();
        }

        if (options.getEvictionPolicy() == LocalCachedMapOptions.EvictionPolicy.NONE) {
            return new NoneCacheMap<>(options.getTimeToLiveInMillis(), options.getMaxIdleInMillis());
        }
        if (options.getEvictionPolicy() == LocalCachedMapOptions.EvictionPolicy.LRU) {
            return new LRUCacheMap<>(options.getCacheSize(), options.getTimeToLiveInMillis(), options.getMaxIdleInMillis());
        }
        if (options.getEvictionPolicy() == LocalCachedMapOptions.EvictionPolicy.LFU) {
            return new LFUCacheMap<>(options.getCacheSize(), options.getTimeToLiveInMillis(), options.getMaxIdleInMillis());
        }
        if (options.getEvictionPolicy() == LocalCachedMapOptions.EvictionPolicy.SOFT) {
            return ReferenceCacheMap.soft(options.getTimeToLiveInMillis(), options.getMaxIdleInMillis());
        }
        if (options.getEvictionPolicy() == LocalCachedMapOptions.EvictionPolicy.WEAK) {
            return ReferenceCacheMap.weak(options.getTimeToLiveInMillis(), options.getMaxIdleInMillis());
        }
        throw new IllegalArgumentException("Invalid eviction policy: " + options.getEvictionPolicy());
    }


}
