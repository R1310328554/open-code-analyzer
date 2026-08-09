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
import org.redisson.BaseRedissonList;
import org.redisson.api.RBlockingQueueAsync;

/**
 * 阻塞队列 {@link org.redisson.api.RBlockingQueueRx} 的 Rx 扩展，继承 {@link RedissonListRx}。
 * <p>
 * {@link #takeElements} 将 {@link RBlockingQueueAsync#takeAsync} 转为按 request 驱动的 Flowable。
 *
 * @author Nikita Koksharov
 *
 * @param <V> - value type
 */
public class RedissonBlockingQueueRx<V> extends RedissonListRx<V> {

    /** 异步阻塞队列 API，takeAsync 供 ElementsStream 使用。 */
    private final RBlockingQueueAsync<V> queue;
    
    public RedissonBlockingQueueRx(RBlockingQueueAsync<V> queue) {
        super((BaseRedissonList<V>) queue);
        this.queue = queue;
    }

    /** 按 downstream request 连续阻塞 take 队列元素。 */
    public Flowable<V> takeElements() {
        return ElementsStream.takeElements(queue::takeAsync);
    }
    
}
