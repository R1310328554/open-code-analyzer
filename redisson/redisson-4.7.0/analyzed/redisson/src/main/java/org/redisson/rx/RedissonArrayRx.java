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

import io.reactivex.rxjava3.processors.ReplayProcessor;
import org.reactivestreams.Publisher;
import org.redisson.api.RArray;
import org.redisson.api.array.ArrayEntry;

/**
 * {@link org.redisson.api.RArrayRx} 流式方法的 RxJava3 门面实现。
 * <p>
 * 仅此处声明的方法覆盖通用 async→rx 代理；接口其余方法仍映射到对应 {@code *Async}。
 * {@link #iterator} 通过 {@link ArrayEntryRxIteratorConsumer} 提供背压 {@link Publisher}。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class RedissonArrayRx<V> {

    /** 被包装的同步/异步 RArray 实例。 */
    private final RArray<V> instance;

    public RedissonArrayRx(RArray<V> instance) {
        this.instance = instance;
    }

    public Publisher<ArrayEntry<V>> iterator() {
        return iterator(10);
    }

    /** 返回带背压的 ArrayEntry 迭代 Publisher，count 为 ARSCAN 分页大小。 */
    public Publisher<ArrayEntry<V>> iterator(int count) {
        ReplayProcessor<ArrayEntry<V>> p = ReplayProcessor.create();
        return p.doOnRequest(new ArrayEntryRxIteratorConsumer<>(p, instance, count));
    }

}
