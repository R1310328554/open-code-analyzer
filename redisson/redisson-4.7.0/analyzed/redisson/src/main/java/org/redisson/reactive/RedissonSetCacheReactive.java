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
 * {@link RSetCache} 的 Reactor 响应式辅助类：
 * 支持带 TTL 的 Set 扫描迭代、批量添加，以及按元素派生锁/信号量。
 * <p>
 * 过期条目在读取时惰性清理；迭代通过 {@link SetReactiveIterator} 分页 SCAN。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 集合元素类型
 */
public class RedissonSetCacheReactive<V> {

    /** 底层带过期 Set 实例。 */
    private final RSetCache<V> instance;
    /** 响应式客户端，用于获取派生分布式对象。 */
    private final RedissonReactiveClient redisson;
    
    /** @param instance 同步 SetCache @param redisson 响应式客户端 */
    public RedissonSetCacheReactive(RSetCache<V> instance, RedissonReactiveClient redisson) {
        this.instance = instance;
        this.redisson = redisson;
    }

    /** 返回 Set 元素的响应式 SCAN 迭代流。 */
    public Publisher<V> iterator() {
        return Flux.create(new SetReactiveIterator<V>() {
            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return ((ScanIterator) instance).scanIteratorAsync(((RedissonObject) instance).getRawName(), client, nextIterPos, null, 10);
            }
        });
    }

    /** 消费上游 Publisher 并逐个异步添加元素。 */
    public Publisher<Boolean> addAll(Publisher<? extends V> c) {
        return new PublisherAdder<V>() {
            @Override
            public RFuture<Boolean> add(Object o) {
                return instance.addAsync((V) o);
            }
        }.addAll(c);
    }
    
    /** 为集合元素派生可过期许可信号量（键名由元素哈希生成）。 */
    public RPermitExpirableSemaphoreReactive getPermitExpirableSemaphore(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "permitexpirablesemaphore");
        return redisson.getPermitExpirableSemaphore(name);
    }

    /** 为集合元素派生计数信号量。 */
    public RSemaphoreReactive getSemaphore(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "semaphore");
        return redisson.getSemaphore(name);
    }
    
    /** 为集合元素派生公平锁。 */
    public RLockReactive getFairLock(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "fairlock");
        return redisson.getFairLock(name);
    }
    
    /** 为集合元素派生读写锁。 */
    public RReadWriteLockReactive getReadWriteLock(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "rw_lock");
        return redisson.getReadWriteLock(name);
    }
    
    /** 为集合元素派生普通互斥锁。 */
    public RLockReactive getLock(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "lock");
        return redisson.getLock(name);
    }

}
