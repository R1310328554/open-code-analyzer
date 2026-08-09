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


import org.redisson.RedissonBlockingDeque;

import reactor.core.publisher.Flux;

/**
 * 阻塞双端队列的 Reactor 扩展：在 {@link RedissonBlockingQueueReactive} 基础上
 * 提供 {@link #takeFirstElements()} 与 {@link #takeLastElements()} 持续消费流。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 元素类型
 */
public class RedissonBlockingDequeReactive<V> extends RedissonBlockingQueueReactive<V> {

    /** 底层阻塞双端队列实现。 */
    private final RedissonBlockingDeque<V> queue;
    
    /** 绑定阻塞双端队列实例。 */
    public RedissonBlockingDequeReactive(RedissonBlockingDeque<V> queue) {
        super(queue);
        this.queue = queue;
    }

    /** 持续从队首阻塞 take，每取到元素 emit 到 {@link Flux}。 */
    public Flux<V> takeFirstElements() {
        return ElementsStream.takeElements(() -> queue.takeFirstAsync());
    }
    
    /** 持续从队尾阻塞 take，每取到元素 emit 到 {@link Flux}。 */
    public Flux<V> takeLastElements() {
        return ElementsStream.takeElements(() -> queue.takeLastAsync());
    }
    
}
