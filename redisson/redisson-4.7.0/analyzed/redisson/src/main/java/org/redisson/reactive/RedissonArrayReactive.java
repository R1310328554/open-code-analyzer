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
import org.redisson.api.RArray;
import org.redisson.api.array.ArrayEntry;
import reactor.core.publisher.Flux;

/**
 * {@link org.redisson.api.RArrayReactive} 的 Reactor 流式方法实现。
 * <p>
 * 此处声明的方法覆盖通用 async→reactive 代理；接口其余方法仍映射到对应 {@code *Async}。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 数组元素类型
 */
public class RedissonArrayReactive<V> {

    /** 底层 Redis 数组实现。 */
    private final RArray<V> instance;

    /** 包装已有 {@link RArray} 实例。 */
    public RedissonArrayReactive(RArray<V> instance) {
        this.instance = instance;
    }

    /** 默认每批 scan 10 个元素的数组条目流。 */
    public Publisher<ArrayEntry<V>> iterator() {
        return iterator(10);
    }

    /** 指定每批 scan 数量的数组条目 {@link Flux}。 */
    public Publisher<ArrayEntry<V>> iterator(int count) {
        return Flux.<ArrayEntry<V>>create(emitter ->
                emitter.onRequest(new ArrayEntryIteratorConsumer<>(emitter, instance, count)));
    }

}
