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

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.reactivestreams.Publisher;
import org.redisson.RedissonObject;
import org.redisson.ScanIterator;
import org.redisson.ScanResult;
import org.redisson.api.*;
import org.redisson.client.RedisClient;

/**
 * 分布式 {@link java.util.Set} 的 RxJava 3 适配。
 * <p>
 * 提供 SSCAN 驱动的 {@link Flowable}/{@link Publisher} 迭代、Reactive Streams 批量添加，
 * 以及按成员值派生锁与信号量的便捷方法。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value
 */
public class RedissonSetRx<V> {

    /** 底层 RSet。 */
    private final RSet<V> instance;
    /** Rx 客户端，用于 getLockByValue 派生对象的获取。 */
    private final RedissonRxClient redisson;

    public RedissonSetRx(RSet<V> instance, RedissonRxClient redisson) {
        this.instance = instance;
        this.redisson = redisson;
    }
    
    /** 订阅 Publisher 并逐个 addAsync，汇总为 Single&lt;Boolean&gt;。 */
    public Single<Boolean> addAll(Publisher<? extends V> c) {
        return new PublisherAdder<Object>() {
            @Override
            public RFuture<Boolean> add(Object e) {
                return instance.addAsync((V) e);
            }
        }.addAll(c);
    }

    /** 指定 SSCAN 批次大小的成员迭代。 */
    public Flowable<V> iterator(int count) {
        return iterator(null, count);
    }
    
    /** 按 glob 模式过滤成员的迭代，默认 count=10。 */
    public Flowable<V> iterator(String pattern) {
        return iterator(pattern, 10);
    }

    /** 按模式与批次 hint 执行 SSCAN 并发射成员。 */
    public Flowable<V> iterator(String pattern, int count) {
        return new SetRxIterator<V>() {
            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return ((ScanIterator) instance).scanIteratorAsync(((RedissonObject) instance).getRawName(), client, nextIterPos, pattern, count);
            }
        }.create();
    }

    /** 默认每批 10 条扫描全部成员。 */
    public Publisher<V> iterator() {
        return iterator(null, 10);
    }
    
    /** 以成员值为键获取可过期许可信号量 Rx 视图。 */
    public RPermitExpirableSemaphoreRx getPermitExpirableSemaphore(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "permitexpirablesemaphore");
        return redisson.getPermitExpirableSemaphore(name);
    }

    /** 以成员值为键获取计数信号量 Rx 视图。 */
    public RSemaphoreRx getSemaphore(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "semaphore");
        return redisson.getSemaphore(name);
    }
    
    /** 以成员值为键获取公平锁 Rx 视图。 */
    public RLockRx getFairLock(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "fairlock");
        return redisson.getFairLock(name);
    }
    
    /** 以成员值为键获取读写锁 Rx 视图。 */
    public RReadWriteLockRx getReadWriteLock(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "rw_lock");
        return redisson.getReadWriteLock(name);
    }
    
    /** 以成员值为键获取可重入锁 Rx 视图。 */
    public RLockRx getLock(V value) {
        String name = ((RedissonObject) instance).getLockByValue(value, "lock");
        return redisson.getLock(name);
    }
    
}
