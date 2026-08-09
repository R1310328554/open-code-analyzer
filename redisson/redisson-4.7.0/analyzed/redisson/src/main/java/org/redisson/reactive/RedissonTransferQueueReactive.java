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
import org.redisson.RedissonTransferQueue;
import org.redisson.api.RFuture;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.function.LongConsumer;

/**
 * {@link RedissonTransferQueue} 的 Reactor 响应式封装：
 * 支持阻塞式 {@link #takeElements()} 与按索引顺序读取的迭代流。
 * <p>
 * Transfer Queue 在 Redis List 上实现生产者-消费者语义；
 * {@link #iterator()} 按递增索引拉取元素直至 null。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 队列元素类型
 */
public class RedissonTransferQueueReactive<V> {

    /** 底层同步 Transfer Queue。 */
    private final RedissonTransferQueue<V> queue;

    /** @param queue 同步队列实例 */
    public RedissonTransferQueueReactive(RedissonTransferQueue<V> queue) {
        this.queue = queue;
    }

    /** 持续阻塞取出元素，通过 {@link ElementsStream} 转为 Flux。 */
    public Flux<V> takeElements() {
        return ElementsStream.takeElements(queue::takeAsync);
    }

    /** 按索引顺序异步读取队列元素，遇 null 结束。 */
    public Publisher<V> iterator() {
        return Flux.create(emitter -> emitter.onRequest(new LongConsumer() {

            /** 当前读取的队列索引。 */
            int currentIndex = 0;

            @Override
            public void accept(long value) {
                onRequest(true, emitter, value);
            }

            /** 递归拉取 n 个元素；forward 控制索引递增或递减。 */
            protected void onRequest(boolean forward, FluxSink<V> emitter, long n) {
                queue.getValueAsync(currentIndex).whenComplete((value, e) -> {
                        if (e != null) {
                            emitter.error(e);
                            return;
                        }

                        if (value != null) {
                            emitter.next(value);
                            if (forward) {
                                currentIndex++;
                            } else {
                                currentIndex--;
                            }
                        }

                        // 索引处无元素，队列已读完
                        if (value == null) {
                            emitter.complete();
                            return;
                        }
                        if (n-1 == 0) {
                            return;
                        }
                        onRequest(forward, emitter, n-1);
                });
            }
        }));
    }

    /** 消费上游 Publisher 并逐个入队。 */
    public Publisher<Boolean> addAll(Publisher<? extends V> c) {
        return new PublisherAdder<V>() {

            @Override
            public RFuture<Boolean> add(Object o) {
                return queue.addAsync((V) o);
            }

        }.addAll(c);
    }


}
