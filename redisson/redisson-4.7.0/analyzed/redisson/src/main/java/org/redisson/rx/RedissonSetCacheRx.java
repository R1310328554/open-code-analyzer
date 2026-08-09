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

import io.reactivex.rxjava3.core.Single;
import org.reactivestreams.Publisher;
import org.redisson.RedissonObject;
import org.redisson.ScanIterator;
import org.redisson.ScanResult;
import org.redisson.api.*;
import org.redisson.client.RedisClient;

/**
 * 带 TTL 的分布式 {@link java.util.Set}（SetCache）Rx 适配。
 * <p>
 * 提供 SSCAN 迭代、Publisher 批量 {@code addAll}，以及按集合元素值派生
 * 锁/信号量 Rx 对象（键名由 {@link RedissonObject#getLockByValue} 生成）。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value
 */
public class RedissonSetCacheRx<V> {

    /** 底层 SetCache。 */
    private final RSetCache<V> instance;
    /** Rx 客户端，用于按值名获取全局锁/信号量。 */
    private final RedissonRxClient redisson;
    
    public RedissonSetCacheRx(RSetCache<V> instance, RedissonRxClient redisson) {
        this.instance = instance;
        this.redisson = redisson;
    }

    /** 默认每批 10 条的 SSCAN 成员流。 */
    public Publisher<V> iterator() {
        return new SetRxIterator<V>() {
            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return ((ScanIterator) instance).scanIteratorAsync(((RedissonObject) instance).getRawName(), client, nextIterPos, null, 10);
            }
        }.create();
    }

    /** 消费 Publisher 全部元素并依次 addAsync。 */
    public Single<Boolean> addAll(Publisher<? extends V> c) {
        return new PublisherAdder<V>() {
            @Override
            public RFuture<Boolean> add(Object o) {
                return instance.addAsync((V) o);
            }
        }.addAll(c);
    }

    /** 以集合元素值为键派生可过期许可信号量。 */
    public RPermitExpirableSemaphoreRx getPermitExpirableSemaphore(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "permitexpirablesemaphore");
        return redisson.getPermitExpirableSemaphore(name);
    }

    /** 以集合元素值为键派生计数信号量。 */
    public RSemaphoreRx getSemaphore(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "semaphore");
        return redisson.getSemaphore(name);
    }
    
    /** 以集合元素值为键派生公平锁。 */
    public RLockRx getFairLock(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "fairlock");
        return redisson.getFairLock(name);
    }
    
    /** 以集合元素值为键派生读写锁。 */
    public RReadWriteLockRx getReadWriteLock(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "rw_lock");
        return redisson.getReadWriteLock(name);
    }
    
    /** 以集合元素值为键派生可重入锁。 */
    public RLockRx getLock(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "lock");
        return redisson.getLock(name);
    }
    
}
