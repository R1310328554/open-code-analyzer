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
import org.redisson.RedissonObject;
import org.redisson.ScanIterator;
import org.redisson.ScanResult;
import org.redisson.api.*;
import org.redisson.client.RedisClient;
import reactor.core.publisher.Flux;

/**
 * {@link RSet} 的 Reactor 响应式辅助类，语义类似 {@link java.util.Set}：
 * 支持 SCAN 迭代、批量添加，以及按元素值派生锁与信号量。
 * <p>
 * 迭代基于 {@link SetReactiveIterator} 分页扫描 Redis Set，
 * 避免 {@code SMEMBERS} 阻塞大键。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 集合元素类型
 */
public class RedissonSetReactive<V> {

    /** 底层同步 Set 实例。 */
    private final RSet<V> instance;
    /** 响应式客户端，用于派生锁/信号量。 */
    private final RedissonReactiveClient redisson;

    /** @param instance 同步 RSet @param redisson 响应式客户端 */
    public RedissonSetReactive(RSet<V> instance, RedissonReactiveClient redisson) {
        this.instance = instance;
        this.redisson = redisson;
    }

    /** 消费上游 Publisher，逐个 {@code SADD} 并汇总结果。 */
    public Publisher<Boolean> addAll(Publisher<? extends V> c) {
        return new PublisherAdder<Object>() {
            @Override
            public RFuture<Boolean> add(Object e) {
                return instance.addAsync((V) e);
            }
        }.addAll(c);
    }

    /** 指定每批 SCAN 数量的迭代。 */
    public Publisher<V> iterator(int count) {
        return iterator(null, count);
    }
    
    /** 带 Glob 模式、默认批量 10 的 SCAN 迭代。 */
    public Publisher<V> iterator(String pattern) {
        return iterator(pattern, 10);
    }

    /** 同时指定匹配模式与 SCAN 批量的迭代流。 */
    public Publisher<V> iterator(String pattern, int count) {
        return Flux.create(new SetReactiveIterator<V>() {
            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return ((ScanIterator) instance).scanIteratorAsync(((RedissonObject) instance).getRawName(), client, nextIterPos, pattern, count);
            }
        });
    }

    /** 默认模式与批量（10）的全量 SCAN 迭代。 */
    public Publisher<V> iterator() {
        return iterator(null, 10);
}
    
    /** 按元素值派生可过期许可信号量。 */
    public RPermitExpirableSemaphoreReactive getPermitExpirableSemaphore(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "permitexpirablesemaphore");
        return redisson.getPermitExpirableSemaphore(name);
    }

    /** 按元素值派生计数信号量。 */
    public RSemaphoreReactive getSemaphore(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "semaphore");
        return redisson.getSemaphore(name);
    }
    
    /** 按元素值派生公平分布式锁。 */
    public RLockReactive getFairLock(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "fairlock");
        return redisson.getFairLock(name);
    }
    
    /** 按元素值派生读写锁。 */
    public RReadWriteLockReactive getReadWriteLock(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "rw_lock");
        return redisson.getReadWriteLock(name);
    }
    
    /** 按元素值派生普通互斥锁。 */
    public RLockReactive getLock(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "lock");
        return redisson.getLock(name);
    }
    
}
