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
package org.redisson.rx;

import java.util.Map;
import java.util.Map.Entry;

import org.reactivestreams.Publisher;
import org.redisson.RedissonMap;
import org.redisson.api.*;

/**
 * 分布式 {@link java.util.concurrent.ConcurrentMap} / {@link java.util.Map} 的 Rx 适配。
 * <p>
 * 提供基于 HSCAN 的键、值、条目 {@link Publisher} 迭代，以及按 map 字段名派生
 * 分布式锁/信号量的 Rx 代理（经 {@link RxProxyBuilder}）。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key
 * @param <V> value
 */
public class RedissonMapRx<K, V> {

    /** 底层 RMap，承载 Redis HASH 读写。 */
    private final RMap<K, V> instance;
    /** Rx 命令执行器，用于构建锁/信号量等子对象的 Rx 代理。 */
    private final CommandRxExecutor executor;

    public RedissonMapRx(RMap<K, V> instance, CommandRxExecutor executor) {
        this.instance = instance;
        this.executor = executor;
    }

    /** 扫描全部条目，默认每批 10 条。 */
    public Publisher<Map.Entry<K, V>> entryIterator() {
        return entryIterator(null);
    }
    
    /** 指定每批扫描条数的条目迭代器。 */
    public Publisher<Entry<K, V>> entryIterator(int count) {
        return entryIterator(null, count);
    }
    
    /** 按 glob 模式过滤键后扫描条目，默认 count=10。 */
    public Publisher<Entry<K, V>> entryIterator(String pattern) {
        return entryIterator(pattern, 10);
    }
    
    /** 按模式与批次大小扫描 map 条目。 */
    public Publisher<Map.Entry<K, V>> entryIterator(String pattern, int count) {
        return new RedissonMapRxIterator<K, V, Map.Entry<K, V>>((RedissonMap<K, V>) instance, pattern, count).create();
    }

    /** 扫描全部值。 */
    public Publisher<V> valueIterator() {
        return valueIterator(null);
    }

    /** 按模式扫描值。 */
    public Publisher<V> valueIterator(String pattern) {
        return valueIterator(pattern, 10);
    }

    /** 指定批次大小扫描值。 */
    public Publisher<V> valueIterator(int count) {
        return valueIterator(null, count);
    }
    
    /** 按模式与批次大小扫描值（从 HSCAN 条目中提取 value）。 */
    public Publisher<V> valueIterator(String pattern, int count) {
        return new RedissonMapRxIterator<K, V, V>((RedissonMap<K, V>) instance, pattern, count) {
            @Override
            V getValue(Entry<Object, Object> entry) {
                return (V) entry.getValue();
            }
        }.create();
    }

    /** 扫描全部键。 */
    public Publisher<K> keyIterator() {
        return keyIterator(null);
    }

    /** 按 glob 模式扫描键。 */
    public Publisher<K> keyIterator(String pattern) {
        return keyIterator(pattern, 10);
    }

    /** 指定批次大小扫描键。 */
    public Publisher<K> keyIterator(int count) {
        return keyIterator(null, count);
    }
    
    /** 按模式与批次大小扫描键（从 HSCAN 条目中提取 key）。 */
    public Publisher<K> keyIterator(String pattern, int count) {
        return new RedissonMapRxIterator<K, V, K>((RedissonMap<K, V>) instance, pattern, count) {
            @Override
            K getValue(Entry<Object, Object> entry) {
                return (K) entry.getKey();
            }
        }.create();
    }

    /** 获取与指定 map 键关联的可过期许可信号量 Rx 视图。 */
    public RPermitExpirableSemaphoreRx getPermitExpirableSemaphore(K key) {
        RPermitExpirableSemaphore s = instance.getPermitExpirableSemaphore(key);
        return RxProxyBuilder.create(executor, s, RPermitExpirableSemaphoreRx.class);
    }

    /** 获取与指定 map 键关联的信号量 Rx 视图。 */
    public RSemaphoreRx getSemaphore(K key) {
        RSemaphore s = instance.getSemaphore(key);
        return RxProxyBuilder.create(executor, s, RSemaphoreRx.class);
    }
    
    /** 获取与指定 map 键关联的公平锁 Rx 视图。 */
    public RLockRx getFairLock(K key) {
        RLock lock = instance.getFairLock(key);
        return RxProxyBuilder.create(executor, lock, RLockRx.class);
    }
    
    /** 获取与指定 map 键关联的读写锁 Rx 视图。 */
    public RReadWriteLockRx getReadWriteLock(K key) {
        RReadWriteLock lock = instance.getReadWriteLock(key);
        return RxProxyBuilder.create(executor, lock, RReadWriteLockRx.class);
    }
    
    /** 获取与指定 map 键关联的可重入锁 Rx 视图。 */
    public RLockRx getLock(K key) {
        RLock lock = instance.getLock(key);
        return RxProxyBuilder.create(executor, lock, RLockRx.class);
    }

}
