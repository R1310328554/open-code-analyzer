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
import org.redisson.RedissonScoredSortedSet;
import org.redisson.ScanResult;
import org.redisson.api.RFuture;
import org.redisson.api.RObject;
import org.redisson.api.RScoredSortedSetAsync;
import org.redisson.client.RedisClient;
import org.redisson.client.protocol.ScoredEntry;

/**
 * 带分数有序集合（ZSET）的 RxJava 3 适配。
 * <p>
 * 支持 {@code ZSCAN} 成员/条目迭代，以及 {@code takeFirstAsync}/{@code takeLastAsync}
 * 驱动的阻塞式弹出流（经 {@link ElementsStream}）。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class RedissonScoredSortedSetRx<V>  {

    /** 底层异步 ZSET 接口（运行时多为 {@link RedissonScoredSortedSet}）。 */
    private final RScoredSortedSetAsync<V> instance;
    
    public RedissonScoredSortedSetRx(RScoredSortedSetAsync<V> instance) {
        this.instance = instance;
    }
    
    /** 内部：按模式与 count 扫描成员值。 */
    private Flowable<V> scanIteratorReactive(String pattern, int count) {
        return new SetRxIterator<V>() {
            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return ((RedissonScoredSortedSet<V>) instance).scanIteratorAsync(client, nextIterPos, pattern, count);
            }
        }.create();
    }

    /** 持续从低分端阻塞弹出元素的 {@link Flowable}。 */
    public Flowable<V> takeFirstElements() {
        return ElementsStream.takeElements(instance::takeFirstAsync);
    }
    
    /** 持续从高分端阻塞弹出元素的 {@link Flowable}。 */
    public Flowable<V> takeLastElements() {
        return ElementsStream.takeElements(instance::takeLastAsync);
    }
    
    /** 返回 Redis 对象逻辑名。 */
    public String getName() {
        return ((RObject) instance).getName();
    }
    
    /** 默认批次（10）扫描全部成员。 */
    public Flowable<V> iterator() {
        return scanIteratorReactive(null, 10);
    }

    /** 按 glob 模式扫描成员。 */
    public Flowable<V> iterator(String pattern) {
        return scanIteratorReactive(pattern, 10);
    }

    /** 指定每批扫描 hint 的成员迭代。 */
    public Flowable<V> iterator(int count) {
        return scanIteratorReactive(null, count);
    }

    /** 按模式与批次大小扫描成员。 */
    public Flowable<V> iterator(String pattern, int count) {
        return scanIteratorReactive(pattern, count);
    }

    /** 内部：扫描带分数的 {@link ScoredEntry}。 */
    private Flowable<ScoredEntry<V>> entryScanIteratorReactive(String pattern, int count) {
        return new SetRxIterator<ScoredEntry<V>>() {
            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return ((RedissonScoredSortedSet<V>) instance).entryScanIteratorAsync(client, nextIterPos, pattern, count);
            }
        }.create();
    }

    /** 默认批次扫描 score+member 条目。 */
    public Flowable<ScoredEntry<V>> entryIterator() {
        return entryScanIteratorReactive(null, 10);
    }

    /** 按模式扫描 score+member 条目。 */
    public Flowable<ScoredEntry<V>> entryIterator(String pattern) {
        return entryScanIteratorReactive(pattern, 10);
    }

    /** 指定批次大小的条目迭代。 */
    public Flowable<ScoredEntry<V>> entryIterator(int count) {
        return entryScanIteratorReactive(null, count);
    }

    /** 按模式与批次大小扫描 score+member 条目。 */
    public Flowable<ScoredEntry<V>> entryIterator(String pattern, int count) {
        return entryScanIteratorReactive(pattern, count);
    }

}
