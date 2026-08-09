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
package org.redisson.reactive;

import org.reactivestreams.Publisher;
import org.redisson.RedissonMap;
import org.redisson.api.*;
import reactor.core.publisher.Flux;

import java.util.Map.Entry;

/**
 * {@link RMap} 的 Reactor 扩展：HSCAN 迭代键/值/条目，以及 map 内嵌锁/信号量工厂。
 * <p>
 * 迭代基于 {@link MapReactiveIterator}；锁类对象经 {@link ReactiveProxyBuilder} 暴露。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class RedissonMapReactive<K, V> {

    /** 底层 Redis Hash 映射。 */
    private final RMap<K, V> map;
    /** 创建嵌套分布式对象代理的执行器。 */
    private final CommandReactiveExecutor commandExecutor;

    /** 绑定 map 与 Reactor 执行上下文。 */
    public RedissonMapReactive(RMap<K, V> map, CommandReactiveExecutor commandExecutor) {
        this.map = map;
        this.commandExecutor = commandExecutor;
    }
    
    /** 全量条目迭代，默认 chunk=10。 */
    public Publisher<Entry<K, V>> entryIterator() {
        return entryIterator(null);
    }
    
    /** 指定 chunk 的条目迭代。 */
    public Publisher<Entry<K, V>> entryIterator(int count) {
        return entryIterator(null, count);
    }
    
    /** 按字段名模式迭代条目。 */
    public Publisher<Entry<K, V>> entryIterator(String pattern) {
        return entryIterator(pattern, 10);
    }
    
    /** 同时指定模式与 chunk 的条目迭代。 */
    public Publisher<Entry<K, V>> entryIterator(String pattern, int count) {
        return Flux.create(new MapReactiveIterator<>((RedissonMap<K, V>) map, pattern, count));
    }

    /** 仅 emit 值的 HSCAN 流。 */
    public Publisher<V> valueIterator() {
        return valueIterator(null);
    }
    
    /** 按模式扫描值。 */
    public Publisher<V> valueIterator(String pattern) {
        return valueIterator(pattern, 10);
    }
    
    /** 指定 chunk 的值迭代。 */
    public Publisher<V> valueIterator(int count) {
        return valueIterator(null, count);
    }
    
    /** 模式 + chunk 的值迭代。 */
    public Publisher<V> valueIterator(String pattern, int count) {
        return Flux.create(new MapReactiveIterator<K, V, V>((RedissonMap<K, V>) map, pattern, count) {
            @Override
            V getValue(Entry<Object, Object> entry) {
                return (V) entry.getValue();
            }
        });
    }

    /** 仅 emit 键的 HSCAN 流。 */
    public Publisher<K> keyIterator() {
        return keyIterator(null);
    }
    
    /** 按模式扫描键。 */
    public Publisher<K> keyIterator(String pattern) {
        return keyIterator(pattern, 10);
    }

    /** 指定 chunk 的键迭代。 */
    public Publisher<K> keyIterator(int count) {
        return keyIterator(null, count);
    }
    
    /** 模式 + chunk 的键迭代。 */
    public Publisher<K> keyIterator(String pattern, int count) {
        return Flux.create(new MapReactiveIterator<K, V, K>((RedissonMap<K, V>) map, pattern, count) {
            @Override
            K getValue(Entry<Object, Object> entry) {
                return (K) entry.getKey();
            }
        });
    }

    /** 获取 map 字段绑定的可过期许可信号量。 */
    public RPermitExpirableSemaphoreReactive getPermitExpirableSemaphore(K key) {
        RPermitExpirableSemaphore s = map.getPermitExpirableSemaphore(key);
        return ReactiveProxyBuilder.create(commandExecutor, s, RPermitExpirableSemaphoreReactive.class);
    }

    /** 获取 map 字段绑定的计数信号量。 */
    public RSemaphoreReactive getSemaphore(K key) {
        RSemaphore s = map.getSemaphore(key);
        return ReactiveProxyBuilder.create(commandExecutor, s, RSemaphoreReactive.class);
    }

    /** 获取 map 字段绑定的公平锁。 */
    public RLockReactive getFairLock(K key) {
        RLock lock = map.getFairLock(key);
        return ReactiveProxyBuilder.create(commandExecutor, lock, RLockReactive.class);
    }

    /** 获取 map 字段绑定的读写锁。 */
    public RReadWriteLockReactive getReadWriteLock(K key)  {
        RReadWriteLock lock = map.getReadWriteLock(key);
        return ReactiveProxyBuilder.create(commandExecutor, lock, RReadWriteLockReactive.class);
    }

    /** 获取 map 字段绑定的可重入锁。 */
    public RLockReactive getLock(K key) {
        RLock lock = map.getLock(key);
        return ReactiveProxyBuilder.create(commandExecutor, lock, RLockReactive.class);
    }

}
