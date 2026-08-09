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

import org.redisson.RedissonBlockingDeque;

import io.reactivex.rxjava3.core.Flowable;

/**
 * 阻塞双端队列 {@link org.redisson.api.RBlockingDequeRx} 的 Rx 扩展。
 * <p>
 * 继承 {@link RedissonBlockingQueueRx}；额外提供从队首/队尾阻塞 take 的 {@link Flowable} 流。
 *
 * @author Nikita Koksharov
 *
 * @param <V> - value type
 */
public class RedissonBlockingDequeRx<V> extends RedissonBlockingQueueRx<V> {

    /** 底层 RedissonBlockingDeque，供 takeFirst/takeLast 异步 API。 */
    private final RedissonBlockingDeque<V> queue;
    
    public RedissonBlockingDequeRx(RedissonBlockingDeque<V> queue) {
        super(queue);
        this.queue = queue;
    }

    /** 从队首连续阻塞 take，直到下游 cancel 或完成 request 配额。 */
    public Flowable<V> takeFirstElements() {
        return ElementsStream.takeElements(queue::takeFirstAsync);
    }
    
    /** 从队尾连续阻塞 take（双端队列语义）。 */
    public Flowable<V> takeLastElements() {
        return ElementsStream.takeElements(queue::takeLastAsync);
    }
    
}
