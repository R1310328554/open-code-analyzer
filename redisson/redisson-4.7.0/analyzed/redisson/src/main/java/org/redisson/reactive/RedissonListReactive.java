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
import org.redisson.RedissonList;
import org.redisson.api.RFuture;
import org.redisson.api.RList;
import org.redisson.client.codec.Codec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.function.LongConsumer;

/**
 * Redis List 的 Reactor 门面：支持正/反向索引迭代与 {@link PublisherAdder} 批量追加。
 * <p>
 * 迭代通过逐索引 {@link #getAsync} 拉取，配合背压 {@link LongConsumer} 控制 emit 速率。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 列表元素类型
 */
public class RedissonListReactive<V> {

    /** 底层分布式 List。 */
    private final RList<V> instance;
    
    /** 包装已有 {@link RList}。 */
    public RedissonListReactive(RList<V> instance) {
        this.instance = instance;
    }

    /** 按名称创建默认 codec 的 List。 */
    public RedissonListReactive(CommandReactiveExecutor commandExecutor, String name) {
        this.instance = new RedissonList<V>(commandExecutor, name, null);
    }

    /** 指定 codec 与名称创建 List。 */
    public RedissonListReactive(Codec codec, CommandReactiveExecutor commandExecutor, String name) {
        this.instance = new RedissonList<V>(codec, commandExecutor, name, null);
    }

    /** 从尾部向头部迭代（索引 -1 递减）。 */
    public Publisher<V> descendingIterator() {
        return iterator(-1, false);
    }

    /** 从索引 0 向尾部正序迭代。 */
    public Publisher<V> iterator() {
        return iterator(0, true);
    }

    /** 从指定索引向头部递减迭代。 */
    public Publisher<V> descendingIterator(int startIndex) {
        return iterator(startIndex, false);
    }

    /** 从指定索引向尾部递增迭代。 */
    public Publisher<V> iterator(int startIndex) {
        return iterator(startIndex, true);
    }

    /** 核心迭代：按 forward 方向逐索引 getAsync 并 respect 背压。 */
    private Publisher<V> iterator(int startIndex, boolean forward) {
        return Flux.create(emitter -> emitter.onRequest(new LongConsumer() {

            /** 当前读取索引。 */
            int currentIndex = startIndex;
            /** 是否曾收到 {@code Long.MAX_VALUE} 无界请求。 */
            volatile boolean maxAccepted;

            /** 累加 request；无界模式下忽略后续有限 request。 */
            @Override
            public void accept(long value) {
                if (Long.MAX_VALUE == value) {
                    maxAccepted = true;
                }
                if (maxAccepted && value != Long.MAX_VALUE) {
                    return;
                }
                onRequest(forward, emitter, value);
            }

            /** 递归拉取最多 n 个元素；遇 null 表示已到边界并 complete。 */
            private void onRequest(boolean forward1, FluxSink<V> emitter, long n) {
                getAsync(currentIndex).whenComplete((value, e) -> {
                        if (e != null) {
                            emitter.error(e);
                            return;
                        }

                        if (value != null) {
                            emitter.next(value);
                            if (forward1) {
                                currentIndex++;
                            } else {
                                currentIndex--;
                            }
                        }

                        if (value == null) {
                            emitter.complete();
                            return;
                        }
                        if (n-1 == 0) {
                            return;
                        }
                        onRequest(forward1, emitter, n-1);
                });
            }

        }));
    }

    /** 按索引异步读取，供迭代器调用。 */
    RFuture<V> getAsync(int currentIndex) {
        return instance.getAsync(currentIndex);
    }

    /** 串行消费上游 Publisher 并逐条 addAsync。 */
    public Publisher<Boolean> addAll(Publisher<? extends V> c) {
        return new PublisherAdder<V>() {

            @Override
            public RFuture<Boolean> add(Object o) {
                return addAsync((V) o);
            }

        }.addAll(c);
    }

    /** 异步追加单个元素。 */
    RFuture<Boolean> addAsync(V o) {
        return instance.addAsync(o);
    }

}
