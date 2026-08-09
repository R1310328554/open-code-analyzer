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

import java.util.AbstractMap;
import java.util.Map.Entry;

import org.redisson.RedissonMap;
import org.redisson.ScanResult;
import org.redisson.api.RFuture;
import org.redisson.client.RedisClient;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.ReplayProcessor;

/**
 * Redis HASH {@code HSCAN} 的 Rx 迭代器工厂。
 * <p>
 * 通过 {@link RxIteratorConsumer} 在订阅者 {@code request} 时驱动游标扫描；
 * 子类可覆写 {@link #getValue} 以投影为键、值或可变 {@link Entry}。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key type
 * @param <V> value type
 * @param <M> entry type
 */
public class RedissonMapRxIterator<K, V, M> {

    /** 被扫描的 RedissonMap 实例。 */
    private final RedissonMap<K, V> map;
    /** HSCAN 键匹配 glob，null 表示全部。 */
    private final String pattern;
    /** 每次 SCAN 建议返回的字段数量 hint。 */
    private final int count;

    public RedissonMapRxIterator(RedissonMap<K, V> map, String pattern, int count) {
        this.map = map;
        this.pattern = pattern;
        this.count = count;
    }
    
    /**
     * 创建背压感知的 {@link Flowable}。
     * <p>
     * {@link ReplayProcessor} 缓存已发射元素；{@code scanIteratorAsync} 在单 Redis 连接上
     * 顺序推进游标，直至 cursor 归零。
     */
    public Flowable<M> create() {
        ReplayProcessor<M> p = ReplayProcessor.create();
        return p.doOnRequest(new RxIteratorConsumer<M>(p) {
            @Override
            protected Object transformValue(Object value) {
                return getValue((Entry<Object, Object>) value);
            }

            @Override
            protected boolean tryAgain() {
                return RedissonMapRxIterator.this.tryAgain();
            }

            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return (RFuture<ScanResult<Object>>) (Object) map.scanIteratorAsync(map.getRawName(), client, nextIterPos, pattern, count);
            }
        });
    }

    /** 扫描遇错时是否重试；默认不重试。 */
    protected boolean tryAgain() {
        return false;
    }

    /**
     * 将 SCAN 原始条目包装为可写 {@link Entry}：{@code setValue} 会调用 {@code map.put}。
     */
    M getValue(Entry<Object, Object> entry) {
        return (M) new AbstractMap.SimpleEntry<K, V>((K) entry.getKey(), (V) entry.getValue()) {

            @Override
            public V setValue(V value) {
                return map.put((K) entry.getKey(), value);
            }

        };
    }

}
