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

import org.redisson.BaseRedissonList;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RFuture;
import reactor.core.publisher.Flux;


/**
 * 阻塞队列的 Reactor 扩展：{@link #takeElements()} 将 {@code takeAsync} 转为
 * 持续 {@link Flux}；列表迭代仍委托底层 {@link BaseRedissonList}。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 元素类型
 */
public class RedissonBlockingQueueReactive<V> extends RedissonListReactive<V> {

    /** 底层阻塞队列。 */
    private final RBlockingQueue<V> queue;
    
    /** 绑定阻塞队列；列表父类传 {@code null}（不用于普通迭代）。 */
    public RedissonBlockingQueueReactive(RBlockingQueue<V> queue) {
        super(null);
        this.queue = queue;
    }

    /** 循环 {@link RBlockingQueue#takeAsync}，有元素即向下游 emit。 */
    public Flux<V> takeElements() {
        return ElementsStream.takeElements(() -> queue.takeAsync());
    }

    /** 按索引读取仍走底层 List 语义。 */
    @Override
    RFuture<V> getAsync(int currentIndex) {
        return ((BaseRedissonList) queue).getAsync(currentIndex);
    }

    /** 追加元素走底层 List addAsync。 */
    @Override
    RFuture<Boolean> addAsync(V o) {
        return ((BaseRedissonList) queue).addAsync(o);
    }
}
