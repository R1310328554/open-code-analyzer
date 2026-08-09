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

import org.redisson.RedissonMap;
import org.redisson.ScanResult;
import org.redisson.api.RFuture;
import org.redisson.client.RedisClient;
import reactor.core.publisher.FluxSink;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

/**
 * 基于 HSCAN 的 {@link RedissonMap} 响应式迭代器：
 * 将 scan 结果转为 {@link Flux} 元素，{@link Entry#setValue} 会写回 Redis。
 * <p>
 * 通过 {@link IteratorConsumer} 处理背压；子类可覆盖 {@link #getValue} 与 {@link #tryAgain()}。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @param <M> 下游 emit 的元素类型（键/值/条目）
 */
public class MapReactiveIterator<K, V, M> implements Consumer<FluxSink<M>> {

    /** 底层 Redis Hash 映射实例。 */
    private final RedissonMap<K, V> map;
    /** HSCAN 键名匹配模式，{@code null} 表示全部。 */
    private final String pattern;
    /** 每次 SCAN 建议返回的元素个数。 */
    private final int count;

    /** 绑定 map、匹配模式与 scan 批次大小。 */
    public MapReactiveIterator(RedissonMap<K, V> map, String pattern, int count) {
        this.map = map;
        this.pattern = pattern;
        this.count = count;
    }
    
    /** 注册背压驱动的 {@link IteratorConsumer}，委托 scan 与值转换。 */
    @Override
    public void accept(FluxSink<M> emitter) {
        emitter.onRequest(new IteratorConsumer<M>(emitter) {
            @Override
            /** 是否在游标归零后重试 scan；默认不重试。 */
    protected boolean tryAgain() {
                return MapReactiveIterator.this.tryAgain();
            }

            @Override
            protected Object transformValue(Object value) {
                return getValue((Entry<Object, Object>) value);
            }

            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return MapReactiveIterator.this.scanIterator(client, nextIterPos);
            }
        });
    }

    protected boolean tryAgain() {
        return false;
    }

    /** 将 scan 原始条目包装为可写 {@link Entry}，setValue 触发 {@link RedissonMap#put}。 */
    M getValue(Entry<Object, Object> entry) {
        return (M) new AbstractMap.SimpleEntry<K, V>((K) entry.getKey(), (V) entry.getValue()) {

            @Override
            public V setValue(V value) {
                return map.put((K) entry.getKey(), value);
            }

        };
    }

    /** 在指定 Redis 节点上发起异步 HSCAN。 */
    public RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
        return (RFuture<ScanResult<Object>>) (Object) map.scanIteratorAsync(map.getRawName(), client, nextIterPos, pattern, count);
    }

}
